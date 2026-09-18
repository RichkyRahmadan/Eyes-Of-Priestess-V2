<script lang="ts">
  import { onMount } from 'svelte';
  import Input from '$lib/components/ui/Input.svelte';
  import Button from '$lib/components/ui/Button.svelte';
  import PinInput from '$lib/components/ui/PinInput.svelte';
  import Badge from '$lib/components/ui/Badge.svelte';
  import { walletApi } from '$lib/api';
  import { walletStore } from '$lib/stores/wallet';
  import { formatIDR } from '$lib/utils/format';
  import { addNotification } from '$lib/stores/notifications';
  import { goto } from '$app/navigation';
  import type { BankAccount } from '$lib/types';

  let bankAccounts = $state<BankAccount[]>([]);
  let selectedAccountId = $state<string>('');
  let amount = $state('');
  let loading = $state(false);
  let pageLoading = $state(true);
  let errors = $state<Record<string, string>>({});
  let showPinModal = $state(false);
  let pinValues = $state(Array(6).fill(''));
  let pinError = $state('');

  const numAmount = $derived(Number(amount.replace(/\D/g, '')) || 0);

  onMount(async () => {
    try {
      bankAccounts = await walletApi.getBankAccounts() as BankAccount[];
      const primary = bankAccounts.find((a) => a.isPrimary) ?? bankAccounts[0];
      if (primary) selectedAccountId = primary.id;
    } finally {
      pageLoading = false;
    }
  });

  function validate() {
    const errs: Record<string, string> = {};
    if (!selectedAccountId) errs.account = 'Pilih rekening bank tujuan';
    if (!numAmount || numAmount < 50000) errs.amount = 'Nominal penarikan minimal Rp 50.000';
    if ($walletStore && numAmount > $walletStore.availableBalance) {
      errs.amount = 'Saldo Anda tidak mencukupi';
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

  async function handleFinalWithdraw() {
    const pin = pinValues.join('');
    if (pin.length !== 6) {
      pinError = 'Masukkan 6 digit PIN';
      return;
    }

    loading = true;
    pinError = '';
    try {
      await walletApi.withdraw({
        bankAccountId: selectedAccountId,
        amount: numAmount,
        pin
      });
      addNotification({
        type: 'success',
        title: 'Penarikan Diproses',
        message: `Penarikan dana sebesar ${formatIDR(numAmount)} sedang dikirim ke bank`
      });
      goto('/wallet');
    } catch (err: unknown) {
      const apiErr = err as { message?: string };
      pinError = apiErr?.message ?? 'Gagal memproses penarikan dana';
    } finally {
      loading = false;
    }
  }
</script>

<svelte:head>
  <title>Tarik Dana - EyesOfPriestess</title>
</svelte:head>

<div class="max-w-xl mx-auto pb-20 lg:pb-8">
  <div class="mb-6">
    <a href="/wallet" class="font-sans text-[13px] text-[var(--color-muted)] hover:text-[var(--color-primary)] transition-colors">
      &larr; Kembali ke Dompet
    </a>
    <h1 class="font-display text-[var(--text-display-md)] text-[var(--color-ink)] tracking-[-0.015em] mt-1">
      Penarikan Dana (Withdraw)
    </h1>
    <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)] mt-1">
      Tarik saldo dari EyesOfPriestess langsung ke rekening bank lokal Anda.
    </p>
  </div>

  {#if pageLoading}
    <div class="bg-[var(--color-surface-card)] p-8 rounded-[var(--radius-xl)] text-center font-sans text-[var(--text-body-sm)] text-[var(--color-muted)]">
      Memuat rekening bank...
    </div>
  {:else if bankAccounts.length === 0}
    <div class="bg-[var(--color-surface-card)] p-8 rounded-[var(--radius-xl)] text-center space-y-4">
      <p class="font-sans text-[var(--text-body-md)] text-[var(--color-ink)] font-medium">
        Anda belum mendaftarkan rekening bank
      </p>
      <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)]">
        Tambahkan rekening bank atas nama Anda sendiri sebelum melakukan penarikan.
      </p>
      <a
        href="/withdraw/add-account"
        class="inline-flex items-center justify-center h-10 px-5 bg-[var(--color-primary)] text-white font-sans font-medium text-[14px] rounded-[var(--radius-md)] hover:bg-[var(--color-primary-active)] transition-colors"
      >
        Tambah Rekening Bank
      </a>
    </div>
  {:else}
    <form onsubmit={handlePreSubmit} class="flex flex-col gap-6">
      <!-- Select Bank Account -->
      <div class="bg-[var(--color-surface-card)] p-6 rounded-[var(--radius-xl)] space-y-3">
        <div class="flex justify-between items-center mb-2">
          <p class="font-sans text-[13px] font-medium text-[var(--color-ink)]">
            Rekening Tujuan
          </p>
          <a href="/withdraw/add-account" class="font-sans text-[12px] text-[var(--color-primary)] hover:underline">
            + Tambah Rekening
          </a>
        </div>

        <div class="flex flex-col gap-2">
          {#each bankAccounts as acc}
            <button
              type="button"
              onclick={() => (selectedAccountId = acc.id)}
              class="p-4 rounded-[var(--radius-lg)] border text-left transition-all flex items-center justify-between {selectedAccountId === acc.id ? 'border-[var(--color-primary)] bg-[var(--color-primary)]/5 text-[var(--color-ink)]' : 'border-[var(--color-hairline)] bg-[var(--color-canvas)] text-[var(--color-muted)]'}"
            >
              <div>
                <span class="font-sans font-medium text-[14px] text-[var(--color-ink)] block">
                  {acc.bankName} — {acc.accountNumber}
                </span>
                <span class="font-sans text-[12px] text-[var(--color-muted)]">
                  a.n. {acc.accountHolderName}
                </span>
              </div>
              {#if acc.isPrimary}
                <Badge variant="coral">Utama</Badge>
              {/if}
            </button>
          {/each}
        </div>
        {#if errors.account}
          <span class="font-sans text-[12px] text-[var(--color-error)] block">{errors.account}</span>
        {/if}
      </div>

      <!-- Amount Input -->
      <div class="bg-[var(--color-surface-card)] p-6 rounded-[var(--radius-xl)] space-y-3">
        <Input
          label="Nominal Penarikan (Rp)"
          type="number"
          bind:value={amount}
          placeholder="100000"
          error={errors.amount}
          required
          id="withdraw-amount"
        />

        <div class="bg-[var(--color-surface-soft)] p-4 rounded-[var(--radius-lg)] space-y-1 text-[13px]">
          <div class="flex justify-between text-[var(--color-body)]">
            <span>Saldo Tersedia:</span>
            <span class="font-medium text-[var(--color-ink)]">{formatIDR($walletStore?.availableBalance ?? 0)}</span>
          </div>
          <div class="flex justify-between text-[var(--color-muted)]">
            <span>Biaya Transfer Bank:</span>
            <span class="text-[var(--color-success)]">GRATIS</span>
          </div>
        </div>
      </div>

      <Button variant="primary" type="submit" size="lg" class="w-full">
        Konfirmasi Penarikan
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
            PIN Penarikan Dana
          </h2>
          <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)] mb-6">
            Tarik {formatIDR(numAmount)} ke rekening bank pilihan Anda
          </p>
          <PinInput bind:pin={pinValues} error={pinError} />
          <div class="mt-6 flex gap-3">
            <Button variant="secondary" class="flex-1" onclick={() => (showPinModal = false)}>
              Batal
            </Button>
            <Button variant="primary" class="flex-1" {loading} onclick={handleFinalWithdraw}>
              Tarik Dana
            </Button>
          </div>
        </div>
      </div>
    {/if}
  {/if}
</div>
