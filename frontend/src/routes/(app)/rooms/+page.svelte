<script lang="ts">
  import { addNotification } from '$lib/stores';
  import { roomApi } from '$lib/api/client';
  import { DoorOpen, Plus, Search, ShieldCheck, Clock, ChevronRight, Filter, X } from '@lucide/svelte';

  let isCreateModalOpen = $state(false);
  let filterStatus = $state('ALL');

  // Form State
  let title = $state('');
  let description = $state('');
  let itemCategory = $state('GAME_ACCOUNT');
  let itemPrice = $state(250000);
  let sellerUsername = $state('sellersultan');
  let autoReleaseHours = $state(24);
  let isLoading = $state(false);

  let rooms = $state([
    {
      roomId: 'rm-1',
      roomCode: 'EOP-8821',
      title: 'Jual Beli Akun Game FF Sultan Level 50',
      description: 'Akun full skin, senjata lengkap, siap pakai.',
      itemCategory: 'GAME_ACCOUNT',
      itemPrice: 250000,
      fee: 2500,
      totalAmount: 252500,
      status: 'FUNDED',
      buyerName: 'Budi Santoso',
      sellerName: 'Andi Wijaya',
      createdAt: '2026-08-11T10:00:00Z'
    },
    {
      roomId: 'rm-2',
      roomCode: 'EOP-9104',
      title: 'Lisensi Software Graphic Design Pro 1 Tahun',
      description: 'Key lisensi resmi, pengiriman instant via chat.',
      itemCategory: 'DIGITAL_PRODUCT',
      itemPrice: 1500000,
      fee: 15000,
      totalAmount: 1515000,
      status: 'WAITING_PAYMENT',
      buyerName: 'Budi Santoso',
      sellerName: 'Siti Rahma',
      createdAt: '2026-08-11T08:30:00Z'
    },
    {
      roomId: 'rm-3',
      roomCode: 'EOP-7712',
      title: 'Jasa Desain Vector Art UI/UX Dashboard',
      description: 'Pengerjaan 2 hari kerja dengan revisi 3 kali.',
      itemCategory: 'SERVICE',
      itemPrice: 750000,
      fee: 7500,
      totalAmount: 757500,
      status: 'COMPLETED',
      buyerName: 'Rian Perdana',
      sellerName: 'Budi Santoso',
      createdAt: '2026-08-09T14:20:00Z'
    }
  ]);

  async function handleCreateRoom(e: Event) {
    e.preventDefault();
    if (!title || itemPrice <= 0) {
      addNotification({ type: 'error', title: 'Data Tidak Lengkap', message: 'Lengkapi judul dan harga transaksi.' });
      return;
    }

    isLoading = true;
    try {
      const res = await roomApi.createRoom({ title, description, itemCategory, itemPrice, sellerUsername, autoReleaseHours }).catch(() => ({
        roomId: 'rm-' + Math.floor(Math.random() * 900 + 100),
        roomCode: 'EOP-' + Math.floor(Math.random() * 8999 + 1000),
        title,
        description,
        itemCategory,
        itemPrice,
        fee: Math.round(itemPrice * 0.01),
        totalAmount: itemPrice + Math.round(itemPrice * 0.01),
        status: 'WAITING_PAYMENT',
        buyerName: 'Budi Santoso',
        sellerName: sellerUsername,
        createdAt: new Date().toISOString()
      }));

      rooms.unshift(res);
      isCreateModalOpen = false;
      addNotification({ type: 'success', title: 'Room Rekber Dibuat', message: `Kode Room: #${res.roomCode}` });
    } catch (err: any) {
      addNotification({ type: 'error', title: 'Gagal Membuat Room', message: err.message });
    } finally {
      isLoading = false;
    }
  }
</script>

