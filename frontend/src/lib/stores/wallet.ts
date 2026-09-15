// src/lib/stores/wallet.ts
import { writable, derived } from 'svelte/store';
import type { Wallet, Transaction } from '$lib/types';

export const walletStore = writable<Wallet | null>(null);
export const transactionsStore = writable<Transaction[]>([]);
export const walletLoading = writable(false);

export const formattedBalance = derived(walletStore, ($w) => {
  if (!$w) return 'Rp 0';
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    minimumFractionDigits: 0
  }).format($w.availableBalance);
});

export const formattedEscrow = derived(walletStore, ($w) => {
  if (!$w) return 'Rp 0';
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    minimumFractionDigits: 0
  }).format($w.escrowBalance);
});

export const formattedTotal = derived(walletStore, ($w) => {
  if (!$w) return 'Rp 0';
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    minimumFractionDigits: 0
  }).format($w.totalBalance);
});
