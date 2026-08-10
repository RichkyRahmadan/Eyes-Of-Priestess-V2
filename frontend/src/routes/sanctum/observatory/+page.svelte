<script lang="ts">
  import { onMount } from 'svelte';
  import { sealStore } from '$lib/stores/seal';
  import { vaultStore } from '$lib/stores/vault';
  import { covenantStore } from '$lib/stores/covenant';
  import { vaultApi } from '$lib/api/vault';
  import { covenantApi } from '$lib/api/covenant';
  import { formatIDR, formatIDRCompact, formatRelative, statusBadgeClass, covenantStatusLabel } from '$lib/utils/formatters';
  import { Wallet, Handshake, TrendingUp, Plus, ArrowUpRight, ArrowDownLeft, Send, RefreshCw } from "@lucide/svelte";
  import LoadingCrystal from '$lib/components/shared/LoadingCrystal.svelte';

  let loading = $state(true);

  onMount(async () => {
    try {
      const [treasury, chronicles, covenants] = await Promise.all([
        vaultApi.getTreasury(),
        vaultApi.getChronicles(0, 5),
        covenantApi.getAll(0, 5)
      ]);
      vaultStore.setTreasury(treasury);
      vaultStore.setChronicles(chronicles.content ?? []);
      covenantStore.setCovenants(covenants ?? []);
    } catch { /* silent */ }
    finally { loading = false; }
  });

  const pilgrim = $derived($sealStore.pilgrim);
  const vault   = $derived($vaultStore);
  const covenants = $derived($covenantStore.covenants.slice(0, 5));

  const quickActions = [
    { href: '/sanctum/vault?action=offering',   icon: ArrowDownLeft, label: 'Top-Up',     color: 'var(--success)' },
    { href: '/sanctum/vault?action=withdrawal', icon: ArrowUpRight,  label: 'Tarik',       color: 'var(--danger)'  },
    { href: '/sanctum/vault?action=tithe',      icon: Send,          label: 'Kirim',       color: 'var(--void-400)'},
    { href: '/sanctum/covenant/forge',          icon: Plus,          label: 'New Covenant', color: 'var(--gold-500)' }
  ];
</script>

<svelte:head>
  <title>Observatory — EyesOfPriestess</title>
</svelte:head>

