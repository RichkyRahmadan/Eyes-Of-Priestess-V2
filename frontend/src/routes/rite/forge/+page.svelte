<script lang="ts">
  import { sealStore } from '$lib/stores/seal';
  import { sealApi } from '$lib/api/seal';
  import { goto } from '$app/navigation';
  import { toast } from 'svelte-sonner';
  import { Gem, Phone, Lock, User, Mail, ArrowRight, ArrowLeft } from "@lucide/svelte";

  let step     = $state(1);
  let phone    = $state('');
  let email    = $state('');
  let fullName = $state('');
  let password = $state('');
  let pin      = $state('');
  let loading  = $state(false);
  let error    = $state('');

  async function handleForge() {
    error = '';
    loading = true;
    try {
      const res = await sealApi.forgeIdentity({ phone, email: email || undefined, fullName, password, pin });
      sealStore.attune(res.pilgrim, res.accessSeal, res.refreshSeal);
      toast.success('Identity forged! Welcome, Pilgrim.');
      goto('/sanctum/observatory');
    } catch (e: unknown) {
      error = e instanceof Error ? e.message : 'Gagal forge identity.';
    } finally {
      loading = false;
    }
  }

  function next() {
    error = '';
    if (step === 1 && (!phone || !fullName)) { error = 'Lengkapi semua field.'; return; }
    if (step === 2 && (password.length < 8))  { error = 'Password minimal 8 karakter.'; return; }
    if (step === 2 && (pin.length !== 6 || !/^\d+$/.test(pin))) { error = 'PIN harus 6 digit angka.'; return; }
    step++;
  }
</script>

<svelte:head>
  <title>Forge Identity — EyesOfPriestess</title>
</svelte:head>

