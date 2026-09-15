<script lang="ts">
  import { page } from '$app/state';

  const status = $derived(page.status);
  const message = $derived(page.error?.message || 'Terjadi kesalahan pada sistem.');

  const errorTitles: Record<number, string> = {
    401: 'Sesi Kedaluwarsa / Belum Masuk',
    403: 'Akses Ditolak (Forbidden)',
    404: 'Halaman Tidak Ditemukan',
    500: 'Gangguan Server Internal'
  };

  const errorDescriptions: Record<number, string> = {
    401: 'Anda harus masuk dengan kredensial yang sah untuk mengakses gerbang ini.',
    403: 'Halaman ini merupakan area terproteksi khusus peran Administrator / Oracle Arbiter.',
    404: 'Jejak atau halaman yang Anda tuju tidak ada dalam arsip Sanctum.',
    500: 'Layanan Sanctum sedang mengalami kendala teknis. Tim teknis sedang menanganinya.'
  };
</script>

<svelte:head>
  <title>{status} - {errorTitles[status] ?? 'Kesalahan'} | EyesOfPriestess</title>
</svelte:head>

<div class="min-h-screen bg-[var(--color-canvas)] flex items-center justify-center p-6">
  <div class="max-w-md w-full text-center flex flex-col items-center gap-6 bg-[var(--color-surface-card)] border border-[var(--color-hairline)] rounded-[var(--radius-xl)] p-8 shadow-sm">
    <!-- Status Badge -->
    <div class="w-16 h-16 rounded-2xl flex items-center justify-center {status === 403 || status === 401 ? 'bg-[var(--color-warning)]/10 text-[var(--color-warning)]' : status === 404 ? 'bg-[var(--color-primary)]/10 text-[var(--color-primary)]' : 'bg-[var(--color-error)]/10 text-[var(--color-error)]'} font-mono text-2xl font-bold">
      {status}
    </div>

    <!-- Title & Description -->
    <div>
      <h1 class="font-display text-[var(--text-display-sm)] text-[var(--color-ink)] tracking-[-0.01em]">
        {errorTitles[status] ?? 'Terjadi Kesalahan'}
      </h1>
      <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)] mt-2">
        {errorDescriptions[status] ?? message}
      </p>
    </div>

    <!-- Actions based on status -->
    <div class="flex flex-col sm:flex-row gap-3 w-full mt-2">
      {#if status === 401}
        <a
          href="/login"
          class="flex-1 inline-flex items-center justify-center h-10 px-4 bg-[var(--color-primary)] text-white font-sans text-[13px] font-medium rounded-[var(--radius-md)] hover:bg-[var(--color-primary-active)] transition-colors"
        >
          Masuk Sekarang
        </a>
      {:else if status === 403}
        <a
          href="/dashboard"
          class="flex-1 inline-flex items-center justify-center h-10 px-4 bg-[var(--color-primary)] text-white font-sans text-[13px] font-medium rounded-[var(--radius-md)] hover:bg-[var(--color-primary-active)] transition-colors"
        >
          Kembali ke Dashboard
        </a>
      {:else if status === 404}
        <a
          href="/dashboard"
          class="flex-1 inline-flex items-center justify-center h-10 px-4 bg-[var(--color-primary)] text-white font-sans text-[13px] font-medium rounded-[var(--radius-md)] hover:bg-[var(--color-primary-active)] transition-colors"
        >
          Kembali ke Beranda
        </a>
      {:else}
        <button
          onclick={() => window.location.reload()}
          class="flex-1 inline-flex items-center justify-center h-10 px-4 bg-[var(--color-primary)] text-white font-sans text-[13px] font-medium rounded-[var(--radius-md)] hover:bg-[var(--color-primary-active)] transition-colors"
        >
          Muat Ulang Halaman
        </button>
      {/if}
      <a
        href="/"
        class="inline-flex items-center justify-center h-10 px-4 bg-[var(--color-surface-soft)] text-[var(--color-ink)] font-sans text-[13px] font-medium rounded-[var(--radius-md)] hover:bg-[var(--color-hairline-soft)] transition-colors"
      >
        Landing Page
      </a>
    </div>
  </div>
</div>
