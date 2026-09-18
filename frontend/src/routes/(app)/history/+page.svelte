<script lang="ts">
  import { onMount } from 'svelte';
  import { walletStore, walletLoading } from '$lib/stores/wallet';
  import { walletApi } from '$lib/api';
  import Badge from '$lib/components/ui/Badge.svelte';
  import Skeleton from '$lib/components/ui/Skeleton.svelte';
  import Pagination from '$lib/components/ui/Pagination.svelte';
  import { formatDate, formatIDR } from '$lib/utils/format';
  import type { Transaction } from '$lib/types';

  let loading = $state(true);
  let transactions = $state<Transaction[]>([]);
  let totalPages = $state(1);
  let totalItems = $state(0);
  let currentPage = $state(0);
  let pageSize = $state(10);

  let searchQuery = $state('');
  let filterType = $state('');
  let filterStatus = $state('');
  let sortBy = $state('newest');
  let dateFrom = $state('');
  let dateTo = $state('');

  onMount(() => loadHistory(0));

  async function loadHistory(page = 0) {
    loading = true;
    try {
      const params = new URLSearchParams({
        page: String(page),
        size: String(pageSize),
        sort: sortBy
      });
      if (searchQuery.trim()) params.set('search', searchQuery.trim());
      if (filterType) params.set('type', filterType);
      if (filterStatus) params.set('status', filterStatus);
      if (dateFrom) params.set('from', dateFrom);
      if (dateTo) params.set('to', dateTo);

      const [wallet, res] = await Promise.all([
        walletApi.getBalance() as Promise<import('$lib/types').Wallet>,
        walletApi.getTransactions(params.toString()) as Promise<import('$lib/types').PaginatedResponse<Transaction>>
      ]);
      walletStore.set(wallet);
      transactions = res.content || [];
      currentPage = res.page;
      totalPages = Math.max(1, res.totalPages);
      totalItems = res.totalElements ?? (res.content ? res.content.length : 0);
    } catch {
      transactions = [];
      totalItems = 0;
      totalPages = 1;
    } finally {
      loading = false;
    }
  }

  function handleFilterChange() {
    currentPage = 0;
    loadHistory(0);
  }

  function resetFilters() {
    searchQuery = '';
    filterType = '';
    filterStatus = '';
    sortBy = 'newest';
    dateFrom = '';
    dateTo = '';
    currentPage = 0;
    loadHistory(0);
  }

  const typeLabels: Record<string, string> = {
    TOPUP: 'Top Up',
    WITHDRAW: 'Tarik Dana',
    P2P_TRANSFER: 'Transfer P2P',
    ESCROW_HOLD: 'Escrow Hold',
    ESCROW_RELEASE: 'Escrow Release',
    ESCROW_REFUND: 'Refund',
    FEE: 'Biaya'
  };
</script>

<svelte:head>
  <title>Riwayat Transaksi - EyesOfPriestess</title>
</svelte:head>

