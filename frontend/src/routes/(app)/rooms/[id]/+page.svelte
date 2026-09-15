<script lang="ts">
  import { onMount } from 'svelte';
  import { page } from '$app/state';
  import { activeRoomStore } from '$lib/stores/rooms';
  import { currentUser } from '$lib/stores/auth';
  import { roomApi } from '$lib/api';
  import { addNotification } from '$lib/stores/notifications';
  import Badge from '$lib/components/ui/Badge.svelte';
  import Avatar from '$lib/components/ui/Avatar.svelte';
  import Button from '$lib/components/ui/Button.svelte';
  import PinInput from '$lib/components/ui/PinInput.svelte';
  import Skeleton from '$lib/components/ui/Skeleton.svelte';
  import { formatIDR, formatDate } from '$lib/utils/format';
  import type { Room } from '$lib/types';

  let loading = $state(true);
  let actionLoading = $state(false);
  let showPinModal = $state(false);
  let pendingAction = $state<'fund' | 'confirm' | null>(null);
  let pinValues = $state(Array(6).fill(''));
  let pinError = $state('');
  let room = $derived($activeRoomStore);

  const userRole = $derived(() => {
    if (!room || !$currentUser) return null;
    if (room.buyer.id === $currentUser.id) return 'buyer';
    if (room.seller.id === $currentUser.id) return 'seller';
    return null;
  });

  const canFund = $derived(
    userRole() === 'buyer' && room?.status === 'WAITING_PAYMENT'
  );
  const canDeliver = $derived(
    userRole() === 'seller' && room?.status === 'FUNDED'
  );
  const canConfirm = $derived(
    userRole() === 'buyer' && room?.status === 'DELIVERED'
  );
  const canDispute = $derived(
    room?.status === 'FUNDED' || room?.status === 'DELIVERED'
  );

  onMount(async () => {
    try {
      const data = (await roomApi.getRoom(page.params.id ?? '')) as Room;
      activeRoomStore.set(data);
    } catch {
      addNotification({ type: 'error', title: 'Gagal memuat room', message: 'Coba muat ulang halaman' });
    } finally {
      loading = false;
    }
  });

  function triggerAction(action: 'fund' | 'confirm') {
    pendingAction = action;
    pinValues = Array(6).fill('');
    pinError = '';
    showPinModal = true;
  }

  async function executeWithPin() {
    const pin = pinValues.join('');
    if (pin.length !== 6) {
      pinError = 'Masukkan PIN 6 digit';
      return;
    }

    actionLoading = true;
    pinError = '';
    try {
      if (pendingAction === 'fund') {
        const updated = await roomApi.fundRoom(room!.roomId, pin) as Room;
        activeRoomStore.set(updated);
        addNotification({ type: 'success', title: 'Dana berhasil dikunci', message: `${formatIDR(room!.totalAmount)} telah masuk ke escrow` });
      } else if (pendingAction === 'confirm') {
        const updated = await roomApi.confirmRoom(room!.roomId, { pin }) as Room;
        activeRoomStore.set(updated);
        addNotification({ type: 'success', title: 'Transaksi selesai', message: 'Dana telah dilepas ke seller' });
      }
      showPinModal = false;
    } catch (err: unknown) {
      const apiErr = err as { message?: string };
      pinError = apiErr?.message ?? 'PIN salah atau terjadi kesalahan';
    } finally {
      actionLoading = false;
    }
  }

  const statusTimelineLabels: Record<string, string> = {
    WAITING_PAYMENT: 'Room dibuat',
    FUNDED: 'Dana dikunci',
    DELIVERED: 'Bukti pengiriman diupload',
    COMPLETED: 'Transaksi selesai',
    DISPUTED: 'Sengketa dibuka',
    CANCELLED: 'Room dibatalkan',
    REFUNDED: 'Dana dikembalikan'
  };
</script>

<svelte:head>
  <title>{room?.title ?? 'Room Detail'} - EyesOfPriestess</title>
</svelte:head>

