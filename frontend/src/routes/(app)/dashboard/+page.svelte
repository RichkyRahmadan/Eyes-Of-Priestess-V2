<script lang="ts">
  import { authStore, walletStore, formattedBalance } from '$lib/stores';
  import { Wallet, PlusCircle, ArrowUpRight, ArrowDownLeft, DoorOpen, ShieldCheck, Clock, CheckCircle2, ChevronRight, AlertCircle } from '@lucide/svelte';

  let user = $derived($authStore.user || { fullName: 'Budi Santoso' });
  let wallet = $derived($walletStore);

  // Mock Active Rooms
  let activeRooms = $state([
    {
      roomId: 'rm-1',
      roomCode: 'EOP-8821',
      title: 'Jual Beli Akun Game FF Sultan Level 50',
      itemPrice: 250000,
      status: 'FUNDED',
      counterpartyName: 'Andi Wijaya',
      createdAt: '2 Jam lalu'
    },
    {
      roomId: 'rm-2',
      roomCode: 'EOP-9104',
      title: 'Lisensi Software Graphic Design Pro',
      itemPrice: 1500000,
      status: 'WAITING_PAYMENT',
      counterpartyName: 'Siti Rahma',
      createdAt: '5 Jam lalu'
    }
  ]);

  // Mock Recent Activity
  let recentTransactions = $state([
    {
      id: 'tx-101',
      type: 'ESCROW_HOLD',
      description: 'Escrow hold for room #EOP-8821',
      amount: 252500,
      direction: 'OUT',
      status: 'SUCCESS',
      date: 'Hari ini, 10:15'
    },
    {
      id: 'tx-102',
      type: 'TOPUP',
      description: 'Top-up Virtual Account BCA',
      amount: 1000000,
      direction: 'IN',
      status: 'SUCCESS',
      date: 'Kemarin, 14:20'
    },
    {
      id: 'tx-103',
      type: 'ESCROW_RELEASE',
      description: 'Escrow release room #EOP-7712',
      amount: 750000,
      direction: 'IN',
      status: 'SUCCESS',
      date: '10 Aug 2026'
    }
  ]);
</script>