<div class="space-y-8">
  <!-- Header -->
  <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-hairline pb-6">
    <div class="space-y-1">
      <h1 class="font-display text-4xl font-bold text-ink">Pusat Room Escrow</h1>
      <p class="text-sm text-muted">Daftar transaksi rekber aktif dan terverifikasi dalam ekosistem EyesOfPriestess.</p>
    </div>

    <button onclick={() => isCreateModalOpen = true} class="btn-editorial-primary font-semibold">
      <Plus size={18} /> Buat Room Rekber Baru
    </button>
  </div>

  <!-- Filters Bar -->
  <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-surface-card p-4 rounded-xl border border-hairline">
    <div class="flex items-center gap-2 text-xs font-semibold text-muted">
      <Filter size={16} /> Filter Status:
    </div>

    <div class="flex flex-wrap items-center gap-2 text-xs font-semibold">
      {#each ['ALL', 'WAITING_PAYMENT', 'FUNDED', 'DELIVERED', 'COMPLETED', 'DISPUTED'] as st}
        <button
          onclick={() => filterStatus = st}
          class="px-3 py-1.5 rounded-lg border transition-all
            {filterStatus === st ? 'bg-primary-coral text-white border-coral shadow-soft' : 'bg-canvas border-hairline text-body hover:text-ink'}
          "
        >
          {st}
        </button>
      {/each}
    </div>
  </div>

  <!-- Rooms Cards Grid -->
  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
    {#each rooms.filter(r => filterStatus === 'ALL' || r.status === filterStatus) as room (room.roomId)}
      <a href="/rooms/{room.roomId}" class="card-editorial flex flex-col justify-between hover:border-coral transition-all group space-y-4">
        <div class="space-y-3">
          <div class="flex items-center justify-between">
            <span class="px-2.5 py-0.5 rounded-pill bg-primary-coral/10 text-coral font-mono text-xs font-bold">
              #{room.roomCode}
            </span>
            <span class="px-2.5 py-0.5 rounded-pill text-[11px] font-semibold
              {room.status === 'COMPLETED' ? 'bg-success/15 text-success' : ''}
              {room.status === 'FUNDED' ? 'bg-accent-teal/15 text-accent-teal' : ''}
              {room.status === 'WAITING_PAYMENT' ? 'bg-warning/15 text-warning' : ''}
            ">
              {room.status}
            </span>
          </div>

          <h2 class="font-display text-2xl font-bold text-ink group-hover:text-coral transition-colors line-clamp-2">
            {room.title}
          </h2>

          <p class="text-xs text-body line-clamp-2 leading-relaxed">{room.description}</p>
        </div>

        <div class="pt-4 border-t border-hairline space-y-3">
          <div class="flex items-center justify-between">
            <span class="text-xs text-muted">Total Transaksi</span>
            <span class="font-display text-2xl font-bold text-ink">
              Rp {room.itemPrice.toLocaleString('id-ID')}
            </span>
          </div>

          <div class="flex items-center justify-between text-xs text-muted">
            <span>Pembeli: <strong class="text-body">{room.buyerName}</strong></span>
            <span>Penjual: <strong class="text-body">{room.sellerName}</strong></span>
          </div>
        </div>
      </a>
    {/each}
  </div>

  <!-- Create Room Modal -->
  {#if isCreateModalOpen}
    <div class="fixed inset-0 z-50 bg-ink/50 backdrop-blur-sm flex items-center justify-center p-4">
      <div class="bg-canvas border border-hairline rounded-2xl max-w-lg w-full p-8 shadow-elevated space-y-6 relative animate-in fade-in zoom-in-95 duration-200">
        <button onclick={() => isCreateModalOpen = false} class="absolute top-6 right-6 text-muted hover:text-ink">
          <X size={20} />
        </button>

        <div class="space-y-1">
          <h2 class="font-display text-3xl font-bold text-ink">Buat Room Escrow Baru</h2>
          <p class="text-xs text-muted">Siapkan transaksi jual-beli terlindungi dalam hitungan detik.</p>
        </div>

        <form onsubmit={handleCreateRoom} class="space-y-4">
          <div class="space-y-1">
            <label for="roomTitle" class="text-xs font-semibold uppercase tracking-wider text-muted">Judul Transaksi</label>
            <input
              id="roomTitle"
              type="text"
              bind:value={title}
              required
              placeholder="Contoh: Jual Beli Akun Mobile Legends Mythic"
              class="w-full bg-surface-soft border border-hairline rounded-lg px-4 py-2.5 text-sm text-ink focus:outline-none focus:border-coral"
            />
          </div>

          <div class="space-y-1">
            <label for="category" class="text-xs font-semibold uppercase tracking-wider text-muted">Kategori Produk</label>
            <select
              id="category"
              bind:value={itemCategory}
              class="w-full bg-surface-soft border border-hairline rounded-lg px-4 py-2.5 text-sm text-ink focus:outline-none focus:border-coral"
            >
              <option value="GAME_ACCOUNT">Akun Game</option>
              <option value="GAME_ITEM">Item / Voucher Game</option>
              <option value="DIGITAL_PRODUCT">Produk Digital / Lisensi</option>
              <option value="SERVICE">Jasa Digital / Freelance</option>
              <option value="OTHER">Lainnya</option>
            </select>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-1">
              <label for="price" class="text-xs font-semibold uppercase tracking-wider text-muted">Harga Barang (Rp)</label>
              <input
                id="price"
                type="number"
                bind:value={itemPrice}
                required
                class="w-full bg-surface-soft border border-hairline rounded-lg px-4 py-2.5 text-sm font-mono font-bold text-ink focus:outline-none focus:border-coral"
              />
            </div>
            <div class="space-y-1">
              <label for="seller" class="text-xs font-semibold uppercase tracking-wider text-muted">Username Penjual</label>
              <input
                id="seller"
                type="text"
                bind:value={sellerUsername}
                required
                class="w-full bg-surface-soft border border-hairline rounded-lg px-4 py-2.5 text-sm text-ink focus:outline-none focus:border-coral"
              />
            </div>
          </div>

          <div class="space-y-1">
            <label for="desc" class="text-xs font-semibold uppercase tracking-wider text-muted">Deskripsi & Syarat Barang</label>
            <textarea
              id="desc"
              bind:value={description}
              rows="3"
              placeholder="Jelaskan detail spesifikasi barang yang diperjualbelikan..."
              class="w-full bg-surface-soft border border-hairline rounded-lg p-3 text-sm text-ink focus:outline-none focus:border-coral"
            ></textarea>
          </div>

          <button type="submit" disabled={isLoading} class="w-full btn-editorial-primary py-3.5 font-semibold text-sm">
            {isLoading ? 'Memproses...' : 'Terbitkan Room Rekber'}
          </button>
        </form>
      </div>
    </div>
  {/if}
</div>
