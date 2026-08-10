/** Format IDR currency */
export const formatIDR = (amount: number) =>
  new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', minimumFractionDigits: 0 }).format(amount);

/** Format compact IDR (e.g. 1.5jt) */
export const formatIDRCompact = (amount: number): string => {
  if (amount >= 1_000_000_000) return `${(amount / 1_000_000_000).toFixed(1)}M`;
  if (amount >= 1_000_000)     return `${(amount / 1_000_000).toFixed(1)}jt`;
  if (amount >= 1_000)         return `${(amount / 1_000).toFixed(0)}rb`;
  return formatIDR(amount);
};

/** Format date */
export const formatDate = (iso: string | undefined | null): string => {
  if (!iso) return '—';
  return new Intl.DateTimeFormat('id-ID', {
    day: 'numeric', month: 'short', year: 'numeric'
  }).format(new Date(iso));
};

/** Format datetime */
export const formatDateTime = (iso: string | undefined | null): string => {
  if (!iso) return '—';
  return new Intl.DateTimeFormat('id-ID', {
    day: 'numeric', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  }).format(new Date(iso));
};

/** Format relative time */
export const formatRelative = (iso: string): string => {
  const now = Date.now();
  const diff = now - new Date(iso).getTime();
  const secs  = Math.floor(diff / 1_000);
  const mins  = Math.floor(secs / 60);
  const hours = Math.floor(mins / 60);
  const days  = Math.floor(hours / 24);
  if (secs  < 60)  return 'baru saja';
  if (mins  < 60)  return `${mins}m lalu`;
  if (hours < 24)  return `${hours}j lalu`;
  if (days  < 7)   return `${days}h lalu`;
  return formatDate(iso);
};

/** Truncate an address / id for display */
export const truncateId = (id: string, chars = 8) =>
  `${id.slice(0, chars)}…${id.slice(-4)}`;

/** Return covenant status badge CSS class */
export const statusBadgeClass = (status: string): string => {
  const map: Record<string, string> = {
    FORGED:    'badge-forged',
    ACCEPTED:  'badge-accepted',
    SEALED:    'badge-sealed',
    DELIVERED: 'badge-delivered',
    FULFILLED: 'badge-fulfilled',
    DISPUTED:  'badge-disputed',
    CANCELLED: 'badge-cancelled',
    PENDING:   'badge-pending',
    COMPLETED: 'badge-completed',
    FAILED:    'badge-failed',
    PROCESSING:'badge-pending'
  };
  return map[status] ?? 'badge-pending';
};

/** Chronicle type human label */
export const chronicleLabel = (type: string): string => {
  const map: Record<string, string> = {
    OFFERING:     'Top-Up',
    WITHDRAWAL:   'Penarikan',
    TITHING_IN:   'Tithe Masuk',
    TITHING_OUT:  'Tithe Keluar',
    SEAL_HOLD:    'Escrow Ditahan',
    SEAL_RELEASE: 'Escrow Dilepas',
    SEAL_REFUND:  'Escrow Dikembalikan',
    TITHE:        'Biaya Layanan'
  };
  return map[type] ?? type;
};

/** Covenant status human label */
export const covenantStatusLabel = (status: string): string => {
  const map: Record<string, string> = {
    FORGED:    'Ditempa',
    ACCEPTED:  'Diterima',
    SEALED:    'Tersegel',
    DELIVERED: 'Terkirim',
    FULFILLED: 'Terpenuhi',
    DISPUTED:  'Sengketa',
    CANCELLED: 'Dibatalkan'
  };
  return map[status] ?? status;
};

/** Countdown from a date string */
export const countdown = (iso: string): string => {
  const diff = new Date(iso).getTime() - Date.now();
  if (diff <= 0) return 'Kedaluwarsa';
  const h = Math.floor(diff / 3_600_000);
  const m = Math.floor((diff % 3_600_000) / 60_000);
  if (h > 48) return `${Math.floor(h / 24)} hari`;
  if (h > 0)  return `${h}j ${m}m`;
  return `${m} menit`;
};
