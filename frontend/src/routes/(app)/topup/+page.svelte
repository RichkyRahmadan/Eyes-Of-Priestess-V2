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

  const presets = [50000, 100000, 250000, 500000, 1000000];

  const banks = [
    { code: 'BCA', name: 'Bank BCA' },
    { code: 'MANDIRI', name: 'Bank Mandiri' },
    { code: 'BNI', name: 'Bank BNI' },
    { code: 'BRI', name: 'Bank BRI' },
    { code: 'PERMATA', name: 'Bank Permata' }
  ];

  async function handleTopUp(e: Event) {
    e.preventDefault();
    error = '';
    const num = Number(amount.replace(/\D/g, ''));
    if (!num || num < 10000) {
      error = 'Nominal minimal Top Up adalah Rp 10.000';
      return;
    }

    loading = true;
    try {
      const res = await walletApi.topUp({
        amount: num,
        method,
        bankCode: method === 'VA' ? bankCode : undefined
      }) as any;
      const order = res?.data || res;
      activeOrder = {
        id: order.id || order.orderId || 'ORD-' + Math.floor(Math.random() * 100000),
        orderId: order.id || order.orderId || 'ORD-' + Math.floor(Math.random() * 100000),
        amount: order.amount || num,
        method: order.paymentMethod || order.method || method,
        bankCode: order.bankCode || (method === 'VA' ? bankCode : undefined),
        status: order.status || 'PENDING',
        virtualAccountNumber: order.virtualAccountNumber || ('8801' + (order.id ? String(order.id).replace(/\D/g, '').padEnd(10, '0').slice(0, 10) : '2938471203')),
        invoiceUrl: order.invoiceUrl || order.paymentDetails?.invoice_url,
        expiryTime: '24 Jam'
      };
      addNotification({ type: 'success', title: 'Instruksi Pembayaran Dibuat', message: `Order #${activeOrder.orderId.slice(0, 8)}` });
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
    if (!activeOrder?.id) return;
    webhookLoading = true;
    try {
      const res = await fetch('http://localhost:8000/api/v1/wallet/webhook/xendit', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'x-callback-token': 'aK5Aa4QpBB7U9zbmDvF1Dm888Vpkb7A11Du4TLLL4S60Y98L'
        },
        body: JSON.stringify({
          event: 'invoice.paid',
          id: 'inv_sim_' + Math.random().toString(36).substring(2, 8),
          external_id: `TopUpOrder-${activeOrder.id}`,
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
          title: 'Webhook Xendit Terverifikasi',
          message: `Saldo ${formatIDR(activeOrder.amount)} berhasil masuk ke dompet!`
        });
      } else {
        addNotification({
          type: 'error',
          title: 'Webhook Gagal',
          message: data?.error?.message || 'Gagal memproses webhook'
        });
      }
    } catch (e: any) {
      addNotification({
        type: 'error',
        title: 'Koneksi Gagal',
        message: e.message || 'Tidak dapat menghubungi webhook'
      });
    } finally {
      webhookLoading = false;
    }
  }
</script>

<svelte:head>
  <title>Top Up Saldo - EyesOfPriestess</title>
</svelte:head>

