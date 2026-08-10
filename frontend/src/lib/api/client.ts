import { ApiException } from '$lib/types/api';
import type { ApiResponse } from '$lib/types/api';
import { sealStore } from '$lib/stores/seal';

const API_BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api/v1';

class SealBearer {
  private async invoke<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    let accessSeal: string | null = null;
    if (typeof localStorage !== 'undefined') {
      accessSeal = localStorage.getItem('accessSeal');
    }

    const isFormData = options.body instanceof FormData;
    const headers: Record<string, string> = {
      ...(accessSeal ? { Authorization: `Bearer ${accessSeal}` } : {}),
      ...(!isFormData ? { 'Content-Type': 'application/json' } : {}),
      ...(options.headers as Record<string, string>)
    };

    const res = await fetch(`${API_BASE}${endpoint}`, { ...options, headers });

    // Token expired — attempt refresh
    if (res.status === 401) {
      const renewed = await this.renewSeal();
      if (renewed) return this.invoke<T>(endpoint, options);
      sealStore.sever();
      throw new ApiException({
        code: 'SESSION_EXPIRED',
        message: 'Your seal has faded. Please perform the Rite of Return.',
        timestamp: new Date().toISOString(),
        path: endpoint,
        requestId: ''
      });
    }

    const body: ApiResponse<T> = await res.json();
    if (!body.success || !res.ok) {
      throw new ApiException(body.error ?? {
        code: 'UNKNOWN_ERROR',
        message: `HTTP ${res.status}`,
        timestamp: new Date().toISOString(),
        path: endpoint,
        requestId: ''
      });
    }
    return body.data as T;
  }

  private async renewSeal(): Promise<boolean> {
    const refreshSeal = typeof localStorage !== 'undefined'
      ? localStorage.getItem('refreshSeal') : null;
    if (!refreshSeal) return false;
    try {
      const res = await fetch(`${API_BASE}/seal/renew`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshSeal })
      });
      const data: ApiResponse<{ accessSeal: string }> = await res.json();
      if (data.success && data.data) {
        sealStore.updateSeal(data.data.accessSeal);
        return true;
      }
    } catch { /* fall through */ }
    return false;
  }

  get<T>(endpoint: string) {
    return this.invoke<T>(endpoint, { method: 'GET' });
  }

  post<T>(endpoint: string, body?: unknown) {
    return this.invoke<T>(endpoint, {
      method: 'POST',
      ...(body !== undefined ? { body: JSON.stringify(body) } : {})
    });
  }

  put<T>(endpoint: string, body?: unknown) {
    return this.invoke<T>(endpoint, {
      method: 'PUT',
      ...(body !== undefined ? { body: JSON.stringify(body) } : {})
    });
  }

  patch<T>(endpoint: string, body?: unknown) {
    return this.invoke<T>(endpoint, {
      method: 'PATCH',
      ...(body !== undefined ? { body: JSON.stringify(body) } : {})
    });
  }

  delete<T>(endpoint: string) {
    return this.invoke<T>(endpoint, { method: 'DELETE' });
  }
}

export const api = new SealBearer();
