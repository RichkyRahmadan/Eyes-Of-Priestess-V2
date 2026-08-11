<script lang="ts">
  import { authStore, addNotification } from '$lib/stores';
  import { roomApi } from '$lib/api/client';
  import { page } from '$app/state';
  import { ShieldCheck, Clock, CheckCircle2, AlertTriangle, Send, Image, FileText, Lock, ArrowRight, User } from '@lucide/svelte';

  let roomId = $derived(page.params.id || 'rm-1');
  let user = $derived($authStore.user);

  let roomStatus = $state<'WAITING_PAYMENT' | 'FUNDED' | 'DELIVERED' | 'COMPLETED' | 'DISPUTED'>('FUNDED');
  let pin = $state('');
  let isActionModalOpen = $state(false);
  let actionType = $state<'FUND' | 'CONFIRM' | 'DELIVER' | 'DISPUTE'>('FUND');

  // Chat messages state
  let messages = $state([
    {
      id: 'm-1',
      senderName: 'System',
      content: 'Room Escrow #EOP-8821 berhasil dibuat. Menunggu pendanaan dari Buyer.',
      type: 'ESCROW_STATUS',
      sentAt: '10:00'
    },
    {
      id: 'm-2',
      senderName: 'Andi Wijaya',
      content: 'Halo mas Budi, dana escrow sudah saya transfer ke vault. Ditunggu datanya ya.',
      type: 'TEXT',
      sentAt: '10:05'
    },
    {
      id: 'm-3',
      senderName: 'System',
      content: 'Dana sebesar Rp 252.500 berhasil terkunci di Escrow Vault.',
      type: 'ESCROW_STATUS',
      sentAt: '10:05'
    }
  ]);

  let newMessageText = $state('');

  function handleSendMessage(e: Event) {
    e.preventDefault();
    if (!newMessageText.trim()) return;

    messages.push({
      id: 'm-' + (messages.length + 1),
      senderName: user?.fullName || 'Budi Santoso',
      content: newMessageText,
      type: 'TEXT',
      sentAt: new Date().toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' })
    });

    newMessageText = '';
  }

  function executeAction() {
    if (actionType === 'FUND') {
      roomStatus = 'FUNDED';
      messages.push({
        id: 'm-' + (messages.length + 1),
        senderName: 'System',
        content: 'Buyer telah memverifikasi PIN dan mendanai Room ini.',
        type: 'ESCROW_STATUS',
        sentAt: 'Baru saja'
      });
      addNotification({ type: 'success', title: 'Dana Terkunci', message: 'Room berhasil didanai.' });
    } else if (actionType === 'DELIVER') {
      roomStatus = 'DELIVERED';
      messages.push({
        id: 'm-' + (messages.length + 1),
        senderName: 'System',
        content: 'Penjual telah mengirimkan bukti barang/jasa.',
        type: 'ESCROW_STATUS',
        sentAt: 'Baru saja'
      });
      addNotification({ type: 'info', title: 'Barang Dikirim', message: 'Menunggu konfirmasi penerimaan pembeli.' });
    } else if (actionType === 'CONFIRM') {
      roomStatus = 'COMPLETED';
      messages.push({
        id: 'm-' + (messages.length + 1),
        senderName: 'System',
        content: 'Buyer telah mengonfirmasi penerimaan barang. Dana dicairkan ke penjual.',
        type: 'ESCROW_STATUS',
        sentAt: 'Baru saja'
      });
      addNotification({ type: 'success', title: 'Transaksi Selesai', message: 'Dana telah diteruskan ke seller.' });
    } else if (actionType === 'DISPUTE') {
      roomStatus = 'DISPUTED';
      messages.push({
        id: 'm-' + (messages.length + 1),
        senderName: 'System',
        content: 'Dispute telah dibuka. Arbitrator EyesOfPriestess akan meninjau bukti.',
        type: 'ESCROW_STATUS',
        sentAt: 'Baru saja'
      });
      addNotification({ type: 'warning', title: 'Dispute Dibuat', message: 'Menunggu peninjauan admin dispute.' });
    }

    isActionModalOpen = false;
    pin = '';
  }
</script>

