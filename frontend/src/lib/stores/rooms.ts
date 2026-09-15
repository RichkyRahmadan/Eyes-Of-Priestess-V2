// src/lib/stores/rooms.ts
import { writable } from 'svelte/store';
import type { Room } from '$lib/types';

export const roomsStore = writable<Room[]>([]);
export const activeRoomStore = writable<Room | null>(null);
export const roomsLoading = writable(false);