{#if loading}
  <div class="max-w-2xl mx-auto">
    <Skeleton rows={4} />
  </div>
{:else if !room}
  <div class="text-center py-16">
    <p class="font-sans text-[var(--text-body-md)] text-[var(--color-muted)]">Room tidak ditemukan</p>
    <a href="/rooms" class="text-[var(--color-primary)] hover:underline font-sans text-[14px] mt-2 inline-block">
      Kembali ke daftar room
    </a>
  </div>
{:else}
  <div class="flex flex-col gap-6 pb-20 lg:pb-8">
    <!-- Header -->
    <div class="flex items-start justify-between gap-4">
      <div>
        <a href="/rooms" class="font-sans text-[13px] text-[var(--color-muted)] hover:text-[var(--color-primary)] transition-colors">
          &larr; Room Escrow
        </a>
        <h1 class="font-display text-[var(--text-display-sm)] text-[var(--color-ink)] tracking-[-0.01em] mt-1">
          {room.title}
        </h1>
        <div class="flex items-center gap-2 mt-1">
          <span class="font-mono text-[12px] text-[var(--color-primary)] bg-[var(--color-primary)]/8 px-2 py-0.5 rounded-[var(--radius-sm)]">
            {room.roomCode}
          </span>
          <Badge status={room.status} />
        </div>
      </div>
      {#if canDispute}
        <Button variant="secondary" size="sm" onclick={() => addNotification({ type: 'warning', title: 'Dispute', message: 'Fitur dispute akan segera tersedia' })}>
          Laporkan Sengketa
        </Button>
      {/if}
    </div>

    <!-- Main content: 2-col on desktop -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-5">
      <!-- Left: room details + actions -->
      <div class="lg:col-span-2 flex flex-col gap-5">
        <!-- Financial breakdown -->
        <div class="bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] p-6">
          <h2 class="font-sans font-medium text-[var(--text-title-sm)] text-[var(--color-ink)] mb-4">
            Detail Escrow
          </h2>
          <div class="space-y-2">
            <div class="flex justify-between items-center py-2 border-b border-[var(--color-hairline-soft)]">
              <span class="font-sans text-[var(--text-body-sm)] text-[var(--color-body)]">Harga barang</span>
              <span class="font-sans font-medium text-[var(--text-body-sm)] text-[var(--color-ink)]">{formatIDR(room.itemPrice)}</span>
            </div>
            <div class="flex justify-between items-center py-2 border-b border-[var(--color-hairline-soft)]">
              <span class="font-sans text-[var(--text-body-sm)] text-[var(--color-body)]">Biaya platform (1%)</span>
              <span class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)]">{formatIDR(room.fee)}</span>
            </div>
            <div class="flex justify-between items-center py-2">
              <span class="font-sans font-medium text-[var(--text-body-sm)] text-[var(--color-ink)]">Total</span>
              <span class="font-display text-[var(--text-title-lg)] text-[var(--color-ink)]">{formatIDR(room.totalAmount)}</span>
            </div>
          </div>
        </div>

        <!-- Participants -->
        <div class="bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] p-6">
          <h2 class="font-sans font-medium text-[var(--text-title-sm)] text-[var(--color-ink)] mb-4">
            Pihak Transaksi
          </h2>
          <div class="flex flex-col sm:flex-row gap-4">
            <div class="flex items-center gap-3 flex-1">
              <Avatar name={room.buyer.name} src={room.buyer.avatar} />
              <div>
                <p class="font-sans font-medium text-[var(--text-body-sm)] text-[var(--color-ink)]">{room.buyer.name}</p>
                <p class="font-sans text-[var(--text-caption)] text-[var(--color-muted)]">Pembeli</p>
              </div>
            </div>
            <div class="flex items-center gap-3 flex-1">
              <Avatar name={room.seller.name} src={room.seller.avatar} />
              <div>
                <p class="font-sans font-medium text-[var(--text-body-sm)] text-[var(--color-ink)]">{room.seller.name}</p>
                <p class="font-sans text-[var(--text-caption)] text-[var(--color-muted)]">Penjual</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Action area -->
        {#if canFund}
          <div class="bg-[var(--color-surface-dark)] rounded-[var(--radius-xl)] p-6">
            <h2 class="font-sans font-medium text-[var(--text-title-sm)] text-[var(--color-on-dark)] mb-2">
              Danai Room Ini
            </h2>
            <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-on-dark-soft)] mb-4">
              {formatIDR(room.totalAmount)} akan dipindahkan dari saldo Anda ke escrow. Dana hanya dilepas setelah Anda konfirmasi penerimaan.
            </p>
            <Button variant="primary" onclick={() => triggerAction('fund')}>
              Kunci Dana {formatIDR(room.totalAmount)}
            </Button>
          </div>
        {:else if canDeliver}
          <div class="bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] p-6">
            <h2 class="font-sans font-medium text-[var(--text-title-sm)] text-[var(--color-ink)] mb-2">
              Upload Bukti Pengiriman
            </h2>
            <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-body)] mb-4">
              Kirimkan bukti pengiriman barang atau jasa ke pembeli.
            </p>
            <a
              href="/rooms/{room.roomId}/deliver"
              class="inline-flex items-center h-10 px-5 bg-[var(--color-primary)] text-white font-sans font-medium text-[14px] rounded-[var(--radius-md)] hover:bg-[var(--color-primary-active)] active:scale-[0.98] transition-all duration-150"
            >
              Upload Bukti
            </a>
          </div>
        {:else if canConfirm}
          <div class="bg-[var(--color-surface-dark)] rounded-[var(--radius-xl)] p-6">
            <h2 class="font-sans font-medium text-[var(--text-title-sm)] text-[var(--color-on-dark)] mb-2">
              Konfirmasi Penerimaan
            </h2>
            <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-on-dark-soft)] mb-4">
              Barang atau jasa sudah Anda terima? Konfirmasi untuk melepas {formatIDR(room.itemPrice)} ke penjual.
            </p>
            <Button variant="primary" onclick={() => triggerAction('confirm')}>
              Saya Sudah Terima
            </Button>
          </div>
        {/if}

        <!-- Chat shortcut -->
        <a
          href="/chat?room={room.chatRoomId}"
          class="flex items-center justify-between bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] p-5 hover:shadow-[var(--shadow-card)] transition-shadow duration-150"
        >
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-[var(--radius-md)] bg-[var(--color-primary)]/10 flex items-center justify-center">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 256 256" fill="var(--color-primary)"><path d="M116,128a12,12,0,1,1,12,12A12,12,0,0,1,116,128Zm-44,0a12,12,0,1,0,12-12A12,12,0,0,0,72,128Zm104,0a12,12,0,1,0-12,12A12,12,0,0,0,176,128Zm52,0A100,100,0,1,1,128,28,100.11,100.11,0,0,1,228,128Zm-16,0a84,84,0,1,0-84,84A84.09,84.09,0,0,0,212,128Z"/></svg>
            </div>
            <div>
              <p class="font-sans font-medium text-[var(--text-body-sm)] text-[var(--color-ink)]">Chat dengan pihak lain</p>
              <p class="font-sans text-[var(--text-caption)] text-[var(--color-muted)]">Bicarakan detail transaksi</p>
            </div>
          </div>
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 256 256" fill="var(--color-muted-soft)"><path d="M221.66,133.66l-72,72a8,8,0,0,1-11.32-11.32L196.69,136H40a8,8,0,0,1,0-16H196.69L138.34,61.66a8,8,0,0,1,11.32-11.32l72,72A8,8,0,0,1,221.66,133.66Z"/></svg>
        </a>
      </div>

      <!-- Right: timeline -->
      <div class="flex flex-col gap-5">
        <div class="bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] p-6">
          <h2 class="font-sans font-medium text-[var(--text-title-sm)] text-[var(--color-ink)] mb-5">
            Timeline
          </h2>
          <ol class="relative border-l-2 border-[var(--color-hairline-soft)] space-y-5 pl-5">
            {#each room.timeline as event, i}
              <li class="relative">
                <div
                  class="absolute -left-[1.45rem] w-3 h-3 rounded-full {i === 0 ? 'bg-[var(--color-primary)]' : 'bg-[var(--color-hairline)]'} border-2 border-[var(--color-canvas)]"
                ></div>
                <p class="font-sans font-medium text-[var(--text-body-sm)] text-[var(--color-ink)]">
                  {statusTimelineLabels[event.status] ?? event.status}
                </p>
                <p class="font-sans text-[var(--text-caption)] text-[var(--color-muted-soft)] mt-0.5">
                  {event.actor} · {formatDate(event.timestamp)}
                </p>
              </li>
            {/each}
          </ol>
        </div>

        <!-- Room info -->
        <div class="bg-[var(--color-surface-soft)] rounded-[var(--radius-xl)] p-5">
          <div class="space-y-2">
            <div>
              <p class="font-sans text-[var(--text-caption)] text-[var(--color-muted)]">Dibuat</p>
              <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-ink)] mt-0.5">
                {formatDate(room.createdAt)}
              </p>
            </div>
            {#if room.autoReleaseAt && (room.status === 'DELIVERED')}
              <div class="pt-2 border-t border-[var(--color-hairline-soft)]">
                <p class="font-sans text-[var(--text-caption)] text-[var(--color-warning)]">Auto-release</p>
                <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-ink)] mt-0.5">
                  {formatDate(room.autoReleaseAt)}
                </p>
              </div>
            {/if}
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- PIN modal -->
  {#if showPinModal}
    <div
      class="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-4"
      role="dialog"
      aria-modal="true"
      aria-label="Verifikasi PIN"
    >
      <div
        class="absolute inset-0 bg-[var(--color-ink)]/40"
        onclick={() => (showPinModal = false)}
        role="presentation"
      ></div>
      <div
        class="relative bg-[var(--color-canvas)] rounded-[var(--radius-xl)] p-8 w-full max-w-sm text-center shadow-[var(--shadow-elevated)]"
        style="animation: modal-in 200ms cubic-bezier(0.16,1,0.3,1) both;"
      >
        <h2 class="font-display text-[var(--text-display-sm)] text-[var(--color-ink)] mb-2">
          Masukkan PIN
        </h2>
        <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)] mb-6">
          {pendingAction === 'fund' ? 'Konfirmasi penguncian dana' : 'Konfirmasi penerimaan barang'}
        </p>
        <PinInput bind:pin={pinValues} error={pinError} />
        <div class="mt-6 flex gap-3">
          <Button variant="secondary" class="flex-1" onclick={() => (showPinModal = false)}>
            Batal
          </Button>
          <Button variant="primary" class="flex-1" loading={actionLoading} onclick={executeWithPin}>
            Konfirmasi
          </Button>
        </div>
      </div>
    </div>
  {/if}
{/if}

<style>
  @keyframes modal-in {
    from { opacity: 0; transform: scale(0.95); }
    to   { opacity: 1; transform: scale(1); }
  }
</style>
