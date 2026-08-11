import { writable, derived } from "svelte/store";
import type { User, AuthTokens, Wallet, Transaction, Room } from "$lib/types";

// Auth Store
function createAuthStore() {
  const { subscribe, set, update } = writable<{
    user: User | null;
    tokens: AuthTokens | null;
    isLoading: boolean;
  }>({ user: null, tokens: null, isLoading: true });

  return {
    subscribe,
    setAuth: (user: User, tokens: AuthTokens) => set({ user, tokens, isLoading: false }),
    clearAuth: () => set({ user: null, tokens: null, isLoading: false }),
    setLoading: (isLoading: boolean) => update(s => ({ ...s, isLoading })),
    updateUser: (user: User) => update(s => ({ ...s, user })),
  };
}

export const authStore = createAuthStore();
export const isAuthenticated = derived(authStore, $auth => !!$auth.tokens?.accessToken);
export const isAdmin = derived(authStore, $auth => $auth.user?.role === "ADMIN");
export const currentUser = derived(authStore, $auth => $auth.user);

// Wallet Store
export const walletStore = writable<Wallet | null>({
  walletId: "w-01",
  availableBalance: 1500000,
  escrowBalance: 500000,
  totalBalance: 2000000,
  currency: "IDR",
  lastUpdated: new Date().toISOString(),
});
export const transactionsStore = writable<Transaction[]>([]);
export const walletLoading = writable(false);

export const formattedBalance = derived(walletStore, $w => {
  if (!$w) return "Rp 0";
  return new Intl.NumberFormat("id-ID", {
    style: "currency",
    currency: "IDR",
    minimumFractionDigits: 0,
  }).format($w.availableBalance);
});

// Rooms Store
export const roomsStore = writable<Room[]>([]);
export const activeRoomStore = writable<Room | null>(null);
export const roomsLoading = writable(false);

// Notifications Store
export interface Notification {
  id: string;
  type: "success" | "error" | "warning" | "info";
  title: string;
  message: string;
  duration?: number;
}

export const notifications = writable<Notification[]>([]);

export function addNotification(notification: Omit<Notification, "id">) {
  const id = crypto.randomUUID();
  notifications.update(n => [...n, { ...notification, id }]);
  setTimeout(() => {
    notifications.update(n => n.filter(item => item.id !== id));
  }, notification.duration || 5000);
}

// WebSocket Store
export const wsConnectionStatus = writable<"connected" | "connecting" | "disconnected">("disconnected");
export const wsUnreadCounts = writable<Record<string, number>>({});
