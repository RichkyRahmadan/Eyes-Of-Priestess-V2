import { api } from './client';
import type {
  CovenantResponse, ForgeCovenantRequest,
  DeliverCovenantRequest, DisputeCovenantRequest
} from '$lib/types/covenant';

export const covenantApi = {
  getAll: (page = 0, size = 20) =>
    api.get<CovenantResponse[]>(`/covenants?page=${page}&size=${size}`),

  getById: (id: string) =>
    api.get<CovenantResponse>(`/covenants/${id}`),

  forge: (req: ForgeCovenantRequest) =>
    api.post<CovenantResponse>('/covenants', req),

  accept: (invitationCode: string) =>
    api.post<CovenantResponse>('/covenants/accept', { invitationCode }),

  seal: (id: string) =>
    api.post<CovenantResponse>(`/covenants/${id}/seal`),

  deliver: (id: string, req: DeliverCovenantRequest) =>
    api.post<CovenantResponse>(`/covenants/${id}/deliver`, req),

  fulfill: (id: string) =>
    api.post<CovenantResponse>(`/covenants/${id}/fulfill`),

  dispute: (id: string, req: DisputeCovenantRequest) =>
    api.post<CovenantResponse>(`/covenants/${id}/dispute`, req),

  cancel: (id: string) =>
    api.post<CovenantResponse>(`/covenants/${id}/cancel`)
};
