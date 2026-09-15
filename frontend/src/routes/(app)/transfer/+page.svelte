<script lang="ts">
  import Input from '$lib/components/ui/Input.svelte';
  import Button from '$lib/components/ui/Button.svelte';
  import PinInput from '$lib/components/ui/PinInput.svelte';
  import { walletApi } from '$lib/api';
  import { walletStore } from '$lib/stores/wallet';
  import { formatIDR } from '$lib/utils/format';
  import { addNotification } from '$lib/stores/notifications';
  import { goto } from '$app/navigation';

  let recipientUsername = $state('');
  let amount = $state('');
  let note = $state('');
  let loading = $state(false);
  let errors = $state<Record<string, string>>({});
  let showPinModal = $state(false);
  let pinValues = $state(Array(6).fill(''));
  let pinError = $state('');

  const numAmount = $derived(Number(amount.replace(/\D/g, '')) || 0);

  function validate() {
    const errs: Record<string, string> = {};
    if (!recipientUsername.trim()) errs.recipientUsername = 'Username penerima wajib diisi';
    if (!numAmount || numAmount < 5000) errs.amount = 'Nominal minimal transfer adalah Rp 5.000';
    if ($walletStore && numAmount > $walletStore.availableBalance) {
      errs.amount = 'Saldo tidak mencukupi';
    }
    return errs;
  }

  function handlePreSubmit(e: Event) {
    e.preventDefault();
    const v = validate();
    if (Object.keys(v).length > 0) {
      errors = v;
      return;
    }
    errors = {};
    showPinModal = true;
  }

  async function handleFinalTransfer() {
    const pin = pinValues.join('');
    if (pin.length !== 6) {
      pinError = 'Masukkan 6 digit PIN';
      return;
    }

    loading = true;
    pinError = '';
    try {
      await walletApi.transferP2P({
        recipientUsername,
        amount: numAmount,
        note,
        pin
      });
      addNotification({
        type: 'success',
        title: 'Transfer Berhasil',
        message: `Berhasil mengirim ${formatIDR(numAmount)} ke @${recipientUsername}`
      });
      goto('/wallet');
    } catch (err: unknown) {
      const apiErr = err as { message?: string };
      pinError = apiErr?.message ?? 'Gagal mengirim dana. Periksa PIN Anda.';
    } finally {
      loading = false;
    }
  }
</script>

<svelte:head>
  <title>Transfer P2P - EyesOfPriestess</title>
</svelte:head>

<div class="max-w-xl mx-auto pb-20 lg:pb-8">
  <div class="mb-6">
    <a href="/wallet" class="font-sans text-[13px] text-[var(--color-muted)] hover:text-[var(--color-primary)] transition-colors">
      &larr; Kembali ke Dompet
    </a>
    <h1 class="font-display text-[var(--text-display-md)] text-[var(--color-ink)] tracking-[-0.015em] mt-1">
      Transfer Antar Pengguna
    </h1>
    <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)] mt-1">
      Kirim saldo secara gratis dan bebas biaya ke sesama pengguna EyesOfPriestess.
    </p>
  </div>

  <form onsubmit={handlePreSubmit} class="flex flex-col gap-6">
    <div class="bg-[var(--color-surface-card)] p-6 rounded-[var(--radius-xl)] flex flex-col gap-4">
      <Input
        label="Username Penerima"
        bind:value={recipientUsername}
        placeholder="username_tujuan"
        error={errors.recipientUsername}
        required
        id="transfer-recipient"
      />

      <Input
        label="Nominal Transfer (Rp)"
        type="number"
        bind:value={amount}
        placeholder="50000"
        error={errors.amount}
        required
        id="transfer-amount"
      />

      <Input
        label="Catatan (Opsional)"
        bind:value={note}
        placeholder="Contoh: Bayar hutang kopi"
        id="transfer-note"
      />
    </div>

    <!-- Balance Check -->
    <div class="bg-[var(--color-surface-soft)] p-4 rounded-[var(--radius-lg)] flex justify-between items-center text-[13px]">
      <span class="text-[var(--color-muted)]">Saldo Tersedia Saat Ini:</span>
      <span class="font-display font-semibold text-[var(--color-ink)]">{formatIDR($walletStore?.availableBalance ?? 0)}</span>
    </div>

    <Button variant="primary" type="submit" size="lg" class="w-full">
      Lanjut & Verifikasi PIN
    </Button>
  </form>

  <!-- PIN Modal -->
  {#if showPinModal}
    <div
      class="fixed inset-0 z-50 flex items-center justify-center p-4"
      role="dialog"
      aria-modal="true"
    >
      <div
        class="absolute inset-0 bg-[var(--color-ink)]/40"
        onclick={() => (showPinModal = false)}
        role="presentation"
      ></div>
      <div
        class="relative bg-[var(--color-canvas)] rounded-[var(--radius-xl)] p-8 w-full max-w-sm text-center shadow-[var(--shadow-elevated)]"
      >
        <h2 class="font-display text-[var(--text-display-sm)] text-[var(--color-ink)] mb-2">
          Verifikasi PIN Transfer
        </h2>
        <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)] mb-6">
          Kirim {formatIDR(numAmount)} ke @{recipientUsername}
        </p>
        <PinInput bind:pin={pinValues} error={pinError} />
        <div class="mt-6 flex gap-3">
          <Button variant="secondary" class="flex-1" onclick={() => (showPinModal = false)}>
            Batal
          </Button>
          <Button variant="primary" class="flex-1" {loading} onclick={handleFinalTransfer}>
            Kirim Dana
          </Button>
        </div>
      </div>
    </div>
  {/if}
</div>
