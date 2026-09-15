<script lang="ts">
  import Input from '$lib/components/ui/Input.svelte';
  import Button from '$lib/components/ui/Button.svelte';
  import { toasts } from '$lib/components/ui/Toaster.svelte';

  let email = $state('');
  let loading = $state(false);
  let submitted = $state(false);
  let error = $state('');

  async function handleSubmit(e: Event) {
    e.preventDefault();
    error = '';

    if (!email) {
      error = 'Email wajib diisi';
      return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      error = 'Format email tidak valid';
      return;
    }

    loading = true;
    try {
      // Simulate API call for forgot password
      await new Promise((resolve) => setTimeout(resolve, 800));
      submitted = true;
      toasts.success('Instruksi pemulihan telah dikirim ke email Anda.');
    } catch {
      error = 'Gagal mengirim permintaan reset password. Silakan coba lagi.';
      toasts.error('Terjadi kendala pada server.');
    } finally {
      loading = false;
    }
  }
</script>

<svelte:head>
  <title>Lupa Password - EyesOfPriestess</title>
</svelte:head>

<div class="flex flex-col gap-5">
  <div class="mb-2">
    <h1 class="font-display text-[var(--text-display-sm)] text-[var(--color-ink)] tracking-[-0.01em] mb-1">
      Pemulihan Kata Sandi
    </h1>
    <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)]">
      Masukkan email yang terdaftar untuk menerima tautan atur ulang password
    </p>
  </div>

  {#if submitted}
    <div class="bg-[var(--color-success)]/10 border border-[var(--color-success)]/20 rounded-[var(--radius-lg)] p-5 text-center flex flex-col gap-3">
      <div class="w-12 h-12 rounded-full bg-[var(--color-success)]/20 text-[var(--color-success)] flex items-center justify-center mx-auto text-xl font-bold">
        ✓
      </div>
      <div>
        <h3 class="font-sans font-medium text-[var(--text-body-md)] text-[var(--color-ink)]">
          Email Terkirim!
        </h3>
        <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)] mt-1">
          Kami telah mengirimkan instruksi pemulihan ke <strong class="text-[var(--color-ink)]">{email}</strong>. Silakan periksa kotak masuk atau spam email Anda.
        </p>
      </div>
      <a
        href="/reset-password?email={encodeURIComponent(email)}"
        class="inline-flex items-center justify-center h-10 px-4 bg-[var(--color-primary)] text-white rounded-[var(--radius-md)] font-sans text-[var(--text-body-sm)] font-medium hover:bg-[var(--color-primary-active)] transition-colors mt-2"
      >
        Lanjut ke Atur Password Baru
      </a>
    </div>
  {:else}
    <form onsubmit={handleSubmit} class="flex flex-col gap-4">
      {#if error}
        <div class="bg-[var(--color-error)]/8 border border-[var(--color-error)]/20 rounded-[var(--radius-md)] px-4 py-3">
          <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-error)]">{error}</p>
        </div>
      {/if}

      <Input
        label="Email Terdaftar"
        type="email"
        bind:value={email}
        placeholder="nama@email.com"
        {error}
        required
        id="forgot-email"
      />

      <Button variant="primary" type="submit" {loading} class="w-full mt-2">
        Kirim Tautan Pemulihan
      </Button>
    </form>
  {/if}

  <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)] text-center">
    Ingat kata sandi Anda?
    <a href="/login" class="text-[var(--color-primary)] hover:underline font-medium">
      Kembali ke Masuk
    </a>
  </p>
</div>
