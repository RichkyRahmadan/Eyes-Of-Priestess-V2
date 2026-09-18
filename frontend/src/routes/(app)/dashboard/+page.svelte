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

  let allTransactions = $state<Transaction[]>([]);
  let allRooms = $state<Room[]>([]);

  onMount(async () => {
    walletLoading.set(true);
    roomsLoading.set(true);
    try {
      const [wallet, txRes, roomRes] = await Promise.all([
        walletApi.getBalance() as Promise<import('$lib/types').Wallet>,
        walletApi.getTransactions('size=50') as Promise<any>,
        roomApi.getRooms('size=50') as Promise<any>
      ]);
      walletStore.set(wallet);

      const txList: Transaction[] = Array.isArray(txRes)
        ? txRes
        : (txRes?.content ?? []);
      const roomList: Room[] = Array.isArray(roomRes)
        ? roomRes
        : (roomRes?.content ?? []);

      allTransactions = txList;
      allRooms = roomList;

      transactionsStore.set(txList.slice(0, 5));

      const activeRooms = roomList
        .filter((r) => ['FUNDED', 'WAITING_PAYMENT', 'DELIVERED'].includes(r.status))
        .slice(0, 4);
      roomsStore.set(activeRooms);
    } catch (e) {
      console.error('Failed to load dashboard data:', e);
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

  function getTxDirection(tx: Transaction): 'IN' | 'OUT' | 'NEUTRAL' {
    if (tx.direction) return tx.direction;
    if (['TOPUP', 'ESCROW_RELEASE', 'ESCROW_REFUND'].includes(tx.type)) return 'IN';
    if (['WITHDRAW', 'P2P_TRANSFER', 'ESCROW_HOLD', 'FEE'].includes(tx.type)) return 'OUT';
    return 'NEUTRAL';
  }

  // ─── Real-Time Dynamic Metrics (Rule 4 S1) ──────────────────────────────────
  const totalTransactionsCount = $derived(allTransactions.length);

  const completedCovenantsCount = $derived(
    allRooms.filter((r) => r.status === 'COMPLETED').length
  );

  const safetyRatio = $derived.by(() => {
    if (allRooms.length === 0) return '100%';
    const disputed = allRooms.filter((r) => r.status === 'DISPUTED').length;
    if (disputed === 0) return '100%';
    const pct = ((allRooms.length - disputed) / allRooms.length) * 100;
    return `${pct.toFixed(1)}%`;
  });

  const avgReleaseTime = $derived.by(() => {
    const completed = allRooms.filter((r) => r.status === 'COMPLETED');
    if (completed.length === 0) return '-';
    let totalHours = 0;
    let count = 0;
    for (const r of completed) {
      const start = r.createdAt ? new Date(r.createdAt).getTime() : 0;
      const end = (r as any).updatedAt ? new Date((r as any).updatedAt).getTime() : 0;
      if (start > 0 && end > start) {
        totalHours += (end - start) / 3600000;
        count++;
      }
    }
    if (count === 0) return '< 1 Jam';
    const avg = totalHours / count;
    return avg < 1 ? '< 1 Jam' : `~${avg.toFixed(1)} Jam`;
  });

  // ─── Dynamic 7-Day Weekly Volume ──────────────────────────────────────────
  const daysOfWeekLabels = ['Min', 'Sen', 'Sel', 'Rab', 'Kam', 'Jum', 'Sab'];

  const weeklyVolumeData = $derived.by(() => {
    const now = new Date();
    const days: { dateStr: string; day: string; amount: number }[] = [];

    // Rolling 7 days up to today
    for (let i = 6; i >= 0; i--) {
      const d = new Date(now);
      d.setDate(now.getDate() - i);
      const dateStr = d.toISOString().split('T')[0];
      const day = daysOfWeekLabels[d.getDay()];
      days.push({ dateStr, day, amount: 0 });
    }

    for (const tx of allTransactions) {
      if (!tx.createdAt) continue;
      const txDateStr = new Date(tx.createdAt).toISOString().split('T')[0];
      const match = days.find((d) => d.dateStr === txDateStr);
      if (match) {
        match.amount += Number(tx.amount || 0);
      }
    }

    const totalWeeklyAmount = days.reduce((sum, d) => sum + d.amount, 0);
    const maxAmount = Math.max(...days.map((d) => d.amount), 0);

    const bars = days.map((d) => ({
      day: d.day,
      amount: d.amount,
      heightPct: maxAmount > 0 ? Math.max(8, Math.round((d.amount / maxAmount) * 100)) : 0
    }));

    return { bars, totalWeeklyAmount };
  });

  // ─── Dynamic Escrow Status Distribution ───────────────────────────────────
  const statusDistribution = $derived.by(() => {
    const total = allRooms.length;
    const completed = allRooms.filter((r) => r.status === 'COMPLETED').length;
    const funded = allRooms.filter((r) => ['FUNDED', 'DELIVERED'].includes(r.status)).length;
    const waiting = allRooms.filter((r) => r.status === 'WAITING_PAYMENT').length;
    const disputed = allRooms.filter((r) => r.status === 'DISPUTED').length;

    return [
      {
        label: 'Selesai (Completed)',
        count: completed,
        color: '#16a34a',
        pct: total > 0 ? Math.round((completed / total) * 100) : 0
      },
      {
        label: 'Terdanai / Terkirim',
        count: funded,
        color: 'var(--color-primary)',
        pct: total > 0 ? Math.round((funded / total) * 100) : 0
      },
      {
        label: 'Menunggu Bayar',
        count: waiting,
        color: '#eab308',
        pct: total > 0 ? Math.round((waiting / total) * 100) : 0
      },
      {
        label: 'Sengketa (Dispute)',
        count: disputed,
        color: '#dc2626',
        pct: total > 0 ? Math.round((disputed / total) * 100) : 0
      }
    ];
  });
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
      {#if $walletLoading}
        <div class="skeleton h-7 w-12 rounded mt-1"></div>
      {:else}
        <p class="font-display text-[var(--text-title-lg)] text-[var(--color-ink)] mt-1">{totalTransactionsCount}</p>
      {/if}
      <p class="text-[11px] text-[var(--color-muted-soft)] mt-0.5">Semua tipe mutasi</p>
    </div>
    <div class="bg-[var(--color-surface-card)] rounded-[var(--radius-lg)] p-4 border border-[var(--color-hairline-soft)]">
      <p class="font-sans text-xs text-[var(--color-muted)] font-medium">Covenant Sukses</p>
      {#if $roomsLoading}
        <div class="skeleton h-7 w-12 rounded mt-1"></div>
      {:else}
        <p class="font-display text-[var(--text-title-lg)] text-[var(--color-success)] mt-1">{completedCovenantsCount}</p>
      {/if}
      <p class="text-[11px] text-[var(--color-success)] mt-0.5">Dana terlepas aman</p>
    </div>
    <div class="bg-[var(--color-surface-card)] rounded-[var(--radius-lg)] p-4 border border-[var(--color-hairline-soft)]">
      <p class="font-sans text-xs text-[var(--color-muted)] font-medium">Rasio Keamanan</p>
      {#if $roomsLoading}
        <div class="skeleton h-7 w-12 rounded mt-1"></div>
      {:else}
        <p class="font-display text-[var(--text-title-lg)] text-[var(--color-primary)] mt-1">{safetyRatio}</p>
      {/if}
      <p class="text-[11px] text-[var(--color-primary)] mt-0.5">Bebas penipuan</p>
    </div>
    <div class="bg-[var(--color-surface-card)] rounded-[var(--radius-lg)] p-4 border border-[var(--color-hairline-soft)]">
      <p class="font-sans text-xs text-[var(--color-muted)] font-medium">Rata-rata Rilis</p>
      {#if $roomsLoading}
        <div class="skeleton h-7 w-12 rounded mt-1"></div>
      {:else}
        <p class="font-display text-[var(--text-title-lg)] text-[var(--color-ink)] mt-1">{avgReleaseTime}</p>
      {/if}
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
          Total: {formatIDR(weeklyVolumeData.totalWeeklyAmount)}
        </span>
      </div>

      <!-- SVG-like Pure CSS Responsive Bar Chart -->
      <div class="h-44 flex items-end justify-between gap-2 pt-6 px-2">
        {#each weeklyVolumeData.bars as bar}
          <div class="flex-1 flex flex-col items-center gap-2 group h-full justify-end">
            <!-- Tooltip Hover -->
            <div class="opacity-0 group-hover:opacity-100 transition-opacity duration-150 text-[10px] font-mono bg-[var(--color-surface-dark)] text-white px-1.5 py-0.5 rounded pointer-events-none whitespace-nowrap shadow">
              {formatIDR(bar.amount)}
            </div>
            <!-- Bar -->
            <div
              class="w-full max-w-[32px] rounded-t-[var(--radius-sm)] {bar.amount > 0 ? 'bg-[var(--color-primary)]/80 hover:bg-[var(--color-primary)]' : 'bg-[var(--color-surface-soft)]'} transition-all duration-200"
              style="height: {bar.amount > 0 ? bar.heightPct : 4}%;"
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
          {allRooms.length > 0 ? `Komposisi status ${allRooms.length} room transaksi terkini` : 'Belum ada room transaksi'}
        </p>
      </div>

      <!-- Multi-segment Progress Bar -->
      <div class="h-3 w-full rounded-full bg-[var(--color-surface-soft)] overflow-hidden flex mb-4">
        {#if allRooms.length === 0}
          <div class="w-full h-full bg-[var(--color-hairline-soft)]" title="Belum ada transaksi"></div>
        {:else}
          {#each statusDistribution as item}
            {#if item.pct > 0}
              <div
                style="width: {item.pct}%; background-color: {item.color};"
                title="{item.label}: {item.pct}%"
              ></div>
            {/if}
          {/each}
        {/if}
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
          {@const dir = getTxDirection(tx)}
          <div
            class="flex items-center gap-4 px-5 py-4 {i < $transactionsStore.length - 1 ? 'border-b border-[var(--color-hairline-soft)]' : ''}"
          >
            <!-- Type indicator -->
            <div
              class="w-9 h-9 rounded-[var(--radius-md)] flex items-center justify-center shrink-0 {dir === 'IN' ? 'bg-[var(--color-success)]/10' : dir === 'OUT' ? 'bg-[var(--color-error)]/10' : 'bg-[var(--color-surface-soft)]'}"
            >
              <span
                class="font-mono text-[14px] {dir === 'IN' ? 'text-[var(--color-success)]' : dir === 'OUT' ? 'text-[var(--color-error)]' : 'text-[var(--color-muted)]'}"
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
                class="font-sans font-medium text-[var(--text-body-sm)] {dir === 'IN' ? 'text-[var(--color-success)]' : dir === 'OUT' ? 'text-[var(--color-error)]' : 'text-[var(--color-muted)]'}"
              >
                {dir === 'IN' ? '+' : dir === 'OUT' ? '-' : ''}{formatIDR(tx.amount)}
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
            href="/rooms/{room.roomId ?? (room as any).id}"
            class="bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] p-5 flex flex-col gap-3 hover:shadow-[var(--shadow-card)] transition-shadow duration-150"
          >
            <div class="flex items-start justify-between gap-2">
              <div class="flex-1 min-w-0">
                <span
                  class="inline-block font-mono text-[11px] text-[var(--color-primary)] bg-[var(--color-primary)]/8 px-2 py-0.5 rounded-[var(--radius-sm)] mb-1"
                >
                  {room.roomCode ?? (room as any).invitationCode ?? 'COV'}
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
                <Avatar name={room.buyer?.name ?? 'Pengguna'} src={room.buyer?.avatar} size="xs" />
                <span class="font-sans text-[var(--text-caption)] text-[var(--color-muted)]">
                  {room.buyer?.name ?? (room.seller?.name ? 'Mitra: ' + room.seller.name : 'Belum bergabung')}
                </span>
              </div>
              <p class="font-display text-[var(--text-title-sm)] text-[var(--color-ink)]">
                {formatIDR(room.totalAmount ?? (room as any).amount ?? 0)}
              </p>
            </div>
          </a>
        {/each}
      </div>
    </div>
  {/if}
</div>

