<script lang="ts">
  interface Props {
    page: number; // 0-indexed
    totalPages: number;
    totalItems: number;
    pageSize: number;
    onPageChange: (newPage: number) => void;
    onPageSizeChange?: (newSize: number) => void;
  }

  let {
    page = 0,
    totalPages = 1,
    totalItems = 0,
    pageSize = 10,
    onPageChange,
    onPageSizeChange
  }: Props = $props();

  const startItem = $derived(totalItems === 0 ? 0 : page * pageSize + 1);
  const endItem = $derived(Math.min((page + 1) * pageSize, totalItems));

  // Compute page buttons to show (e.g. 1 ... 4 5 6 ... 10)
  const visiblePages = $derived(() => {
    const pages: (number | string)[] = [];
    const total = totalPages;
    const current = page; // 0-indexed

    if (total <= 7) {
      for (let i = 0; i < total; i++) pages.push(i);
    } else {
      pages.push(0);
      if (current > 3) pages.push('...');

      const start = Math.max(1, current - 1);
      const end = Math.min(total - 2, current + 1);

      for (let i = start; i <= end; i++) {
        if (!pages.includes(i)) pages.push(i);
      }

      if (current < total - 4) pages.push('...');
      if (!pages.includes(total - 1)) pages.push(total - 1);
    }
    return pages;
  });
</script>

<div class="flex flex-col sm:flex-row items-center justify-between gap-4 py-4 px-2 border-t border-[var(--color-hairline-soft)] font-sans text-[var(--text-body-sm)]">
  <!-- Total Data Counter -->
  <div class="text-[var(--color-muted)] text-xs order-2 sm:order-1">
    {#if totalItems > 0}
      Menampilkan <span class="font-medium text-[var(--color-ink)]">{startItem}-{endItem}</span> dari <span class="font-medium text-[var(--color-ink)]">{totalItems}</span> data
    {:else}
      Tidak ada data
    {/if}
  </div>

  <!-- Page Navigation (Prev / Numbers / Next) -->
  <div class="flex items-center gap-1 order-1 sm:order-2">
    <!-- Prev Button -->
    <button
      onclick={() => onPageChange(page - 1)}
      disabled={page <= 0}
      aria-label="Halaman Sebelumnya"
      class="h-8 px-2.5 rounded-[var(--radius-md)] border border-[var(--color-hairline)] bg-[var(--color-surface-card)] text-[var(--color-ink)] hover:bg-[var(--color-surface-soft)] disabled:opacity-40 disabled:cursor-not-allowed transition-colors text-xs font-medium inline-flex items-center gap-1"
    >
      ‹ <span class="hidden sm:inline">Sebelumnya</span>
    </button>

    <!-- Page Numbers -->
    {#each visiblePages() as p}
      {#if p === '...'}
        <span class="px-2 text-xs text-[var(--color-muted-soft)]">...</span>
      {:else}
        {@const pageNum = Number(p)}
        <button
          onclick={() => onPageChange(pageNum)}
          class="h-8 min-w-[32px] px-2 rounded-[var(--radius-md)] text-xs font-medium transition-colors {pageNum === page ? 'bg-[var(--color-primary)] text-white' : 'border border-[var(--color-hairline)] bg-[var(--color-surface-card)] text-[var(--color-ink)] hover:bg-[var(--color-surface-soft)]'}"
        >
          {pageNum + 1}
        </button>
      {/if}
    {/each}

    <!-- Next Button -->
    <button
      onclick={() => onPageChange(page + 1)}
      disabled={page >= totalPages - 1}
      aria-label="Halaman Selanjutnya"
      class="h-8 px-2.5 rounded-[var(--radius-md)] border border-[var(--color-hairline)] bg-[var(--color-surface-card)] text-[var(--color-ink)] hover:bg-[var(--color-surface-soft)] disabled:opacity-40 disabled:cursor-not-allowed transition-colors text-xs font-medium inline-flex items-center gap-1"
    >
      <span class="hidden sm:inline">Selanjutnya</span> ›
    </button>
  </div>

  <!-- Items-Per-Page Selector -->
  {#if onPageSizeChange}
    <div class="flex items-center gap-2 text-xs text-[var(--color-muted)] order-3">
      <span>Per halaman:</span>
      <select
        value={pageSize}
        onchange={(e) => onPageSizeChange(Number((e.target as HTMLSelectElement).value))}
        class="h-8 px-2 bg-[var(--color-canvas)] border border-[var(--color-hairline)] rounded-[var(--radius-md)] text-xs text-[var(--color-ink)] focus:outline-none focus:border-[var(--color-primary)] cursor-pointer"
      >
        <option value={10}>10</option>
        <option value={25}>25</option>
        <option value={50}>50</option>
      </select>
    </div>
  {/if}
</div>
