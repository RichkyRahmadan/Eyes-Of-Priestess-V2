<script lang="ts">
  import { walletStore, formattedBalance, addNotification } from '$lib/stores';
  import { walletApi } from '$lib/api/client';
  import { Wallet, ArrowDownLeft, ArrowUpRight, Plus, Building2, ShieldCheck, Filter } from '@lucide/svelte';

  let filterType = $state('ALL');

  let bankAccounts = $state([
    {
      id: 'bank-1',
      bankCode: 'BCA',
      bankName: 'Bank Central Asia',
      accountNumber: '1234567890',
      accountHolderName: 'BUDI SANTOSO',
      isPrimary: true
    },
    {
      id: 'bank-2',
      bankCode: 'MANDIRI',
      bankName: 'Bank Mandiri',
      accountNumber: '9876543210',
      accountHolderName: 'BUDI SANTOSO',
      isPrimary: false
    }
  ]);

  let transactions = $state([
    {
      transactionId: 'tx-001',
      type: 'TOPUP',
      direction: 'IN',
      amount: 1000000,
      fee: 0,
      status: 'SUCCESS',
      description: 'Top-up VA BCA',
      createdAt: '2026-08-11T10:00:00Z'
    },
    {
      transactionId: 'tx-002',
      type: 'ESCROW_HOLD',
      direction: 'OUT',
      amount: 252500,
      fee: 2500,
      status: 'SUCCESS',
      description: 'Fund Escrow Room #EOP-8821',
      createdAt: '2026-08-11T10:15:00Z'
    },
    {
      transactionId: 'tx-003',
      type: 'P2P_TRANSFER',
      direction: 'OUT',
      amount: 50000,
      fee: 0,
      status: 'SUCCESS',
      description: 'Transfer ke user @andiw',
      createdAt: '2026-08-10T15:30:00Z'
    }
  ]);
</script>

<div class="space-y-8">
  <!-- Header -->
  <div class="border-b border-hairline pb-6 space-y-1">
    <h1 class="font-display text-4xl font-bold text-ink">E-Wallet Vault</h1>
    <p class="text-sm text-muted">Kelola saldo dompet digital, riwayat transaksi, dan rekening penarikan bank Anda.</p>
  </div>

  <!-- Balance Banner Hero -->
  <div class="card-editorial p-8 grid grid-cols-1 md:grid-cols-12 gap-8 items-center">
    <div class="md:col-span-7 space-y-3">
      <div class="text-xs font-semibold uppercase tracking-wider text-muted">Saldo Dompet Digital Utama</div>
      <div class="font-display text-5xl lg:text-6xl font-bold text-ink">
        {$formattedBalance}
      </div>
      <div class="text-xs text-muted">
        Terakhir diperbarui: <span class="font-mono text-body">Hari ini, 12:00 WIB</span>
      </div>
    </div>

    <div class="md:col-span-5 flex flex-wrap md:flex-col gap-3">
      <a href="/topup" class="btn-editorial-primary py-3 text-sm font-semibold">
        <ArrowDownLeft size={18} /> Top-Up Instant Saldo
      </a>
      <a href="/withdraw" class="btn-editorial-secondary py-3 text-sm font-semibold">
        <ArrowUpRight size={18} /> Tarik Saldo Ke Rekening
      </a>
      <a href="/transfer" class="btn-editorial-secondary py-3 text-sm font-semibold">
        <Wallet size={18} /> Transfer P2P Antar User
      </a>
    </div>
  </div>

  <!-- Bank Accounts Section -->
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <h2 class="font-display text-2xl font-bold text-ink">Rekening Bank Terhubung</h2>
      <button class="text-xs font-semibold text-coral hover:underline flex items-center gap-1">
        <Plus size={14} /> Tambah Rekening
      </button>
    </div>

    <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
      {#each bankAccounts as bank (bank.id)}
        <div class="card-editorial p-5 flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="w-12 h-12 rounded-xl bg-surface-cream-strong flex items-center justify-center text-ink font-mono font-bold">
              {bank.bankCode}
            </div>
            <div>
              <div class="text-sm font-bold text-ink">{bank.bankName}</div>
              <div class="font-mono text-xs text-body">{bank.accountNumber}</div>
              <div class="text-[11px] text-muted uppercase">{bank.accountHolderName}</div>
            </div>
          </div>

          {#if bank.isPrimary}
            <span class="px-2.5 py-0.5 rounded-pill bg-coral/10 text-coral font-mono text-[11px] font-semibold">
              UTAMA
            </span>
          {/if}
        </div>
      {/each}
    </div>
  </div>

  <!-- Filterable Transaction History -->
  <div class="space-y-4 pt-4 border-t border-hairline">
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <h2 class="font-display text-2xl font-bold text-ink">Riwayat Transaksi</h2>

      <!-- Filters -->
      <div class="flex items-center gap-2 bg-surface-soft p-1 rounded-lg border border-hairline text-xs font-semibold">
        <button
          onclick={() => filterType = 'ALL'}
          class="px-3 py-1.5 rounded-md transition-all {filterType === 'ALL' ? 'bg-canvas text-ink shadow-soft' : 'text-muted'}"
        >
          Semua
        </button>
        <button
          onclick={() => filterType = 'IN'}
          class="px-3 py-1.5 rounded-md transition-all {filterType === 'IN' ? 'bg-canvas text-ink shadow-soft' : 'text-muted'}"
        >
          Uang Masuk
        </button>
        <button
          onclick={() => filterType = 'OUT'}
          class="px-3 py-1.5 rounded-md transition-all {filterType === 'OUT' ? 'bg-canvas text-ink shadow-soft' : 'text-muted'}"
        >
          Uang Keluar
        </button>
      </div>
    </div>

    <!-- Table -->
    <div class="bg-surface-card rounded-xl border border-hairline overflow-hidden">
      <table class="w-full text-left border-collapse">
        <thead>
          <tr class="border-b border-hairline bg-surface-cream-strong/50 text-[11px] font-semibold uppercase text-muted tracking-wider">
            <th class="p-4">Jenis Transaksi</th>
            <th class="p-4">Deskripsi</th>
            <th class="p-4">Tanggal</th>
            <th class="p-4 text-right">Nominal</th>
            <th class="p-4 text-center">Status</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-hairline text-xs">
          {#each transactions.filter(t => filterType === 'ALL' || t.direction === filterType) as tx (tx.transactionId)}
            <tr class="hover:bg-surface-soft/60 transition-colors">
              <td class="p-4 font-mono font-semibold text-ink">
                <span class="inline-flex items-center gap-1.5">
                  <span class="w-2 h-2 rounded-full {tx.direction === 'IN' ? 'bg-success' : 'bg-coral'}"></span>
                  {tx.type}
                </span>
              </td>
              <td class="p-4 font-medium text-body">{tx.description}</td>
              <td class="p-4 text-muted">{new Date(tx.createdAt).toLocaleString('id-ID')}</td>
              <td class="p-4 text-right font-mono font-bold {tx.direction === 'IN' ? 'text-success' : 'text-ink'}">
                {tx.direction === 'IN' ? '+' : '-'} Rp {tx.amount.toLocaleString('id-ID')}
              </td>
              <td class="p-4 text-center">
                <span class="px-2.5 py-0.5 rounded-pill bg-success/15 text-success font-semibold">
                  {tx.status}
                </span>
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
    </div>
  </div>
</div>
