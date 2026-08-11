import { browser } from "$app/environment";
import { authStore } from "$lib/stores";
import { goto } from "$app/navigation";
import type { ApiError } from "$lib/types";

const API_BASE = "http://localhost:8080/api/v1";

async function refreshAccessToken(): Promise<string | null> {
  return null;
}

export async function apiFetch<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const url = `${API_BASE}${endpoint}`;

  let authStoreValue: any;
  authStore.subscribe(s => { authStoreValue = s; })();

  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...((options.headers as Record<string, string>) || {}),
  };

  if (authStoreValue?.tokens?.accessToken) {
    headers["Authorization"] = `Bearer ${authStoreValue.tokens.accessToken}`;
  }

  try {
    const response = await fetch(url, { ...options, headers });

    if (response.status === 401) {
      const newToken = await refreshAccessToken();
      if (newToken) {
        headers["Authorization"] = `Bearer ${newToken}`;
        const retryResponse = await fetch(url, { ...options, headers });
        if (!retryResponse.ok) throw await retryResponse.json();
        return retryResponse.json();
      }
      authStore.clearAuth();
      if (browser) goto("/login");
      throw new Error("Session expired");
    }

    if (!response.ok) {
      const error: ApiError = await response.json().catch(() => ({
        timestamp: new Date().toISOString(),
        status: response.status,
        error: response.statusText,
        message: "API Request Failed",
        path: endpoint,
        traceId: crypto.randomUUID()
      }));
      throw error;
    }

    if (response.status === 204) return undefined as T;
    return response.json();
  } catch (err) {
    throw err;
  }
}

// Service-specific clients
export const authApi = {
  login: (data: any) => apiFetch("/auth/login", { method: "POST", body: JSON.stringify(data) }),
  register: (data: any) => apiFetch("/auth/register", { method: "POST", body: JSON.stringify(data) }),
  me: () => apiFetch("/auth/me"),
  logout: () => apiFetch("/auth/logout", { method: "POST" }),
  setPin: (pin: string, confirmPin: string) => apiFetch("/auth/set-pin", { method: "POST", body: JSON.stringify({ pin, confirmPin }) }),
  verifyPin: (pin: string) => apiFetch("/auth/verify-pin", { method: "POST", body: JSON.stringify({ pin }) }),
};

export const walletApi = {
  getBalance: () => apiFetch("/wallet/balance"),
  getTransactions: (params?: string) => apiFetch(`/wallet/transactions?${params || ""}`),
  topup: (data: any) => apiFetch("/wallet/topup", { method: "POST", body: JSON.stringify(data) }),
  withdraw: (data: any) => apiFetch("/wallet/withdraw", { method: "POST", body: JSON.stringify(data) }),
  transfer: (data: any) => apiFetch("/wallet/transfer", { method: "POST", body: JSON.stringify(data) }),
  getBankAccounts: () => apiFetch("/wallet/bank-accounts"),
  addBankAccount: (data: any) => apiFetch("/wallet/bank-accounts", { method: "POST", body: JSON.stringify(data) }),
};

export const roomApi = {
  getRooms: (params?: string) => apiFetch(`/room/?${params || ""}`),
  getRoom: (id: string) => apiFetch(`/room/${id}`),
  createRoom: (data: any) => apiFetch("/room/", { method: "POST", body: JSON.stringify(data) }),
  fundRoom: (id: string, pin: string) => apiFetch(`/room/${id}/fund`, { method: "POST", body: JSON.stringify({ pin }) }),
  deliverRoom: (id: string, data: any) => apiFetch(`/room/${id}/deliver`, { method: "POST", body: JSON.stringify(data) }),
  confirmRoom: (id: string, data: any) => apiFetch(`/room/${id}/confirm`, { method: "POST", body: JSON.stringify(data) }),
  disputeRoom: (id: string, data: any) => apiFetch(`/room/${id}/dispute`, { method: "POST", body: JSON.stringify(data) }),
};

export const chatApi = {
  getRooms: () => apiFetch("/chat/rooms"),
  getMessages: (roomId: string, params?: string) => apiFetch(`/chat/rooms/${roomId}/messages?${params || ""}`),
  markRead: (roomId: string) => apiFetch(`/chat/rooms/${roomId}/read`, { method: "POST" }),
};

export const disputeApi = {
  getDisputes: () => apiFetch("/dispute/"),
  getDispute: (id: string) => apiFetch(`/dispute/${id}`),
  submitEvidence: (id: string, data: any) => apiFetch(`/dispute/${id}/evidence`, { method: "POST", body: JSON.stringify(data) }),
};
