<script lang="ts">
  import Input from '$lib/components/ui/Input.svelte';
  import Button from '$lib/components/ui/Button.svelte';
  import Badge from '$lib/components/ui/Badge.svelte';
  import { walletApi } from '$lib/api';
  import { formatIDR } from '$lib/utils/format';
  import { addNotification } from '$lib/stores/notifications';
  import type { TopUpOrder } from '$lib/types';

  let amount = $state('');
  let method = $state<'VA' | 'QRIS' | 'EWALLET'>('VA');
  let bankCode = $state('BCA');
  let loading = $state(false);
  let error = $state('');
  let activeOrder = $state<TopUpOrder | null>(null);
  let copied = $state(false);

  const presets = [50000, 100000, 250000, 500000, 1000000, 2500000];

  const banks = [
    { code: 'BCA', name: 'Bank Central Asia', short: 'BCA' },
    { code: 'MANDIRI', name: 'Bank Mandiri', short: 'MANDIRI' },
    { code: 'BNI', name: 'Bank Negara Indonesia', short: 'BNI' },
    { code: 'BRI', name: 'Bank Rakyat Indonesia', short: 'BRI' },
    { code: 'PERMATA', name: 'Bank Permata', short: 'PERMATA' }
  ];

  async function handleTopUp(e: Event) {
    e.preventDefault();
    error = '';
    const num = Number(amount.replace(/\D/g, ''));
    if (!num || num < 10000) {
      error = 'Nominal minimal pengisian saldo adalah Rp 10.000';
      return;
    }

    loading = true;
    try {
      const res = (await walletApi.topUp({
        amount: num,
        method,
        bankCode: method === 'VA' ? bankCode : undefined
      })) as any;
      const order = res?.data || res;
      const orderIdStr = order.id || order.orderId || 'ORD-' + Math.floor(Math.random() * 100000);
      activeOrder = {
        id: orderIdStr,
        orderId: orderIdStr,
        amount: order.amount || num,
        method: order.paymentMethod || order.method || method,
        bankCode: order.bankCode || (method === 'VA' ? bankCode : undefined),
        status: order.status || 'PENDING',
        virtualAccountNumber:
          order.virtualAccountNumber ||
          '8801' + (order.id ? String(order.id).replace(/\D/g, '').padEnd(10, '0').slice(0, 10) : '2938471203'),
        invoiceUrl: order.invoiceUrl || order.paymentDetails?.invoice_url,
        expiryTime: '24 Jam'
      };
      addNotification({
        type: 'success',
        title: 'Instruksi Pembayaran Diterbitkan',
        message: `Nomor order #${orderIdStr.slice(0, 8)} berhasil dibuat.`
      });
    } catch (err: unknown) {
      const apiErr = err as { message?: string };
      error = apiErr?.message ?? 'Gagal membuat invoice top up';
    } finally {
      loading = false;
    }
  }

  let webhookLoading = $state(false);
  let webhookSuccess = $state(false);

  async function simulateWebhookPayment() {
    if (!activeOrder) return;
    const orderIdentifier = activeOrder.orderId || activeOrder.id || '';
    if (!orderIdentifier) return;
    webhookLoading = true;
    try {
      const res = await fetch('/api/v1/wallet/webhook/xendit', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'x-callback-token': 'xnd_webhook_dummy'
        },
        body: JSON.stringify({
          event: 'invoice.paid',
          id: 'inv_sim_' + Math.random().toString(36).substring(2, 8),
          external_id: `TopUpOrder-${orderIdentifier}`,
          status: 'PAID',
          amount: activeOrder.amount,
          paid_amount: activeOrder.amount,
          payment_method: activeOrder.method === 'VA' ? 'VIRTUAL_ACCOUNT' : activeOrder.method,
          payment_channel: activeOrder.bankCode || 'BCA',
          paid_at: new Date().toISOString()
        })
      });
      const data = await res.json();
      if (res.ok) {
        webhookSuccess = true;
        addNotification({
          type: 'success',
          title: 'Konfirmasi Dana Masuk',
          message: `Saldo ${formatIDR(activeOrder.amount)} telah berhasil dikreditkan ke dompet Anda.`
        });
      } else {
        addNotification({
          type: 'error',
          title: 'Verifikasi Gagal',
          message: data?.error?.message || 'Gagal memproses konfirmasi webhook'
        });
      }
    } catch (e: any) {
      addNotification({
        type: 'error',
        title: 'Kendala Koneksi',
        message: e.message || 'Tidak dapat menghubungi server gateway'
      });
    } finally {
      webhookLoading = false;
    }
  }

  function copyVA() {
    if (!activeOrder?.virtualAccountNumber) return;
    navigator.clipboard.writeText(activeOrder.virtualAccountNumber);
    copied = true;
    addNotification({ type: 'info', title: 'Tersalin', message: 'Nomor Virtual Account disalin ke papan klip' });
    setTimeout(() => {
      copied = false;
    }, 2000);
  }
