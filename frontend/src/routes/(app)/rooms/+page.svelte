<script lang="ts">
  import { onMount } from 'svelte';
  import { roomsStore, roomsLoading } from '$lib/stores/rooms';
  import { roomApi } from '$lib/api';
  import Badge from '$lib/components/ui/Badge.svelte';
  import Avatar from '$lib/components/ui/Avatar.svelte';
  import Skeleton from '$lib/components/ui/Skeleton.svelte';
  import Pagination from '$lib/components/ui/Pagination.svelte';
  import { formatIDR, formatDate } from '$lib/utils/format';
  import type { Room, RoomStatus } from '$lib/types';

  let filterStatus = $state<RoomStatus | ''>('');
  let filterCategory = $state('');
  let searchQuery = $state('');
  let sortBy = $state('newest');
  let totalPages = $state(1);
  let totalItems = $state(0);
  let currentPage = $state(0);
  let pageSize = $state(10);

  onMount(() => loadRooms(0));

  async function loadRooms(page = 0) {
    if ($roomsStore.length === 0 || page !== 0) {
      roomsLoading.set(true);
    }
    try {
      const params = new URLSearchParams({
        page: String(page),
        size: String(pageSize),
        sort: sortBy
      });
      if (filterStatus) params.set('status', filterStatus);
      if (filterCategory) params.set('category', filterCategory);
      if (searchQuery.trim()) params.set('search', searchQuery.trim());

      const res = (await roomApi.getRooms(params.toString())) as import('$lib/types').PaginatedResponse<Room>;
      roomsStore.set(res.content || []);
      currentPage = res.page;
      totalPages = Math.max(1, res.totalPages);
      totalItems = res.totalElements ?? (res.content ? res.content.length : 0);
    } catch {
      roomsStore.set([]);
      totalItems = 0;
      totalPages = 1;
    } finally {
      roomsLoading.set(false);
    }
  }

  function handleFilterChange() {
    currentPage = 0;
    loadRooms(0);
  }

  function resetFilters() {
    searchQuery = '';
    filterStatus = '';
    filterCategory = '';
    sortBy = 'newest';
    currentPage = 0;
    loadRooms(0);
  }

  const categoryLabels: Record<string, string> = {
    GAME_ACCOUNT: 'Akun Game',
    GAME_ITEM: 'Item Game',
    DIGITAL_PRODUCT: 'Produk Digital',
    PHYSICAL_PRODUCT: 'Produk Fisik',
    SERVICE: 'Jasa',
    OTHER: 'Lainnya'
  };
</script>

<svelte:head>
  <title>Room Escrow - EyesOfPriestess</title>
</svelte:head>

