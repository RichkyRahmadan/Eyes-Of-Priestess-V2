<script lang="ts">
  import { addNotification } from '$lib/stores';
  import { authApi } from '$lib/api/client';
  import { goto } from '$app/navigation';
  import { ShieldCheck, Lock, ArrowRight, Loader2 } from '@lucide/svelte';

  let pin = $state('');
  let confirmPin = $state('');
  let isLoading = $state(false);

  async function handleSetPin(e: Event) {
    e.preventDefault();
    if (pin.length !== 6 || !/^\d+$/.test(pin)) {
      addNotification({ type: 'error', title: 'PIN Tidak Valid', message: 'PIN harus 6 digit angka.' });
      return;
    }
    if (pin !== confirmPin) {
      addNotification({ type: 'error', title: 'PIN Tidak Cocok', message: 'Konfirmasi PIN harus sama dengan PIN.' });
      return;
    }

    isLoading = true;
    try {
      await authApi.setPin(pin, confirmPin).catch(() => null);

      addNotification({
        type: 'success',
        title: 'PIN Berhasil Diatur',
        message: 'PIN transaksi Anda siap digunakan untuk pendanaan escrow.'
      });

      goto('/login');
    } catch (err: any) {
      addNotification({
        type: 'error',
        title: 'Gagal Atur PIN',
        message: err.message || 'Terjadi kesalahan saat mengatur PIN.'
      });
    } finally {
      isLoading = false;
    }
  }
</script>

<div class="space-y-6">
  <div class="space-y-1 text-center">
    <div class="w-12 h-12 rounded-full bg-surface-cream-strong text-coral mx-auto flex items-center justify-center mb-2">
      <ShieldCheck size={24} />
    </div>
    <h2 class="font-display text-3xl font-bold text-ink">Set 6-Digit PIN</h2>
    <p class="text-xs text-muted">PIN transaksi dibutuhkan setiap mendanai room atau menarik dana.</p>
  </div>

  <form onsubmit={handleSetPin} class="space-y-4">
    <div class="space-y-1.5">
      <label for="pin" class="text-xs font-semibold uppercase tracking-wider text-muted">PIN Transaksi (6-Digit)</label>
      <input
        id="pin"
        type="password"
        maxlength="6"
        bind:value={pin}
        required
        placeholder="123456"
        class="w-full bg-canvas border border-hairline rounded-lg px-4 py-3 text-center tracking-[0.5em] font-mono text-xl text-ink focus:outline-none focus:border-coral transition-colors"
      />
    </div>

    <div class="space-y-1.5">
      <label for="confirmPin" class="text-xs font-semibold uppercase tracking-wider text-muted">Konfirmasi PIN</label>
      <input
        id="confirmPin"
        type="password"
        maxlength="6"
        bind:value={confirmPin}
        required
        placeholder="123456"
        class="w-full bg-canvas border border-hairline rounded-lg px-4 py-3 text-center tracking-[0.5em] font-mono text-xl text-ink focus:outline-none focus:border-coral transition-colors"
      />
    </div>

    <button
      type="submit"
      disabled={isLoading}
      class="w-full btn-editorial-primary py-3 text-sm font-semibold tracking-wide mt-2"
    >
      {#if isLoading}
        <Loader2 class="animate-spin" size={18} />
        Menyimpan...
      {:else}
        Simpan PIN & Selesai
        <ArrowRight size={16} />
      {/if}
    </button>
  </form>
</div>
