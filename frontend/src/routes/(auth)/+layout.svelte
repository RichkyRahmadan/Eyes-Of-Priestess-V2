<script lang="ts">
  import { Shield } from '@lucide/svelte';
  let { children } = $props();
</script>

<div class="min-h-screen bg-canvas flex flex-col justify-center items-center p-6">
  <!-- Centered Brand Banner -->
  <div class="mb-8 text-center space-y-2">
    <a href="/" class="inline-flex items-center gap-2">
      <div class="w-10 h-10 rounded-xl bg-primary-coral flex items-center justify-center text-white shadow-soft">
        <Shield size={22} />
      </div>
      <span class="font-display text-3xl font-bold tracking-tight text-ink">
        EyesOfPriestess
      </span>
    </a>
    <p class="text-xs text-muted font-sans uppercase tracking-widest">Platform Escrow E-Wallet Terpercaya</p>
  </div>

  <!-- Main Card Container -->
  <div class="w-full max-w-md card-editorial shadow-elevated">
    {@render children()}
  </div>

  <!-- Footer link -->
  <div class="mt-8 text-xs text-muted text-center">
    <a href="/" class="hover:text-ink underline">← Kembali ke Halaman Utama</a>
  </div>
</div>
