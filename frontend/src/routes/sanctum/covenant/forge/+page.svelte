<script lang="ts">
  import { goto } from '$app/navigation';
  import { covenantApi } from '$lib/api/covenant';
  import { toast } from 'svelte-sonner';
  import { ArrowLeft, ArrowRight, Check } from "@lucide/svelte";

  let step      = $state(1);
  let title     = $state('');
  let desc      = $state('');
  let role      = $state<'BUYER' | 'SELLER'>('BUYER');
  let amount    = $state('');
  let autoHours = $state('48');
  let loading   = $state(false);
  let result    = $state<{ invitationCode?: string; id: string } | null>(null);

  async function forge() {
    loading = true;
    try {
      const res = await covenantApi.forge({
        title, description: desc || undefined,
        initiatorRole: role, amount: +amount,
        autoReleaseHours: +autoHours
      });
      result = res;
      step = 4;
      toast.success('Covenant berhasil ditempa!');
    } catch (e: unknown) { toast.error(e instanceof Error ? e.message : 'Gagal.'); }
    finally { loading = false; }
  }

  function next() {
    if (step === 1 && !title) return;
    if (step === 2 && (!amount || +amount < 1000)) return;
    step++;
  }
</script>

<svelte:head>
  <title>Forge Covenant — EyesOfPriestess</title>
</svelte:head>

