<script lang="ts">
  import { Shield, ArrowRight, Lock, Scale, Zap, CheckCircle2, DollarSign } from '@lucide/svelte';
</script>

<div class="min-h-screen bg-canvas text-ink flex flex-col justify-between">
  <!-- Header -->
  <header class="h-20 px-8 max-w-7xl mx-auto w-full flex items-center justify-between border-b border-hairline">
    <div class="flex items-center gap-3">
      <div class="w-10 h-10 rounded-xl bg-primary-coral flex items-center justify-center text-white shadow-card">
        <Shield size={22} />
      </div>
      <span class="font-display text-3xl font-bold tracking-tight text-ink">
        EyesOfPriestess
      </span>
    </div>

    <div class="flex items-center gap-4">
      <a href="/login" class="px-5 py-2.5 rounded-lg text-sm font-semibold text-body hover:text-ink transition-colors">
        Masuk
      </a>
      <a href="/register" class="btn-editorial-primary text-sm font-semibold px-6 py-2.5">
        Mulai Rekber
        <ArrowRight size={16} />
      </a>
    </div>
  </header>

  <!-- Hero Section -->
  <main class="max-w-7xl mx-auto px-8 py-20 w-full grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
    <div class="lg:col-span-7 space-y-8">
      <div class="inline-flex items-center gap-2 px-4 py-1.5 rounded-pill bg-surface-card border border-hairline text-xs font-semibold uppercase tracking-wider text-coral">
        <span class="w-2 h-2 rounded-full bg-coral animate-pulse"></span>
        Platform Escrow & E-Wallet Generasi Baru
      </div>

      <h1 class="font-display text-5xl sm:text-6xl lg:text-7xl font-bold leading-none tracking-tight text-ink">
        Jual Beli Digital <br />
        <span class="text-coral italic font-serif font-normal">Tanpa Khawatir</span> Penipuan.
      </h1>

      <p class="text-lg text-body max-w-2xl leading-relaxed">
        EyesOfPriestess mengamankan setiap transaksi online dengan sistem escrow terintegrasi, instant-ban security, dan dompet digital reaktif berkecepatan tinggi.
      </p>

      <div class="flex flex-wrap items-center gap-4 pt-4">
        <a href="/register" class="btn-editorial-primary text-base font-semibold px-8 py-3.5 shadow-card">
          Buka Room Rekber
          <ArrowRight size={18} />
        </a>
        <a href="/login" class="btn-editorial-secondary text-base font-semibold px-8 py-3.5">
          Lihat Demo Dashboard
        </a>
      </div>

      <!-- Key stats -->
      <div class="grid grid-cols-3 gap-6 pt-8 border-t border-hairline max-w-lg">
        <div>
          <div class="font-display text-3xl font-bold text-ink">Rp 0</div>
          <div class="text-xs text-muted">Risiko Penipuan</div>
        </div>
        <div>
          <div class="font-display text-3xl font-bold text-ink">100%</div>
          <div class="text-xs text-muted">Jaminan Dana Aman</div>
        </div>
        <div>
          <div class="font-display text-3xl font-bold text-ink">&lt; 1 Detik</div>
          <div class="text-xs text-muted">Konfirmasi Real-time</div>
        </div>
      </div>
    </div>

    <!-- Right Hero Graphic / Mock Card -->
    <div class="lg:col-span-5 relative">
      <div class="card-dark-chrome p-8 space-y-6 shadow-elevated relative z-10">
        <div class="flex items-center justify-between border-b border-surface-dark-elevated pb-4">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-full bg-primary-coral text-white flex items-center justify-center font-display font-bold">
              EO
            </div>
            <div>
              <div class="text-xs font-mono uppercase tracking-wider text-coral">ROOM #EOP-8821</div>
              <div class="text-sm font-semibold text-on-dark">Escrow Transaction Hold</div>
            </div>
          </div>
          <span class="px-3 py-1 rounded-pill bg-success/20 text-success text-xs font-semibold">FUNDED</span>
        </div>

        <div class="space-y-4">
          <div class="flex justify-between items-end">
            <span class="text-xs text-on-dark-soft uppercase">Nominal Terkunci</span>
            <span class="font-display text-3xl font-bold text-on-dark">Rp 1.500.000</span>
          </div>
          <div class="text-xs text-on-dark-soft leading-relaxed">
            Barang: Akun Game / Produk Digital Sultan <br />
            Status Dana: Dikunci di Vault hingga Buyer Konfirmasi Penerimaan.
          </div>
        </div>

        <div class="pt-4 border-t border-surface-dark-elevated flex items-center justify-between text-xs text-on-dark-soft">
          <span class="flex items-center gap-1.5 text-success">
            <CheckCircle2 size={14} /> Pin Verified
          </span>
          <span class="font-mono">Auto-Release: 24:00:00</span>
        </div>
      </div>

      <!-- Accent Decorative Backdrop -->
      <div class="absolute -inset-4 bg-surface-card rounded-3xl transform rotate-2 -z-10 border border-hairline"></div>
    </div>
  </main>

  <!-- Features Grid -->
  <section class="max-w-7xl mx-auto px-8 py-16 w-full border-t border-hairline space-y-12">
    <div class="text-center max-w-2xl mx-auto space-y-3">
      <h2 class="font-display text-4xl font-bold text-ink">Mengapa EyesOfPriestess?</h2>
      <p class="text-body text-base">Arsitektur terdistribusi modern dirancang khusus untuk keamanan transaksi P2P tinggi.</p>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
      <div class="card-editorial space-y-4">
        <div class="w-12 h-12 rounded-xl bg-surface-cream-strong flex items-center justify-center text-coral">
          <Lock size={24} />
        </div>
        <h3 class="font-display text-2xl font-bold text-ink">Smart Escrow Vault</h3>
        <p class="text-sm text-body leading-relaxed">
          Dana pembeli dikunci secara aman di dompet khusus dan hanya dicairkan kepada penjual setelah konfirmasi barang diterima tanpa cacat.
        </p>
      </div>

      <div class="card-editorial space-y-4">
        <div class="w-12 h-12 rounded-xl bg-surface-cream-strong flex items-center justify-center text-coral">
          <Scale size={24} />
        </div>
        <h3 class="font-display text-2xl font-bold text-ink">Pusat Arbitrase Dispute</h3>
        <p class="text-sm text-body leading-relaxed">
          Jika terjadi ketidaksesuaian barang, sistem arbitrase yang adil dan efisien memproses bukti transaksi secara akurat dan transparan.
        </p>
      </div>

      <div class="card-editorial space-y-4">
        <div class="w-12 h-12 rounded-xl bg-surface-cream-strong flex items-center justify-center text-coral">
          <Zap size={24} />
        </div>
        <h3 class="font-display text-2xl font-bold text-ink">Instant-Ban Security</h3>
        <p class="text-sm text-body leading-relaxed">
          Sistem deteksi penipuan reaktif secara otomatis membekukan akun yang terindikasi kecurangan melalui lapisan keamanan Redis blacklist.
        </p>
      </div>
    </div>
  </section>

  <!-- Footer -->
  <footer class="border-t border-hairline py-8 px-8 max-w-7xl mx-auto w-full flex flex-col sm:flex-row items-center justify-between text-xs text-muted">
    <div>
      © 2026 EyesOfPriestess — Escrow E-Wallet Platform. Warm Editorial Edition.
    </div>
    <div class="flex items-center gap-6 mt-4 sm:mt-0">
      <a href="/login" class="hover:text-ink transition-colors">Masuk</a>
      <a href="/register" class="hover:text-ink transition-colors">Daftar</a>
      <a href="/rooms" class="hover:text-ink transition-colors">Escrow Rooms</a>
    </div>
  </footer>
</div>
