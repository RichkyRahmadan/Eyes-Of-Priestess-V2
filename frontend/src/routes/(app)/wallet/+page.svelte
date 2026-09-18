<script lang="ts">
  import { onMount } from 'svelte';
  import { walletStore, transactionsStore, walletLoading } from '$lib/stores/wallet';
  import { walletApi } from '$lib/api';
  import Badge from '$lib/components/ui/Badge.svelte';
  import Button from '$lib/components/ui/Button.svelte';
  import Skeleton from '$lib/components/ui/Skeleton.svelte';
  import ConfirmModal from '$lib/components/ui/ConfirmModal.svelte';
  import { toasts } from '$lib/components/ui/Toaster.svelte';
  import { formatDate, formatIDR } from '$lib/utils/format';
  import type { Transaction, TransactionType, TransactionStatus, BankAccount } from '$lib/types';

  let bankAccounts = $state<BankAccount[]>([]);
  let currentPage = $state(0);
  let totalPages = $state(0);
  let filterType = $state<TransactionType | ''>('');
  let filterStatus = $state<TransactionStatus | ''>('');
  let loadingMore = $state(false);
  let selectedAccountToDelete = $state<BankAccount | null>(null);
  let showDeleteModal = $state(false);
  let deletingAccount = $state(false);

  onMount(() => loadData());

  async function loadData(page = 0) {
    if (!$walletStore || page !== 0) {
      walletLoading.set(true);
    }
    try {
      const params = new URLSearchParams({ page: String(page), size: '20' });
      if (filterType) params.set('type', filterType);
      if (filterStatus) params.set('status', filterStatus);

      const [wallet, txRes, accs] = await Promise.all([
        walletApi.getBalance() as Promise<import('$lib/types').Wallet>,
        walletApi.getTransactions(params.toString()) as Promise<import('$lib/types').PaginatedResponse<Transaction>>,
        walletApi.getBankAccounts() as Promise<BankAccount[]>
      ]);
      walletStore.set(wallet);
      if (page === 0) transactionsStore.set(txRes.content);
      else transactionsStore.update((curr) => [...curr, ...txRes.content]);
      currentPage = txRes.page;
      totalPages = txRes.totalPages;
      bankAccounts = accs;
    } finally {
      walletLoading.set(false);
    }
  }

  async function loadMore() {
    loadingMore = true;
    await loadData(currentPage + 1);
    loadingMore = false;
  }

  function confirmDeleteAccount(acc: BankAccount) {
    selectedAccountToDelete = acc;
    showDeleteModal = true;
  }

  async function handleDeleteAccount() {
    if (!selectedAccountToDelete) return;
    deletingAccount = true;
    try {
      await walletApi.deleteBankAccount(selectedAccountToDelete.id);
      bankAccounts = bankAccounts.filter((a) => a.id !== selectedAccountToDelete!.id);
      toasts.success('Rekening bank berhasil dihapus.');
      showDeleteModal = false;
    } catch {
      toasts.error('Gagal menghapus rekening bank.');
    } finally {
      deletingAccount = false;
      selectedAccountToDelete = null;
    }
  }

  const typeLabels: Record<string, string> = {
    TOPUP: 'Top Up',
    WITHDRAW: 'Tarik Dana',
    P2P_TRANSFER: 'Transfer P2P',
    ESCROW_HOLD: 'Escrow Hold',
    ESCROW_RELEASE: 'Escrow Release',
    ESCROW_REFUND: 'Refund Escrow',
    FEE: 'Biaya'
  };
</script>

<svelte:head>
  <title>Dompet - EyesOfPriestess</title>
</svelte:head>