<div class="max-w-xl mx-auto pb-20 lg:pb-8">
  <div class="mb-6">
    <a href="/wallet" class="font-sans text-[13px] text-[var(--color-muted)] hover:text-[var(--color-primary)] transition-colors">
      &larr; Kembali ke Dompet
    </a>
    <h1 class="font-display text-[var(--text-display-md)] text-[var(--color-ink)] tracking-[-0.015em] mt-1">
      Top Up Saldo
    </h1>
    <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)] mt-1">
      Isi saldo dompet EyesOfPriestess Anda secara instan melalui Xendit Payment Gateway.
    </p>
  </div>

  {#if activeOrder}
    <!-- Active Payment Order details -->
    <div class="bg-[var(--color-surface-card)] p-6 rounded-[var(--radius-xl)] space-y-6 border border-[var(--color-hairline)] shadow-sm">
      <div class="flex justify-between items-start border-b border-[var(--color-hairline-soft)] pb-4">
        <div>
          <p class="font-sans text-[12px] text-[var(--color-muted)] uppercase tracking-wider">Metode Pembayaran</p>
          <p class="font-sans font-semibold text-[16px] text-[var(--color-ink)] mt-0.5">
            {activeOrder.method === 'VA' ? `Virtual Account ${activeOrder.bankCode}` : activeOrder.method}
          </p>
        </div>
        {#if webhookSuccess}
          <Badge variant="emerald">Pembayaran Lunas (PAID)</Badge>
        {:else}
          <Badge variant="amber">Menunggu Pembayaran</Badge>
        {/if}
      </div>

      {#if !webhookSuccess}
        <div class="text-center py-4 px-2 bg-[var(--color-surface-soft)] rounded-[var(--radius-lg)] border border-[var(--color-hairline-soft)]">
          <p class="font-sans text-[13px] text-[var(--color-muted)]">Nomor Virtual Account</p>
          <p class="font-mono text-[18px] sm:text-[24px] font-bold text-[var(--color-primary)] tracking-widest my-1 break-all">
            {activeOrder.virtualAccountNumber}
          </p>
          <button
            type="button"
            onclick={() => {
              navigator.clipboard.writeText(activeOrder?.virtualAccountNumber ?? '');
              addNotification({ type: 'info', title: 'Tersalin', message: 'Nomor VA berhasil disalin' });
            }}
            class="font-sans text-[12px] text-[var(--color-primary)] hover:underline font-medium"
          >
            Salin Nomor VA
          </button>
        </div>
      {:else}
        <div class="text-center py-6 px-4 bg-emerald-500/10 rounded-[var(--radius-lg)] border border-emerald-500/20 text-emerald-800">
          <p class="font-sans text-[14px] font-bold">Pembayaran Berhasil Diterima!</p>
          <p class="font-sans text-[13px] opacity-80 mt-1">
            Xendit Webhook telah memproses konfirmasi transfer dana secara otomatis.
          </p>
        </div>
      {/if}

      <div class="space-y-2 text-[14px]">
        <div class="flex justify-between">
          <span class="text-[var(--color-muted)]">Nominal Top Up</span>
          <span class="font-display font-semibold text-[var(--color-ink)]">{formatIDR(activeOrder.amount)}</span>
        </div>
        <div class="flex justify-between">
          <span class="text-[var(--color-muted)]">ID Transaksi</span>
          <span class="font-mono text-[12px] text-[var(--color-muted)]">{activeOrder.id}</span>
        </div>
        <div class="flex justify-between">
          <span class="text-[var(--color-muted)]">Status Xendit</span>
          <span class="font-sans font-medium {webhookSuccess ? 'text-emerald-600' : 'text-amber-600'}">
            {webhookSuccess ? 'SETTLED' : 'PENDING'}
          </span>
        </div>
      </div>

      <!-- Action Buttons -->
      <div class="space-y-3 pt-2">
        {#if !webhookSuccess}
          <button
            type="button"
            onclick={simulateWebhookPayment}
            disabled={webhookLoading}
            class="w-full py-3 px-4 rounded-[var(--radius-md)] bg-emerald-600 hover:bg-emerald-700 text-white font-sans text-[13px] font-semibold flex items-center justify-center gap-2 transition-colors disabled:opacity-50"
          >
            {#if webhookLoading}
              <span>Memproses Webhook Xendit...</span>
            {:else}
              <span>⚡ Simulasikan Pelunasan Xendit (Trigger Webhook)</span>
            {/if}
          </button>

          {#if activeOrder.invoiceUrl}
            <a
              href={activeOrder.invoiceUrl}
              target="_blank"
              rel="noreferrer"
              class="block w-full py-2.5 px-4 rounded-[var(--radius-md)] border border-[var(--color-hairline)] text-center font-sans text-[13px] text-[var(--color-ink)] hover:bg-[var(--color-canvas)] transition-colors"
            >
              Lihat Invoice Checkout Xendit ↗
            </a>
          {/if}
        {:else}
          <a
            href="/wallet"
            class="block w-full py-3 px-4 rounded-[var(--radius-md)] bg-[var(--color-primary)] text-white text-center font-sans text-[13px] font-semibold hover:opacity-90 transition-opacity"
          >
            Lihat Saldo Dompet Terbaru &rarr;
          </a>
        {/if}

        <Button variant="secondary" class="w-full" onclick={() => { activeOrder = null; webhookSuccess = false; }}>
          Buat Transaksi Baru
        </Button>
      </div>
    </div>
  {:else}
    <form onsubmit={handleTopUp} class="flex flex-col gap-6">
      <!-- Method selector -->
      <div class="bg-[var(--color-surface-card)] p-4 sm:p-5 rounded-[var(--radius-xl)]">
        <p class="font-sans text-[13px] font-medium text-[var(--color-ink)] mb-3">
          Pilih Metode Pembayaran
        </p>
        <div class="grid grid-cols-3 gap-1.5 sm:gap-2">
          {#each [
            { id: 'VA', label: 'Virtual Account' },
            { id: 'QRIS', label: 'QRIS' },
            { id: 'EWALLET', label: 'E-Wallet' }
          ] as m}
            <button
              type="button"
              onclick={() => (method = m.id as 'VA' | 'QRIS' | 'EWALLET')}
              class="py-2.5 sm:py-3 px-1.5 sm:px-2 rounded-[var(--radius-md)] border text-center transition-all duration-150 font-sans text-[11px] sm:text-[13px] font-medium leading-tight {method === m.id ? 'border-[var(--color-primary)] bg-[var(--color-primary)]/5 text-[var(--color-ink)]' : 'border-[var(--color-hairline)] bg-[var(--color-canvas)] text-[var(--color-muted)]'}"
            >
              {m.label}
            </button>
          {/each}
        </div>

        {#if method === 'VA'}
          <div class="mt-4 pt-4 border-t border-[var(--color-hairline-soft)]">
            <p class="font-sans text-[12px] text-[var(--color-muted)] mb-2">Pilih Bank</p>
            <div class="grid grid-cols-2 sm:grid-cols-3 gap-1.5 sm:gap-2">
              {#each banks as b}
                <button
                  type="button"
                  onclick={() => (bankCode = b.code)}
                  class="py-2 px-2.5 sm:px-3 rounded-[var(--radius-sm)] border text-left text-[12px] sm:text-[13px] font-sans truncate transition-colors {bankCode === b.code ? 'border-[var(--color-primary)] text-[var(--color-ink)] font-medium bg-[var(--color-surface-cream-strong)]' : 'border-[var(--color-hairline)] text-[var(--color-muted)]'}"
                >
                  {b.name}
                </button>
              {/each}
            </div>
          </div>
        {/if}
      </div>

      <!-- Amount input -->
      <div class="bg-[var(--color-surface-card)] p-6 rounded-[var(--radius-xl)] space-y-4">
        <Input
          label="Nominal Top Up (Rp)"
          type="number"
          bind:value={amount}
          placeholder="100000"
          error={error}
          required
          id="topup-amount"
        />

        <div>
          <p class="font-sans text-[12px] text-[var(--color-muted)] mb-2">Pilihan Cepat</p>
          <div class="flex flex-wrap gap-2">
            {#each presets as p}
              <button
                type="button"
                onclick={() => (amount = String(p))}
                class="px-3 py-1.5 rounded-[var(--radius-md)] bg-[var(--color-canvas)] border border-[var(--color-hairline)] font-sans text-[13px] text-[var(--color-ink)] hover:border-[var(--color-primary)] transition-colors"
              >
                {formatIDR(p)}
              </button>
            {/each}
          </div>
        </div>
      </div>

      <Button variant="primary" type="submit" size="lg" {loading} class="w-full">
        Bayar Sekarang
      </Button>
    </form>
  {/if}
</div>
