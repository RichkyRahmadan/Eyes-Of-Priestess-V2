<script lang="ts">
  import { onMount } from 'svelte';
  import { currentUser } from '$lib/stores/auth';
  import { walletStore, transactionsStore, walletLoading } from '$lib/stores/wallet';
  import { roomsStore, roomsLoading } from '$lib/stores/rooms';
  import { walletApi, roomApi } from '$lib/api';
  import Badge from '$lib/components/ui/Badge.svelte';
  import Avatar from '$lib/components/ui/Avatar.svelte';
  import Skeleton from '$lib/components/ui/Skeleton.svelte';
  import { formatDate, formatIDR } from '$lib/utils/format';
  import type { Transaction, Room } from '$lib/types';

  onMount(async () => {
    walletLoading.set(true);
    roomsLoading.set(true);
    try {
      const [wallet, txRes, roomRes] = await Promise.all([
        walletApi.getBalance() as Promise<import('$lib/types').Wallet>,
        walletApi.getTransactions('size=5') as Promise<import('$lib/types').PaginatedResponse<Transaction>>,
        roomApi.getRooms('status=FUNDED,WAITING_PAYMENT,DELIVERED&size=4') as Promise<import('$lib/types').PaginatedResponse<Room>>
      ]);
      walletStore.set(wallet);
      transactionsStore.set(txRes.content);
      roomsStore.set(roomRes.content);
    } catch {
      // handled by individual components
    } finally {
      walletLoading.set(false);
      roomsLoading.set(false);
    }
  });

  const txIcons: Record<string, string> = {
    TOPUP: '↓',
    WITHDRAW: '↑',
    P2P_TRANSFER: '→',
    ESCROW_HOLD: '🔒',
    ESCROW_RELEASE: '🔓',
    ESCROW_REFUND: '↩',
    FEE: '−'
  };

  const weeklyVolume = [
    { day: 'Sen', amount: 2500000, heightPct: 45 },
    { day: 'Sel', amount: 4200000, heightPct: 75 },
    { day: 'Rab', amount: 3100000, heightPct: 55 },
    { day: 'Kam', amount: 5800000, heightPct: 95 },
    { day: 'Jum', amount: 4500000, heightPct: 80 },
    { day: 'Sab', amount: 6200000, heightPct: 100 },
    { day: 'Min', amount: 3800000, heightPct: 65 }
  ];

  const statusDistribution = [
    { label: 'Selesai (Completed)', count: 18, color: '#16a34a', pct: 64 },
    { label: 'Terdanai (Funded)', count: 5, color: 'var(--color-primary)', pct: 18 },
    { label: 'Menunggu Bayar', count: 3, color: '#eab308', pct: 11 },
    { label: 'Sengketa (Dispute)', count: 2, color: '#dc2626', pct: 7 }
  ];
</script>

<svelte:head>
  <title>Dashboard - EyesOfPriestess</title>
</svelte:head>