<div class="flex flex-col gap-8 pb-20 lg:pb-8">
  <!-- Balance header: left-aligned, no centered hero -->
  <div
    class="bg-[var(--color-surface-dark)] rounded-[var(--radius-xl)] p-8 flex flex-col sm:flex-row items-start sm:items-end justify-between gap-6"
  >
    <div>
      <p class="font-sans text-[var(--text-caption)] text-[var(--color-on-dark-soft)] font-medium mb-2">
        Saldo Tersedia
      </p>
      {#if $walletLoading && !$walletStore}
        <div class="skeleton h-12 w-48 rounded-[var(--radius-sm)] opacity-20"></div>
      {:else}
        <p
          class="font-display text-[var(--text-display-lg)] text-[var(--color-on-dark)] leading-none tracking-[-0.02em] count-animate"
        >
          {$walletStore ? formatIDR($walletStore.availableBalance) : 'Rp 0'}
        </p>
      {/if}
      {#if $walletStore}
        <p class="font-mono text-[12px] text-[var(--color-on-dark-soft)] mt-2 tracking-wider">
          Dana escrow: {formatIDR($walletStore.escrowBalance)}
        </p>
      {/if}
    </div>
    <div class="flex gap-2 flex-wrap">
      <a
        href="/topup"
        class="inline-flex items-center h-9 px-4 bg-[var(--color-primary)] text-white font-sans font-medium text-[13px] rounded-[var(--radius-md)] hover:bg-[var(--color-primary-active)] active:scale-[0.98] transition-all duration-150"
      >
        Top Up
      </a>
      <a
        href="/transfer"
        class="inline-flex items-center h-9 px-4 bg-[var(--color-surface-dark-elevated)] text-[var(--color-on-dark)] font-sans font-medium text-[13px] rounded-[var(--radius-md)] hover:bg-[var(--color-surface-dark-soft)] active:scale-[0.98] transition-all duration-150"
      >
        Transfer
      </a>
      <a
        href="/withdraw"
        class="inline-flex items-center h-9 px-4 bg-[var(--color-surface-dark-elevated)] text-[var(--color-on-dark)] font-sans font-medium text-[13px] rounded-[var(--radius-md)] hover:bg-[var(--color-surface-dark-soft)] active:scale-[0.98] transition-all duration-150"
      >
        Tarik Dana
      </a>
    </div>
  </div>

  <!-- Transaction history -->
  <div>
    <h2 class="font-sans font-medium text-[var(--text-title-md)] text-[var(--color-ink)] mb-4">
      Riwayat Transaksi
    </h2>

    <!-- Filters -->
    <div class="flex gap-2 flex-wrap mb-4">
      <select
        bind:value={filterType}
        onchange={() => loadData(0)}
        class="h-9 px-3 bg-[var(--color-canvas)] border border-[var(--color-hairline)] rounded-[var(--radius-md)] font-sans text-[var(--text-body-sm)] text-[var(--color-ink)] focus:outline-none focus:border-[var(--color-primary)] transition-colors cursor-pointer"
      >
        <option value="">Semua Tipe</option>
        {#each Object.entries(typeLabels) as [val, label]}
          <option value={val}>{label}</option>
        {/each}
      </select>
      <select
        bind:value={filterStatus}
        onchange={() => loadData(0)}
        class="h-9 px-3 bg-[var(--color-canvas)] border border-[var(--color-hairline)] rounded-[var(--radius-md)] font-sans text-[var(--text-body-sm)] text-[var(--color-ink)] focus:outline-none focus:border-[var(--color-primary)] transition-colors cursor-pointer"
      >
        <option value="">Semua Status</option>
        <option value="SUCCESS">Berhasil</option>
        <option value="PENDING">Menunggu</option>
        <option value="FAILED">Gagal</option>
      </select>
    </div>

    <div class="bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] overflow-hidden">
      {#if $walletLoading && $transactionsStore.length === 0}
        <div class="p-5">
          <Skeleton rows={5} />
        </div>
      {:else if $transactionsStore.length === 0}
        <div class="p-12 text-center">
          <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)]">
            Belum ada transaksi
          </p>
        </div>
      {:else}
        {#each $transactionsStore as tx, i}
          <div
            class="flex items-center gap-4 px-5 py-4 {i < $transactionsStore.length - 1 ? 'border-b border-[var(--color-hairline-soft)]' : ''}"
          >
            <div
              class="w-9 h-9 rounded-[var(--radius-md)] shrink-0 flex items-center justify-center text-[var(--text-body-sm)] font-medium {tx.direction === 'IN' ? 'bg-[var(--color-success)]/10 text-[var(--color-success)]' : tx.direction === 'OUT' ? 'bg-[var(--color-error)]/10 text-[var(--color-error)]' : 'bg-[var(--color-surface-soft)] text-[var(--color-muted)]'}"
            >
              {tx.direction === 'IN' ? '↓' : tx.direction === 'OUT' ? '↑' : '·'}
            </div>
            <div class="flex-1 min-w-0">
              <p
                class="font-sans font-medium text-[var(--text-body-sm)] text-[var(--color-ink)] truncate"
              >
                {tx.description}
              </p>
              <p class="font-sans text-[var(--text-caption)] text-[var(--color-muted-soft)] mt-0.5">
                {typeLabels[tx.type] ?? tx.type} · {formatDate(tx.createdAt)}
              </p>
            </div>
            <div class="text-right shrink-0">
              <p
                class="font-sans font-medium text-[var(--text-body-sm)] {tx.direction === 'IN' ? 'text-[var(--color-success)]' : tx.direction === 'OUT' ? 'text-[var(--color-error)]' : 'text-[var(--color-muted)]'}"
              >
                {tx.direction === 'IN' ? '+' : tx.direction === 'OUT' ? '-' : ''}{formatIDR(tx.amount)}
              </p>
              <div class="mt-1">
                <Badge status={tx.status} />
              </div>
            </div>
          </div>
        {/each}

        {#if currentPage < totalPages - 1}
          <div class="p-4 border-t border-[var(--color-hairline-soft)] flex justify-center">
            <Button variant="ghost" size="sm" loading={loadingMore} onclick={loadMore}>
              Muat lebih banyak
            </Button>
          </div>
        {/if}
      {/if}
    </div>
  </div>

  <!-- Bank accounts -->
  <div>
    <div class="flex items-center justify-between mb-4">
      <h2 class="font-sans font-medium text-[var(--text-title-md)] text-[var(--color-ink)]">
        Rekening Bank
      </h2>
      <a
        href="/withdraw/add-account"
        class="font-sans text-[var(--text-body-sm)] text-[var(--color-primary)] hover:underline"
      >
        Tambah rekening
      </a>
    </div>
    {#if bankAccounts.length === 0}
      <div
        class="bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] p-8 text-center"
      >
        <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)]">
          Belum ada rekening bank tersimpan
        </p>
      </div>
    {:else}
      <div class="flex flex-col gap-2">
        {#each bankAccounts as acc}
          <div
            class="bg-[var(--color-surface-card)] rounded-[var(--radius-lg)] px-5 py-4 flex items-center gap-4"
          >
            <div class="flex-1 min-w-0">
              <p class="font-sans font-medium text-[var(--text-body-sm)] text-[var(--color-ink)]">
                {acc.bankName}
              </p>
              <p class="font-mono text-[var(--text-caption)] text-[var(--color-muted)] mt-0.5">
                {acc.accountNumber}
              </p>
              <p class="font-sans text-[var(--text-caption)] text-[var(--color-muted-soft)]">
                {acc.accountHolderName}
              </p>
            </div>
            <div class="flex items-center gap-2">
              {#if acc.isPrimary}
                <Badge variant="coral">Utama</Badge>
              {/if}
              <button
                type="button"
                onclick={() => confirmDeleteAccount(acc)}
                class="p-2 text-[var(--color-muted)] hover:text-[var(--color-error)] hover:bg-[var(--color-error)]/10 rounded-[var(--radius-md)] transition-colors"
                title="Hapus Rekening"
              >
                <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                </svg>
              </button>
            </div>
          </div>
        {/each}
      </div>
    {/if}
  </div>

  <!-- Delete Confirmation Modal (Rule 5 S1) -->
  <ConfirmModal
    open={showDeleteModal}
    title="Hapus Rekening Bank"
    description={`Apakah Anda yakin ingin menghapus rekening ${selectedAccountToDelete?.bankName ?? ''} - ${selectedAccountToDelete?.accountNumber ?? ''}? Tindakan ini tidak dapat dibatalkan.`}
    confirmLabel="Hapus Rekening"
    cancelLabel="Batal"
    danger={true}
    loading={deletingAccount}
    onConfirm={handleDeleteAccount}
    onCancel={() => { showDeleteModal = false; selectedAccountToDelete = null; }}
  />
</div>