<div class="rite-page">
  <div class="rite-glow"></div>

  <div class="rite-card animate-fade-scale">
    <div class="rite-header">
      <div class="rite-icon">
        <Gem size={28} color="var(--gold-400)" />
      </div>
      <h1 class="rite-title">Forge Identity</h1>
      <p class="rite-sub">Ciptakan Seal abadi di bawah tatapan Priestess</p>

      <!-- Progress -->
      <div class="steps-progress">
        {#each [1,2,3] as s}
          <div class="progress-dot" class:active={s <= step} class:done={s < step}></div>
          {#if s < 3}
            <div class="progress-line" class:active={s < step}></div>
          {/if}
        {/each}
      </div>
    </div>

    {#if error}
      <div class="form-alert">{error}</div>
    {/if}

    {#if step === 1}
      <!-- Step 1: Identity -->
      <div class="step-form animate-fade-in">
        <p class="step-label">Step 1 — Identitas</p>
        <div class="form-group">
          <label class="form-label" for="fullName">Nama Lengkap</label>
          <div class="input-wrapper">
            <span class="input-icon left"><User size={16} /></span>
            <input id="fullName" type="text" placeholder="Nama lengkap" bind:value={fullName} class="input-icon-left" required />
          </div>
        </div>
        <div class="form-group">
          <label class="form-label" for="phone">Nomor Telefon</label>
          <div class="input-wrapper">
            <span class="input-icon left"><Phone size={16} /></span>
            <input id="phone" type="tel" placeholder="+62812..." bind:value={phone} class="input-icon-left" required />
          </div>
        </div>
        <div class="form-group">
          <label class="form-label" for="email">Email <span class="text-muted">(opsional)</span></label>
          <div class="input-wrapper">
            <span class="input-icon left"><Mail size={16} /></span>
            <input id="email" type="email" placeholder="email@example.com" bind:value={email} class="input-icon-left" />
          </div>
        </div>
        <button class="btn btn-primary btn-full btn-lg" onclick={next}>
          Lanjutkan <ArrowRight size={18} />
        </button>
      </div>

    {:else if step === 2}
      <!-- Step 2: Security -->
      <div class="step-form animate-fade-in">
        <p class="step-label">Step 2 — Keamanan</p>
        <div class="form-group">
          <label class="form-label" for="password">Password</label>
          <div class="input-wrapper">
            <span class="input-icon left"><Lock size={16} /></span>
            <input id="password" type="password" placeholder="Min. 8 karakter" bind:value={password} class="input-icon-left" required />
          </div>
          <span class="form-hint">Minimal 8 karakter, kombinasi huruf & angka</span>
        </div>
        <div class="form-group">
          <label class="form-label" for="pin">PIN (6 digit)</label>
          <div class="input-wrapper">
            <span class="input-icon left"><Lock size={16} /></span>
            <input id="pin" type="password" placeholder="••••••" bind:value={pin} maxlength={6} class="input-icon-left" inputmode="numeric" pattern="\d{6}" required />
          </div>
          <span class="form-hint">Digunakan untuk konfirmasi transaksi</span>
        </div>
        <div class="btn-row">
          <button class="btn btn-ghost" onclick={() => step--}>
            <ArrowLeft size={18} /> Kembali
          </button>
          <button class="btn btn-primary flex-1" onclick={next}>
            Lanjutkan <ArrowRight size={18} />
          </button>
        </div>
      </div>

    {:else}
      <!-- Step 3: Confirm -->
      <div class="step-form animate-fade-in">
        <p class="step-label">Step 3 — Konfirmasi</p>
        <div class="confirm-card">
          <div class="confirm-row">
            <span class="text-muted text-sm">Nama</span>
            <span class="text-sm font-semibold">{fullName}</span>
          </div>
          <div class="confirm-row">
            <span class="text-muted text-sm">Telefon</span>
            <span class="text-sm font-semibold">{phone}</span>
          </div>
          {#if email}
            <div class="confirm-row">
              <span class="text-muted text-sm">Email</span>
              <span class="text-sm font-semibold">{email}</span>
            </div>
          {/if}
        </div>

        <div class="btn-row">
          <button class="btn btn-ghost" onclick={() => step--}>
            <ArrowLeft size={18} /> Kembali
          </button>
          <button class="btn btn-gold flex-1 btn-lg" onclick={handleForge} disabled={loading}>
            {#if loading}
              <span class="spinner"></span> Forging…
            {:else}
              Forge Identity ✦
            {/if}
          </button>
        </div>
      </div>
    {/if}

    <p class="rite-alt">
      Sudah punya Seal? <a href="/rite">Perform the Rite</a>
    </p>
  </div>
</div>

<style>
.rite-page {
  min-height: 100vh;
  display: flex; align-items: center; justify-content: center;
  padding: 2rem 1rem;
  background: var(--surface-900);
  position: relative; overflow: hidden;
}

.rite-glow {
  position: absolute;
  width: 500px; height: 500px;
  top: 50%; left: 50%;
  transform: translate(-50%,-50%);
  background: radial-gradient(circle, rgba(109,40,217,0.12) 0%, transparent 70%);
  pointer-events: none;
}

.rite-card {
  width: 100%; max-width: 440px;
  background: var(--surface-800);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-2xl);
  padding: 2.5rem 2rem;
  position: relative; z-index: 1;
  display: flex; flex-direction: column; gap: 1.25rem;
}

.rite-header { text-align: center; }
.rite-icon {
  width: 64px; height: 64px; border-radius: 50%;
  background: linear-gradient(135deg, var(--void-800), var(--void-900));
  border: 1px solid var(--border-gold);
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 1rem; box-shadow: var(--shadow-gold);
}
.rite-title { font-size: 1.5rem; margin-bottom: 0.375rem; }
.rite-sub   { color: var(--text-muted); font-size: 0.875rem; margin-bottom: 1rem; }

.steps-progress {
  display: flex; align-items: center; justify-content: center; gap: 0;
  margin-top: 1rem;
}
.progress-dot {
  width: 10px; height: 10px; border-radius: 50%;
  background: var(--surface-600);
  transition: background var(--transition-normal);
}
.progress-dot.active { background: var(--void-500); }
.progress-dot.done   { background: var(--gold-500); }
.progress-line {
  width: 40px; height: 2px; background: var(--surface-600);
  transition: background var(--transition-normal);
}
.progress-line.active { background: var(--void-500); }

.step-form { display: flex; flex-direction: column; gap: 1.25rem; }
.step-label { font-size: 0.75rem; font-weight: 700; letter-spacing: 0.1em; text-transform: uppercase; color: var(--gold-400); }

.form-alert {
  background: var(--danger-bg); border: 1px solid rgba(239,68,68,0.3);
  border-radius: var(--radius-md); padding: 0.75rem 1rem;
  font-size: 0.875rem; color: var(--danger);
}

.confirm-card {
  background: var(--surface-700); border-radius: var(--radius-lg);
  border: 1px solid var(--border-subtle); padding: 1rem;
  display: flex; flex-direction: column; gap: 0.75rem;
}
.confirm-row { display: flex; justify-content: space-between; align-items: center; }

.btn-row { display: flex; gap: 0.75rem; }
.flex-1 { flex: 1; }
.font-semibold { font-weight: 600; }

.rite-alt { text-align: center; font-size: 0.875rem; color: var(--text-muted); }
.rite-alt a { color: var(--void-400); font-weight: 600; }

.spinner {
  width: 16px; height: 16px;
  border: 2px solid rgba(255,255,255,0.3); border-top-color: #fff;
  border-radius: 50%; animation: crystal-spin 0.6s linear infinite; display: inline-block;
}
@keyframes crystal-spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
</style>

