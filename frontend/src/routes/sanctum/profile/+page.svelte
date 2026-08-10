<script lang="ts">
  import { sealStore } from '$lib/stores/seal';
  import { sealApi } from '$lib/api/seal';
  import { goto } from '$app/navigation';
  import { toast } from 'svelte-sonner';
  import { User, Phone, Mail, Shield, Lock, LogOut, Star } from "@lucide/svelte";

  const pilgrim = $derived($sealStore.pilgrim);

  let changingPw  = $state(false);
  let changingPin = $state(false);
  let oldPw = $state(''); let newPw = $state('');
  let oldPin = $state(''); let newPin = $state('');

  async function handleLogout() {
    try { await sealApi.sever(); } catch { /* ignore */ }
    sealStore.sever();
    goto('/rite');
  }

  async function submitPwChange() {
    try { await sealApi.changePassword(oldPw, newPw); toast.success('Password berhasil diubah!'); oldPw = ''; newPw = ''; changingPw = false; }
    catch (e: unknown) { toast.error(e instanceof Error ? e.message : 'Gagal.'); }
  }

  async function submitPinChange() {
    try { await sealApi.changePin(oldPin, newPin); toast.success('PIN berhasil diubah!'); oldPin = ''; newPin = ''; changingPin = false; }
    catch (e: unknown) { toast.error(e instanceof Error ? e.message : 'Gagal.'); }
  }
</script>

<svelte:head>
  <title>Profile — EyesOfPriestess</title>
</svelte:head>

<div class="profile-page animate-fade-in">
  <!-- Pilgrim Card -->
  <div class="pilgrim-hero glow-border-void">
    <div class="pilgrim-avatar-lg">
      {pilgrim?.fullName.charAt(0).toUpperCase() ?? '?'}
    </div>
    <div class="pilgrim-detail">
      <h2 class="pilgrim-name">{pilgrim?.fullName ?? '—'}</h2>
      <p class="text-sm text-muted">{pilgrim?.phone ?? ''}</p>
      {#if pilgrim?.email}
        <p class="text-sm text-muted">{pilgrim.email}</p>
      {/if}
      <div class="pilgrim-badges">
        {#if pilgrim?.role === 'ORACLE'}
          <span class="badge badge-forged"><Shield size={12} /> Oracle</span>
        {/if}
        <span class="badge" class:badge-fulfilled={pilgrim?.attuned} class:badge-pending={!pilgrim?.attuned}>
          {pilgrim?.attuned ? 'Attuned' : 'Unattuned'}
        </span>
      </div>
    </div>
    <div class="score-block">
      <Star size={20} color="var(--gold-400)" />
      <p class="score-num">{pilgrim?.covenantScore ?? 0}</p>
      <p class="text-xs text-muted">Covenant Score</p>
    </div>
  </div>

  <!-- Settings -->
  <div class="settings-grid">
    <!-- Change Password -->
    <div class="card">
      <div class="setting-header">
        <Lock size={18} color="var(--void-400)" />
        <h3 class="setting-title">Ubah Password</h3>
        <button class="btn btn-ghost btn-sm ml-auto" onclick={() => (changingPw = !changingPw)}>
          {changingPw ? 'Batal' : 'Ubah'}
        </button>
      </div>
      {#if changingPw}
        <form onsubmit={(e) => { e.preventDefault(); submitPwChange(); }} class="setting-form">
          <div class="form-group">
            <label class="form-label" for="oldPw">Password Lama</label>
            <input id="oldPw" type="password" bind:value={oldPw} required />
          </div>
          <div class="form-group">
            <label class="form-label" for="newPw">Password Baru</label>
            <input id="newPw" type="password" bind:value={newPw} minlength={8} required />
          </div>
          <button type="submit" class="btn btn-primary btn-full">Simpan Password</button>
        </form>
      {/if}
    </div>

    <!-- Change PIN -->
    <div class="card">
      <div class="setting-header">
        <Shield size={18} color="var(--void-400)" />
        <h3 class="setting-title">Ubah PIN</h3>
        <button class="btn btn-ghost btn-sm ml-auto" onclick={() => (changingPin = !changingPin)}>
          {changingPin ? 'Batal' : 'Ubah'}
        </button>
      </div>
      {#if changingPin}
        <form onsubmit={(e) => { e.preventDefault(); submitPinChange(); }} class="setting-form">
          <div class="form-group">
            <label class="form-label" for="oldPin">PIN Lama</label>
            <input id="oldPin" type="password" maxlength={6} inputmode="numeric" bind:value={oldPin} required />
          </div>
          <div class="form-group">
            <label class="form-label" for="newPin">PIN Baru (6 digit)</label>
            <input id="newPin" type="password" maxlength={6} inputmode="numeric" bind:value={newPin} pattern="\d{6}" required />
          </div>
          <button type="submit" class="btn btn-primary btn-full">Simpan PIN</button>
        </form>
      {/if}
    </div>
  </div>

  <!-- Danger Zone -->
  <div class="card danger-zone">
    <h3 class="setting-title" style="color:var(--danger)">Danger Zone</h3>
    <p class="text-sm text-muted">Sever Seal akan mengakhiri sesi aktif Anda.</p>
    <button class="btn btn-danger btn-sm" onclick={handleLogout} style="width:fit-content; margin-top:1rem;">
      <LogOut size={16} /> Sever Seal
    </button>
  </div>
</div>

<style>
.profile-page { display: flex; flex-direction: column; gap: 1.5rem; max-width: 800px; }

.pilgrim-hero {
  background: linear-gradient(135deg, var(--surface-800), var(--surface-700));
  border-radius: var(--radius-xl); padding: 2rem;
  display: flex; align-items: center; gap: 1.5rem; flex-wrap: wrap;
}

.pilgrim-avatar-lg {
  width: 72px; height: 72px; border-radius: 50%;
  background: linear-gradient(135deg, var(--void-600), var(--void-800));
  display: flex; align-items: center; justify-content: center;
  font-size: 1.75rem; font-weight: 800; color: var(--void-200);
  border: 2px solid var(--border-gold); flex-shrink: 0;
  box-shadow: var(--shadow-gold);
}

.pilgrim-detail { flex: 1; }
.pilgrim-name   { font-size: 1.25rem; font-weight: 700; margin-bottom: 0.25rem; }
.pilgrim-badges { display: flex; gap: 0.5rem; margin-top: 0.75rem; flex-wrap: wrap; }

.score-block { display: flex; flex-direction: column; align-items: center; gap: 0.25rem; }
.score-num   { font-size: 1.75rem; font-weight: 900; color: var(--gold-400); line-height: 1; }

.settings-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1.25rem; }
@media (max-width: 640px) { .settings-grid { grid-template-columns: 1fr; } }

.setting-header { display: flex; align-items: center; gap: 0.625rem; margin-bottom: 0; }
.setting-title  { font-size: 0.9375rem; font-weight: 600; }
.ml-auto { margin-left: auto; }

.setting-form { display: flex; flex-direction: column; gap: 1rem; margin-top: 1.25rem; }

.danger-zone { border-color: rgba(239,68,68,0.2); }
</style>

