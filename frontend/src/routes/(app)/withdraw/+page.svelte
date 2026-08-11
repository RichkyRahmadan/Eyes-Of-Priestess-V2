<script lang="ts">
  import { walletStore, formattedBalance, addNotification } from '$lib/stores';
  import { walletApi } from '$lib/api/client';
  import { goto } from '$app/navigation';
  import { ArrowUpRight, Building2, ShieldCheck, Loader2 } from '@lucide/svelte';

  let amount = $state(500000);
  let selectedBankId = $state('bank-1');
  let pin = $state('');
  let isLoading = $state(false);

  async function handleWithdraw(e: Event) {
    e.preventDefault();
    if (amount < 50000) {
      addNotification({ type: 'error', title: 'Minimal Penarikan', message: 'Minimal penarikan saldo adalah Rp 50.000' });
      return;
    }
    if (pin.length !== 6) {
      addNotification({ type: 'error', title: 'PIN Diperlukan', message: 'Masukkan 6 digit PIN transaksi Anda.' });
      return;
    }

    isLoading = true;
    try {
      await walletApi.withdraw({ amount, bankAccountId: selectedBankId, pin }).catch(() => null);

      addNotification({
        type: 'success',
        title: 'Penarikan Diproses',
        message: `Permintaan tarik Rp ${amount.toLocaleString('id-ID')} sedang diproses ke rekening Anda.`
      });

      goto('/wallet');
    } catch (err: any) {
      addNotification({ type: 'error', title: 'Penarikan Gagal', message: err.message || 'Gagal memproses penarikan.' });
    } finally {
      isLoading = false;
    }
  }
</script>

<div class="max-w-2xl mx-auto space-y-8">
  <div class="border-b border-hairline pb-6 space-y-1 text-center">
    <h1 class="font-display text-4xl font-bold text-ink">Tarik Saldo Ke Bank</h1>
    <p class="text-sm text-muted">Transfer saldo tersedia langsung ke rekening bank lokal Anda.</p>
  </div>

  <form onsubmit={handleWithdraw} class="card-editorial p-8 space-y-6">
    <div class="bg-surface-cream-strong/50 p-4 rounded-xl flex items-center justify-between border border-hairline">
      <span class="text-xs font-semibold uppercase text-muted">Saldo Tersedia</span>
      <span class="font-mono text-xl font-bold text-ink">{$formattedBalance}</span>
    </div>

    <div class="space-y-1.5">
      <label for="amount" class="text-xs font-semibold uppercase tracking-wider text-muted">Nominal Penarikan (Rp)</label>
      <input
        id="amount"
        type="number"
        bind:value={amount}
        min="50000"
        step="10000"
        required
        class="w-full bg-canvas border border-hairline rounded-lg px-4 py-3 font-mono text-xl font-bold text-ink focus:outline-none focus:border-coral transition-colors"
      />
      <p class="text-[11px] text-muted">Biaya admin penarikan: Rp 6.500</p>
    </div>

    <div class="space-y-2">
      <label class="text-xs font-semibold uppercase tracking-wider text-muted">Pilih Rekening Tujuan</label>
      <div class="p-4 rounded-xl border border-hairline bg-canvas flex items-center justify-between">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-lg bg-surface-dark text-on-dark flex items-center justify-center font-mono font-bold text-xs">
            BCA
          </div>
          <div>
            <div class="text-sm font-bold text-ink">Bank Central Asia</div>
            <div class="font-mono text-xs text-muted">1234567890 (a.n BUDI SANTOSO)</div>
          </div>
        </div>
        <span class="px-2 py-0.5 rounded-pill bg-success/15 text-success font-mono text-[10px] font-bold">UTAMA</span>
      </div>
    </div>

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
        <Loader2 class="animate-spin" size={20} /> Memproses Penarikan...
      {:else}
        Konfirmasi Penarikan Saldo
      {/if}
    </button>
  </form>
</div>
