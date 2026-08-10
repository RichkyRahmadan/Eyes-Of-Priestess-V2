<script lang="ts">
  import { onMount } from 'svelte';
  import { covenantStore } from '$lib/stores/covenant';
  import { covenantApi } from '$lib/api/covenant';
  import { formatIDRCompact, formatRelative, statusBadgeClass, covenantStatusLabel } from '$lib/utils/formatters';
  import { toast } from 'svelte-sonner';
  import { Plus, Handshake, X } from "@lucide/svelte";
  import LoadingCrystal from '$lib/components/shared/LoadingCrystal.svelte';

  let loading       = $state(true);
  let showJoin      = $state(false);
  let joinCode      = $state('');
  let joining       = $state(false);
  let filterStatus  = $state('ALL');

  onMount(async () => {
    try {
      const res = await covenantApi.getAll();
      covenantStore.setCovenants(res ?? []);
    } catch { toast.error('Gagal memuat covenant.'); }
    finally { loading = false; }
  });

  const filtered = $derived(
    filterStatus === 'ALL'
      ? $covenantStore.covenants
      : $covenantStore.covenants.filter(c => c.status === filterStatus)
  );

  async function joinCovenant() {
    if (!joinCode.trim()) return;
    joining = true;
    try {
      const res = await covenantApi.accept(joinCode.trim().toUpperCase());
      toast.success('Covenant berhasil diterima!');
      covenantStore.setCovenants([res, ...$covenantStore.covenants]);
      showJoin = false; joinCode = '';
    } catch (e: unknown) { toast.error(e instanceof Error ? e.message : 'Kode tidak valid.'); }
    finally { joining = false; }
  }

  const statuses = ['ALL', 'FORGED', 'ACCEPTED', 'SEALED', 'DELIVERED', 'FULFILLED', 'DISPUTED', 'CANCELLED'];
</script>

<svelte:head>
  <title>Covenant Hall — EyesOfPriestess</title>
</svelte:head>

