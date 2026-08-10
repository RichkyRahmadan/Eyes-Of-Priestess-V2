import { api } from './client';
import type { JudgmentCase } from '$lib/types/judgment';

export const judgmentApi = {
  getMyCases: () =>
    api.get<JudgmentCase[]>('/judgment/my'),

  getById: (id: string) =>
    api.get<JudgmentCase>(`/judgment/${id}`),

  getAll: () =>
    api.get<JudgmentCase[]>('/judgment'),

  assign: (id: string) =>
    api.post<JudgmentCase>(`/judgment/${id}/assign`),

  render: (id: string, resolution: string, notes: string) =>
    api.post<JudgmentCase>(`/judgment/${id}/render`, { resolution, oracleNotes: notes })
};
