<script lang="ts">
  import { onMount } from 'svelte';
  import { vaultStore } from '$lib/stores/vault';
  import { vaultApi } from '$lib/api/vault';
  import { formatIDR, formatIDRCompact, formatDateTime, statusBadgeClass, chronicleLabel } from '$lib/utils/formatters';
  import { toast } from 'svelte-sonner';
  import { Wallet, ArrowDownLeft, ArrowUpRight, Send, X, RefreshCw, ChevronDown } from "@lucide/svelte";
  import LoadingCrystal from '$lib/components/shared/LoadingCrystal.svelte';
  import type { ChronicleType } from '$lib/types/vault';

  let loading    = $state(true);
  let modal      = $state<'offering' | 'withdrawal' | 'tithe' | null>(null);
  let submitting = $state(false);

  // Offering
  let offerAmount = $state('');
  let offerMethod = $state<'VIRTUAL_ACCOUNT' | 'E_WALLET'>('VIRTUAL_ACCOUNT');
  let offerBank   = $state('BCA');

  // Withdrawal
  let wdAmount  = $state('');
  let wdBank    = $state('BCA');
  let wdAccNum  = $state('');
  let wdAccName = $state('');
  let wdPin     = $state('');

  // Tithe
  let tithePhone  = $state('');
  let titheAmount = $state('');
  let titheNote   = $state('');
  let tithePin    = $state('');

  onMount(async () => {
    try {
      const [t, c] = await Promise.all([vaultApi.getTreasury(), vaultApi.getChronicles()]);
      vaultStore.setTreasury(t);
      vaultStore.setChronicles(c.content ?? []);
    } catch { toast.error('Gagal memuat data vault.'); }
    finally { loading = false; }
  });

  async function submitOffering() {
    submitting = true;
    try {
      await vaultApi.makeOffering({ amount: +offerAmount, method: offerMethod, bank: offerMethod === 'VIRTUAL_ACCOUNT' ? offerBank : undefined });
      toast.success('Offering dikonfirmasi! Cek VA/link pembayaran Anda.');
      modal = null;
      const [t, c] = await Promise.all([vaultApi.getTreasury(), vaultApi.getChronicles()]);
      vaultStore.setTreasury(t);
      vaultStore.setChronicles(c.content ?? []);
    } catch (e: unknown) {
      toast.error(e instanceof Error ? e.message : 'Gagal.');
    } finally { submitting = false; }
  }

  async function submitWithdrawal() {
    submitting = true;
    try {
      await vaultApi.withdraw({ amount: +wdAmount, bankCode: wdBank, bankName: wdBank, accountNumber: wdAccNum, accountName: wdAccName, pin: wdPin });
      toast.success('Penarikan berhasil dikajukan!');
      modal = null;
      const [t, c] = await Promise.all([vaultApi.getTreasury(), vaultApi.getChronicles()]);
      vaultStore.setTreasury(t); vaultStore.setChronicles(c.content ?? []);
    } catch (e: unknown) { toast.error(e instanceof Error ? e.message : 'Gagal.'); }
    finally { submitting = false; }
  }

  async function submitTithe() {
    submitting = true;
    try {
      await vaultApi.tithe({ recipientPhone: tithePhone, amount: +titheAmount, note: titheNote, pin: tithePin });
      toast.success('Tithing terkirim!');
      modal = null;
      const [t, c] = await Promise.all([vaultApi.getTreasury(), vaultApi.getChronicles()]);
      vaultStore.setTreasury(t); vaultStore.setChronicles(c.content ?? []);
    } catch (e: unknown) { toast.error(e instanceof Error ? e.message : 'Gagal.'); }
    finally { submitting = false; }
  }

  const chronicleSign = (type: ChronicleType) =>
    ['OFFERING','TITHING_IN','SEAL_RELEASE','SEAL_REFUND'].includes(type) ? '+' : '-';
  const chronicleColor = (type: ChronicleType) =>
    ['OFFERING','TITHING_IN','SEAL_RELEASE','SEAL_REFUND'].includes(type) ? 'var(--success)' : 'var(--danger)';
</script>

<svelte:head>
  <title>Vault Sanctum — EyesOfPriestess</title>
</svelte:head>

