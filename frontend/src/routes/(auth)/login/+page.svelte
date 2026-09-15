<script lang="ts">
  import Input from '$lib/components/ui/Input.svelte';
  import Button from '$lib/components/ui/Button.svelte';
  import { authApi } from '$lib/api';
  import { authStore } from '$lib/stores/auth';
  import { goto } from '$app/navigation';
  import type { ApiError } from '$lib/types';

  let email = $state('');
  let password = $state('');
  let loading = $state(false);
  let errors = $state<Record<string, string>>({});
  let globalError = $state('');

  async function handleSubmit(e: Event) {
    e.preventDefault();
    errors = {};
    globalError = '';

    if (!email) errors.email = 'Email wajib diisi';
    if (!password) errors.password = 'Password wajib diisi';
    if (Object.keys(errors).length) return;

    loading = true;
    try {
      const res = await authApi.login({ email, password }) as {
        user: import('$lib/types').User;
        tokens: import('$lib/types').AuthTokens;
      };
      authStore.setAuth(res.user, res.tokens);
      if (!res.user.isPinSet) {
        goto('/set-pin');
      } else {
        goto('/dashboard');
      }
    } catch (err) {
      const apiErr = err as ApiError;
      globalError = apiErr?.message ?? 'Terjadi kesalahan. Coba lagi.';
    } finally {
      loading = false;
    }
  }
</script>

<svelte:head>
  <title>Masuk - EyesOfPriestess</title>
</svelte:head>

<form onsubmit={handleSubmit} class="flex flex-col gap-5">
  <div class="mb-2">
    <h1 class="font-display text-[var(--text-display-sm)] text-[var(--color-ink)] tracking-[-0.01em] mb-1">
      Selamat datang kembali
    </h1>
    <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)]">
      Masuk untuk melanjutkan transaksi Anda
    </p>
  </div>

  {#if globalError}
    <div
      class="bg-[var(--color-error)]/8 border border-[var(--color-error)]/20 rounded-[var(--radius-md)] px-4 py-3"
    >
      <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-error)]">{globalError}</p>
    </div>
  {/if}

  <Input
    label="Email"
    type="email"
    bind:value={email}
    placeholder="nama@email.com"
    error={errors.email}
    required
    id="login-email"
  />

  <div class="flex flex-col gap-1.5">
    <Input
      label="Password"
      type="password"
      bind:value={password}
      placeholder="Minimal 8 karakter"
      error={errors.password}
      required
      id="login-password"
    />
    <a
      href="/forgot-password"
      class="font-sans text-[var(--text-caption)] text-[var(--color-primary)] hover:underline self-end"
    >
      Lupa password?
    </a>
  </div>

  <Button variant="primary" type="submit" {loading} class="w-full mt-1">
    Masuk
  </Button>

  <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)] text-center">
    Belum punya akun?
    <a href="/register" class="text-[var(--color-primary)] hover:underline font-medium">
      Daftar sekarang
    </a>
  </p>
</form>