<div class="flex flex-col gap-6 pb-20 lg:pb-8">
  <div>
    <h1 class="font-display text-[var(--text-display-md)] text-[var(--color-ink)] tracking-[-0.015em]">
      Riwayat Transaksi
    </h1>
    <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)] mt-1">
      Log pencatatan mutasi saldo dan transaksi escrow secara transparan
    </p>
  </div>

  <!-- Search, Filter & Sort Bar (Rule 6 S1) -->
  <div class="bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] p-4 border border-[var(--color-hairline-soft)] flex flex-col gap-3">
    <!-- Top Row: Search + Type + Status + Sort -->
    <div class="grid grid-cols-1 sm:grid-cols-12 gap-3">
      <!-- Search Input -->
      <div class="sm:col-span-4 relative">
        <input
          type="text"
          bind:value={searchQuery}
          oninput={handleFilterChange}
          placeholder="Cari deskripsi transaksi / pihak lawan..."
          class="w-full h-9 pl-9 pr-3 bg-[var(--color-canvas)] border border-[var(--color-hairline)] rounded-[var(--radius-md)] font-sans text-xs text-[var(--color-ink)] placeholder-[var(--color-muted)] focus:outline-none focus:border-[var(--color-primary)]"
        />
        <span class="absolute left-3 top-2.5 text-[var(--color-muted)] flex items-center">
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 256 256" fill="currentColor"><path d="M229.66,218.34l-50.07-50.06a88.11,88.11,0,1,0-11.31,11.31l50.06,50.07a8,8,0,0,0,11.32-11.32ZM40,112a72,72,0,1,1,72,72A72.08,72.08,0,0,1,40,112Z"/></svg>
        </span>
      </div>

      <!-- Type Filter -->
      <div class="sm:col-span-3">
        <select
          bind:value={filterType}
          onchange={handleFilterChange}
          class="w-full h-9 px-3 bg-[var(--color-canvas)] border border-[var(--color-hairline)] rounded-[var(--radius-md)] font-sans text-xs text-[var(--color-ink)] focus:outline-none focus:border-[var(--color-primary)] cursor-pointer"
        >
          <option value="">Semua Tipe</option>
          {#each Object.entries(typeLabels) as [val, label]}
            <option value={val}>{label}</option>
          {/each}
        </select>
      </div>

      <!-- Status Filter -->
      <div class="sm:col-span-2">
        <select
          bind:value={filterStatus}
          onchange={handleFilterChange}
          class="w-full h-9 px-3 bg-[var(--color-canvas)] border border-[var(--color-hairline)] rounded-[var(--radius-md)] font-sans text-xs text-[var(--color-ink)] focus:outline-none focus:border-[var(--color-primary)] cursor-pointer"
        >
          <option value="">Semua Status</option>
          <option value="SUCCESS">Berhasil</option>
          <option value="PENDING">Menunggu</option>
          <option value="FAILED">Gagal</option>
        </select>
      </div>

      <!-- Sort By -->
      <div class="sm:col-span-3">
        <select
          bind:value={sortBy}
          onchange={handleFilterChange}
          class="w-full h-9 px-3 bg-[var(--color-canvas)] border border-[var(--color-hairline)] rounded-[var(--radius-md)] font-sans text-xs text-[var(--color-ink)] focus:outline-none focus:border-[var(--color-primary)] cursor-pointer"
        >
          <option value="newest">Waktu: Terbaru</option>
          <option value="oldest">Waktu: Terlama</option>
          <option value="amount_desc">Nominal: Tertinggi</option>
          <option value="amount_asc">Nominal: Terendah</option>
        </select>
      </div>
    </div>

    <!-- Bottom Row: Date Range Filter + Reset -->
    <div class="flex items-center flex-wrap gap-3 pt-2 border-t border-[var(--color-hairline-soft)] text-xs text-[var(--color-muted)]">
      <span class="font-medium">Rentang Tanggal:</span>
      <input
        type="date"
        bind:value={dateFrom}
        onchange={handleFilterChange}
        aria-label="Dari tanggal"
        class="h-8 px-2.5 bg-[var(--color-canvas)] border border-[var(--color-hairline)] rounded-[var(--radius-md)] text-xs text-[var(--color-ink)] focus:outline-none focus:border-[var(--color-primary)]"
      />
      <span>s/d</span>
      <input
        type="date"
        bind:value={dateTo}
        onchange={handleFilterChange}
        aria-label="Sampai tanggal"
        class="h-8 px-2.5 bg-[var(--color-canvas)] border border-[var(--color-hairline)] rounded-[var(--radius-md)] text-xs text-[var(--color-ink)] focus:outline-none focus:border-[var(--color-primary)]"
      />

      {#if searchQuery || filterType || filterStatus || sortBy !== 'newest' || dateFrom || dateTo}
        <button
          onclick={resetFilters}
          class="h-8 px-3 text-xs text-[var(--color-error)] hover:bg-[var(--color-error)]/10 rounded-[var(--radius-md)] transition-colors ml-auto"
        >
          Reset Filter
        </button>
      {/if}
    </div>
  </div>

  <!-- Transaction List -->
  <div class="bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] overflow-hidden border border-[var(--color-hairline-soft)]">
    {#if loading && transactions.length === 0}
      <div class="p-5">
        <Skeleton rows={6} />
      </div>
    {:else if transactions.length === 0}
      <div class="p-16 text-center">
        <p class="font-sans text-[var(--text-body-md)] text-[var(--color-muted)]">
          Tidak ada transaksi ditemukan untuk kriteria ini.
        </p>
        {#if searchQuery || filterType || filterStatus || dateFrom || dateTo}
          <button
            onclick={resetFilters}
            class="mt-3 inline-flex items-center h-8 px-3 text-xs text-[var(--color-primary)] hover:underline"
          >
            Bersihkan Filter
          </button>
        {/if}
      </div>
    {:else}
      {#each transactions as tx, i}
        <div
          class="flex items-center gap-4 px-5 py-4 {i < transactions.length - 1 ? 'border-b border-[var(--color-hairline-soft)]' : ''} hover:bg-[var(--color-surface-soft)]/20 transition-colors"
        >
          <div
            class="w-10 h-10 rounded-[var(--radius-md)] shrink-0 flex items-center justify-center {tx.direction === 'IN' ? 'bg-[var(--color-success)]/10' : tx.direction === 'OUT' ? 'bg-[var(--color-error)]/10' : 'bg-[var(--color-surface-soft)]'}"
          >
            <span
              class="font-sans font-medium text-[var(--text-title-sm)] {tx.direction === 'IN' ? 'text-[var(--color-success)]' : tx.direction === 'OUT' ? 'text-[var(--color-error)]' : 'text-[var(--color-muted)]'}"
            >
              {tx.direction === 'IN' ? '↓' : tx.direction === 'OUT' ? '↑' : '·'}
            </span>
          </div>
          <div class="flex-1 min-w-0">
            <p class="font-sans font-medium text-[var(--text-body-sm)] text-[var(--color-ink)] truncate">
              {tx.description}
            </p>
            <p class="font-sans text-[var(--text-caption)] text-[var(--color-muted)] mt-0.5">
              {typeLabels[tx.type] ?? tx.type}
              {#if tx.counterpartyName}· {tx.counterpartyName}{/if}
            </p>
            <p class="font-sans text-[var(--text-caption)] text-[var(--color-muted-soft)] mt-0.5">
              {formatDate(tx.createdAt)}
            </p>
          </div>
          <div class="text-right shrink-0">
            <p
              class="font-sans font-medium text-[var(--text-body-sm)] {tx.direction === 'IN' ? 'text-[var(--color-success)]' : tx.direction === 'OUT' ? 'text-[var(--color-error)]' : 'text-[var(--color-muted)]'}"
            >
              {tx.direction === 'IN' ? '+' : tx.direction === 'OUT' ? '-' : ''}{formatIDR(tx.amount)}
            </p>
            {#if tx.fee > 0}
              <p class="font-sans text-[var(--text-caption)] text-[var(--color-muted-soft)] mt-0.5">
                Biaya: {formatIDR(tx.fee)}
              </p>
            {/if}
            <div class="mt-1">
              <Badge status={tx.status} />
            </div>
          </div>
        </div>
      {/each}

      <!-- Standard Pagination (Rule 7 S1) -->
      <Pagination
        page={currentPage}
        {totalPages}
        {totalItems}
        {pageSize}
        onPageChange={(p) => loadHistory(p)}
        onPageSizeChange={(s) => { pageSize = s; currentPage = 0; loadHistory(0); }}
      />
    {/if}
  </div>
</div>
