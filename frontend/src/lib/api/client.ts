// src/lib/api/client.ts
import { browser } from '$app/environment';
import { goto } from '$app/navigation';
import { authStore } from '$lib/stores/auth';
import type { ApiError } from '$lib/types';

const API_BASE = 'http://localhost:8080/api/v1';

async function refreshAccessToken(): Promise<string | null> {
  let refreshToken: string | undefined;
  authStore.subscribe((s) => {
    refreshToken = s.tokens?.refreshToken;
  })();

  if (!refreshToken) return null;

  try {
    const res = await fetch(`${API_BASE}/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken })
    });
    if (!res.ok) return null;
    const data = await res.json();
    authStore.subscribe((s) => {
      if (s.user && data.accessToken) {
        authStore.setAuth(s.user, {
          accessToken: data.accessToken,
          refreshToken: data.refreshToken ?? refreshToken!,
          tokenType: data.tokenType ?? 'Bearer',
          expiresIn: data.expiresIn ?? 900
        });
      }
    })();
    return data.accessToken;
  } catch {
    return null;
  }
}

export async function apiFetch<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const url = `${API_BASE}${endpoint}`;

  let authStoreValue: { tokens: { accessToken?: string } | null } = { tokens: null };
  authStore.subscribe((s) => {
    authStoreValue = s;
  })();

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...((options.headers as Record<string, string>) || {})
  };

  if (authStoreValue?.tokens?.accessToken) {
    headers['Authorization'] = `Bearer ${authStoreValue.tokens.accessToken}`;
  }

  const response = await fetch(url, { ...options, headers });

  if (response.status === 401) {
    const newToken = await refreshAccessToken();
    if (newToken) {
      headers['Authorization'] = `Bearer ${newToken}`;
      const retryResponse = await fetch(url, { ...options, headers });
      if (!retryResponse.ok) throw await retryResponse.json();
      return retryResponse.json();
    }
    authStore.clearAuth();
    if (browser) goto('/login');
    throw new Error('Sesi berakhir, silakan login kembali.');
  }

  if (!response.ok) {
    const error: ApiError = await response.json();
    throw error;
  }

  if (response.status === 204) return undefined as T;
  return response.json();
}
