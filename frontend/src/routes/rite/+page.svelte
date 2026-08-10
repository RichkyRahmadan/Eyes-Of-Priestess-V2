<script lang="ts">
  import { sealStore } from '$lib/stores/seal';
  import { sealApi } from '$lib/api/seal';
  import { goto } from '$app/navigation';
  import { toast } from 'svelte-sonner';
  import { Gem, Phone, Lock, ArrowRight, Eye, EyeOff } from "@lucide/svelte";

  let phone    = $state('');
  let password = $state('');
  let showPass = $state(false);
  let loading  = $state(false);
  let error    = $state('');

  async function handleRite() {
    error = '';
    if (!phone || !password) { error = 'Lengkapi semua field.'; return; }
    loading = true;
    try {
      const res = await sealApi.perform({ phone, password });
      sealStore.attune(res.pilgrim, res.accessSeal, res.refreshSeal);
      toast.success('Seal terpasang. Selamat datang kembali, Pilgrim.');
      goto('/sanctum/observatory');
    } catch (e: unknown) {
      error = e instanceof Error ? e.message : 'Gagal melakukan Rite of Return.';
    } finally {
      loading = false;
    }
  }
</script>

<svelte:head>
  <title>Rite of Return — EyesOfPriestess</title>
</svelte:head>

<div class="rite-page">
  <div class="rite-glow"></div>

  <div class="rite-card animate-fade-scale">
    <!-- Header -->
    <div class="rite-header">
      <div class="rite-icon">
        <Gem size={28} color="var(--gold-400)" />
      </div>
      <h1 class="rite-title">Rite of Return</h1>
      <p class="rite-sub">Pasang kembali Seal-mu untuk memasuki Sanctum</p>
    </div>

    <!-- Form -->
    <form onsubmit={(e) => { e.preventDefault(); handleRite(); }} class="rite-form">
      {#if error}
        <div class="form-alert">{error}</div>
      {/if}

      <div class="form-group">
        <label class="form-label" for="phone">Nomor Telefon</label>
        <div class="input-wrapper">
          <span class="input-icon left"><Phone size={16} /></span>
          <input
            id="phone"
            type="tel"
            placeholder="+62812..."
            bind:value={phone}
            class="input-icon-left"
            autocomplete="tel"
            required
          />
        </div>
      </div>

      <div class="form-group">
        <label class="form-label" for="password">Password</label>
        <div class="input-wrapper">
          <span class="input-icon left"><Lock size={16} /></span>
          <input
            id="password"
            type={showPass ? 'text' : 'password'}
            placeholder="••••••••"
            bind:value={password}
            class="input-icon-left input-icon-right"
            autocomplete="current-password"
            required
          />
          <button
            type="button"
            class="input-icon right toggle-pass"
            onclick={() => (showPass = !showPass)}
            aria-label="Toggle password visibility"
          >
            {#if showPass}
              <EyeOff size={16} />
            {:else}
              <Eye size={16} />
            {/if}
          </button>
        </div>
      </div>

      <button type="submit" class="btn btn-primary btn-full btn-lg" disabled={loading}>
        {#if loading}
          <span class="spinner"></span> Channeling…
        {:else}
          Perform the Rite <ArrowRight size={18} />
        {/if}
      </button>
    </form>

    <div class="divider">atau</div>

    <p class="rite-alt">
      Belum memiliki Seal?
      <a href="/rite/forge">Forge Identity</a>
    </p>
  </div>
</div>

<style>
.rite-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 1rem;
  background: var(--surface-900);
  position: relative;
  overflow: hidden;
}

.rite-glow {
  position: absolute;
  width: 500px; height: 500px;
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  background: radial-gradient(circle, rgba(109,40,217,0.12) 0%, transparent 70%);
  pointer-events: none;
}

.rite-card {
  width: 100%;
  max-width: 420px;
  background: var(--surface-800);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-2xl);
  padding: 2.5rem 2rem;
  position: relative;
  z-index: 1;
}

.rite-header { text-align: center; margin-bottom: 2rem; }

.rite-icon {
  width: 64px; height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--void-800), var(--void-900));
  border: 1px solid var(--border-gold);
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 1rem;
  box-shadow: var(--shadow-gold);
}

.rite-title { font-size: 1.5rem; margin-bottom: 0.375rem; }
.rite-sub   { color: var(--text-muted); font-size: 0.875rem; }

.rite-form { display: flex; flex-direction: column; gap: 1.25rem; }

.form-alert {
  background: var(--danger-bg);
  border: 1px solid rgba(239,68,68,0.3);
  border-radius: var(--radius-md);
  padding: 0.75rem 1rem;
  font-size: 0.875rem;
  color: var(--danger);
}

.toggle-pass { pointer-events: all; cursor: pointer; }

.spinner {
  width: 16px; height: 16px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: crystal-spin 0.6s linear infinite;
  display: inline-block;
}

.divider {
  display: flex; align-items: center; gap: 1rem;
  color: var(--text-muted); font-size: 0.8125rem;
  margin: 1.5rem 0 1rem;
}
.divider::before, .divider::after {
  content: ''; flex: 1; height: 1px; background: var(--border-default);
}

.rite-alt { text-align: center; font-size: 0.875rem; color: var(--text-muted); }
.rite-alt a { color: var(--void-400); font-weight: 600; }

@keyframes crystal-spin {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}
</style>