<div class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
  <!-- Left Column (60%): Room Details & Timeline -->
  <div class="lg:col-span-7 space-y-6">
    <!-- Header Card -->
    <div class="card-editorial p-6 space-y-4">
      <div class="flex items-center justify-between">
        <span class="px-3 py-1 rounded-pill bg-primary-coral/10 text-coral font-mono text-xs font-bold">
          ROOM #EOP-8821
        </span>
        <span class="px-3 py-1 rounded-pill text-xs font-bold
          {roomStatus === 'COMPLETED' ? 'bg-success/15 text-success' : ''}
          {roomStatus === 'FUNDED' ? 'bg-accent-teal/15 text-accent-teal' : ''}
          {roomStatus === 'WAITING_PAYMENT' ? 'bg-warning/15 text-warning' : ''}
          {roomStatus === 'DELIVERED' ? 'bg-accent-amber/15 text-accent-amber' : ''}
          {roomStatus === 'DISPUTED' ? 'bg-error/15 text-error' : ''}
        ">
          {roomStatus}
        </span>
      </div>

      <div class="space-y-1">
        <h1 class="font-display text-3xl font-bold text-ink">Jual Beli Akun Game FF Sultan Level 50</h1>
        <p class="text-xs text-muted">Kategori: <strong class="text-body font-mono">GAME_ACCOUNT</strong> • Auto-release: <span class="font-mono">24 Jam</span></p>
      </div>

      <!-- Price Breakdown -->
      <div class="bg-surface-cream-strong/50 p-4 rounded-xl space-y-2 border border-hairline">
        <div class="flex justify-between text-xs text-muted">
          <span>Harga Barang</span>
          <span class="font-mono text-ink">Rp 250.000</span>
        </div>
        <div class="flex justify-between text-xs text-muted">
          <span>Biaya Layanan Escrow (1%)</span>
          <span class="font-mono text-ink">Rp 2.500</span>
        </div>
        <div class="pt-2 border-t border-hairline flex justify-between items-end">
          <span class="text-xs font-bold text-ink uppercase">Total Dana Dalam Vault</span>
          <span class="font-display text-3xl font-bold text-coral">Rp 252.500</span>
        </div>
      </div>

      <!-- Participants Info -->
      <div class="grid grid-cols-2 gap-4 pt-2">
        <div class="p-3 rounded-lg border border-hairline bg-canvas flex items-center gap-3">
          <div class="w-8 h-8 rounded-full bg-surface-dark text-on-dark flex items-center justify-center font-bold text-xs">
            B
          </div>
          <div>
            <div class="text-xs font-bold text-ink">Budi Santoso</div>
            <div class="text-[10px] text-coral uppercase font-semibold">Buyer (Pembeli)</div>
          </div>
        </div>

        <div class="p-3 rounded-lg border border-hairline bg-canvas flex items-center gap-3">
          <div class="w-8 h-8 rounded-full bg-primary-coral text-white flex items-center justify-center font-bold text-xs">
            A
          </div>
          <div>
            <div class="text-xs font-bold text-ink">Andi Wijaya</div>
            <div class="text-[10px] text-muted uppercase font-semibold">Seller (Penjual)</div>
          </div>
        </div>
      </div>

      <!-- Action Buttons Contextual to Status -->
      <div class="pt-4 border-t border-hairline flex flex-wrap gap-3">
        {#if roomStatus === 'WAITING_PAYMENT'}
          <button onclick={() => { actionType = 'FUND'; isActionModalOpen = true; }} class="btn-editorial-primary flex-1 py-3 text-sm font-semibold">
            <Lock size={16} /> Fund Room (Kunci Dana)
          </button>
        {:else if roomStatus === 'FUNDED'}
          <button onclick={() => { actionType = 'DELIVER'; isActionModalOpen = true; }} class="btn-editorial-primary flex-1 py-3 text-sm font-semibold">
            Konfirmasi Pengiriman (Seller)
          </button>
          <button onclick={() => { actionType = 'DISPUTE'; isActionModalOpen = true; }} class="btn-editorial-secondary py-3 text-sm font-semibold text-error border-error/30">
            Buka Dispute
          </button>
        {:else if roomStatus === 'DELIVERED'}
          <button onclick={() => { actionType = 'CONFIRM'; isActionModalOpen = true; }} class="btn-editorial-primary flex-1 py-3 text-sm font-semibold">
            <CheckCircle2 size={16} /> Konfirmasi Diterima & Rilis Dana
          </button>
          <button onclick={() => { actionType = 'DISPUTE'; isActionModalOpen = true; }} class="btn-editorial-secondary py-3 text-sm font-semibold text-error border-error/30">
            Buka Dispute
          </button>
        {:else if roomStatus === 'COMPLETED'}
          <div class="w-full text-center py-2 text-xs font-bold text-success bg-success/10 rounded-lg">
            ✓ Transaksi Selesai & Dana Telah Dicairkan
          </div>
        {:else if roomStatus === 'DISPUTED'}
          <a href="/disputes" class="w-full btn-editorial-secondary text-center py-3 text-sm font-semibold">
            Lihat Detail Arbitrase Dispute
          </a>
        {/if}
      </div>
    </div>

    <!-- Timeline Stepper -->
    <div class="card-editorial p-6 space-y-4">
      <h2 class="font-display text-xl font-bold text-ink">Timeline Transaksi</h2>
      <div class="space-y-4 border-l-2 border-hairline pl-4 ml-2">
        <div class="relative space-y-0.5">
          <div class="absolute -left-[23px] top-1 w-3 h-3 rounded-full bg-success"></div>
          <div class="text-xs font-bold text-ink">Room Dibuat</div>
          <div class="text-[11px] text-muted">Hari ini, 10:00 WIB oleh Penjual</div>
        </div>
        <div class="relative space-y-0.5">
          <div class="absolute -left-[23px] top-1 w-3 h-3 rounded-full {roomStatus !== 'WAITING_PAYMENT' ? 'bg-success' : 'bg-hairline'}"></div>
          <div class="text-xs font-bold text-ink">Dana Terkunci di Vault</div>
          <div class="text-[11px] text-muted">Hari ini, 10:05 WIB (Rp 252.500)</div>
        </div>
        <div class="relative space-y-0.5">
          <div class="absolute -left-[23px] top-1 w-3 h-3 rounded-full {roomStatus === 'DELIVERED' || roomStatus === 'COMPLETED' ? 'bg-success' : 'bg-hairline'}"></div>
          <div class="text-xs font-bold text-ink">Pengiriman Barang / Akun</div>
          <div class="text-[11px] text-muted">Bukti pengiriman diunggah di panel chat</div>
        </div>
        <div class="relative space-y-0.5">
          <div class="absolute -left-[23px] top-1 w-3 h-3 rounded-full {roomStatus === 'COMPLETED' ? 'bg-success' : 'bg-hairline'}"></div>
          <div class="text-xs font-bold text-ink">Pencairan Dana Ke Penjual</div>
          <div class="text-[11px] text-muted">Pelepasan otomatis atau via konfirmasi buyer</div>
        </div>
      </div>
    </div>
  </div>

  <!-- Right Column (40%): Dark Navy Chat Area -->
  <div class="lg:col-span-5 h-[620px] card-dark-chrome p-0 flex flex-col justify-between overflow-hidden shadow-elevated border border-surface-dark-elevated">
    <!-- Chat Topbar -->
    <div class="p-4 bg-surface-dark-elevated border-b border-surface-dark-soft flex items-center justify-between">
      <div class="flex items-center gap-3">
        <div class="w-8 h-8 rounded-full bg-primary-coral text-white flex items-center justify-center font-bold text-xs">
          EP
        </div>
        <div>
          <div class="text-xs font-bold text-on-dark">Enclave Diskusi Room</div>
          <div class="text-[10px] text-on-dark-soft">Encrypted Escrow Channel</div>
        </div>
      </div>
      <span class="w-2 h-2 rounded-full bg-success"></span>
    </div>

    <!-- Chat Messages Stream -->
    <div class="flex-1 p-4 overflow-y-auto space-y-3 font-sans text-xs">
      {#each messages as msg (msg.id)}
        {#if msg.type === 'ESCROW_STATUS'}
          <div class="my-2 p-3 rounded-lg bg-surface-dark-elevated border border-surface-dark-soft text-center text-on-dark-soft text-[11px] space-y-1">
            <div class="font-mono text-coral font-semibold uppercase tracking-wider text-[10px]">System Notification</div>
            <div class="text-on-dark">{msg.content}</div>
          </div>
        {:else}
          <div class="flex flex-col {msg.senderName === (user?.fullName || 'Budi Santoso') ? 'items-end' : 'items-start'}">
            <div class="text-[10px] text-on-dark-soft mb-1">{msg.senderName} • {msg.sentAt}</div>
            <div class="p-3 rounded-xl max-w-[85%] leading-relaxed
              {msg.senderName === (user?.fullName || 'Budi Santoso') ? 'bg-primary-coral text-white rounded-br-none' : 'bg-surface-dark-elevated text-on-dark rounded-bl-none'}
            ">
              {msg.content}
            </div>
          </div>
        {/if}
      {/each}
    </div>

    <!-- Chat Input Area -->
    <form onsubmit={handleSendMessage} class="p-3 bg-surface-dark-elevated border-t border-surface-dark-soft flex items-center gap-2">
      <input
        type="text"
        bind:value={newMessageText}
        placeholder="Tulis pesan atau kirim data akun..."
        class="flex-1 bg-surface-dark border border-surface-dark-soft rounded-lg px-3 py-2 text-xs text-on-dark focus:outline-none focus:border-coral"
      />
      <button type="submit" class="p-2 rounded-lg bg-primary-coral text-white hover:bg-primary-active transition-colors">
        <Send size={16} />
      </button>
    </form>
  </div>
</div>

<!-- Modal Dialog -->
{#if isActionModalOpen}
  <div class="fixed inset-0 z-50 bg-ink/50 backdrop-blur-sm flex items-center justify-center p-4">
    <div class="bg-canvas border border-hairline rounded-2xl max-w-sm w-full p-6 shadow-elevated space-y-4">
      <h3 class="font-display text-2xl font-bold text-ink">Konfirmasi PIN Transaksi</h3>
      <p class="text-xs text-muted">Masukkan 6 digit PIN untuk mengeksekusi aksi ini.</p>

      <input
        type="password"
        maxlength="6"
        bind:value={pin}
        placeholder="••••••"
        class="w-full bg-surface-soft border border-hairline rounded-lg py-3 tracking-[0.5em] font-mono text-xl text-center text-ink focus:outline-none focus:border-coral"
      />

      <div class="flex gap-2">
        <button onclick={() => isActionModalOpen = false} class="btn-editorial-secondary flex-1 py-2.5 text-xs font-semibold">
          Batal
        </button>
        <button onclick={executeAction} class="btn-editorial-primary flex-1 py-2.5 text-xs font-semibold">
          Eksekusi
        </button>
      </div>
    </div>
  </div>
{/if}
