<script lang="ts">
  import { authStore, addNotification } from '$lib/stores';
  import { User, Mail, Phone, ShieldCheck, Key, Lock, Check } from '@lucide/svelte';

  let user = $derived($authStore.user || {
    fullName: 'Budi Santoso',
    username: 'budisantoso',
    email: 'user@example.com',
    phoneNumber: '+6281234567890',
    isPinSet: true,
    isVerified: true
  });

  let fullName = $state('Budi Santoso');
  let phoneNumber = $state('+6281234567890');

  function saveProfile(e: Event) {
    e.preventDefault();
    addNotification({ type: 'success', title: 'Profil Diperbarui', message: 'Perubahan profil berhasil disimpan.' });
  }
</script>

<div class="max-w-3xl mx-auto space-y-8">
  <div class="border-b border-hairline pb-6 space-y-1">
    <h1 class="font-display text-4xl font-bold text-ink">Pengaturan Profil & Keamanan</h1>
    <p class="text-sm text-muted">Kelola informasi identitas, nomor telepon, dan status PIN transaksi 6-digit Anda.</p>
  </div>

  <!-- Profile Card -->
  <form onsubmit={saveProfile} class="card-editorial p-8 space-y-6">
    <div class="flex items-center gap-4">
      <div class="w-16 h-16 rounded-full bg-surface-dark text-on-dark flex items-center justify-center font-display text-2xl font-bold">
        {user.fullName.charAt(0)}
      </div>
      <div>
        <h2 class="font-display text-2xl font-bold text-ink">{user.fullName}</h2>
        <div class="font-mono text-xs text-coral">@{user.username}</div>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div class="space-y-1">
        <label for="pName" class="text-xs font-semibold uppercase tracking-wider text-muted">Nama Lengkap</label>
        <input
          id="pName"
          type="text"
          bind:value={fullName}
          class="w-full bg-canvas border border-hairline rounded-lg px-4 py-2.5 text-sm text-ink focus:outline-none focus:border-coral"
        />
      </div>

      <div class="space-y-1">
        <label for="pPhone" class="text-xs font-semibold uppercase tracking-wider text-muted">Nomor Telepon / WhatsApp</label>
        <input
          id="pPhone"
          type="text"
          bind:value={phoneNumber}
          class="w-full bg-canvas border border-hairline rounded-lg px-4 py-2.5 text-sm text-ink focus:outline-none focus:border-coral"
        />
      </div>
    </div>

    <div class="space-y-1">
      <label for="pEmail" class="text-xs font-semibold uppercase tracking-wider text-muted">Email Address (Readonly)</label>
      <input
        id="pEmail"
        type="email"
        value={user.email}
        disabled
        class="w-full bg-surface-soft border border-hairline rounded-lg px-4 py-2.5 text-sm text-muted"
      />
    </div>

    <button type="submit" class="btn-editorial-primary font-semibold py-2.5 px-6">
      Simpan Perubahan
    </button>
  </form>

  <!-- Security Verification Status Card -->
  <div class="card-dark-chrome p-8 space-y-6">
    <div class="flex items-center justify-between border-b border-surface-dark-elevated pb-4">
      <div class="flex items-center gap-3">
        <ShieldCheck size={24} class="text-success" />
        <div>
          <div class="text-sm font-bold text-on-dark">Status Keamanan Vault</div>
          <div class="text-xs text-on-dark-soft">Sistem Keamanan Reaktif Instaban Aktif</div>
        </div>
      </div>
      <span class="px-3 py-1 rounded-pill bg-success/20 text-success text-xs font-bold font-mono">VERIFIED</span>
    </div>

    <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
      <div class="p-4 rounded-xl bg-surface-dark-elevated space-y-1 border border-surface-dark-soft">
        <div class="font-bold text-on-dark flex items-center justify-between">
          <span>PIN Transaksi 6-Digit</span>
          <Check size={14} class="text-success" />
        </div>
        <div class="text-on-dark-soft">PIN telah dikonfigurasi untuk otorisasi pendanaan dan penarikan.</div>
      </div>

      <div class="p-4 rounded-xl bg-surface-dark-elevated space-y-1 border border-surface-dark-soft">
        <div class="font-bold text-on-dark flex items-center justify-between">
          <span>Sesi Aktif</span>
          <span class="font-mono text-coral font-bold">1 Device</span>
        </div>
        <div class="text-on-dark-soft">Token JWT RS256 berumur 15 menit dengan otentikasi Bearer.</div>
      </div>
    </div>
  </div>
</div>
