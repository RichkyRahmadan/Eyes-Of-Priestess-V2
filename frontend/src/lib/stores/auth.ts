// src/lib/stores/auth.ts
import { writable, derived } from 'svelte/store';
import { browser } from '$app/environment';
import type { User, AuthTokens } from '$lib/types';

const STORAGE_KEY = 'eop_auth_state';

interface AuthState {
  user: User | null;
  tokens: AuthTokens | null;
  isLoading: boolean;
}

function loadInitialState(): AuthState {
  if (browser) {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored) {
        const parsed = JSON.parse(stored);
        if (parsed.user && parsed.tokens?.accessToken) {
          return {
            user: parsed.user,
            tokens: parsed.tokens,
            isLoading: false
          };
        }
      }
    } catch (e) {
      console.error('Failed to restore auth session:', e);
    }
  }
  return {
    user: null,
    tokens: null,
    isLoading: false
  };
}

function createAuthStore() {
  const initialState = loadInitialState();
  const { subscribe, set, update } = writable<AuthState>(initialState);

  return {
    subscribe,
    setAuth: (user: User, tokens: AuthTokens) => {
      if (browser) {
        try {
          localStorage.setItem(STORAGE_KEY, JSON.stringify({ user, tokens }));
        } catch (e) {
          console.error('Failed to persist auth session:', e);
        }
      }
      set({ user, tokens, isLoading: false });
    },
    clearAuth: () => {
      if (browser) {
        try {
          localStorage.removeItem(STORAGE_KEY);
        } catch (e) {
          console.error('Failed to clear auth session:', e);
        }
      }
      set({ user: null, tokens: null, isLoading: false });
    },
    setLoading: (isLoading: boolean) =>
      update((s) => ({ ...s, isLoading })),
    updateUser: (user: User) =>
      update((s) => {
        if (browser && s.tokens) {
          try {
            localStorage.setItem(STORAGE_KEY, JSON.stringify({ user, tokens: s.tokens }));
          } catch (e) {
            console.error('Failed to update user in auth session:', e);
          }
        }
        return { ...s, user };
      })
  };
}

export const authStore = createAuthStore();

export const isAuthenticated = derived(
  authStore,
  ($auth) => !!$auth.tokens?.accessToken
);
export const isAdmin = derived(
  authStore,
  ($auth) => $auth.user?.role === 'ADMIN'
);
export const currentUser = derived(authStore, ($auth) => $auth.user);
