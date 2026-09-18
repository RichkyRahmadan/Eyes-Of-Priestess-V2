<script lang="ts">
  import Input from '$lib/components/ui/Input.svelte';
  import Button from '$lib/components/ui/Button.svelte';
  import { authApi } from '$lib/api';
  import { authStore } from '$lib/stores/auth';
  import { goto } from '$app/navigation';
  import type { ApiError } from '$lib/types';

  let form = $state({
    fullName: '',
    username: '',
    email: '',
    phoneNumber: '',
    password: '',
    confirmPassword: '',
    pin: ''
  });
  let loading = $state(false);
  let errors = $state<Record<string, string>>({});
  let globalError = $state('');

  function validate() {
    const e: Record<string, string> = {};
    if (!form.fullName.trim()) e.fullName = 'Nama lengkap wajib diisi';
    if (!form.username.match(/^[a-zA-Z0-9_]{3,50}$/))
      e.username = 'Username 3-50 karakter, hanya huruf, angka, dan underscore';
    if (!form.email.match(/^[^\s@]+@[^\s@]+\.[^\s@]+$/))
      e.email = 'Format email tidak valid';
    if (!form.phoneNumber.match(/^(\+62|62|0)[0-9]{8,13}$/))
      e.phoneNumber = 'Format nomor Indonesia tidak valid (contoh: 081234567890)';
    if (form.password.length < 8)
      e.password = 'Password minimal 8 karakter';
    if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(form.password))
      e.password = 'Password harus mengandung huruf besar, huruf kecil, dan angka';
    if (form.password !== form.confirmPassword)
      e.confirmPassword = 'Konfirmasi password tidak cocok';
    if (!form.pin.match(/^[0-9]{6}$/))
      e.pin = 'PIN harus berupa 6 digit angka numerik';
    if (form.pin === form.password)
      e.pin = 'PIN tidak boleh sama dengan password';
    return e;
  }

  async function handleSubmit(e: Event) {
    e.preventDefault();
    errors = {};
    globalError = '';

    const validation = validate();
    if (Object.keys(validation).length) {
      errors = validation;
      return;
    }

    loading = true;
    const cleanPhone = form.phoneNumber.trim().replace(/[\s-]/g, '');

    try {
      await authApi.register({
        email: form.email,
        password: form.password,
        fullName: form.fullName,
        phone: cleanPhone,
        phoneNumber: cleanPhone,
        username: form.username,
        pin: form.pin
      });

      // Attempt automatic login after registration
      try {
        const loginRes = (await authApi.login({
          phone: cleanPhone,
          password: form.password
        })) as any;

        const tokens = loginRes?.tokens || (loginRes?.accessToken ? {
          accessToken: loginRes.accessToken,
          refreshToken: loginRes.refreshToken || '',
          tokenType: loginRes.tokenType || 'Bearer',
          expiresIn: loginRes.expiresIn || 900
        } : null);

        const user = loginRes?.user || loginRes?.User;

        if (tokens && user) {
          authStore.setAuth(user, tokens);
          goto('/dashboard');
          return;
        }
      } catch {
        // Fallback to login page if auto-login does not succeed
      }

      goto('/login');
    } catch (err) {
      const apiErr = err as ApiError;
      if (apiErr?.details?.length) {
        apiErr.details.forEach((d) => {
          const fieldKey = d.field === 'phone' ? 'phoneNumber' : d.field;
          errors[fieldKey] = d.message;
        });
      } else {
        globalError = apiErr?.message ?? 'Terjadi kesalahan saat registrasi. Coba lagi.';
      }
    } finally {
      loading = false;
    }
  }
</script>

<svelte:head>
  <title>Daftar - EyesOfPriestess</title>
</svelte:head>

<form onsubmit={handleSubmit} class="flex flex-col gap-4">
  <div class="mb-1">
    <h1
      class="font-display text-[var(--text-display-sm)] text-[var(--color-ink)] tracking-[-0.01em] mb-1"
    >
      Buat akun baru
    </h1>
    <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)]">
      Gratis selamanya, tanpa biaya pendaftaran
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
    label="Nama Lengkap"
    bind:value={form.fullName}
    placeholder="Nama sesuai KTP"
    error={errors.fullName}
    required
    id="reg-fullname"
  />
  <Input
    label="Username"
    bind:value={form.username}
    placeholder="username_kamu"
    error={errors.username}
    required
    id="reg-username"
  />
  <Input
    label="Email"
    type="email"
    bind:value={form.email}
    placeholder="nama@email.com"
    error={errors.email}
    required
    id="reg-email"
  />
  <Input
    label="Nomor Telepon"
    type="tel"
    bind:value={form.phoneNumber}
    placeholder="081234567890"
    error={errors.phoneNumber}
    required
    id="reg-phone"
  />
  <Input
    label="Password"
    type="password"
    bind:value={form.password}
    placeholder="Min. 8 karakter, huruf besar + angka"
    error={errors.password}
    required
    id="reg-password"
  />
  <Input
    label="Konfirmasi Password"
    type="password"
    bind:value={form.confirmPassword}
    placeholder="Ulangi password"
    error={errors.confirmPassword}
    required
    id="reg-confirm"
  />
  <Input
    label="PIN Transaksi (6 Digit Angka)"
    type="password"
    inputmode="numeric"
    maxlength={6}
    bind:value={form.pin}
    placeholder="Contoh: 123456"
    error={errors.pin}
    required
    id="reg-pin"
  />

  <Button variant="primary" type="submit" {loading} class="w-full mt-1">
    Buat Akun
  </Button>

  <p class="font-sans text-[var(--text-caption)] text-[var(--color-muted)] text-center">
    Dengan mendaftar, Anda menyetujui
    <a href="/syarat" class="text-[var(--color-primary)] hover:underline">Syarat & Ketentuan</a>
    kami.
  </p>

  <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)] text-center">
    Sudah punya akun?
    <a href="/login" class="text-[var(--color-primary)] hover:underline font-medium">Masuk</a>
  </p>
</form>
