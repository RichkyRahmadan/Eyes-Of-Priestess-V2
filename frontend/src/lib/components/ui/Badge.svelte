<script lang="ts">
  import type { RoomStatus, TransactionStatus } from '$lib/types';

  type BadgeVariant =
    | 'coral'
    | 'neutral'
    | 'success'
    | 'warning'
    | 'error'
    | 'teal';

  interface Props {
    variant?: BadgeVariant;
    status?: TransactionStatus | RoomStatus;
    class?: string;
    children?: import('svelte').Snippet;
  }

  let {
    variant,
    status,
    class: className = '',
    children
  }: Props = $props();

  // Derive variant from status if provided
  const resolvedVariant: BadgeVariant = $derived.by(() => {
    if (variant) return variant;
    if (!status) return 'neutral';
    const statusMap: Record<string, BadgeVariant> = {
      SUCCESS: 'success',
      COMPLETED: 'success',
      PENDING: 'warning',
      WAITING_PAYMENT: 'warning',
      FUNDED: 'teal',
      DELIVERED: 'teal',
      FAILED: 'error',
      DISPUTED: 'error',
      CANCELLED: 'error',
      REFUNDED: 'neutral'
    };
    return statusMap[status] ?? 'neutral';
  });

  const styles: Record<BadgeVariant, string> = {
    coral:
      'bg-[var(--color-primary)] text-[var(--color-on-primary)]',
    neutral:
      'bg-[var(--color-surface-card)] text-[var(--color-body)]',
    success:
      'bg-[var(--color-success)]/10 text-[var(--color-success)]',
    warning:
      'bg-[var(--color-warning)]/10 text-[var(--color-warning)]',
    error:
      'bg-[var(--color-error)]/10 text-[var(--color-error)]',
    teal:
      'bg-[var(--color-accent-teal)]/10 text-[var(--color-accent-teal)]'
  };

  const statusLabels: Record<string, string> = {
    SUCCESS: 'Berhasil',
    PENDING: 'Menunggu',
    FAILED: 'Gagal',
    WAITING_PAYMENT: 'Menunggu Pembayaran',
    FUNDED: 'Dana Terkunci',
    DELIVERED: 'Dikirim',
    COMPLETED: 'Selesai',
    DISPUTED: 'Sengketa',
    CANCELLED: 'Dibatalkan',
    REFUNDED: 'Dikembalikan'
  };
</script>

<span
  class="inline-flex items-center px-2.5 py-0.5 rounded-[var(--radius-pill)] font-sans text-[var(--text-caption)] font-medium {styles[resolvedVariant]} {className}"
>
  {#if status}
    {statusLabels[status] ?? status}
  {:else}
    {@render children?.()}
  {/if}
</span>
