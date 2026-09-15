<script lang="ts">
  import Input from '$lib/components/ui/Input.svelte';
  import Button from '$lib/components/ui/Button.svelte';
  import { walletApi } from '$lib/api';
  import { addNotification } from '$lib/stores/notifications';
  import { goto } from '$app/navigation';

  let bankCode = $state('BCA');
  let accountNumber = $state('');
  let accountHolderName = $state('');
  let isPrimary = $state(true);
  let loading = $state(false);
  let error = $state('');

  const banks = [
    { code: 'BCA', name: 'Bank BCA' },
    { code: 'MANDIRI', name: 'Bank Mandiri' },
    { code: 'BNI', name: 'Bank BNI' },
    { code: 'BRI', name: 'Bank BRI' },
    { code: 'CIMB', name: 'Bank CIMB Niaga' },
    { code: 'PERMATA', name: 'Bank Permata' },
    { code: 'DANAMON', name: 'Bank Danamon' },
    { code: 'BSI', name: 'Bank Syariah Indonesia' }
  ];

  async function handleSubmit(e: Event) {
    e.preventDefault();
    error = '';
    if (!accountNumber.trim() || accountNumber.length < 6) {
      error = 'Nomor rekening tidak valid';
      return;
    }
    if (!accountHolderName.trim()) {
      error = 'Nama pemilik rekening wajib diisi';
      return;
    }

    loading = true;
    try {
      const selectedBank = banks.find((b) => b.code === bankCode);
      await walletApi.addBankAccount({
        bankCode,
        bankName: selectedBank?.name ?? bankCode,
        accountNumber,
        accountHolderName,
        isPrimary
      });
      addNotification({
        type: 'success',
        title: 'Rekening Tersimpan',
        message: `${selectedBank?.name} (${accountNumber}) berhasil ditambahkan`
      });
      goto('/withdraw');
    } catch (err: unknown) {
      const apiErr = err as { message?: string };
      error = apiErr?.message ?? 'Gagal menambahkan rekening bank';
    } finally {
      loading = false;
    }
  }
</script>

<svelte:head>
  <title>Tambah Rekening Bank - EyesOfPriestess</title>
</svelte:head>

<div class="max-w-xl mx-auto pb-20 lg:pb-8">
  <div class="mb-6">
    <a href="/withdraw" class="font-sans text-[13px] text-[var(--color-muted)] hover:text-[var(--color-primary)] transition-colors">
      &larr; Kembali ke Penarikan
    </a>
    <h1 class="font-display text-[var(--text-display-md)] text-[var(--color-ink)] tracking-[-0.015em] mt-1">
      Tambah Rekening Bank
    </h1>
    <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)] mt-1">
      Pastikan nama pemilik rekening sesuai dengan identitas Anda.
    </p>
  </div>

  <form onsubmit={handleSubmit} class="flex flex-col gap-6">
    {#if error}
      <div class="bg-[var(--color-error)]/8 border border-[var(--color-error)]/20 rounded-[var(--radius-md)] px-4 py-3">
        <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-error)]">{error}</p>
      </div>
    {/if}

    <div class="bg-[var(--color-surface-card)] p-6 rounded-[var(--radius-xl)] flex flex-col gap-4">
      <div class="flex flex-col gap-1.5">
        <label for="add-bank-code" class="font-sans text-[13px] font-medium text-[var(--color-ink)]">
          Nama Bank
        </label>
        <select
          id="add-bank-code"
          bind:value={bankCode}
          class="h-10 px-3 bg-[var(--color-canvas)] border border-[var(--color-hairline)] rounded-[var(--radius-md)] font-sans text-[14px] text-[var(--color-ink)] focus:outline-none focus:border-[var(--color-primary)]"
        >
          {#each banks as b}
            <option value={b.code}>{b.name}</option>
          {/each}
        </select>
      </div>

      <Input
        label="Nomor Rekening"
        type="number"
        bind:value={accountNumber}
        placeholder="1234567890"
        required
        id="add-account-number"
      />

      <Input
        label="Nama Pemilik Rekening (Sesuai KTP & Buku Tabungan)"
        bind:value={accountHolderName}
        placeholder="Nama Lengkap Pemilik"
        required
        id="add-account-holder"
      />

      <label class="flex items-center gap-2 cursor-pointer pt-2">
        <input
          type="checkbox"
          bind:checked={isPrimary}
          class="w-4 h-4 rounded border-[var(--color-hairline)] text-[var(--color-primary)] focus:ring-0"
        />
        <span class="font-sans text-[13px] text-[var(--color-ink)]">Jadikan rekening utama</span>
      </label>
    </div>

    <Button variant="primary" type="submit" size="lg" {loading} class="w-full">
      Simpan Rekening Bank
    </Button>
  </form>
</div>