<div class="forge-page animate-fade-in">
  <div class="forge-header">
    <a href="/sanctum/covenant" class="btn btn-ghost btn-sm">
      <ArrowLeft size={16} /> Kembali
    </a>
    <h2 class="text-h2">Forge New Covenant</h2>
    <div class="prog-steps">
      {#each [1,2,3,4] as s}
        <div class="prog-step" class:active={s <= step} class:done={s < step || step === 4}>
          {#if s < step || step === 4}
            <Check size={12} />
          {:else}
            {s}
          {/if}
        </div>
        {#if s < 4}
          <div class="prog-line" class:active={s < step}></div>
        {/if}
      {/each}
    </div>
  </div>

  <div class="forge-card card">
    {#if step === 1}
      <!-- Details -->
      <div class="forge-step animate-fade-in">
        <p class="step-eyebrow">Step 1 — Detail Covenant</p>
        <div class="form-group">
          <label class="form-label" for="title">Judul Covenant *</label>
          <input id="title" type="text" placeholder="Jual beli item rare game..." bind:value={title} required />
        </div>
        <div class="form-group">
          <label class="form-label" for="desc">Deskripsi <span class="text-muted">(opsional)</span></label>
          <textarea id="desc" placeholder="Detail barang/jasa..." bind:value={desc} rows={3} style="resize:vertical;"></textarea>
        </div>
        <div class="form-group">
          <label class="form-label">Peran Saya</label>
          <div class="role-selector">
            <button
              class="role-btn" class:selected={role === 'BUYER'}
              onclick={() => role = 'BUYER'}
            >
              <span class="role-emoji">🛒</span>
              <p class="role-title">Buyer</p>
              <p class="text-xs text-muted">Saya yang membayar</p>
            </button>
            <button
              class="role-btn" class:selected={role === 'SELLER'}
              onclick={() => role = 'SELLER'}
            >
              <span class="role-emoji">📦</span>
              <p class="role-title">Seller</p>
              <p class="text-xs text-muted">Saya yang menjual</p>
            </button>
          </div>
        </div>
        <button class="btn btn-primary btn-full" onclick={next} disabled={!title}>
          Lanjut <ArrowRight size={18} />
        </button>
      </div>

    {:else if step === 2}
      <!-- Amount -->
      <div class="forge-step animate-fade-in">
        <p class="step-eyebrow">Step 2 — Nilai Covenant</p>
        <div class="form-group">
          <label class="form-label" for="amount">Nilai Transaksi (IDR) *</label>
          <input id="amount" type="number" min="1000" placeholder="100000" bind:value={amount} required />
          <span class="form-hint">Minimum Rp 1.000</span>
        </div>
        <div class="form-group">
          <label class="form-label" for="autoH">Auto-Release (jam)</label>
          <select id="autoH" bind:value={autoHours}>
            <option value="24">24 jam</option>
            <option value="48">48 jam (default)</option>
            <option value="72">72 jam</option>
            <option value="168">7 hari</option>
          </select>
          <span class="form-hint">Dana otomatis dilepas jika Buyer tidak merespons setelah Seller mengirim.</span>
        </div>
        <div class="btn-row">
          <button class="btn btn-ghost" onclick={() => step--}><ArrowLeft size={18} /> Kembali</button>
          <button class="btn btn-primary flex-1" onclick={next} disabled={!amount || +amount < 1000}>
            Lanjut <ArrowRight size={18} />
          </button>
        </div>
      </div>

    {:else if step === 3}
      <!-- Confirm -->
      <div class="forge-step animate-fade-in">
        <p class="step-eyebrow">Step 3 — Konfirmasi</p>
        <div class="summary-card">
          <div class="summary-row">
            <span class="text-muted text-sm">Judul</span>
            <span class="text-sm font-semibold">{title}</span>
          </div>
          <div class="summary-row">
            <span class="text-muted text-sm">Peran</span>
            <span class="text-sm font-semibold">{role === 'BUYER' ? '🛒 Buyer' : '📦 Seller'}</span>
          </div>
          <div class="summary-row">
            <span class="text-muted text-sm">Nilai</span>
            <span class="text-sm font-semibold gradient-gold">Rp {Number(amount).toLocaleString('id-ID')}</span>
          </div>
          <div class="summary-row">
            <span class="text-muted text-sm">Auto-Release</span>
            <span class="text-sm font-semibold">{autoHours} jam</span>
          </div>
        </div>
        <div class="btn-row">
          <button class="btn btn-ghost" onclick={() => step--}><ArrowLeft size={18} /> Kembali</button>
          <button class="btn btn-gold flex-1 btn-lg" onclick={forge} disabled={loading}>
            {loading ? 'Forging…' : '✦ Forge Covenant'}
          </button>
        </div>
      </div>

    {:else}
      <!-- Success -->
      <div class="forge-step forge-success animate-fade-in">
        <div class="success-icon">✦</div>
        <h3 class="text-h2 text-center">Covenant Ditempa!</h3>
        <p class="text-center text-sm text-muted">Bagikan kode undangan ini ke counterparty Anda.</p>
        {#if result?.invitationCode}
          <div class="code-box">
            <p class="code-label">Kode Undangan</p>
            <p class="code-value gradient-gold">{result.invitationCode}</p>
          </div>
        {/if}
        <div class="success-actions">
          <a href="/sanctum/covenant/{result?.id}" class="btn btn-primary btn-full">
            Lihat Covenant
          </a>
          <a href="/sanctum/covenant" class="btn btn-ghost btn-full">
            Kembali ke Covenant Hall
          </a>
        </div>
      </div>
    {/if}
  </div>
</div>

<style>
.forge-page { display: flex; flex-direction: column; gap: 1.5rem; max-width: 560px; margin: 0 auto; }

.forge-header { display: flex; flex-direction: column; gap: 1rem; }
.prog-steps { display: flex; align-items: center; gap: 0; }
.prog-step {
  width: 28px; height: 28px; border-radius: 50%;
  background: var(--surface-600); display: flex; align-items: center; justify-content: center;
  font-size: 0.75rem; font-weight: 700; color: var(--text-muted);
  transition: all var(--transition-normal);
}
.prog-step.active { background: var(--void-600); color: #fff; }
.prog-step.done   { background: var(--gold-500); color: var(--void-950); }
.prog-line { flex: 1; height: 2px; background: var(--surface-600); min-width: 24px; transition: background var(--transition-normal); }
.prog-line.active { background: var(--void-500); }

.forge-step { display: flex; flex-direction: column; gap: 1.25rem; }
.step-eyebrow { font-size: 0.75rem; font-weight: 700; letter-spacing: 0.12em; text-transform: uppercase; color: var(--gold-400); }

.role-selector { display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; }
.role-btn {
  display: flex; flex-direction: column; align-items: center; gap: 0.375rem;
  padding: 1.25rem; border-radius: var(--radius-lg);
  background: var(--surface-700); border: 2px solid var(--border-subtle);
  cursor: pointer; transition: all var(--transition-fast); text-align: center;
}
.role-btn:hover  { border-color: var(--border-default); }
.role-btn.selected { border-color: var(--void-500); background: rgba(139,92,246,0.1); }
.role-emoji { font-size: 1.75rem; line-height: 1; }
.role-title { font-size: 0.875rem; font-weight: 700; color: var(--text-primary); }

.summary-card {
  background: var(--surface-700); border-radius: var(--radius-lg);
  border: 1px solid var(--border-subtle); padding: 1rem;
  display: flex; flex-direction: column; gap: 0.75rem;
}
.summary-row { display: flex; justify-content: space-between; align-items: center; }
.font-semibold { font-weight: 600; }

.btn-row { display: flex; gap: 0.75rem; }
.flex-1  { flex: 1; }

.forge-success { align-items: center; text-align: center; gap: 1.5rem; }
.success-icon {
  width: 72px; height: 72px; border-radius: 50%;
  background: linear-gradient(135deg, var(--gold-500), var(--gold-600));
  display: flex; align-items: center; justify-content: center;
  font-size: 2rem; color: var(--void-950);
  box-shadow: var(--shadow-gold); animation: float 3s ease-in-out infinite;
}
.code-box {
  width: 100%; padding: 1.25rem; border-radius: var(--radius-lg);
  background: var(--surface-700); border: 1px solid var(--border-gold);
  text-align: center;
}
.code-label { font-size: 0.75rem; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.1em; margin-bottom: 0.5rem; }
.code-value { font-size: 2rem; font-weight: 900; letter-spacing: 0.15em; }
.success-actions { display: flex; flex-direction: column; gap: 0.75rem; width: 100%; }

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50%       { transform: translateY(-8px); }
}
</style>

