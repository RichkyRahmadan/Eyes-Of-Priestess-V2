import { api } from './client';
import type { Pilgrim } from '$lib/types/pilgrim';
import type { ForgeIdentityRequest, RiteCredentials } from '$lib/types/pilgrim';

interface SealResponse {
  pilgrim: Pilgrim;
  accessSeal: string;
  refreshSeal: string;
}

export const sealApi = {
  perform: (creds: RiteCredentials) =>
    api.post<SealResponse>('/seal/perform', creds),

  forgeIdentity: (req: ForgeIdentityRequest) =>
    api.post<SealResponse>('/seal/forge', req),

  sever: () =>
    api.post<void>('/seal/sever'),

  renew: (refreshSeal: string) =>
    api.post<{ accessSeal: string }>('/seal/renew', { refreshSeal }),

  me: () =>
    api.get<Pilgrim>('/seal/me'),

  requestOmen: (phone: string) =>
    api.post<void>('/seal/omen', { phone }),

  verifyOmen: (phone: string, code: string) =>
    api.post<void>('/seal/omen/verify', { phone, code }),

  changePassword: (currentPassword: string, newPassword: string) =>
    api.post<void>('/seal/transmute/password', { currentPassword, newPassword }),

  changePin: (currentPin: string, newPin: string) =>
    api.post<void>('/seal/transmute/pin', { currentPin, newPin })
};
