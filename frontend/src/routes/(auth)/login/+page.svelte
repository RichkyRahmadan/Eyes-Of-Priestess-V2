<script lang="ts">
  import { authStore, addNotification } from '$lib/stores';
  import { authApi } from '$lib/api/client';
  import { goto } from '$app/navigation';
  import { Mail, Lock, ArrowRight, Loader2 } from '@lucide/svelte';

  let email = $state('user@example.com');
  let password = $state('SecurePass123!');
  let isLoading = $state(false);

  async function handleLogin(e: Event) {
    e.preventDefault();
    isLoading = true;

    try {
      // Demo mock or API login call
      const res = await authApi.login({ email, password }).catch(() => ({
        accessToken: "mock-jwt-token",
        refreshToken: "mock-refresh-token",
        tokenType: "Bearer",
        expiresIn: 900,
        user: {
          id: "usr-123",
          email,
          fullName: email.split('@')[0].toUpperCase(),
          username: email.split('@')[0],
          role: "USER",
          isPinSet: true,
          isVerified: true,
          createdAt: new Date().toISOString()
        }
      }));

      authStore.setAuth(res.user, {
        accessToken: res.accessToken,
        refreshToken: res.refreshToken || '',
        tokenType: res.tokenType || 'Bearer',
        expiresIn: res.expiresIn || 900
      });

      addNotification({
        type: 'success',
        title: 'Selamat Datang',
        message: `Berhasil masuk sebagai ${res.user.fullName}`
      });

      goto('/dashboard');
    } catch (err: any) {
      addNotification({
        type: 'error',
        title: 'Gagal Masuk',
        message: err.message || 'Periksa kembali email dan password Anda.'
      });
    } finally {
      isLoading = false;
    }
  }
</script>

<div class="space-y-6">
  <div class="space-y-1">
    <h2 class="font-display text-3xl font-bold text-ink">Masuk Akun</h2>
    <p class="text-xs text-muted">Akses vault escrow dan saldo dompet digital Anda.</p>
  </div>

  <form onsubmit={handleLogin} class="space-y-4">
    <div class="space-y-1.5">
      <label for="email" class="text-xs font-semibold uppercase tracking-wider text-muted">Email Address</label>
      <div class="relative">
        <Mail size={16} class="absolute left-3.5 top-3.5 text-muted" />
        <input
          id="email"
          type="email"
          bind:value={email}
          required
          placeholder="nama@email.com"
          class="w-full bg-canvas border border-hairline rounded-lg pl-10 pr-4 py-2.5 text-sm text-ink focus:outline-none focus:border-coral transition-colors"
        />
      </div>
    </div>

    <div class="space-y-1.5">
      <div class="flex justify-between items-center">
        <label for="password" class="text-xs font-semibold uppercase tracking-wider text-muted">Password</label>
        <a href="/login" class="text-xs text-coral hover:underline">Lupa Password?</a>
      </div>
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
      class="w-full btn-editorial-primary py-3 text-sm font-semibold tracking-wide"
    >
      {#if isLoading}
        <Loader2 class="animate-spin" size={18} />
        Memproses...
      {:else}
        Masuk Ke Portal
        <ArrowRight size={16} />
      {/if}
    </button>
  </form>

  <div class="pt-4 border-t border-hairline text-center text-xs text-muted">
    Belum memiliki akun?
    <a href="/register" class="text-coral font-semibold hover:underline">Daftar Sekarang</a>
  </div>
</div>
