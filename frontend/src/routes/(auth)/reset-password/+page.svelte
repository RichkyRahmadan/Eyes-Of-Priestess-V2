<script lang="ts">
  import { page } from '$app/state';
  import { goto } from '$app/navigation';
  import Input from '$lib/components/ui/Input.svelte';
  import Button from '$lib/components/ui/Button.svelte';
  import { toasts } from '$lib/components/ui/Toaster.svelte';

  let email = $state(page.url.searchParams.get('email') ?? '');
  let token = $state(page.url.searchParams.get('token') ?? '123456');
  let newPassword = $state('');
  let confirmPassword = $state('');
  let loading = $state(false);
  let errors = $state<Record<string, string>>({});
  let globalError = $state('');

  async function handleSubmit(e: Event) {
    e.preventDefault();
    errors = {};
    globalError = '';

    if (!newPassword) {
      errors.newPassword = 'Password baru wajib diisi';
    } else if (newPassword.length < 8) {
      errors.newPassword = 'Password minimal 8 karakter';
    }

    if (!confirmPassword) {
      errors.confirmPassword = 'Konfirmasi password wajib diisi';
    } else if (newPassword !== confirmPassword) {
      errors.confirmPassword = 'Konfirmasi password tidak cocok';
    }

    if (!token) {
      errors.token = 'Kode token pemulihan wajib diisi';
    }

    if (Object.keys(errors).length > 0) return;

    loading = true;
    try {
      // Simulate backend reset password call
      await new Promise((resolve) => setTimeout(resolve, 800));
      toasts.success('Kata sandi berhasil diubah. Silakan masuk kembali.');
      goto('/login');
    } catch {
      globalError = 'Gagal mengatur ulang kata sandi. Token mungkin sudah kedaluwarsa.';
      toasts.error('Pembaruan kata sandi gagal.');
    } finally {
      loading = false;
    }
  }
</script>

<svelte:head>
  <title>Reset Password - EyesOfPriestess</title>
</svelte:head>

<form onsubmit={handleSubmit} class="flex flex-col gap-5">
  <div class="mb-2">
    <h1 class="font-display text-[var(--text-display-sm)] text-[var(--color-ink)] tracking-[-0.01em] mb-1">
      Atur Ulang Kata Sandi
    </h1>
    <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)]">
      Masukkan kode verifikasi dan kata sandi baru untuk akun Anda
    </p>
  </div>

  {#if globalError}
    <div class="bg-[var(--color-error)]/8 border border-[var(--color-error)]/20 rounded-[var(--radius-md)] px-4 py-3">
      <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-error)]">{globalError}</p>
    </div>
  {/if}

  {#if email}
    <div class="bg-[var(--color-surface-soft)] rounded-[var(--radius-md)] px-3.5 py-2.5 text-xs text-[var(--color-muted)]">
      Email Akun: <strong class="text-[var(--color-ink)] font-mono">{email}</strong>
    </div>
  {/if}

  <Input
    label="Kode Token Verifikasi / Omen"
    type="text"
    bind:value={token}
    placeholder="Contoh: 123456"
    error={errors.token}
    required
    id="reset-token"
  />

  <Input
    label="Kata Sandi Baru"
    type="password"
    bind:value={newPassword}
    placeholder="Minimal 8 karakter"
    error={errors.newPassword}
    required
    id="reset-new-password"
  />

  <Input
    label="Konfirmasi Kata Sandi Baru"
    type="password"
    bind:value={confirmPassword}
    placeholder="Ulangi kata sandi baru"
    error={errors.confirmPassword}
    required
    id="reset-confirm-password"
  />

  <Button variant="primary" type="submit" {loading} class="w-full mt-2">
    Simpan Kata Sandi Baru
  </Button>

  <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)] text-center">
    Batal mereset?
    <a href="/login" class="text-[var(--color-primary)] hover:underline font-medium">
      Kembali ke Masuk
    </a>
  </p>
</form>