{#if loading}
  <LoadingCrystal />
{:else}
<div class="vault animate-fade-in">
  <!-- Treasury Card -->
  {#if $vaultStore.treasury}
    <div class="treasury-hero glow-border-gold">
      <div class="th-main">
        <div>
          <p class="th-label">Available Treasury</p>
          <p class="th-amount gradient-gold">{formatIDR($vaultStore.treasury.availableTreasury)}</p>
          <div class="th-stats">
            <span>Tersegel: {formatIDRCompact($vaultStore.treasury.sealedTreasury)}</span>
            <span class="sep">·</span>
            <span>Total: {formatIDRCompact($vaultStore.treasury.totalTreasury)}</span>
          </div>
        </div>
        <Wallet size={40} color="var(--gold-400)" style="opacity:0.5" />
      </div>

      <div class="th-actions">
        <button class="action-btn" onclick={() => modal = 'offering'}>
          <ArrowDownLeft size={18} color="var(--success)" />
          Top-Up
        </button>
        <button class="action-btn" onclick={() => modal = 'withdrawal'}>
          <ArrowUpRight size={18} color="var(--danger)" />
          Tarik Dana
        </button>
        <button class="action-btn" onclick={() => modal = 'tithe'}>
          <Send size={18} color="var(--void-400)" />
          Kirim (Tithe)
        </button>
      </div>
    </div>
  {/if}

  <!-- Chronicles -->
  <div class="card">
    <div class="card-header">
      <h2 class="text-h3">Chronicle Transaksi</h2>
    </div>

    {#if $vaultStore.chronicles.length === 0}
      <div class="empty-state">
        <Wallet size={40} color="var(--text-muted)" />
        <p>Belum ada riwayat transaksi</p>
      </div>
    {:else}
      <div class="chronicle-table">
        {#each $vaultStore.chronicles as c}
          <div class="chronicle-row">
            <div class="cr-type">
              <span class="cr-dot" style="background:{chronicleColor(c.type)}"></span>
              <div>
                <p class="text-sm font-medium">{chronicleLabel(c.type)}</p>
                {#if c.counterpartyName}
                  <p class="text-xs text-muted">{c.counterpartyName}</p>
                {/if}
              </div>
            </div>
            <div class="cr-status">
              <span class="badge {statusBadgeClass(c.status)}">{c.status}</span>
            </div>
            <p class="text-xs text-muted">{formatDateTime(c.createdAt)}</p>
            <p class="cr-amount" style="color:{chronicleColor(c.type)}">
              {chronicleSign(c.type)}{formatIDRCompact(c.amount)}
            </p>
          </div>
        {/each}
      </div>
    {/if}
  </div>
</div>

<!-- MODALS -->
{#if modal}
  <div class="modal-backdrop" onclick={() => modal = null} role="dialog" aria-modal="true">
    <div class="modal-box animate-fade-scale" onclick={(e) => e.stopPropagation()}>
      <div class="modal-header">
        <h3 class="modal-title">
          {modal === 'offering' ? 'Top-Up Treasury' : modal === 'withdrawal' ? 'Tarik Dana' : 'Kirim (Tithe)'}
        </h3>
        <button class="icon-btn" onclick={() => modal = null}><X size={18} /></button>
      </div>

      {#if modal === 'offering'}
        <form class="modal-form" onsubmit={(e) => { e.preventDefault(); submitOffering(); }}>
          <div class="form-group">
            <label class="form-label" for="offerAmount">Jumlah (IDR)</label>
            <input id="offerAmount" type="number" min="10000" placeholder="50000" bind:value={offerAmount} required />
          </div>
          <div class="form-group">
            <label class="form-label" for="offerMethod">Metode</label>
            <select id="offerMethod" bind:value={offerMethod}>
              <option value="VIRTUAL_ACCOUNT">Virtual Account</option>
              <option value="E_WALLET">E-Wallet</option>
            </select>
          </div>
          {#if offerMethod === 'VIRTUAL_ACCOUNT'}
            <div class="form-group">
              <label class="form-label" for="offerBank">Bank</label>
              <select id="offerBank" bind:value={offerBank}>
                {#each ['BCA','BNI','BRI','MANDIRI','PERMATA'] as b}
                  <option value={b}>{b}</option>
                {/each}
              </select>
            </div>
          {/if}
          <button type="submit" class="btn btn-gold btn-full" disabled={submitting}>
            {submitting ? 'Processing…' : 'Konfirmasi Top-Up'}
          </button>
        </form>

      {:else if modal === 'withdrawal'}
        <form class="modal-form" onsubmit={(e) => { e.preventDefault(); submitWithdrawal(); }}>
          <div class="form-group">
            <label class="form-label" for="wdAmount">Jumlah (IDR)</label>
            <input id="wdAmount" type="number" min="10000" bind:value={wdAmount} required />
          </div>
          <div class="form-group">
            <label class="form-label" for="wdBank">Bank</label>
            <select id="wdBank" bind:value={wdBank}>
              {#each ['BCA','BNI','BRI','MANDIRI','PERMATA'] as b}
                <option value={b}>{b}</option>
              {/each}
            </select>
          </div>
          <div class="form-group">
            <label class="form-label" for="wdAccNum">No. Rekening</label>
            <input id="wdAccNum" type="text" placeholder="1234567890" bind:value={wdAccNum} required />
          </div>
          <div class="form-group">
            <label class="form-label" for="wdAccName">Nama Rekening</label>
            <input id="wdAccName" type="text" placeholder="Nama pemilik rekening" bind:value={wdAccName} required />
          </div>
          <div class="form-group">
            <label class="form-label" for="wdPin">PIN (6 digit)</label>
            <input id="wdPin" type="password" maxlength={6} inputmode="numeric" bind:value={wdPin} required />
          </div>
          <button type="submit" class="btn btn-primary btn-full" disabled={submitting}>
            {submitting ? 'Processing…' : 'Ajukan Penarikan'}
          </button>
        </form>

      {:else}
        <form class="modal-form" onsubmit={(e) => { e.preventDefault(); submitTithe(); }}>
          <div class="form-group">
            <label class="form-label" for="tithePhone">Nomor Penerima</label>
            <input id="tithePhone" type="tel" placeholder="+62812..." bind:value={tithePhone} required />
          </div>
          <div class="form-group">
            <label class="form-label" for="titheAmt">Jumlah (IDR)</label>
            <input id="titheAmt" type="number" min="1000" bind:value={titheAmount} required />
          </div>
          <div class="form-group">
            <label class="form-label" for="titheNote">Catatan <span class="text-muted">(opsional)</span></label>
            <input id="titheNote" type="text" placeholder="..." bind:value={titheNote} />
          </div>
          <div class="form-group">
            <label class="form-label" for="tithePin">PIN</label>
            <input id="tithePin" type="password" maxlength={6} inputmode="numeric" bind:value={tithePin} required />
          </div>
          <button type="submit" class="btn btn-primary btn-full" disabled={submitting}>
            {submitting ? 'Processing…' : 'Kirim Tithe'}
          </button>
        </form>
      {/if}
    </div>
  </div>
{/if}
{/if}

<style>
.vault { display: flex; flex-direction: column; gap: 1.5rem; }

.treasury-hero {
  background: linear-gradient(135deg, var(--surface-800), var(--surface-700));
  border-radius: var(--radius-xl); padding: 1.75rem;
}

.th-main { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 1.5rem; }
.th-label { font-size: 0.8125rem; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.08em; margin-bottom: 0.5rem; }
.th-amount { font-size: 2.25rem; font-weight: 800; line-height: 1; margin-bottom: 0.5rem; }
.th-stats { display: flex; gap: 0.5rem; font-size: 0.8125rem; color: var(--text-muted); }
.sep { opacity: 0.4; }

.th-actions { display: flex; gap: 0.75rem; }
.action-btn {
  display: flex; align-items: center; gap: 0.5rem;
  padding: 0.625rem 1rem; background: var(--surface-600);
  border-radius: var(--radius-md); border: 1px solid var(--border-subtle);
  font-size: 0.875rem; font-weight: 600; color: var(--text-primary);
  transition: all var(--transition-fast); cursor: pointer;
}
.action-btn:hover { background: var(--surface-500); }

.card-header { margin-bottom: 1.25rem; }
.font-medium { font-weight: 500; }

.chronicle-table { display: flex; flex-direction: column; gap: 0; }
.chronicle-row {
  display: grid; grid-template-columns: 1fr auto auto auto;
  align-items: center; gap: 1rem;
  padding: 0.875rem 0; border-bottom: 1px solid var(--border-subtle);
}
.chronicle-row:last-child { border-bottom: none; }

.cr-type { display: flex; align-items: center; gap: 0.625rem; }
.cr-dot  { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.cr-amount { font-weight: 700; font-size: 0.875rem; text-align: right; }

.empty-state { display: flex; flex-direction: column; align-items: center; gap: 0.75rem; padding: 3rem; color: var(--text-muted); font-size: 0.875rem; }

/* Modal */
.modal-backdrop {
  position: fixed; inset: 0; z-index: 100;
  background: rgba(0,0,0,0.7); backdrop-filter: blur(8px);
  display: flex; align-items: center; justify-content: center;
  padding: 1rem;
}
.modal-box {
  background: var(--surface-800); border: 1px solid var(--border-default);
  border-radius: var(--radius-2xl); padding: 2rem; width: 100%; max-width: 440px;
  max-height: 90vh; overflow-y: auto;
}
.modal-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 1.5rem; }
.modal-title  { font-size: 1.125rem; font-weight: 600; }
.modal-form   { display: flex; flex-direction: column; gap: 1.25rem; }
.icon-btn {
  width: 36px; height: 36px; display: flex; align-items: center; justify-content: center;
  background: var(--surface-700); border-radius: var(--radius-md); color: var(--text-muted);
}

@media (max-width: 640px) {
  .th-actions { flex-direction: column; }
  .chronicle-row { grid-template-columns: 1fr auto; }
  .cr-status, .chronicle-row > p.text-xs { display: none; }
}
</style>