<div class="space-y-8">
  <!-- 1. Welcome Band -->
  <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-hairline pb-6">
    <div class="space-y-1">
      <h1 class="font-display text-4xl lg:text-5xl font-bold text-ink">
        Selamat Datang, <span class="text-coral italic">{user.fullName}</span>
      </h1>
      <p class="text-sm text-muted">Ringkasan saldo dompet digital dan status transaksi escrow terkini Anda.</p>
    </div>

    <div class="flex items-center gap-3">
      <a href="/rooms?create=true" class="btn-editorial-primary text-sm font-semibold">
        <PlusCircle size={18} />
        Buat Room Baru
      </a>
    </div>
  </div>

  <!-- 2. Balance Cards Row (3-up) -->
  <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
    <!-- Available Balance Card -->
    <div class="card-editorial space-y-3">
      <div class="flex items-center justify-between text-muted text-xs font-semibold uppercase tracking-wider">
        <span>Saldo Tersedia</span>
        <Wallet size={16} class="text-coral" />
      </div>
      <div class="font-display text-3xl lg:text-4xl font-bold text-ink">
        {$formattedBalance}
      </div>
      <div class="text-xs text-muted pt-2 border-t border-hairline flex items-center justify-between">
        <span>Siap digunakan atau ditarik</span>
        <a href="/topup" class="text-coral font-semibold hover:underline">Top Up +</a>
      </div>
    </div>

    <!-- Escrow Balance Card -->
    <div class="card-editorial space-y-3">
      <div class="flex items-center justify-between text-muted text-xs font-semibold uppercase tracking-wider">
        <span>Dana Escrow Terkunci</span>
        <Clock size={16} class="text-accent-amber" />
      </div>
      <div class="font-display text-3xl lg:text-4xl font-bold text-ink">
        Rp {wallet?.escrowBalance.toLocaleString('id-ID') || '500.000'}
      </div>
      <div class="text-xs text-muted pt-2 border-t border-hairline flex items-center justify-between">
        <span>Dalam proses transaksi active</span>
        <a href="/rooms" class="text-coral font-semibold hover:underline">Detail Room →</a>
      </div>
    </div>

    <!-- Total Balance Card (Dark Chrome Product Theme) -->
    <div class="card-dark-chrome space-y-3">
      <div class="flex items-center justify-between text-on-dark-soft text-xs font-semibold uppercase tracking-wider">
        <span>Total Aset Vault</span>
        <ShieldCheck size={16} class="text-success" />
      </div>
      <div class="font-display text-3xl lg:text-4xl font-bold text-on-dark">
        Rp {((wallet?.availableBalance || 1500000) + (wallet?.escrowBalance || 500000)).toLocaleString('id-ID')}
      </div>
      <div class="text-xs text-on-dark-soft pt-2 border-t border-surface-dark-elevated flex items-center justify-between">
        <span>Terproteksi Sistem Instaban</span>
        <span class="text-success font-mono font-semibold">100% Safe</span>
      </div>
    </div>
  </div>

  <!-- 3. Quick Actions Bar -->
  <div class="flex flex-wrap items-center gap-4 bg-surface-card p-4 rounded-xl border border-hairline">
    <span class="text-xs font-semibold uppercase tracking-wider text-muted mr-2">Aksi Cepat:</span>
    <a href="/topup" class="btn-editorial-primary text-xs font-semibold">
      <ArrowDownLeft size={16} /> Top-Up Saldo
    </a>
    <a href="/transfer" class="btn-editorial-secondary text-xs font-semibold">
      <Wallet size={16} /> Transfer P2P
    </a>
    <a href="/withdraw" class="btn-editorial-secondary text-xs font-semibold">
      <ArrowUpRight size={16} /> Tarik Ke Bank
    </a>
    <a href="/rooms" class="btn-editorial-secondary text-xs font-semibold">
      <DoorOpen size={16} /> Kelola Room Escrow
    </a>
  </div>

  <!-- 4. Active Escrow Rooms & Recent Activity Grid -->
  <div class="grid grid-cols-1 lg:grid-cols-12 gap-8">
    <!-- Active Rooms List (7 Cols) -->
    <div class="lg:col-span-7 space-y-4">
      <div class="flex items-center justify-between">
        <h2 class="font-display text-2xl font-bold text-ink">Room Escrow Aktif</h2>
        <a href="/rooms" class="text-xs font-semibold text-coral hover:underline flex items-center gap-1">
          Lihat Semua <ChevronRight size={14} />
        </a>
      </div>

      <div class="space-y-3">
        {#each activeRooms as room (room.roomId)}
          <a href="/rooms/{room.roomId}" class="block card-editorial hover:border-coral transition-all group">
            <div class="flex items-start justify-between gap-4">
              <div class="space-y-1">
                <div class="flex items-center gap-2">
                  <span class="px-2.5 py-0.5 rounded-pill bg-primary-coral/10 text-coral font-mono text-xs font-semibold">
                    #{room.roomCode}
                  </span>
                  <span class="text-xs text-muted">{room.createdAt}</span>
                </div>
                <h3 class="font-display text-xl font-bold text-ink group-hover:text-coral transition-colors">
                  {room.title}
                </h3>
                <p class="text-xs text-muted">Counterparty: <span class="font-semibold text-body">{room.counterpartyName}</span></p>
              </div>

              <div class="text-right shrink-0 space-y-1">
                <div class="font-display text-lg font-bold text-ink">
                  Rp {room.itemPrice.toLocaleString('id-ID')}
                </div>
                <span class="inline-block px-2.5 py-0.5 rounded-pill text-xs font-semibold
                  {room.status === 'FUNDED' ? 'bg-success/15 text-success' : 'bg-warning/15 text-warning'}
                ">
                  {room.status}
                </span>
              </div>
            </div>
          </a>
        {/each}
      </div>
    </div>

    <!-- Recent Activity Table (5 Cols) -->
    <div class="lg:col-span-5 space-y-4">
      <div class="flex items-center justify-between">
        <h2 class="font-display text-2xl font-bold text-ink">Aktivitas Terakhir</h2>
        <a href="/history" class="text-xs font-semibold text-coral hover:underline flex items-center gap-1">
          Riwayat <ChevronRight size={14} />
        </a>
      </div>

      <div class="bg-surface-card rounded-xl border border-hairline p-4 space-y-3">
        {#each recentTransactions as tx (tx.id)}
          <div class="flex items-center justify-between py-2 border-b border-hairline last:border-0">
            <div class="flex items-center gap-3">
              <div class="w-8 h-8 rounded-lg flex items-center justify-center shrink-0
                {tx.direction === 'IN' ? 'bg-success/10 text-success' : 'bg-primary-coral/10 text-coral'}
              ">
                {#if tx.direction === 'IN'}
                  <ArrowDownLeft size={16} />
                {:else}
                  <ArrowUpRight size={16} />
                {/if}
              </div>
              <div>
                <div class="text-xs font-semibold text-ink line-clamp-1">{tx.description}</div>
                <div class="text-[11px] text-muted">{tx.date}</div>
              </div>
            </div>

            <div class="text-right shrink-0">
              <div class="font-mono text-xs font-bold {tx.direction === 'IN' ? 'text-success' : 'text-ink'}">
                {tx.direction === 'IN' ? '+' : '-'} Rp {tx.amount.toLocaleString('id-ID')}
              </div>
            </div>
          </div>
        {/each}
      </div>
    </div>
  </div>

  <!-- 5. Warm Editorial CTA Callout Band -->
  <div class="bg-primary-coral text-white rounded-2xl p-8 lg:p-10 flex flex-col md:flex-row items-center justify-between gap-6 shadow-card">
    <div class="space-y-2 text-center md:text-left">
      <h3 class="font-display text-3xl font-bold leading-tight">
        Jual Beli Produk Digital Tanpa Risiko Penipuan
      </h3>
      <p class="text-sm text-white/90 max-w-xl">
        Gunakan fitur Room Escrow EyesOfPriestess untuk mengunci dana hingga transaksi benar-benar diverifikasi kedua belah pihak.
      </p>
    </div>
    <a href="/rooms?create=true" class="bg-canvas text-ink hover:bg-surface-soft font-semibold px-6 py-3 rounded-lg text-sm transition-all shrink-0">
      Buat Room Rekber
    </a>
  </div>
</div>
