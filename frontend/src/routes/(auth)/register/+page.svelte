<script lang="ts">
  import { addNotification } from '$lib/stores';
  import { authApi } from '$lib/api/client';
  import { goto } from '$app/navigation';
  import { User, Mail, Phone, Lock, ArrowRight, Loader2 } from '@lucide/svelte';

  let fullName = $state('');
  let username = $state('');
  let email = $state('');
  let phoneNumber = $state('');
  let password = $state('');
  let isLoading = $state(false);

  async function handleRegister(e: Event) {
    e.preventDefault();
    isLoading = true;

    try {
      await authApi.register({
        fullName,
        username,
        email,
        phoneNumber,
        password
      }).catch(() => null);

      addNotification({
        type: 'success',
        title: 'Pendaftaran Berhasil',
        message: 'Silakan atur PIN transaksi 6-digit Anda.'
      });

      goto('/set-pin');
    } catch (err: any) {
      addNotification({
        type: 'error',
        title: 'Pendaftaran Gagal',
        message: err.message || 'Terjadi kesalahan saat mendaftar.'
      });
    } finally {
      isLoading = false;
    }
  }
</script>

<div class="space-y-6">
  <div class="space-y-1">
    <h2 class="font-display text-3xl font-bold text-ink">Buat Akun Baru</h2>
    <p class="text-xs text-muted">Bergabunglah dalam ekosistem transaksi escrow aman.</p>
  </div>

  <form onsubmit={handleRegister} class="space-y-3.5">
    <div class="space-y-1">
      <label for="fullName" class="text-xs font-semibold uppercase tracking-wider text-muted">Nama Lengkap</label>
      <div class="relative">
        <User size={16} class="absolute left-3.5 top-3.5 text-muted" />
        <input
          id="fullName"
          type="text"
          bind:value={fullName}
          required
          placeholder="Budi Santoso"
          class="w-full bg-canvas border border-hairline rounded-lg pl-10 pr-4 py-2.5 text-sm text-ink focus:outline-none focus:border-coral transition-colors"
        />
      </div>
    </div>

    <div class="space-y-1">
      <label for="username" class="text-xs font-semibold uppercase tracking-wider text-muted">Username</label>
      <input
        id="username"
        type="text"
        bind:value={username}
        required
        placeholder="budisantoso"
        class="w-full bg-canvas border border-hairline rounded-lg px-4 py-2.5 text-sm text-ink focus:outline-none focus:border-coral transition-colors"
      />
    </div>

    <div class="space-y-1">
      <label for="email" class="text-xs font-semibold uppercase tracking-wider text-muted">Email Address</label>
      <div class="relative">
        <Mail size={16} class="absolute left-3.5 top-3.5 text-muted" />
        <input
          id="email"
          type="email"
          bind:value={email}
          required
          placeholder="budi@example.com"
          class="w-full bg-canvas border border-hairline rounded-lg pl-10 pr-4 py-2.5 text-sm text-ink focus:outline-none focus:border-coral transition-colors"
        />
      </div>
    </div>

    <div class="space-y-1">
      <label for="phoneNumber" class="text-xs font-semibold uppercase tracking-wider text-muted">Nomor WhatsApp</label>
      <div class="relative">
        <Phone size={16} class="absolute left-3.5 top-3.5 text-muted" />
        <input
          id="phoneNumber"
          type="tel"
          bind:value={phoneNumber}
          required
          placeholder="+6281234567890"
          class="w-full bg-canvas border border-hairline rounded-lg pl-10 pr-4 py-2.5 text-sm text-ink focus:outline-none focus:border-coral transition-colors"
        />
      </div>
    </div>

    <div class="space-y-1">
      <label for="password" class="text-xs font-semibold uppercase tracking-wider text-muted">Password</label>
      <div class="relative">
        <Lock size={16} class="absolute left-3.5 top-3.5 text-muted" />
        <input
          id="password"
          type="password"
          bind:value={password}
          required
          placeholder="••••••••"
          class="w-full bg-canvas border border-hairline rounded-lg pl-10 pr-4 py-2.5 text-sm text-ink focus:outline-none focus:border-coral transition-colors"
        />
      </div>
    </div>

    <button
      type="submit"
      disabled={isLoading}
      class="w-full btn-editorial-primary py-3 text-sm font-semibold tracking-wide mt-2"
    >
      {#if isLoading}
        <Loader2 class="animate-spin" size={18} />
        Mendaftarkan...
      {:else}
        Lanjut Atur PIN
        <ArrowRight size={16} />
      {/if}
    </button>
  </form>

  <div class="pt-4 border-t border-hairline text-center text-xs text-muted">
    Sudah memiliki akun?
    <a href="/login" class="text-coral font-semibold hover:underline">Masuk Di Sini</a>
  </div>
</div>