{#if loading}
  <LoadingCrystal />
{:else}
<div class="covenant-page animate-fade-in">
  <!-- Header -->
  <div class="page-header">
    <div>
      <h2 class="text-h2">Covenant Hall</h2>
      <p class="text-sm text-muted">Kelola semua perjanjian escrow Anda</p>
    </div>
    <div class="header-actions">
      <button class="btn btn-ghost btn-sm" onclick={() => (showJoin = true)}>
        Bergabung
      </button>
      <a href="/sanctum/covenant/forge" class="btn btn-gold btn-sm">
        <Plus size={16} /> Forge Covenant
      </a>
    </div>
  </div>

  <!-- Filters -->
  <div class="filter-bar">
    {#each statuses as s}
      <button
        class="filter-btn"
        class:active={filterStatus === s}
        onclick={() => (filterStatus = s)}
      >
        {s === 'ALL' ? 'Semua' : covenantStatusLabel(s)}
      </button>
    {/each}
  </div>

  <!-- List -->
  {#if filtered.length === 0}
    <div class="empty-card">
      <Handshake size={48} color="var(--text-muted)" />
      <p class="empty-title">Belum ada Covenant</p>
      <p class="text-sm text-muted">Forge covenant pertama Anda atau bergabung via kode.</p>
      <a href="/sanctum/covenant/forge" class="btn btn-gold btn-sm">
        <Plus size={16} /> Forge Covenant
      </a>
    </div>
  {:else}
    <div class="covenant-grid">
      {#each filtered as cov}
        <a href="/sanctum/covenant/{cov.id}" class="covenant-card card">
          <div class="cc-header">
            <span class="badge {statusBadgeClass(cov.status)}">{covenantStatusLabel(cov.status)}</span>
            <span class="text-xs text-muted">{formatRelative(cov.createdAt)}</span>
          </div>
          <h3 class="cc-title">{cov.title}</h3>
          {#if cov.description}
            <p class="cc-desc text-sm text-muted">{cov.description}</p>
          {/if}
          <div class="cc-footer">
            <div class="cc-role">
              <span class="role-badge" class:buyer={cov.initiatorId === cov.buyerId}>
                {cov.initiatorRole === 'BUYER' ? '🛒 Buyer' : '📦 Seller'}
              </span>
            </div>
            <p class="cc-amount gradient-gold">{formatIDRCompact(cov.amount)}</p>
          </div>
        </a>
      {/each}
    </div>
  {/if}
</div>

<!-- Join Modal -->
{#if showJoin}
  <div class="modal-backdrop" onclick={() => (showJoin = false)} role="dialog" aria-modal="true">
    <div class="modal-box animate-fade-scale" onclick={(e) => e.stopPropagation()}>
      <div class="modal-header">
        <h3 class="modal-title">Bergabung ke Covenant</h3>
        <button class="icon-btn" onclick={() => (showJoin = false)}><X size={18} /></button>
      </div>
      <form onsubmit={(e) => { e.preventDefault(); joinCovenant(); }} class="modal-form">
        <div class="form-group">
          <label class="form-label" for="joinCode">Kode Undangan</label>
          <input
            id="joinCode"
            type="text"
            placeholder="e.g. ABC12345"
            bind:value={joinCode}
            style="text-transform:uppercase; letter-spacing:0.1em; text-align:center; font-size:1.25rem; font-weight:700;"
            required
          />
          <span class="form-hint">Minta kode undangan dari Initiator Covenant</span>
        </div>
        <button type="submit" class="btn btn-primary btn-full" disabled={joining}>
          {joining ? 'Bergabung…' : 'Masuk ke Covenant'}
        </button>
      </form>
    </div>
  </div>
{/if}
{/if}

<style>
.covenant-page { display: flex; flex-direction: column; gap: 1.5rem; }

.page-header {
  display: flex; align-items: flex-start; justify-content: space-between; gap: 1rem; flex-wrap: wrap;
}
.header-actions { display: flex; gap: 0.75rem; }

.filter-bar {
  display: flex; gap: 0.5rem; flex-wrap: wrap;
  padding: 0.5rem;
  background: var(--surface-800);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-subtle);
}

.filter-btn {
  padding: 0.375rem 0.875rem; border-radius: var(--radius-md);
  font-size: 0.8125rem; font-weight: 500; color: var(--text-muted);
  transition: all var(--transition-fast); cursor: pointer;
}
.filter-btn:hover  { color: var(--text-primary); background: var(--surface-700); }
.filter-btn.active { background: rgba(139,92,246,0.15); color: var(--void-300); }

.empty-card {
  display: flex; flex-direction: column; align-items: center; gap: 1rem;
  padding: 4rem 2rem; text-align: center;
  background: var(--surface-800); border-radius: var(--radius-xl);
  border: 1px dashed var(--border-default);
}
.empty-title { font-size: 1.125rem; font-weight: 600; }

.covenant-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1rem;
}

.covenant-card { display: flex; flex-direction: column; gap: 0.75rem; text-decoration: none; }
.covenant-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-glow); }

.cc-header { display: flex; align-items: center; justify-content: space-between; }
.cc-title  { font-size: 1rem; font-weight: 600; }
.cc-desc   { line-height: 1.5; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.cc-footer { display: flex; align-items: center; justify-content: space-between; margin-top: auto; }
.cc-amount { font-size: 1.125rem; font-weight: 800; }

.role-badge {
  font-size: 0.75rem; font-weight: 600; padding: 0.2rem 0.6rem;
  border-radius: var(--radius-full); background: rgba(139,92,246,0.12);
  color: var(--void-300); border: 1px solid rgba(139,92,246,0.2);
}

.modal-backdrop {
  position: fixed; inset: 0; z-index: 100;
  background: rgba(0,0,0,0.7); backdrop-filter: blur(8px);
  display: flex; align-items: center; justify-content: center; padding: 1rem;
}
.modal-box {
  background: var(--surface-800); border: 1px solid var(--border-default);
  border-radius: var(--radius-2xl); padding: 2rem; width: 100%; max-width: 400px;
}
.modal-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 1.5rem; }
.modal-title  { font-size: 1.125rem; font-weight: 600; }
.modal-form   { display: flex; flex-direction: column; gap: 1.25rem; }
.icon-btn {
  width: 36px; height: 36px; display: flex; align-items: center; justify-content: center;
  background: var(--surface-700); border-radius: var(--radius-md); color: var(--text-muted);
}
</style>