</script>

<svelte:head>
  <title>Top Up Saldo - EyesOfPriestess</title>
</svelte:head>

<div class="max-w-2xl mx-auto pb-20 lg:pb-12">
  <!-- Navigation Header -->
  <div class="mb-8">
    <a
      href="/wallet"
      class="inline-flex items-center gap-2 font-sans text-[13px] text-[var(--color-muted)] hover:text-[var(--color-primary)] transition-colors mb-3"
    >
      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 256 256" fill="currentColor"><path d="M224,128a8,8,0,0,1-8,8H59.31l58.35,58.34a8,8,0,0,1-11.32,11.32l-72-72a8,8,0,0,1,0-11.32l72-72a8,8,0,0,1,11.32,11.32L59.31,120H216A8,8,0,0,1,224,128Z"/></svg>
      Kembali ke Dompet
    </a>
    <h1 class="font-display text-[var(--text-display-md)] text-[var(--color-ink)] tracking-[-0.015em]">
      Top Up Saldo
    </h1>
    <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)] mt-1.5 leading-relaxed">
      Pengisian saldo instan melalui gerbang pembayaran Xendit dengan jaminan keamanan transaksi protokol escrow.
    </p>
  </div>

  {#if activeOrder}
    <!-- Active Payment Order State -->
    <div class="space-y-6">
      <!-- Main Payment Slip Card -->
      <div class="bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] p-6 sm:p-8 border border-[var(--color-hairline-soft)] shadow-sm space-y-6">
        <!-- Status Header -->
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-5 border-b border-[var(--color-hairline-soft)]">
          <div>
            <span class="font-mono text-[11px] uppercase tracking-wider text-[var(--color-muted)]">Instruksi Pembayaran</span>
            <p class="font-sans font-semibold text-[16px] text-[var(--color-ink)] mt-0.5">
              {activeOrder.method === 'VA' ? `Virtual Account ${activeOrder.bankCode}` : activeOrder.method}
            </p>
          </div>
          <div>
            {#if webhookSuccess}
              <Badge variant="success">Pembayaran Lunas (PAID)</Badge>
            {:else}
              <Badge variant="amber">Menunggu Pembayaran</Badge>
            {/if}
          </div>
        </div>

        {#if !webhookSuccess}
          <!-- Virtual Account Display Box -->
          <div class="text-center py-6 px-4 bg-[var(--color-canvas)] rounded-[var(--radius-lg)] border border-[var(--color-hairline-soft)] flex flex-col items-center">
            <span class="font-sans text-[12px] uppercase tracking-wider text-[var(--color-muted)]">
              Nomor Rekening Virtual Account
            </span>
            <span class="font-mono text-[22px] sm:text-[28px] font-bold text-[var(--color-primary)] tracking-widest my-2 break-all">
              {activeOrder.virtualAccountNumber}
            </span>
            <button
              type="button"
              onclick={copyVA}
              class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-[var(--radius-md)] bg-[var(--color-surface-card)] border border-[var(--color-hairline)] hover:border-[var(--color-primary)] text-[var(--color-ink)] font-sans text-[12px] font-medium transition-colors"
            >
              {#if copied}
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 256 256" fill="var(--color-success)"><path d="M229.66,77.66l-128,128a8,8,0,0,1-11.32,0l-56-56a8,8,0,0,1,11.32-11.32L96,188.69,218.34,66.34a8,8,0,0,1,11.32,11.32Z"/></svg>
                Tersalin
              {:else}
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 256 256" fill="currentColor"><path d="M216,32H88a8,8,0,0,0-8,8V80H40a8,8,0,0,0-8,8V216a8,8,0,0,0,8,8H168a8,8,0,0,0,8-8V176h40a8,8,0,0,0,8-8V40A8,8,0,0,0,216,32ZM160,208H48V96H160Zm48-48H176V88a8,8,0,0,0-8-8H96V48H208Z"/></svg>
                Salin Nomor VA
              {/if}
            </button>
          </div>
        {:else}
          <!-- Success Settlement Banner -->
          <div class="py-6 px-6 bg-[var(--color-success)]/10 rounded-[var(--radius-lg)] border border-[var(--color-success)]/20 text-center flex flex-col items-center gap-2">
            <div class="w-12 h-12 rounded-full bg-[var(--color-success)]/20 text-[var(--color-success)] flex items-center justify-center">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 256 256" fill="currentColor"><path d="M229.66,77.66l-128,128a8,8,0,0,1-11.32,0l-56-56a8,8,0,0,1,11.32-11.32L96,188.69,218.34,66.34a8,8,0,0,1,11.32,11.32Z"/></svg>
            </div>
            <p class="font-sans text-[15px] font-bold text-[var(--color-ink)]">
              Pembayaran Berhasil Dikonfirmasi
            </p>
            <p class="font-sans text-[13px] text-[var(--color-muted)] max-w-md">
              Sistem telah memverifikasi mutasi dana masuk secara otomatis. Saldo Anda telah diperbarui dan siap digunakan untuk bertransaksi.
            </p>
          </div>
        {/if}

        <!-- Invoice Details Table -->
        <div class="space-y-3 pt-2 text-[14px]">
          <div class="flex justify-between items-center py-1 border-b border-[var(--color-hairline-soft)]">
            <span class="text-[var(--color-muted)]">Total Pembayaran</span>
            <span class="font-display font-semibold text-[18px] text-[var(--color-ink)]">
              {formatIDR(activeOrder.amount)}
            </span>
          </div>
          <div class="flex justify-between items-center py-1 border-b border-[var(--color-hairline-soft)]">
            <span class="text-[var(--color-muted)]">Nomor Referensi Order</span>
            <span class="font-mono text-[12px] text-[var(--color-ink)] font-medium">
              {activeOrder.orderId}
            </span>
          </div>
          <div class="flex justify-between items-center py-1 border-b border-[var(--color-hairline-soft)]">
            <span class="text-[var(--color-muted)]">Batas Waktu Pelunasan</span>
            <span class="font-sans text-[13px] text-[var(--color-muted)]">
              24 Jam dari waktu pemesanan
            </span>
          </div>
          <div class="flex justify-between items-center py-1">
            <span class="text-[var(--color-muted)]">Status Rekonsiliasi</span>
            <span class="font-sans font-medium text-[13px] {webhookSuccess ? 'text-[var(--color-success)]' : 'text-[var(--color-warning)]'}">
              {webhookSuccess ? 'SETTLED / SELESAI' : 'PENDING PAYMENT'}
            </span>
          </div>
        </div>

        <!-- Sandbox Simulation & Actions -->
        <div class="space-y-3 pt-4">
          {#if !webhookSuccess}
            <div class="p-4 rounded-[var(--radius-lg)] bg-[var(--color-surface-soft)] border border-[var(--color-hairline-soft)] space-y-2">
              <div class="flex items-center gap-2">
                <span class="inline-block px-1.5 py-0.5 rounded text-[10px] font-mono font-semibold bg-[var(--color-primary)] text-white">
                  SANDBOX TOOL
                </span>
                <span class="font-sans text-[12px] font-medium text-[var(--color-ink)]">
                  Simulasi Pembayaran Xendit
                </span>
              </div>
              <p class="font-sans text-[12px] text-[var(--color-muted)] leading-relaxed">
                Di lingkungan pengujian ini, Anda dapat memicu callback webhook Xendit untuk memverifikasi otomatisasi penambahan saldo tanpa transfer bank riil.
              </p>
              <button
                type="button"
                onclick={simulateWebhookPayment}
                disabled={webhookLoading}
                class="w-full mt-2 py-2.5 px-4 rounded-[var(--radius-md)] bg-[var(--color-ink)] hover:bg-[var(--color-ink)]/90 text-white font-sans text-[13px] font-medium flex items-center justify-center gap-2 transition-colors disabled:opacity-50"
              >
                {#if webhookLoading}
                  <span class="animate-pulse">Mengirim Verifikasi Callback...</span>
                {:else}
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 256 256" fill="currentColor"><path d="M240,128a8,8,0,0,1-8,8H72.49l51.75,51.76a8,8,0,1,1-11.31,11.31l-65.5-65.5a8,8,0,0,1,0-11.31l65.5-65.5a8,8,0,0,1,11.31,11.31L72.49,120H232A8,8,0,0,1,240,128Z" transform="scale(-1, 1) translate(-256, 0)"/></svg>
                  Simulasikan Pelunasan Xendit
                {/if}
              </button>
            </div>

            {#if activeOrder.invoiceUrl}
              <a
                href={activeOrder.invoiceUrl}
                target="_blank"
                rel="noreferrer"
                class="inline-flex items-center justify-center gap-2 w-full py-2.5 px-4 rounded-[var(--radius-md)] border border-[var(--color-hairline)] text-center font-sans text-[13px] text-[var(--color-ink)] hover:bg-[var(--color-canvas)] transition-colors"
              >
                Buka Halaman Checkout Xendit
                <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 256 256" fill="currentColor"><path d="M200,64V168a8,8,0,0,1-16,0V83.31L69.66,197.66a8,8,0,0,1-11.32-11.32L172.69,72H88a8,8,0,0,1,0-16H192A8,8,0,0,1,200,64Z"/></svg>
              </a>
            {/if}
          {:else}
            <a
              href="/wallet"
              class="inline-flex items-center justify-center gap-2 w-full py-3 px-4 rounded-[var(--radius-md)] bg-[var(--color-primary)] text-white font-sans text-[13px] font-medium hover:bg-[var(--color-primary-active)] transition-colors"
            >
              Lihat Saldo Dompet Terbaru
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 256 256" fill="currentColor"><path d="M224,128a8,8,0,0,1-8,8H59.31l58.35,58.34a8,8,0,0,1-11.32,11.32l-72-72a8,8,0,0,1,0-11.32l72-72a8,8,0,0,1,11.32,11.32L59.31,120H216A8,8,0,0,1,224,128Z" transform="scale(-1, 1) translate(-256, 0)"/></svg>
            </a>
          {/if}

          <button
            type="button"
            onclick={() => { activeOrder = null; webhookSuccess = false; }}
            class="w-full py-2.5 px-4 rounded-[var(--radius-md)] border border-[var(--color-hairline-soft)] text-center font-sans text-[13px] text-[var(--color-muted)] hover:text-[var(--color-ink)] hover:bg-[var(--color-surface-soft)] transition-colors"
          >
            Buat Transaksi Top Up Baru
          </button>
        </div>
      </div>
    </div>
  {:else}
    <!-- Top-Up Form -->
    <form onsubmit={handleTopUp} class="space-y-6">
      <!-- Payment Method Selection -->
      <div class="bg-[var(--color-surface-card)] p-6 rounded-[var(--radius-xl)] border border-[var(--color-hairline-soft)] shadow-sm space-y-4">
        <div>
          <span class="block font-sans text-[13px] font-medium text-[var(--color-ink)]">
            Metode Pembayaran
          </span>
          <p class="font-sans text-[12px] text-[var(--color-muted)] mt-0.5">
            Pilih kanal pengiriman dana yang Anda gunakan
          </p>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-3 gap-3">
          {#each [
            { id: 'VA', title: 'Virtual Account', desc: 'Transfer Bank Otomatis' },
            { id: 'QRIS', title: 'QRIS', desc: 'Scan Semua E-Wallet & Bank' },
            { id: 'EWALLET', title: 'E-Wallet', desc: 'OVO, GoPay, DANA' }
          ] as m}
            <button
              type="button"
              onclick={() => (method = m.id as 'VA' | 'QRIS' | 'EWALLET')}
              class="p-4 rounded-[var(--radius-lg)] border text-left transition-all duration-150 flex flex-col justify-between gap-2 {method === m.id ? 'border-[var(--color-primary)] bg-[var(--color-primary)]/5 ring-1 ring-[var(--color-primary)]/30' : 'border-[var(--color-hairline)] bg-[var(--color-canvas)] hover:border-[var(--color-muted-soft)]'}"
            >
              <div class="flex items-center justify-between">
                <span class="font-sans font-semibold text-[13px] text-[var(--color-ink)]">
                  {m.title}
                </span>
                <span class="w-3.5 h-3.5 rounded-full border flex items-center justify-center {method === m.id ? 'border-[var(--color-primary)] bg-[var(--color-primary)]' : 'border-[var(--color-muted)]/40'}">
                  {#if method === m.id}
                    <span class="w-1.5 h-1.5 rounded-full bg-white"></span>
                  {/if}
                </span>
              </div>
              <span class="font-sans text-[11px] text-[var(--color-muted)] leading-tight">
                {m.desc}
              </span>
            </button>
          {/each}
        </div>

        {#if method === 'VA'}
          <!-- Bank Selector -->
          <div class="pt-4 border-t border-[var(--color-hairline-soft)] space-y-3">
            <span class="block font-sans text-[12px] font-medium text-[var(--color-muted)]">
              Pilih Bank Penerbit Virtual Account
            </span>
            <div class="grid grid-cols-2 sm:grid-cols-3 gap-2">
              {#each banks as b}
                <button
                  type="button"
                  onclick={() => (bankCode = b.code)}
                  class="p-3 rounded-[var(--radius-md)] border text-left transition-colors flex items-center justify-between gap-2 {bankCode === b.code ? 'border-[var(--color-primary)] bg-[var(--color-primary)]/10 font-semibold text-[var(--color-ink)]' : 'border-[var(--color-hairline)] bg-[var(--color-canvas)] text-[var(--color-muted)] hover:border-[var(--color-muted-soft)]'}"
                >
                  <span class="font-sans text-[13px] truncate">{b.short}</span>
                  {#if bankCode === b.code}
                    <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 256 256" fill="var(--color-primary)"><path d="M229.66,77.66l-128,128a8,8,0,0,1-11.32,0l-56-56a8,8,0,0,1,11.32-11.32L96,188.69,218.34,66.34a8,8,0,0,1,11.32,11.32Z"/></svg>
                  {/if}
                </button>
              {/each}
            </div>
          </div>
        {/if}
      </div>

      <!-- Amount Input Card -->
      <div class="bg-[var(--color-surface-card)] p-6 rounded-[var(--radius-xl)] border border-[var(--color-hairline-soft)] shadow-sm space-y-4">
        <div>
          <label for="topup-amount" class="block font-sans text-[13px] font-medium text-[var(--color-ink)] mb-1">
            Nominal Pengisian (IDR)
          </label>
          <Input
            label=""
            type="number"
            bind:value={amount}
            placeholder="100000"
            error={error}
            required
            id="topup-amount"
          />
          <p class="font-sans text-[11px] text-[var(--color-muted)] mt-1">
            Minimal pengisian saldo adalah Rp 10.000
          </p>
        </div>

        <!-- Quick Amount Presets -->
        <div>
          <span class="block font-sans text-[12px] text-[var(--color-muted)] mb-2">
            Pilihan Nominal Cepat
          </span>
          <div class="flex flex-wrap gap-2">
            {#each presets as p}
              <button
                type="button"
                onclick={() => (amount = String(p))}
                class="px-3.5 py-1.5 rounded-[var(--radius-md)] border font-sans text-[12px] font-medium transition-colors {Number(amount) === p ? 'border-[var(--color-primary)] bg-[var(--color-primary)] text-white' : 'border-[var(--color-hairline)] bg-[var(--color-canvas)] text-[var(--color-ink)] hover:border-[var(--color-primary)]'}"
              >
                {formatIDR(p)}
              </button>
            {/each}
          </div>
        </div>
      </div>

      <!-- Submit Action -->
      <div class="space-y-3">
        <Button variant="primary" type="submit" size="lg" {loading} class="w-full">
          Lanjutkan Pembayaran
        </Button>

        <!-- Trust & Security Seal -->
        <div class="flex items-center justify-center gap-2 text-center text-[var(--color-muted)] text-[12px] font-sans pt-1">
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 256 256" fill="currentColor"><path d="M208,40H48A16,16,0,0,0,32,56V120c0,88,88,128,88,128s88-40,88-128V56A16,16,0,0,0,208,40Zm-80,188c-24.31-13.62-64-44.59-64-108V56H128Z"/></svg>
          <span>Transaksi Terproteksi Enkripsi TLS 1.3 & Gateway Xendit</span>
        </div>
      </div>
    </form>
  {/if}
</div>
