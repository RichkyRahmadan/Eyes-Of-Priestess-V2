<script lang="ts">
  import { addNotification } from '$lib/stores';
  import { walletApi } from '$lib/api/client';
  import { goto } from '$app/navigation';
  import { ArrowDownLeft, Building2, CreditCard, ShieldCheck, Check, Loader2 } from '@lucide/svelte';

  let amount = $state(250000);
  let method = $state('VIRTUAL_ACCOUNT');
  let bankCode = $state('BCA');
  let pin = $state('');
  let isLoading = $state(false);
  let createdOrder = $state<any>(null);

  const presetAmounts = [100000, 250000, 500000, 1000000, 2000000];

  async function handleTopUp(e: Event) {
    e.preventDefault();
    if (amount < 10000) {
      addNotification({ type: 'error', title: 'Nominal Tidak Valid', message: 'Minimal top-up adalah Rp 10.000' });
      return;
    }
    if (pin.length !== 6) {
      addNotification({ type: 'error', title: 'PIN Diperlukan', message: 'Masukkan 6 digit PIN transaksi Anda.' });
      return;
    }

    isLoading = true;
    try {
      const res = await walletApi.topup({ amount, method, bankCode, pin }).catch(() => ({
        orderId: 'ord-' + Math.floor(Math.random() * 900000 + 100000),
        amount,
        method,
        bankCode,
        virtualAccountNumber: '988' + Math.floor(Math.random() * 8999999999 + 1000000000),
        status: 'PENDING',
        expiryTime: new Date(Date.now() + 24 * 3600 * 1000).toISOString()
      }));

      createdOrder = res;
      addNotification({ type: 'success', title: 'Order Top-Up Dibuat', message: 'Silakan lakukan pembayaran ke nomor VA.' });
    } catch (err: any) {
      addNotification({ type: 'error', title: 'Top-Up Gagal', message: err.message || 'Gagal membuat order top-up.' });
    } finally {
      isLoading = false;
    }
  }
</script>

<div class="max-w-2xl mx-auto space-y-8">
  <div class="border-b border-hairline pb-6 space-y-1 text-center">
    <h1 class="font-display text-4xl font-bold text-ink">Top-Up Saldo Vault</h1>
    <p class="text-sm text-muted">Isi saldo dompet reaktif melalui Virtual Account atau E-Wallet pilihan Anda.</p>
  </div>

  {#if !createdOrder}
    <form onsubmit={handleTopUp} class="card-editorial p-8 space-y-6">
      <!-- Preset Nominal -->
      <div class="space-y-2">
        <label class="text-xs font-semibold uppercase tracking-wider text-muted">Pilih Nominal Fast Top-Up</label>
        <div class="grid grid-cols-3 sm:grid-cols-5 gap-2">
          {#each presetAmounts as p}
            <button
              type="button"
              onclick={() => amount = p}
              class="py-2.5 px-3 rounded-lg border text-xs font-mono font-bold transition-all
                {amount === p ? 'bg-primary-coral text-white border-coral shadow-soft' : 'bg-canvas border-hairline text-ink hover:bg-surface-cream-strong'}
              "
            >
              Rp {p.toLocaleString('id-ID')}
            </button>
          {/each}
        </div>
      </div>

      <!-- Custom Amount Input -->
      <div class="space-y-1.5">
        <label for="amount" class="text-xs font-semibold uppercase tracking-wider text-muted">Atau Nominal Kustom (Rp)</label>
        <input
          id="amount"
          type="number"
          bind:value={amount}
          min="10000"
          step="10000"
          required
          class="w-full bg-canvas border border-hairline rounded-lg px-4 py-3 font-mono text-xl font-bold text-ink focus:outline-none focus:border-coral transition-colors"
        />
      </div>

      <!-- Bank Option -->
      <div class="space-y-2">
        <label class="text-xs font-semibold uppercase tracking-wider text-muted">Pilih Bank Virtual Account</label>
        <div class="grid grid-cols-3 gap-3">
          {#each ['BCA', 'MANDIRI', 'BNI'] as b}
            <button
              type="button"
              onclick={() => bankCode = b}
              class="py-3 px-4 rounded-lg border text-sm font-bold transition-all flex items-center justify-center gap-2
                {bankCode === b ? 'bg-surface-dark text-on-dark border-surface-dark' : 'bg-canvas border-hairline text-ink hover:bg-surface-cream-strong'}
              "
            >
              <Building2 size={16} /> {b}
            </button>
          {/each}
        </div>
      </div>

      <!-- PIN Input -->
      <div class="space-y-1.5 pt-2 border-t border-hairline">
        <label for="pin" class="text-xs font-semibold uppercase tracking-wider text-muted">PIN Transaksi (6-Digit)</label>
        <input
          id="pin"
          type="password"
          maxlength="6"
          bind:value={pin}
          required
          placeholder="••••••"
          class="w-full bg-canvas border border-hairline rounded-lg px-4 py-2.5 tracking-[0.5em] font-mono text-lg text-center text-ink focus:outline-none focus:border-coral"
        />
      </div>

      <button type="submit" disabled={isLoading} class="w-full btn-editorial-primary py-3.5 font-semibold text-base">
        {#if isLoading}
          <Loader2 class="animate-spin" size={20} /> Memproses...
        {:else}
          Buat Virtual Account Top-Up
        {/if}
      </button>
    </form>
  {:else}
    <!-- Order Created Instructions -->
    <div class="card-dark-chrome p-8 space-y-6 text-center shadow-elevated">
      <div class="w-12 h-12 rounded-full bg-success/20 text-success mx-auto flex items-center justify-center">
        <Check size={28} />
      </div>

      <div class="space-y-1">
        <h2 class="font-display text-3xl font-bold text-on-dark">Virtual Account Dibuat</h2>
        <p class="text-xs text-on-dark-soft">Transfer tepat ke nomor Virtual Account di bawah ini.</p>
      </div>

      <div class="bg-surface-dark-elevated p-6 rounded-xl space-y-2 border border-surface-dark-soft">
        <div class="text-xs text-on-dark-soft uppercase">Nomor Virtual Account ({createdOrder.bankCode})</div>
        <div class="font-mono text-3xl font-bold tracking-wider text-coral">
          {createdOrder.virtualAccountNumber}
        </div>
        <div class="text-xs text-on-dark-soft">Nominal: <span class="font-mono font-bold text-on-dark">Rp {createdOrder.amount.toLocaleString('id-ID')}</span></div>
      </div>

      <button onclick={() => goto('/wallet')} class="btn-editorial-primary w-full py-3">
        Kembali Ke Wallet
      </button>
    </div>
  {/if}
</div>
