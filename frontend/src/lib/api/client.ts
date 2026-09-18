// src/lib/api/client.ts
import { browser } from '$app/environment';
import { goto } from '$app/navigation';
import { authStore } from '$lib/stores/auth';
import type { ApiError } from '$lib/types';

// Use same-origin proxy in browser for zero-CORS preflight & instant connection; 127.0.0.1 on server
const API_BASE = browser ? '/api/v1' : 'http://127.0.0.1:8000/api/v1';

export interface ApiFetchOptions extends RequestInit {
  bypassCache?: boolean;
  cacheTtlMs?: number;
}

interface CacheEntry {
  data: any;
  expiresAt: number;
}

// In-memory read cache for idempotent GET requests
const responseCache = new Map<string, CacheEntry>();

// In-flight promise deduplication to prevent duplicate concurrent network requests
const inFlightRequests = new Map<string, Promise<any>>();

export function clearApiCache(prefix?: string) {
  if (!prefix) {
    responseCache.clear();
    return;
  }
  for (const key of responseCache.keys()) {
    if (key.includes(prefix)) {
      responseCache.delete(key);
    }
  }
}

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
  options: ApiFetchOptions = {}
): Promise<T> {
  const method = (options.method || 'GET').toUpperCase();
  const url = `${API_BASE}${endpoint}`;
  const isGet = method === 'GET';
  const cacheKey = `${endpoint}::${authStoreValueToken() ?? ''}`;

  // If mutation (POST/PUT/DELETE/PATCH), invalidate cache
  if (!isGet) {
    if (endpoint.startsWith('/wallet')) clearApiCache('/wallet');
    else if (endpoint.startsWith('/room')) clearApiCache('/room');
    else if (endpoint.startsWith('/chat')) clearApiCache('/chat');
    else if (endpoint.startsWith('/dispute')) clearApiCache('/dispute');
    else clearApiCache();
  }

  // 1. Check in-memory read cache for GET requests
  if (isGet && !options.bypassCache) {
    const cached = responseCache.get(cacheKey);
    if (cached && Date.now() < cached.expiresAt) {
      return cached.data as T;
    }
  }

  // 2. In-flight request deduplication for GET requests
  if (isGet && !options.bypassCache && inFlightRequests.has(cacheKey)) {
    return inFlightRequests.get(cacheKey) as Promise<T>;
  }

  const fetchPromise = executeFetch<T>(url, endpoint, options, method, cacheKey);

  if (isGet) {
    inFlightRequests.set(cacheKey, fetchPromise);
  }

  try {
    return await fetchPromise;
  } finally {
    if (isGet) {
      inFlightRequests.delete(cacheKey);
    }
  }
}

function authStoreValueToken(): string | undefined {
  let token: string | undefined;
  authStore.subscribe((s) => {
    token = s.tokens?.accessToken;
  })();
  return token;
}

async function executeFetch<T>(
  url: string,
  endpoint: string,
  options: ApiFetchOptions,
  method: string,
  cacheKey: string
): Promise<T> {
  const token = authStoreValueToken();

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...((options.headers as Record<string, string>) || {})
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const { bypassCache, cacheTtlMs = 3000, ...fetchOptions } = options;

  const response = await fetch(url, { ...fetchOptions, method, headers });

  if (response.status === 401) {
    const newToken = await refreshAccessToken();
    if (newToken) {
      headers['Authorization'] = `Bearer ${newToken}`;
      const retryResponse = await fetch(url, { ...fetchOptions, method, headers });
      if (!retryResponse.ok) throw await retryResponse.json();
      const retryResult = await retryResponse.json();
      const data = extractResponseData<T>(retryResult);
      if (method === 'GET') {
        responseCache.set(cacheKey, { data, expiresAt: Date.now() + cacheTtlMs });
      }
      return data;
    }
    authStore.clearAuth();
    clearApiCache();
    if (browser) goto('/login');
    throw new Error('Sesi berakhir, silakan login kembali.');
  }

  if (!response.ok) {
    let errBody: any;
    try {
      errBody = await response.json();
    } catch {
      errBody = { message: response.statusText || 'Terjadi kesalahan sistem' };
    }
    const message = errBody?.error?.message || errBody?.message || 'Terjadi kesalahan sistem';
    const rawField = errBody?.error?.field || errBody?.field;
    const field = rawField ? String(rawField).replace(/.*\.([a-zA-Z0-9_]+)$/, '$1') : undefined;
    const error: ApiError = {
      ...errBody,
      message,
      ...(field ? { details: [{ field, message }] } : (errBody?.details ? { details: errBody.details } : {}))
    };
    throw error;
  }

  if (response.status === 204) return undefined as T;

  const result = await response.json();
  const data = extractResponseData<T>(result);

  // Cache successful GET responses
  if (method === 'GET' && !bypassCache) {
    responseCache.set(cacheKey, { data, expiresAt: Date.now() + cacheTtlMs });
  }

  return data;
}

function extractResponseData<T>(result: any): T {
  if (result && typeof result === 'object' && 'data' in result && result.success === true) {
    return result.data as T;
  }
  return result as T;
}
