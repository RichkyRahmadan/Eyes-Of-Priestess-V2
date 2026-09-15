// src/lib/stores/auth.ts
import { writable, derived } from 'svelte/store';
import type { User, AuthTokens } from '$lib/types';

interface AuthState {
  user: User | null;
  tokens: AuthTokens | null;
  isLoading: boolean;
}

function createAuthStore() {
  const { subscribe, set, update } = writable<AuthState>({
    user: null,
    tokens: null,
    isLoading: true
  });

  return {
    subscribe,
    setAuth: (user: User, tokens: AuthTokens) =>
      set({ user, tokens, isLoading: false }),
    clearAuth: () =>
      set({ user: null, tokens: null, isLoading: false }),
    setLoading: (isLoading: boolean) =>
      update((s) => ({ ...s, isLoading })),
    updateUser: (user: User) =>
      update((s) => ({ ...s, user }))
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
