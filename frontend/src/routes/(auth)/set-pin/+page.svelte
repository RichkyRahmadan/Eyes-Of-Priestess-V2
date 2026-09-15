<script lang="ts">
  import PinInput from '$lib/components/ui/PinInput.svelte';
  import Button from '$lib/components/ui/Button.svelte';
  import { authApi } from '$lib/api';
  import { authStore } from '$lib/stores/auth';
  import { goto } from '$app/navigation';
  import type { ApiError } from '$lib/types';

  let pin = $state(Array(6).fill(''));
  let confirmPin = $state(Array(6).fill(''));
  let step = $state<'set' | 'confirm'>('set');
  let loading = $state(false);
  let error = $state('');

  function handlePinChange(value: string) {
    if (value.length === 6 && step === 'set') {
      step = 'confirm';
    }
  }

  function handleConfirmChange(value: string) {
    error = '';
    if (value.length === 6) {
      if (pin.join('') !== value) {
        error = 'PIN tidak cocok. Coba lagi.';
        confirmPin = Array(6).fill('');
      }
    }
  }

  async function handleSubmit() {
    const pinValue = pin.join('');
    const confirmValue = confirmPin.join('');

    if (pinValue.length !== 6 || confirmValue.length !== 6) {
      error = 'Masukkan PIN 6 digit secara lengkap';
      return;
    }
    if (pinValue !== confirmValue) {
      error = 'PIN tidak cocok';
      return;
    }

    loading = true;
    error = '';
    try {
      await authApi.setPin(pinValue);
      authStore.updateUser({ ...(await authApi.me() as import('$lib/types').User) });
      goto('/dashboard');
    } catch (err) {
      const apiErr = err as ApiError;
      error = apiErr?.message ?? 'Gagal menyimpan PIN. Coba lagi.';
    } finally {
      loading = false;
    }
  }
</script>

<svelte:head>
  <title>Atur PIN - EyesOfPriestess</title>
</svelte:head>

<div class="flex flex-col items-center text-center gap-6">
  <div>
    <h1
      class="font-display text-[var(--text-display-sm)] text-[var(--color-ink)] tracking-[-0.01em] mb-1"
    >
      {step === 'set' ? 'Atur PIN Keamanan' : 'Konfirmasi PIN Anda'}
    </h1>
    <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)]">
      {step === 'set'
        ? 'PIN 6 digit diperlukan untuk setiap transaksi penting'
        : 'Masukkan PIN yang sama untuk konfirmasi'}
    </p>
  </div>

  {#if step === 'set'}
    <PinInput bind:pin onchange={handlePinChange} {error} />
  {:else}
    <PinInput bind:pin={confirmPin} onchange={handleConfirmChange} {error} />
    <div class="flex flex-col items-center gap-3 w-full">
      <Button
        variant="primary"
        class="w-full"
        {loading}
        onclick={handleSubmit}
      >
        Simpan PIN
      </Button>
      <button
        onclick={() => {
          step = 'set';
          pin = Array(6).fill('');
          confirmPin = Array(6).fill('');
          error = '';
        }}
        class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)] hover:text-[var(--color-primary)] transition-colors"
      >
        Ubah PIN
      </button>
    </div>
  {/if}

  <div
    class="w-full bg-[var(--color-surface-soft)] rounded-[var(--radius-md)] px-4 py-3 text-left"
  >
    <p class="font-sans text-[var(--text-caption)] text-[var(--color-muted)] leading-relaxed">
      PIN dienkripsi dengan Argon2. Salah memasukkan 3 kali berturut-turut akan mengunci akun selama 15 menit. Jangan bagikan PIN kepada siapa pun.
    </p>
  </div>
</div>
