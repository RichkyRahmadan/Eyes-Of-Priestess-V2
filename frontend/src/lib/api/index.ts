// src/lib/api/index.ts
import { apiFetch } from './client.js';

export const authApi = {
  login: (data: { email: string; password: string }) =>
    apiFetch('/auth/login', { method: 'POST', body: JSON.stringify(data) }),
  register: (data: {
    email: string;
    password: string;
    fullName: string;
    phoneNumber: string;
    username: string;
  }) => apiFetch('/auth/register', { method: 'POST', body: JSON.stringify(data) }),
  me: () => apiFetch('/auth/me'),
  logout: () => apiFetch('/auth/logout', { method: 'POST' }),
  setPin: (pin: string) =>
    apiFetch('/auth/set-pin', { method: 'POST', body: JSON.stringify({ pin }) }),
  verifyPin: (pin: string) =>
    apiFetch('/auth/verify-pin', { method: 'POST', body: JSON.stringify({ pin }) })
};

export const walletApi = {
  getBalance: () => apiFetch('/wallet/balance'),
  getTransactions: (params?: string) =>
    apiFetch(`/wallet/transactions${params ? '?' + params : ''}`),
  topup: (data: { amount: number; method: string; bankCode?: string }) =>
    apiFetch('/wallet/topup', { method: 'POST', body: JSON.stringify(data) }),
  transfer: (data: {
    recipientIdentifier: string;
    amount: number;
    note?: string;
    pin: string;
  }) => apiFetch('/wallet/transfer', { method: 'POST', body: JSON.stringify(data) }),
  transferP2P: (data: {
    recipientUsername: string;
    amount: number;
    note?: string;
    pin: string;
  }) =>
    apiFetch('/wallet/transfer', {
      method: 'POST',
      body: JSON.stringify({
        recipientIdentifier: data.recipientUsername,
        amount: data.amount,
        note: data.note,
        pin: data.pin
      })
    }),
  withdraw: (data: {
    bankAccountId: string;
    amount: number;
    pin: string;
  }) => apiFetch('/wallet/withdraw', { method: 'POST', body: JSON.stringify(data) }),
  getBankAccounts: () => apiFetch('/wallet/bank-accounts'),
  addBankAccount: (data: {
    bankCode: string;
    accountNumber: string;
    accountHolderName: string;
  }) =>
    apiFetch('/wallet/bank-accounts', { method: 'POST', body: JSON.stringify(data) })
};

export const roomApi = {
  getRooms: (params?: string) =>
    apiFetch(`/room/${params ? '?' + params : ''}`),
  getRoom: (id: string) => apiFetch(`/room/${id}`),
  createRoom: (data: {
    title: string;
    description: string;
    itemCategory: string;
    itemPrice: number;
    buyerIdentifier: string;
  }) => apiFetch('/room/', { method: 'POST', body: JSON.stringify(data) }),
  fundRoom: (id: string, pin: string) =>
    apiFetch(`/room/${id}/fund`, { method: 'POST', body: JSON.stringify({ pin }) }),
  deliverRoom: (
    id: string,
    data: { notes?: string; proofFiles: string[] }
  ) => apiFetch(`/room/${id}/deliver`, { method: 'POST', body: JSON.stringify(data) }),
  confirmRoom: (
    id: string,
    data: { pin: string; rating?: number; review?: string }
  ) => apiFetch(`/room/${id}/confirm`, { method: 'POST', body: JSON.stringify(data) }),
  disputeRoom: (
    id: string,
    data: { reason: string; description: string; evidenceUrls?: string[] }
  ) => apiFetch(`/room/${id}/dispute`, { method: 'POST', body: JSON.stringify(data) }),
  cancelRoom: (id: string) =>
    apiFetch(`/room/${id}/cancel`, { method: 'POST' })
};

export const chatApi = {
  getRooms: () => apiFetch('/chat/rooms'),
  getMessages: (roomId: string, params?: string) =>
    apiFetch(`/chat/rooms/${roomId}/messages${params ? '?' + params : ''}`),
  markRead: (roomId: string) =>
    apiFetch(`/chat/rooms/${roomId}/read`, { method: 'POST' })
};

export const disputeApi = {
  getDisputes: (params?: string) =>
    apiFetch(`/dispute/${params ? '?' + params : ''}`),
  getDispute: (id: string) => apiFetch(`/dispute/${id}`),
  addEvidence: (
    id: string,
    data: { type: string; url: string; description?: string }
  ) =>
    apiFetch(`/dispute/${id}/evidence`, {
      method: 'POST',
      body: JSON.stringify(data)
    }),
  resolve: (
    id: string,
    data: { decision: string; reason: string; refundAmount?: number }
  ) =>
    apiFetch(`/dispute/${id}/resolve`, {
      method: 'POST',
      body: JSON.stringify(data)
    })
};
