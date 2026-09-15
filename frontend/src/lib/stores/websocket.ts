// src/lib/stores/websocket.ts
import { writable } from 'svelte/store';

export const wsConnectionStatus = writable<'connected' | 'connecting' | 'disconnected'>('disconnected');
export const wsUnreadCounts = writable<Record<string, number>>({});
