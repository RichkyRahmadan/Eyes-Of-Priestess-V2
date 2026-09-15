// src/lib/stores/notifications.ts
import { writable } from 'svelte/store';

export interface Notification {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  title: string;
  message: string;
  duration?: number;
}

export const notifications = writable<Notification[]>([]);

export function addNotification(notification: Omit<Notification, 'id'>) {
  const id = crypto.randomUUID();
  notifications.update((n) => [...n, { ...notification, id }]);
  setTimeout(() => {
    removeNotification(id);
  }, notification.duration ?? 5000);
}

export function removeNotification(id: string) {
  notifications.update((n) => n.filter((item) => item.id !== id));
}

export const toasts = {
  success: (message: string, title = 'Berhasil') =>
    addNotification({ type: 'success', title, message }),
  error: (message: string, title = 'Kesalahan') =>
    addNotification({ type: 'error', title, message }),
  warning: (message: string, title = 'Perhatian') =>
    addNotification({ type: 'warning', title, message }),
  info: (message: string, title = 'Info') =>
    addNotification({ type: 'info', title, message })
};