{#if loading}
  <LoadingCrystal />
{:else}
<div class="observatory animate-fade-in">
  <!-- Welcome banner -->
  <div class="welcome-banner">
    <div class="welcome-text">
      <p class="welcome-eyebrow">The Priestess greets you</p>
      <h2 class="welcome-name">Welcome, {pilgrim?.fullName ?? 'Pilgrim'}</h2>
    </div>
    <div class="covenant-score-badge">
      <TrendingUp size={14} />
      <span>Score {pilgrim?.covenantScore ?? 0}</span>
    </div>
  </div>

  <!-- Treasury Card -->
  {#if vault.treasury}
    <div class="treasury-card glow-border-gold">
      <div class="treasury-inner">
        <div>
          <p class="treasury-label">Available Treasury</p>
          <p class="treasury-amount gradient-gold">{formatIDR(vault.treasury.availableTreasury)}</p>
          <p class="treasury-sub">
            Sealed: {formatIDRCompact(vault.treasury.sealedTreasury)} &nbsp;|&nbsp;
            Total: {formatIDRCompact(vault.treasury.totalTreasury)}
          </p>
        </div>
        <div class="treasury-icon">
          <Wallet size={32} color="var(--gold-400)" />
        </div>
      </div>

      <!-- Quick Actions -->
      <div class="quick-actions">
        {#each quickActions as qa}
          <a href={qa.href} class="quick-action">
            <div class="qa-icon" style="color:{qa.color}; background: color-mix(in srgb, {qa.color} 12%, transparent)">
              <qa.icon size={18} />
            </div>
            <span class="qa-label">{qa.label}</span>
          </a>
        {/each}
      </div>
    </div>
  {/if}

  <!-- Two-column lower section -->
  <div class="lower-grid">
    <!-- Recent Chronicles -->
    <div class="card">
      <div class="card-header">
        <h3 class="card-title">Recent Chronicles</h3>
        <a href="/sanctum/vault" class="text-sm text-muted">Lihat semua →</a>
      </div>
      {#if vault.chronicles.length === 0}
        <p class="empty-msg">Belum ada riwayat transaksi.</p>
      {:else}
        <div class="chronicle-list">
          {#each vault.chronicles as c}
            <div class="chronicle-item">
              <div class="chronicle-info">
                <p class="chronicle-type text-sm">{c.type.replace('_', ' ')}</p>
                <p class="text-xs text-muted">{formatRelative(c.createdAt)}</p>
              </div>
              <div class="chronicle-amount" class:credit={c.type === 'OFFERING' || c.type === 'TITHING_IN' || c.type === 'SEAL_RELEASE'}>
                {c.type === 'OFFERING' || c.type === 'TITHING_IN' || c.type === 'SEAL_RELEASE' ? '+' : '-'}{formatIDRCompact(c.amount)}
              </div>
            </div>
          {/each}
        </div>
      {/if}
    </div>

    <!-- Active Covenants -->
    <div class="card">
      <div class="card-header">
        <h3 class="card-title">Active Covenants</h3>
        <a href="/sanctum/covenant" class="text-sm text-muted">Lihat semua →</a>
      </div>
      {#if covenants.length === 0}
        <p class="empty-msg">Belum ada covenant aktif.</p>
      {:else}
        <div class="covenant-list">
          {#each covenants as cov}
            <a href="/sanctum/covenant/{cov.id}" class="covenant-item">
              <div class="covenant-info">
                <p class="text-sm font-medium truncate">{cov.title}</p>
                <p class="text-xs text-muted">{formatIDRCompact(cov.amount)}</p>
              </div>
              <span class="badge {statusBadgeClass(cov.status)}">{covenantStatusLabel(cov.status)}</span>
            </a>
          {/each}
        </div>
      {/if}
    </div>
  </div>
</div>
{/if}

<style>
.observatory { display: flex; flex-direction: column; gap: 1.5rem; }

.welcome-banner {
  display: flex; align-items: center; justify-content: space-between;
}
.welcome-eyebrow { font-size: 0.75rem; color: var(--gold-400); text-transform: uppercase; letter-spacing: 0.1em; font-weight: 600; margin-bottom: 0.25rem; }
.welcome-name    { font-size: 1.5rem; font-family: 'Cinzel', serif; }

.covenant-score-badge {
  display: flex; align-items: center; gap: 0.375rem;
  padding: 0.375rem 0.875rem;
  background: rgba(16,185,129,0.1); border: 1px solid rgba(16,185,129,0.25);
  border-radius: var(--radius-full); color: var(--success); font-size: 0.8125rem; font-weight: 700;
}

.treasury-card {
  background: linear-gradient(135deg, var(--surface-800), var(--surface-700));
  border-radius: var(--radius-xl);
  padding: 1.75rem;
}

.treasury-inner { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 1.5rem; }
.treasury-label { font-size: 0.8125rem; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.08em; margin-bottom: 0.5rem; }
.treasury-amount { font-size: 2rem; font-weight: 800; line-height: 1; margin-bottom: 0.375rem; }
.treasury-sub    { font-size: 0.8125rem; color: var(--text-muted); }
.treasury-icon   { opacity: 0.7; }

.quick-actions { display: flex; gap: 0.75rem; flex-wrap: wrap; }
.quick-action {
  display: flex; flex-direction: column; align-items: center; gap: 0.375rem;
  padding: 0.75rem 1rem; background: var(--surface-700); border-radius: var(--radius-lg);
  text-decoration: none; transition: all var(--transition-fast); flex: 1; min-width: 80px;
  border: 1px solid var(--border-subtle);
}
.quick-action:hover { background: var(--surface-600); transform: translateY(-2px); }
.qa-icon {
  width: 40px; height: 40px; border-radius: var(--radius-md);
  display: flex; align-items: center; justify-content: center;
}
.qa-label { font-size: 0.75rem; font-weight: 600; color: var(--text-secondary); }

.lower-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; }
@media (max-width: 768px) { .lower-grid { grid-template-columns: 1fr; } }

.card-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 1rem; }
.card-title  { font-size: 0.9375rem; font-weight: 600; }
.empty-msg   { font-size: 0.875rem; color: var(--text-muted); text-align: center; padding: 1.5rem 0; }

.chronicle-list { display: flex; flex-direction: column; gap: 0.625rem; }
.chronicle-item { display: flex; align-items: center; justify-content: space-between; padding: 0.5rem 0; border-bottom: 1px solid var(--border-subtle); }
.chronicle-item:last-child { border-bottom: none; }
.chronicle-type { font-weight: 500; }
.chronicle-amount { font-size: 0.875rem; font-weight: 700; color: var(--danger); }
.chronicle-amount.credit { color: var(--success); }

.covenant-list { display: flex; flex-direction: column; gap: 0.5rem; }
.covenant-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 0.625rem 0.75rem; border-radius: var(--radius-md);
  background: var(--surface-700); text-decoration: none;
  transition: background var(--transition-fast);
}
.covenant-item:hover { background: var(--surface-600); }
.font-medium { font-weight: 500; }
.truncate { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 150px; }
</style>

