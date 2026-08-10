// ═══════════════════════════════════════════════════════
// LIBRARY EXPORTS
// Centralized exports for $lib imports
// ═══════════════════════════════════════════════════════

// API Clients
export { api, sealApi, covenantApi, vaultApi, judgmentApi } from './api';

// Stores
export { sealStore, isAuthenticated, currentPilgrim, isOracle } from './stores/seal';
export { uiStore } from './stores/ui';
export { covenantStore } from './stores/covenant';
export { vaultStore, treasury } from './stores/vault';

// Types
export type * from './types/api';
export type * from './types/pilgrim';
export type * from './types/covenant';
export type * from './types/vault';
export type * from './types/judgment';
export type * from './types/communion';

// Utilities
export {
  formatIDR,
  formatIDRCompact,
  formatDate,
  formatDateTime,
  formatRelative,
  truncateId,
  statusBadgeClass,
  chronicleLabel,
  covenantStatusLabel,
  countdown
} from './utils/formatters';

// Components (shared)
export { default as LoadingCrystal } from './components/shared/LoadingCrystal.svelte';
export { default as Sidebar } from './components/sanctum/Sidebar.svelte';
export { default as Veil } from './components/sanctum/Veil.svelte';
