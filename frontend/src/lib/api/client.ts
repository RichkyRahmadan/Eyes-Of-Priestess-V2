import { ApiException } from '$lib/types/api';
import type { ApiResponse, ApiError } from '$lib/types/api';
import { sealStore } from '$lib/stores/seal';

const API_BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api/v1';

interface RequestOptions extends RequestInit {
  skipAuth?: boolean;
  skipRetry?: boolean;
}

class ApiClient {
  private async invoke<T>(endpoint: string, options: RequestOptions = {}): Promise<T> {
    const { skipAuth = false, skipRetry = false, ...fetchOptions } = options;
    
    let accessSeal: string | null = null;
    if (!skipAuth && typeof localStorage !== 'undefined') {
      accessSeal = localStorage.getItem('accessSeal');
    }

    const isFormData = fetchOptions.body instanceof FormData;
    const headers: Record<string, string> = {
      ...(accessSeal ? { Authorization: `Bearer ${accessSeal}` } : {}),
      ...(!isFormData && !fetchOptions.headers?.['Content-Type'] 
        ? { 'Content-Type': 'application/json' } 
        : {}),
      ...(fetchOptions.headers as Record<string, string> ?? {})
    };

    const res = await fetch(`${API_BASE}${endpoint}`, { 
      ...fetchOptions, 
      headers 
    });

    if (res.status === 401 && !skipRetry && accessSeal) {
      const renewed = await this.refreshToken();
      if (renewed) return this.invoke<T>(endpoint, { ...options, skipRetry: true });
      
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
    
    if (!res.ok || !body.success) {
      throw new ApiException(body.error ?? this.createError(res, endpoint));
    }
    
    if (body.data === undefined) {
      throw new ApiException(this.createError(res, endpoint, 'No data returned'));
    }
    
    return body.data;
  }

  private createError(res: Response, endpoint: string, message?: string): ApiError {
    return {
      code: 'HTTP_ERROR',
      message: message ?? `HTTP ${res.status}: ${res.statusText}`,
      timestamp: new Date().toISOString(),
      path: endpoint,
      requestId: ''
    };
  }

  private async refreshToken(): Promise<boolean> {
    if (typeof localStorage === 'undefined') return false;
    
    const refreshSeal = localStorage.getItem('refreshSeal');
    if (!refreshSeal) return false;
    
    try {
      const res = await fetch(`${API_BASE}/seal/renew`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshSeal })
      });
      
      if (!res.ok) return false;
      
      const data: ApiResponse<{ accessSeal: string }> = await res.json();
      if (data.success && data.data?.accessSeal) {
        sealStore.updateSeal(data.data.accessSeal);
        return true;
      }
    } catch {
      // Silent fail - will trigger logout
    }
    return false;
  }

  get<T>(endpoint: string, options?: Omit<RequestOptions, 'method' | 'body'>) {
    return this.invoke<T>(endpoint, { ...options, method: 'GET' });
  }

  post<T>(endpoint: string, body?: unknown, options?: Omit<RequestOptions, 'method' | 'body'>) {
    return this.invoke<T>(endpoint, {
      ...options,
      method: 'POST',
      body: body !== undefined ? JSON.stringify(body) : undefined
    });
  }

  put<T>(endpoint: string, body?: unknown, options?: Omit<RequestOptions, 'method' | 'body'>) {
    return this.invoke<T>(endpoint, {
      ...options,
      method: 'PUT',
      body: body !== undefined ? JSON.stringify(body) : undefined
    });
  }

  patch<T>(endpoint: string, body?: unknown, options?: Omit<RequestOptions, 'method' | 'body'>) {
    return this.invoke<T>(endpoint, {
      ...options,
      method: 'PATCH',
      body: body !== undefined ? JSON.stringify(body) : undefined
    });
  }

  delete<T>(endpoint: string, options?: Omit<RequestOptions, 'method'>) {
    return this.invoke<T>(endpoint, { ...options, method: 'DELETE' });
  }

  upload<T>(endpoint: string, formData: FormData, options?: Omit<RequestOptions, 'method' | 'body'>) {
    return this.invoke<T>(endpoint, {
      ...options,
      method: 'POST',
      body: formData
    });
  }
}

export const api = new ApiClient();
