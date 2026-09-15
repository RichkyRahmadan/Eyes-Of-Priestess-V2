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
      const order = await walletApi.topUp({
        amount: num,
        method,
        bankCode: method === 'VA' ? bankCode : undefined
      }) as TopUpOrder;
      activeOrder = order;
      addNotification({ type: 'success', title: 'Instruksi Pembayaran Dibuat', message: `Order #${order.orderId}` });
    } catch (err: unknown) {
      const apiErr = err as { message?: string };
      error = apiErr?.message ?? 'Gagal membuat invoice top up';
    } finally {
      loading = false;
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
      Isi saldo dompet EyesOfPriestess Anda secara instan.
    </p>
  </div>

  {#if activeOrder}
    <!-- Active Payment Order details -->
    <div class="bg-[var(--color-surface-card)] p-6 rounded-[var(--radius-xl)] space-y-6">
      <div class="flex justify-between items-start border-b border-[var(--color-hairline-soft)] pb-4">
        <div>
          <p class="font-sans text-[12px] text-[var(--color-muted)] uppercase tracking-wider">Metode Pembayaran</p>
          <p class="font-sans font-semibold text-[16px] text-[var(--color-ink)] mt-0.5">
            {activeOrder.method === 'VA' ? `Virtual Account ${activeOrder.bankCode}` : activeOrder.method}
          </p>
        </div>
        <Badge variant="amber">Menunggu Pembayaran</Badge>
      </div>

      <div class="text-center py-4 bg-[var(--color-surface-soft)] rounded-[var(--radius-lg)]">
        <p class="font-sans text-[13px] text-[var(--color-muted)]">Nomor Virtual Account</p>
        <p class="font-mono text-[24px] font-bold text-[var(--color-primary)] tracking-widest my-1">
          {activeOrder.virtualAccountNumber ?? '88012938471203'}
        </p>
        <button
          onclick={() => {
            navigator.clipboard.writeText(activeOrder?.virtualAccountNumber ?? '88012938471203');
            addNotification({ type: 'info', title: 'Tersalin', message: 'Nomor VA berhasil disalin' });
          }}
          class="font-sans text-[12px] text-[var(--color-primary)] hover:underline font-medium"
        >
          Salin Nomor VA
        </button>
      </div>

      <div class="space-y-2 text-[14px]">
        <div class="flex justify-between">
          <span class="text-[var(--color-muted)]">Nominal Top Up</span>
          <span class="font-display font-semibold text-[var(--color-ink)]">{formatIDR(activeOrder.amount)}</span>
        </div>
        <div class="flex justify-between">
          <span class="text-[var(--color-muted)]">Batas Waktu Pembayaran</span>
          <span class="font-sans text-[var(--color-error)] font-medium">24 Jam</span>
        </div>
      </div>

      <Button variant="secondary" class="w-full" onclick={() => (activeOrder = null)}>
        Buat Transaksi Lain
      </Button>
    </div>
  {:else}
    <form onsubmit={handleTopUp} class="flex flex-col gap-6">
      <!-- Method selector -->
      <div class="bg-[var(--color-surface-card)] p-5 rounded-[var(--radius-xl)]">
        <label class="font-sans text-[13px] font-medium text-[var(--color-ink)] mb-3 block">
          Pilih Metode Pembayaran
        </label>
        <div class="grid grid-cols-3 gap-2">
          {#each [
            { id: 'VA', label: 'Virtual Account' },
            { id: 'QRIS', label: 'QRIS' },
            { id: 'EWALLET', label: 'E-Wallet' }
          ] as m}
            <button
              type="button"
              onclick={() => (method = m.id as 'VA' | 'QRIS' | 'EWALLET')}
              class="py-3 px-2 rounded-[var(--radius-md)] border text-center transition-all duration-150 font-sans text-[13px] font-medium {method === m.id ? 'border-[var(--color-primary)] bg-[var(--color-primary)]/5 text-[var(--color-ink)]' : 'border-[var(--color-hairline)] bg-[var(--color-canvas)] text-[var(--color-muted)]'}"
            >
              {m.label}
            </button>
          {/each}
        </div>

        {#if method === 'VA'}
          <div class="mt-4 pt-4 border-t border-[var(--color-hairline-soft)]">
            <label class="font-sans text-[12px] text-[var(--color-muted)] mb-2 block">Pilih Bank</label>
            <div class="grid grid-cols-2 sm:grid-cols-3 gap-2">
              {#each banks as b}
                <button
                  type="button"
                  onclick={() => (bankCode = b.code)}
                  class="py-2 px-3 rounded-[var(--radius-sm)] border text-left text-[13px] font-sans transition-colors {bankCode === b.code ? 'border-[var(--color-primary)] text-[var(--color-ink)] font-medium bg-[var(--color-surface-cream-strong)]' : 'border-[var(--color-hairline)] text-[var(--color-muted)]'}"
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
          <label class="font-sans text-[12px] text-[var(--color-muted)] mb-2 block">Pilihan Cepat</label>
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
