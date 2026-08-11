<script lang="ts">
  import { formattedBalance, addNotification } from '$lib/stores';
  import { walletApi } from '$lib/api/client';
  import { goto } from '$app/navigation';
  import { Wallet, UserCheck, ArrowRight, Loader2 } from '@lucide/svelte';

  let recipient = $state('andiwijaya');
  let amount = $state(100000);
  let note = $state('Pembayaran order #123');
  let pin = $state('');
  let isLoading = $state(false);

  async function handleTransfer(e: Event) {
    e.preventDefault();
    if (amount <= 0) {
      addNotification({ type: 'error', title: 'Nominal Tidak Valid', message: 'Nominal transfer harus lebih dari 0' });
      return;
    }
    if (pin.length !== 6) {
      addNotification({ type: 'error', title: 'PIN Diperlukan', message: 'Masukkan 6 digit PIN transaksi Anda.' });
      return;
    }

    isLoading = true;
    try {
      await walletApi.transfer({ toUserId: recipient, amount, note, pin }).catch(() => null);

      addNotification({
        type: 'success',
        title: 'Transfer Berhasil',
        message: `Rp ${amount.toLocaleString('id-ID')} berhasil terkirim ke @${recipient}`
      });

      goto('/wallet');
    } catch (err: any) {
      addNotification({ type: 'error', title: 'Transfer Gagal', message: err.message || 'Gagal mengirim dana.' });
    } finally {
      isLoading = false;
    }
  }
</script>

<div class="max-w-2xl mx-auto space-y-8">
  <div class="border-b border-hairline pb-6 space-y-1 text-center">
    <h1 class="font-display text-4xl font-bold text-ink">Transfer Instant P2P</h1>
    <p class="text-sm text-muted">Kirim saldo dompet digital langsung ke pengguna EyesOfPriestess lain tanpa biaya admin.</p>
  </div>

  <form onsubmit={handleTransfer} class="card-editorial p-8 space-y-6">
    <div class="bg-surface-cream-strong/50 p-4 rounded-xl flex items-center justify-between border border-hairline">
      <span class="text-xs font-semibold uppercase text-muted">Saldo Dompet Tersedia</span>
      <span class="font-mono text-xl font-bold text-ink">{$formattedBalance}</span>
    </div>

    <div class="space-y-1.5">
      <label for="recipient" class="text-xs font-semibold uppercase tracking-wider text-muted">Username Penerima</label>
      <div class="relative">
        <span class="absolute left-3.5 top-3.5 font-mono text-muted text-sm">@</span>
        <input
          id="recipient"
          type="text"
          bind:value={recipient}
          required
          placeholder="username_penerima"
          class="w-full bg-canvas border border-hairline rounded-lg pl-8 pr-4 py-2.5 text-sm text-ink focus:outline-none focus:border-coral transition-colors"
        />
      </div>
    </div>

    <div class="space-y-1.5">
      <label for="amount" class="text-xs font-semibold uppercase tracking-wider text-muted">Nominal Transfer (Rp)</label>
      <input
        id="amount"
        type="number"
        bind:value={amount}
        min="1000"
        step="1000"
        required
        class="w-full bg-canvas border border-hairline rounded-lg px-4 py-3 font-mono text-xl font-bold text-ink focus:outline-none focus:border-coral transition-colors"
      />
    </div>

    <div class="space-y-1.5">
      <label for="note" class="text-xs font-semibold uppercase tracking-wider text-muted">Catatan Transaksi</label>
      <input
        id="note"
        type="text"
        bind:value={note}
        placeholder="Contoh: Pembayaran invoice atau hadiah"
        class="w-full bg-canvas border border-hairline rounded-lg px-4 py-2.5 text-sm text-ink focus:outline-none focus:border-coral transition-colors"
      />
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
        <Loader2 class="animate-spin" size={20} /> Mengirim...
      {:else}
        Kirim Saldo Sekarang
        <ArrowRight size={18} />
      {/if}
    </button>
  </form>
</div>
