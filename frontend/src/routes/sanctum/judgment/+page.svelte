<script lang="ts">
  import { onMount } from 'svelte';
  import { judgmentApi } from '$lib/api/judgment';
  import { formatIDR, formatRelative } from '$lib/utils/formatters';
  import { toast } from 'svelte-sonner';
  import { Scale } from "@lucide/svelte";
  import LoadingCrystal from '$lib/components/shared/LoadingCrystal.svelte';
  import type { JudgmentCase } from '$lib/types/judgment';

  let loading = $state(true);
  let cases   = $state<JudgmentCase[]>([]);

  onMount(async () => {
    try { cases = await judgmentApi.getMyCases(); }
    catch { toast.error('Gagal memuat kasus.'); }
    finally { loading = false; }
  });

  const statusLabel: Record<string, string> = {
    OPEN: 'Terbuka', DELIBERATING: 'Sedang Ditinjau', RENDERED: 'Selesai'
  };
  const statusColor: Record<string, string> = {
    OPEN: 'var(--warning)', DELIBERATING: 'var(--info)', RENDERED: 'var(--success)'
  };
</script>

<svelte:head>
  <title>Judgment Sanctum — EyesOfPriestess</title>
</svelte:head>

{#if loading}
  <LoadingCrystal />
{:else}
<div class="judgment-page animate-fade-in">
  <div class="page-header">
    <div>
      <h2 class="text-h2">Judgment Sanctum</h2>
      <p class="text-sm text-muted">Kasus sengketa aktif dan riwayat penyelesaian</p>
    </div>
  </div>

  {#if cases.length === 0}
    <div class="empty-card">
      <Scale size={48} color="var(--text-muted)" />
      <p class="empty-title">Tidak Ada Sengketa</p>
      <p class="text-sm text-muted">Semua Covenant Anda berjalan lancar. Priestess bersabda baik.</p>
    </div>
  {:else}
    <div class="cases-list">
      {#each cases as c}
        <a href="/sanctum/covenant/{c.covenantId}" class="case-card card">
          <div class="case-header">
            <div class="case-status" style="color:{statusColor[c.status]}">
              <div class="status-dot" style="background:{statusColor[c.status]}"></div>
              {statusLabel[c.status] ?? c.status}
            </div>
            <span class="text-xs text-muted">{formatRelative(c.openedAt)}</span>
          </div>
          <p class="case-reason">{c.reason}</p>
          {#if c.resolution}
            <div class="resolution-badge">
              ✓ Resolusi: {c.resolution === 'RELEASE' ? 'Dana Dilepas ke Seller' : c.resolution === 'REFUND' ? 'Dana Dikembalikan' : 'Dana Dibagi'}
            </div>
          {/if}
          {#if c.oracleNotes}
            <p class="text-sm text-muted italic">"{c.oracleNotes}"</p>
          {/if}
        </a>
      {/each}
    </div>
  {/if}
</div>
{/if}

<style>
.judgment-page { display: flex; flex-direction: column; gap: 1.5rem; }
.page-header { display: flex; align-items: flex-start; justify-content: space-between; flex-wrap: wrap; }

.empty-card {
  display: flex; flex-direction: column; align-items: center; gap: 1rem;
  padding: 4rem 2rem; text-align: center;
  background: var(--surface-800); border-radius: var(--radius-xl);
  border: 1px dashed var(--border-default);
}
.empty-title { font-size: 1.125rem; font-weight: 600; }

.cases-list { display: flex; flex-direction: column; gap: 1rem; }
.case-card  { display: flex; flex-direction: column; gap: 0.75rem; text-decoration: none; }
.case-card:hover { border-color: var(--border-strong); }

.case-header { display: flex; align-items: center; justify-content: space-between; }
.case-status { display: flex; align-items: center; gap: 0.375rem; font-size: 0.8125rem; font-weight: 600; }
.status-dot  { width: 8px; height: 8px; border-radius: 50%; }
.case-reason { font-weight: 500; }

.resolution-badge {
  display: inline-flex; padding: 0.25rem 0.75rem;
  background: var(--success-bg); border: 1px solid rgba(16,185,129,0.3);
  border-radius: var(--radius-full); font-size: 0.75rem; font-weight: 600; color: var(--success);
  width: fit-content;
}
</style>

