import { api } from './client';
import type { Treasury, Chronicle, OfferingRequest, OfferingResponse, WithdrawalRequest, TithingRequest } from '$lib/types/vault';

interface PagedChronicles { content: Chronicle[]; page: number; size: number; totalElements: number; }

export const vaultApi = {
  getTreasury: () =>
    api.get<Treasury>('/vault/treasury'),

  getChronicles: (page = 0, size = 20) =>
    api.get<PagedChronicles>(`/vault/chronicles?page=${page}&size=${size}`),

  makeOffering: (req: OfferingRequest) =>
    api.post<OfferingResponse>('/vault/offerings', req),

  withdraw: (req: WithdrawalRequest) =>
    api.post<Chronicle>('/vault/withdrawals', req),

  tithe: (req: TithingRequest) =>
    api.post<Chronicle>('/vault/tithe', req)
};