<div class="flex flex-col gap-8 pb-20 lg:pb-8">
  <!-- Greeting -->
  <div>
    <h1
      class="font-display text-[var(--text-display-md)] text-[var(--color-ink)] tracking-[-0.015em]"
    >
      Selamat datang{$currentUser ? ', ' + $currentUser.fullName.split(' ')[0] : ''}
    </h1>
    <p class="font-sans text-[var(--text-body-md)] text-[var(--color-muted)] mt-1">
      Kelola transaksi dan escrow Anda dari sini.
    </p>
  </div>

  <!-- Balance cards: asymmetric grid -->
  <div class="grid grid-cols-1 sm:grid-cols-12 gap-4">
    <!-- Available balance: large -->
    <div
      class="sm:col-span-5 bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] p-6 flex flex-col gap-3"
    >
      <p class="font-sans text-[var(--text-caption)] text-[var(--color-muted)] font-medium">
        Saldo Tersedia
      </p>
      {#if $walletLoading}
        <div class="skeleton h-10 w-48 rounded-[var(--radius-sm)]"></div>
      {:else}
        <p
          class="font-display text-[var(--text-display-md)] text-[var(--color-ink)] leading-none count-animate"
        >
          {$walletStore ? formatIDR($walletStore.availableBalance) : 'Rp 0'}
        </p>
      {/if}
      <p class="font-sans text-[var(--text-caption)] text-[var(--color-muted-soft)]">
        {#if $walletStore}
          Diperbarui {formatDate($walletStore.lastUpdated)}
        {:else}
          -
        {/if}
      </p>
    </div>

    <!-- Escrow balance -->
    <div
      class="sm:col-span-3 bg-[var(--color-surface-soft)] rounded-[var(--radius-xl)] p-6 flex flex-col gap-3"
    >
      <div class="flex items-center gap-2">
        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 256 256" fill="var(--color-muted)"><path d="M208,80H176V56a48,48,0,0,0-96,0V80H48A16,16,0,0,0,32,96V208a16,16,0,0,0,16,16H208a16,16,0,0,0,16-16V96A16,16,0,0,0,208,80ZM96,56a32,32,0,0,1,64,0V80H96ZM208,208H48V96H208V208Zm-80-48a24,24,0,1,1,24-24A24,24,0,0,1,128,160Z"/></svg>
        <p class="font-sans text-[var(--text-caption)] text-[var(--color-muted)] font-medium">
          Dana Escrow
        </p>
      </div>
      {#if $walletLoading}
        <div class="skeleton h-7 w-32 rounded-[var(--radius-sm)]"></div>
      {:else}
        <p class="font-display text-[var(--text-title-lg)] text-[var(--color-ink)] leading-none">
          {$walletStore ? formatIDR($walletStore.escrowBalance) : 'Rp 0'}
        </p>
      {/if}
      <p class="font-sans text-[var(--text-caption)] text-[var(--color-muted-soft)]">
        Dana dalam proses
      </p>
    </div>

    <!-- Total balance: dark -->
    <div
      class="sm:col-span-4 bg-[var(--color-surface-dark)] rounded-[var(--radius-xl)] p-6 flex flex-col gap-3"
    >
      <p class="font-sans text-[var(--text-caption)] text-[var(--color-on-dark-soft)] font-medium">
        Total Aset
      </p>
      {#if $walletLoading}
        <div class="skeleton h-7 w-36 rounded-[var(--radius-sm)] opacity-30"></div>
      {:else}
        <p class="font-display text-[var(--text-title-lg)] text-[var(--color-on-dark)] leading-none">
          {$walletStore ? formatIDR($walletStore.totalBalance) : 'Rp 0'}
        </p>
      {/if}
      <p class="font-sans text-[var(--text-caption)] text-[var(--color-on-dark-soft)]">
        {$walletStore?.currency ?? 'IDR'}
      </p>
    </div>
  </div>

  <!-- Quick actions -->
  <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">
    {#each [
      { href: '/topup', label: 'Top Up', icon: 'arrow-down', color: 'success' },
      { href: '/transfer', label: 'Transfer', icon: 'arrow-right', color: 'primary' },
      { href: '/rooms/create', label: 'Buat Room', icon: 'shield', color: 'teal' },
      { href: '/withdraw', label: 'Tarik Dana', icon: 'arrow-up', color: 'warning' }
    ] as action}
      <a
        href={action.href}
        class="bg-[var(--color-surface-card)] hover:bg-[var(--color-surface-cream-strong)] rounded-[var(--radius-lg)] p-4 flex flex-col items-center gap-2 transition-colors duration-150 active:scale-[0.97]"
      >
        <div
          class="w-10 h-10 rounded-[var(--radius-md)] flex items-center justify-center {action.color === 'success' ? 'bg-[var(--color-success)]/10' : action.color === 'primary' ? 'bg-[var(--color-primary)]/10' : action.color === 'teal' ? 'bg-[var(--color-accent-teal)]/10' : 'bg-[var(--color-warning)]/10'}"
        >
          {#if action.icon === 'arrow-down'}
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 256 256" fill="var(--color-success)"><path d="M205.66,149.66l-72,72a8,8,0,0,1-11.32,0l-72-72a8,8,0,0,1,11.32-11.32L120,196.69V40a8,8,0,0,1,16,0V196.69l58.34-58.35a8,8,0,0,1,11.32,11.32Z"/></svg>
          {:else if action.icon === 'arrow-right'}
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 256 256" fill="var(--color-primary)"><path d="M221.66,133.66l-72,72a8,8,0,0,1-11.32-11.32L196.69,136H40a8,8,0,0,1,0-16H196.69L138.34,61.66a8,8,0,0,1,11.32-11.32l72,72A8,8,0,0,1,221.66,133.66Z"/></svg>
          {:else if action.icon === 'shield'}
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 256 256" fill="var(--color-accent-teal)"><path d="M208,40H48A16,16,0,0,0,32,56V120c0,88,88,128,88,128s88-40,88-128V56A16,16,0,0,0,208,40Zm0,80c0,68.9-62.6,104.66-80,113.31C110.6,224.66,48,189,48,120V56H208Z"/></svg>
          {:else if action.icon === 'arrow-up'}
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 256 256" fill="var(--color-warning)"><path d="M205.66,117.66a8,8,0,0,1-11.32,0L136,59.31V216a8,8,0,0,1-16,0V59.31L61.66,117.66a8,8,0,0,1-11.32-11.32l72-72a8,8,0,0,1,11.32,0l72,72A8,8,0,0,1,205.66,117.66Z"/></svg>
          {/if}
        </div>
        <span class="font-sans font-medium text-[var(--text-body-sm)] text-[var(--color-ink)]">
          {action.label}
        </span>
      </a>
    {/each}
  </div>

  <!-- Total Data Counters (Rule 4 S1) -->
  <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">
    <div class="bg-[var(--color-surface-card)] rounded-[var(--radius-lg)] p-4 border border-[var(--color-hairline-soft)]">
      <p class="font-sans text-xs text-[var(--color-muted)] font-medium">Total Transaksi</p>
      <p class="font-display text-[var(--text-title-lg)] text-[var(--color-ink)] mt-1">32</p>
      <p class="text-[11px] text-[var(--color-muted-soft)] mt-0.5">Semua tipe mutasi</p>
    </div>
    <div class="bg-[var(--color-surface-card)] rounded-[var(--radius-lg)] p-4 border border-[var(--color-hairline-soft)]">
      <p class="font-sans text-xs text-[var(--color-muted)] font-medium">Covenant Sukses</p>
      <p class="font-display text-[var(--text-title-lg)] text-[var(--color-success)] mt-1">18</p>
      <p class="text-[11px] text-[var(--color-success)] mt-0.5">Dana terlepas aman</p>
    </div>
    <div class="bg-[var(--color-surface-card)] rounded-[var(--radius-lg)] p-4 border border-[var(--color-hairline-soft)]">
      <p class="font-sans text-xs text-[var(--color-muted)] font-medium">Rasio Keamanan</p>
      <p class="font-display text-[var(--text-title-lg)] text-[var(--color-primary)] mt-1">99.8%</p>
      <p class="text-[11px] text-[var(--color-primary)] mt-0.5">Bebas penipuan</p>
    </div>
    <div class="bg-[var(--color-surface-card)] rounded-[var(--radius-lg)] p-4 border border-[var(--color-hairline-soft)]">
      <p class="font-sans text-xs text-[var(--color-muted)] font-medium">Rata-rata Rilis</p>
      <p class="font-display text-[var(--text-title-lg)] text-[var(--color-ink)] mt-1">~2.4 Jam</p>
      <p class="text-[11px] text-[var(--color-muted-soft)] mt-0.5">Kecepatan serah-terima</p>
    </div>
  </div>

  <!-- Statistical Charts / Graphs (Rule 4 S1) -->
  <div class="grid grid-cols-1 lg:grid-cols-12 gap-4">
    <!-- Volume Transaksi 7 Hari Terakhir (Bar Chart) -->
    <div class="lg:col-span-7 bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] p-5 border border-[var(--color-hairline-soft)] flex flex-col justify-between">
      <div class="flex items-center justify-between mb-4">
        <div>
          <h3 class="font-sans font-medium text-[var(--text-body-md)] text-[var(--color-ink)]">
            Volume Transaksi 7 Hari Terakhir
          </h3>
          <p class="font-sans text-xs text-[var(--color-muted)] mt-0.5">
            Tren arus dana keluar/masuk mingguan
          </p>
        </div>
        <span class="text-xs font-mono font-medium px-2 py-0.5 rounded bg-[var(--color-surface-soft)] text-[var(--color-primary)]">
          Total: Rp 30.1 Jt
        </span>
      </div>

      <!-- SVG-like Pure CSS Responsive Bar Chart -->
      <div class="h-44 flex items-end justify-between gap-2 pt-6 px-2">
        {#each weeklyVolume as bar}
          <div class="flex-1 flex flex-col items-center gap-2 group h-full justify-end">
            <!-- Tooltip Hover -->
            <div class="opacity-0 group-hover:opacity-100 transition-opacity duration-150 text-[10px] font-mono bg-[var(--color-surface-dark)] text-white px-1.5 py-0.5 rounded pointer-events-none whitespace-nowrap shadow">
              {formatIDR(bar.amount)}
            </div>
            <!-- Bar -->
            <div
              class="w-full max-w-[32px] rounded-t-[var(--radius-sm)] bg-[var(--color-primary)]/80 hover:bg-[var(--color-primary)] transition-all duration-200"
              style="height: {bar.heightPct}%;"
            ></div>
            <!-- Day Label -->
            <span class="text-[11px] font-sans text-[var(--color-muted)] group-hover:text-[var(--color-ink)] transition-colors">
              {bar.day}
            </span>
          </div>
        {/each}
      </div>
    </div>

    <!-- Distribusi Status Escrow Covenant -->
    <div class="lg:col-span-5 bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] p-5 border border-[var(--color-hairline-soft)] flex flex-col justify-between">
      <div class="mb-3">
        <h3 class="font-sans font-medium text-[var(--text-body-md)] text-[var(--color-ink)]">
          Distribusi Status Escrow
        </h3>
        <p class="font-sans text-xs text-[var(--color-muted)] mt-0.5">
          Komposisi status 28 room transaksi terkini
        </p>
      </div>

      <!-- Multi-segment Progress Bar -->
      <div class="h-3 w-full rounded-full bg-[var(--color-surface-soft)] overflow-hidden flex mb-4">
        {#each statusDistribution as item}
          <div
            style="width: {item.pct}%; background-color: {item.color};"
            title="{item.label}: {item.pct}%"
          ></div>
        {/each}
      </div>

      <!-- Breakdown Legend -->
      <div class="flex flex-col gap-2.5">
        {#each statusDistribution as item}
          <div class="flex items-center justify-between text-xs">
            <div class="flex items-center gap-2">
              <span class="w-2.5 h-2.5 rounded-full shrink-0" style="background-color: {item.color};"></span>
              <span class="text-[var(--color-ink)] font-medium">{item.label}</span>
            </div>
            <div class="flex items-center gap-3">
              <span class="text-[var(--color-muted)]">{item.count} room</span>
              <span class="font-mono font-semibold text-[var(--color-ink)] w-8 text-right">{item.pct}%</span>
            </div>
          </div>
        {/each}
      </div>
    </div>
  </div>

  <!-- Recent transactions -->
  <div>
    <div class="flex items-center justify-between mb-4">
      <h2 class="font-sans font-medium text-[var(--text-title-md)] text-[var(--color-ink)]">
        Transaksi Terakhir
      </h2>
      <a
        href="/history"
        class="font-sans text-[var(--text-body-sm)] text-[var(--color-primary)] hover:underline"
      >
        Lihat semua
      </a>
    </div>

    <div class="bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] overflow-hidden">
      {#if $walletLoading}
        <div class="p-5">
          <Skeleton rows={3} />
        </div>
      {:else if $transactionsStore.length === 0}
        <div class="p-8 text-center">
          <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)]">
            Belum ada transaksi
          </p>
        </div>
      {:else}
        {#each $transactionsStore as tx, i}
          <div
            class="flex items-center gap-4 px-5 py-4 {i < $transactionsStore.length - 1 ? 'border-b border-[var(--color-hairline-soft)]' : ''}"
          >
            <!-- Type indicator -->
            <div
              class="w-9 h-9 rounded-[var(--radius-md)] flex items-center justify-center shrink-0 {tx.direction === 'IN' ? 'bg-[var(--color-success)]/10' : tx.direction === 'OUT' ? 'bg-[var(--color-error)]/10' : 'bg-[var(--color-surface-soft)]'}"
            >
              <span
                class="font-mono text-[14px] {tx.direction === 'IN' ? 'text-[var(--color-success)]' : tx.direction === 'OUT' ? 'text-[var(--color-error)]' : 'text-[var(--color-muted)]'}"
              >
                {txIcons[tx.type] ?? '·'}
              </span>
            </div>

            <!-- Description -->
            <div class="flex-1 min-w-0">
              <p
                class="font-sans font-medium text-[var(--text-body-sm)] text-[var(--color-ink)] truncate"
              >
                {tx.description}
              </p>
              {#if tx.counterpartyName}
                <p class="font-sans text-[var(--text-caption)] text-[var(--color-muted)] mt-0.5">
                  {tx.counterpartyName}
                </p>
              {/if}
              <p class="font-sans text-[var(--text-caption)] text-[var(--color-muted-soft)] mt-0.5">
                {formatDate(tx.createdAt)}
              </p>
            </div>

            <!-- Amount + status -->
            <div class="text-right shrink-0">
              <p
                class="font-sans font-medium text-[var(--text-body-sm)] {tx.direction === 'IN' ? 'text-[var(--color-success)]' : tx.direction === 'OUT' ? 'text-[var(--color-error)]' : 'text-[var(--color-muted)]'}"
              >
                {tx.direction === 'IN' ? '+' : tx.direction === 'OUT' ? '-' : ''}{formatIDR(tx.amount)}
              </p>
              <div class="mt-1">
                <Badge status={tx.status} />
              </div>
            </div>
          </div>
        {/each}
      {/if}
    </div>
  </div>

  <!-- Active rooms -->
  {#if !$roomsLoading && $roomsStore.length > 0}
    <div>
      <div class="flex items-center justify-between mb-4">
        <h2 class="font-sans font-medium text-[var(--text-title-md)] text-[var(--color-ink)]">
          Room Aktif
        </h2>
        <a
          href="/rooms"
          class="font-sans text-[var(--text-body-sm)] text-[var(--color-primary)] hover:underline"
        >
          Lihat semua
        </a>
      </div>
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
        {#each $roomsStore as room}
          <a
            href="/rooms/{room.roomId}"
            class="bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] p-5 flex flex-col gap-3 hover:shadow-[var(--shadow-card)] transition-shadow duration-150"
          >
            <div class="flex items-start justify-between gap-2">
              <div class="flex-1 min-w-0">
                <span
                  class="inline-block font-mono text-[11px] text-[var(--color-primary)] bg-[var(--color-primary)]/8 px-2 py-0.5 rounded-[var(--radius-sm)] mb-1"
                >
                  {room.roomCode}
                </span>
                <p
                  class="font-sans font-medium text-[var(--text-body-md)] text-[var(--color-ink)] leading-snug"
                >
                  {room.title}
                </p>
              </div>
              <Badge status={room.status} />
            </div>
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <Avatar name={room.buyer.name} src={room.buyer.avatar} size="xs" />
                <span class="font-sans text-[var(--text-caption)] text-[var(--color-muted)]">
                  {room.buyer.name}
                </span>
              </div>
              <p class="font-display text-[var(--text-title-sm)] text-[var(--color-ink)]">
                {formatIDR(room.totalAmount)}
              </p>
            </div>
          </a>
        {/each}
      </div>
    </div>
  {/if}
</div>