<div class="flex flex-col gap-6 pb-20 lg:pb-8">
  <!-- Header Title & Action -->
  <div class="flex items-center justify-between">
    <div>
      <h1 class="font-display text-[var(--text-display-md)] text-[var(--color-ink)] tracking-[-0.015em]">
        Room Escrow
      </h1>
      <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)] mt-1">
        Kelola dan pantau seluruh transaksi covenant terproteksi Anda
      </p>
    </div>
    <a
      href="/rooms/create"
      class="inline-flex items-center gap-2 h-9 px-4 bg-[var(--color-primary)] text-white font-sans font-medium text-[13px] rounded-[var(--radius-md)] hover:bg-[var(--color-primary-active)] active:scale-[0.98] transition-all duration-150 shadow-sm"
    >
      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 256 256" fill="currentColor"><path d="M224,128a8,8,0,0,1-8,8H136v80a8,8,0,0,1-16,0V136H40a8,8,0,0,1,0-16h80V40a8,8,0,0,1,16,0v80h80A8,8,0,0,1,224,128Z"/></svg>
      Buat Room Baru
    </a>
  </div>

  <!-- Search, Filter & Sort Bar (Simultaneous, Rule 6 S1) -->
  <div class="bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] p-4 border border-[var(--color-hairline-soft)] flex flex-col gap-3">
    <!-- Top Row: Search input + Category + Sort -->
    <div class="grid grid-cols-1 sm:grid-cols-12 gap-3">
      <!-- Search -->
      <div class="sm:col-span-5 relative">
        <input
          type="text"
          bind:value={searchQuery}
          oninput={handleFilterChange}
          placeholder="Cari kode room, judul transaksi, atau nama pihak..."
          class="w-full h-9 pl-9 pr-3 bg-[var(--color-canvas)] border border-[var(--color-hairline)] rounded-[var(--radius-md)] font-sans text-xs text-[var(--color-ink)] placeholder-[var(--color-muted)] focus:outline-none focus:border-[var(--color-primary)]"
        />
        <span class="absolute left-3 top-2.5 text-[var(--color-muted)] text-xs">🔍</span>
      </div>

      <!-- Category Filter -->
      <div class="sm:col-span-3">
        <select
          bind:value={filterCategory}
          onchange={handleFilterChange}
          class="w-full h-9 px-3 bg-[var(--color-canvas)] border border-[var(--color-hairline)] rounded-[var(--radius-md)] font-sans text-xs text-[var(--color-ink)] focus:outline-none focus:border-[var(--color-primary)] cursor-pointer"
        >
          <option value="">Semua Kategori</option>
          {#each Object.entries(categoryLabels) as [val, label]}
            <option value={val}>{label}</option>
          {/each}
        </select>
      </div>

      <!-- Sort By -->
      <div class="sm:col-span-3">
        <select
          bind:value={sortBy}
          onchange={handleFilterChange}
          class="w-full h-9 px-3 bg-[var(--color-canvas)] border border-[var(--color-hairline)] rounded-[var(--radius-md)] font-sans text-xs text-[var(--color-ink)] focus:outline-none focus:border-[var(--color-primary)] cursor-pointer"
        >
          <option value="newest">Urutkan: Terbaru</option>
          <option value="oldest">Urutkan: Terlama</option>
          <option value="title_asc">Judul: A ke Z</option>
          <option value="title_desc">Judul: Z ke A</option>
          <option value="price_desc">Nilai: Tertinggi</option>
          <option value="price_asc">Nilai: Terendah</option>
        </select>
      </div>

      <!-- Reset Filter Button -->
      {#if searchQuery || filterStatus || filterCategory || sortBy !== 'newest'}
        <div class="sm:col-span-1 flex items-center justify-end">
          <button
            onclick={resetFilters}
            class="h-9 px-3 text-xs text-[var(--color-error)] hover:bg-[var(--color-error)]/10 rounded-[var(--radius-md)] transition-colors whitespace-nowrap"
          >
            Reset
          </button>
        </div>
      {/if}
    </div>

    <!-- Bottom Row: Status Filter Badges -->
    <div class="flex gap-1.5 flex-wrap pt-2 border-t border-[var(--color-hairline-soft)]">
      {#each [
        { value: '', label: 'Semua Status' },
        { value: 'WAITING_PAYMENT', label: 'Menunggu Bayar' },
        { value: 'FUNDED', label: 'Terdanai' },
        { value: 'DELIVERED', label: 'Telah Dikirim' },
        { value: 'COMPLETED', label: 'Selesai' },
        { value: 'DISPUTED', label: 'Sengketa' }
      ] as tab}
        <button
          onclick={() => { filterStatus = tab.value as RoomStatus | ''; handleFilterChange(); }}
          class="h-7 px-3 rounded-[var(--radius-md)] font-sans text-xs transition-colors duration-150 {filterStatus === tab.value ? 'bg-[var(--color-primary)] text-white font-medium' : 'bg-[var(--color-surface-soft)] text-[var(--color-muted)] hover:text-[var(--color-ink)]'}"
        >
          {tab.label}
        </button>
      {/each}
    </div>
  </div>

  <!-- Room Grid -->
  {#if $roomsLoading && $roomsStore.length === 0}
    <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
      {#each Array(4) as _}
        <div class="bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] p-5 border border-[var(--color-hairline-soft)]">
          <Skeleton rows={3} />
        </div>
      {/each}
    </div>
  {:else if $roomsStore.length === 0}
    <div class="bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] p-16 text-center border border-[var(--color-hairline-soft)]">
      <p class="font-sans text-[var(--text-body-md)] text-[var(--color-muted)] mb-4">
        Tidak ada room escrow yang sesuai dengan kriteria pencarian Anda.
      </p>
      {#if searchQuery || filterStatus || filterCategory}
        <button
          onclick={resetFilters}
          class="inline-flex items-center h-9 px-4 bg-[var(--color-surface-soft)] text-[var(--color-ink)] font-sans font-medium text-xs rounded-[var(--radius-md)] hover:bg-[var(--color-hairline)] transition-colors"
        >
          Hapus Filter Pencarian
        </button>
      {:else}
        <a
          href="/rooms/create"
          class="inline-flex items-center h-9 px-4 bg-[var(--color-primary)] text-white font-sans font-medium text-xs rounded-[var(--radius-md)] hover:bg-[var(--color-primary-active)] transition-colors"
        >
          Buat room pertama Anda
        </a>
      {/if}
    </div>
  {:else}
    <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
      {#each $roomsStore as room}
        <a
          href="/rooms/{room.roomId}"
          class="bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] p-5 flex flex-col gap-4 border border-[var(--color-hairline-soft)] hover:shadow-[var(--shadow-card)] hover:-translate-y-0.5 transition-all duration-150 group"
        >
          <div class="flex items-start justify-between gap-3">
            <div class="flex-1 min-w-0">
              <span class="inline-block font-mono text-[11px] text-[var(--color-primary)] bg-[var(--color-primary)]/8 px-2 py-0.5 rounded-[var(--radius-sm)] mb-1.5 font-semibold">
                {room.roomCode}
              </span>
              <p class="font-sans font-medium text-[var(--text-body-md)] text-[var(--color-ink)] leading-snug group-hover:text-[var(--color-primary)] transition-colors">
                {room.title}
              </p>
              <p class="font-sans text-[var(--text-caption)] text-[var(--color-muted)] mt-0.5">
                {categoryLabels[room.itemCategory] ?? room.itemCategory}
              </p>
            </div>
            <Badge status={room.status} />
          </div>

          <div class="flex items-center justify-between pt-3 border-t border-[var(--color-hairline-soft)]">
            <div class="flex items-center gap-2">
              <Avatar name={room.buyer?.name ?? 'Pembeli'} src={room.buyer?.avatar} size="xs" />
              <span class="font-sans text-[var(--text-caption)] text-[var(--color-muted)]">
                {room.buyer?.name ?? 'Pembeli'}
              </span>
            </div>
            <p class="font-display text-[var(--text-title-sm)] text-[var(--color-ink)]">
              {formatIDR(room.totalAmount)}
            </p>
          </div>

          <p class="font-sans text-[var(--text-caption)] text-[var(--color-muted-soft)]">
            Dibuat {formatDate(room.createdAt)}
          </p>
        </a>
      {/each}
    </div>

    <!-- Standard Pagination (Rule 7 S1) -->
    <Pagination
      page={currentPage}
      {totalPages}
      {totalItems}
      {pageSize}
      onPageChange={(p) => loadRooms(p)}
      onPageSizeChange={(s) => { pageSize = s; currentPage = 0; loadRooms(0); }}
    />
  {/if}
</div>
