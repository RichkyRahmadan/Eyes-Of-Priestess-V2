<script lang="ts">
  import Button from '$lib/components/ui/Button.svelte';
</script>

<svelte:head>
  <title>EyesOfPriestess - Transaksi P2P Tanpa Rasa Khawatir</title>
  <meta
    name="description"
    content="Platform escrow P2P terpercaya untuk transaksi digital yang aman. Dana Anda terlindungi sampai barang atau jasa diterima."
  />
</svelte:head>

<!-- Navigation -->
<nav
  class="h-16 bg-[var(--color-canvas)] border-b border-[var(--color-hairline-soft)] sticky top-0 z-50"
>
  <div class="max-w-[1200px] mx-auto px-6 h-full flex items-center justify-between">
    <a href="/">
      <span
        class="font-display text-[var(--text-display-sm)] text-[var(--color-ink)] tracking-tight leading-none"
      >
        Eyes<span class="text-[var(--color-primary)]">Of</span>Priestess
      </span>
    </a>
    <div class="flex items-center gap-4">
      <a
        href="/login"
        class="font-sans font-medium text-[14px] text-[var(--color-body)] hover:text-[var(--color-primary)] transition-colors"
      >
        Masuk
      </a>
      <Button variant="primary" size="sm">
        <a href="/register" class="text-inherit no-underline">Daftar Gratis</a>
      </Button>
    </div>
  </div>
</nav>

<!-- Hero: Asymmetric split layout -->
<section class="bg-[var(--color-canvas)] pt-20 pb-section">
  <div class="max-w-[1200px] mx-auto px-6 grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
    <!-- Left: copy -->
    <div>
      <h1
        class="font-display text-[var(--text-display-xl)] text-[var(--color-ink)] leading-[1.05] tracking-[-0.02em] mb-6"
        style="font-size: clamp(40px, 6vw, 64px);"
      >
        Transaksi P2P<br />
        <span class="text-[var(--color-primary)] italic">Tanpa Rasa Khawatir</span>
      </h1>
      <p
        class="font-sans text-[var(--text-body-md)] text-[var(--color-body)] max-w-[480px] leading-relaxed mb-8"
      >
        Dana Anda terlindungi di escrow sampai barang atau jasa benar-benar diterima. Tidak ada lagi risiko penipuan dalam transaksi digital.
      </p>
      <div class="flex items-center gap-3 flex-wrap">
        <Button variant="primary" size="lg">
          <a href="/register" class="text-inherit no-underline">Mulai Sekarang</a>
        </Button>
        <Button variant="secondary" size="lg">
          <a href="#cara-kerja" class="text-inherit no-underline">Pelajari Cara Kerja</a>
        </Button>
      </div>
    </div>

    <!-- Right: dark product mockup card -->
    <div
      class="bg-[var(--color-surface-dark)] rounded-[var(--radius-xl)] p-8 text-[var(--color-on-dark)] shadow-[var(--shadow-elevated)]"
    >
      <div class="flex items-center justify-between mb-6">
        <div>
          <p class="font-mono text-[11px] text-[var(--color-on-dark-soft)] tracking-wider uppercase mb-1">
            Room Escrow
          </p>
          <p class="font-sans font-medium text-[var(--text-title-md)]">EOP-DGT842</p>
        </div>
        <span
          class="inline-flex items-center px-2.5 py-0.5 rounded-[var(--radius-pill)] text-[13px] font-medium bg-[var(--color-accent-teal)]/15 text-[var(--color-accent-teal)]"
        >
          Dana Terkunci
        </span>
      </div>

      <div class="space-y-3 mb-6">
        <div
          class="bg-[var(--color-surface-dark-elevated)] rounded-[var(--radius-md)] p-4"
        >
          <div class="flex justify-between items-start">
            <div>
              <p class="font-sans text-[13px] text-[var(--color-on-dark-soft)]">Item</p>
              <p class="font-sans font-medium text-[var(--text-body-md)] mt-0.5">
                Akun Mobile Legends Diamond 5000
              </p>
            </div>
            <div class="text-right">
              <p class="font-sans text-[13px] text-[var(--color-on-dark-soft)]">Nilai</p>
              <p class="font-display text-[var(--text-title-lg)] mt-0.5">Rp 450.000</p>
            </div>
          </div>
        </div>
        <div
          class="bg-[var(--color-surface-dark-elevated)] rounded-[var(--radius-md)] p-4 flex items-center gap-3"
        >
          <div
            class="w-8 h-8 rounded-full bg-[var(--color-success)]/15 flex items-center justify-center shrink-0"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 256 256" fill="var(--color-success)"><path d="M208,40H48A16,16,0,0,0,32,56V120c0,88,88,128,88,128s88-40,88-128V56A16,16,0,0,0,208,40Zm0,80c0,68.9-62.6,104.66-80,113.31C110.6,224.66,48,189,48,120V56H208ZM82.34,130.34,104,152l69.66-69.66a8,8,0,0,1,11.32,11.32l-75.32,75.32a8,8,0,0,1-11.32,0l-27.32-27.32a8,8,0,0,1,11.32-11.32Z"/></svg>
          </div>
          <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-on-dark)]">
            Dana di-hold oleh EyesOfPriestess. Seller dapat mengirim barang dengan aman.
          </p>
        </div>
      </div>

      <div class="flex gap-2">
        <button
          class="flex-1 h-9 bg-[var(--color-surface-dark-elevated)] rounded-[var(--radius-md)] font-sans text-[13px] text-[var(--color-on-dark-soft)] hover:text-[var(--color-on-dark)] transition-colors"
        >
          Lihat Chat
        </button>
        <button
          class="flex-1 h-9 bg-[var(--color-primary)] rounded-[var(--radius-md)] font-sans text-[13px] font-medium text-white hover:bg-[var(--color-primary-active)] transition-colors"
        >
          Konfirmasi Terima
        </button>
      </div>
    </div>
  </div>
</section>

<!-- How It Works: asymmetric, no 3-equal-cards -->
<section id="cara-kerja" class="bg-[var(--color-surface-card)] py-section">
  <div class="max-w-[1200px] mx-auto px-6">
    <h2
      class="font-display text-[var(--text-display-md)] text-[var(--color-ink)] tracking-[-0.015em] mb-16 max-w-[560px]"
    >
      Tiga langkah menuju transaksi yang aman
    </h2>

    <div class="grid grid-cols-1 md:grid-cols-12 gap-6">
      <!-- Step 1: wide -->
      <div
        class="md:col-span-5 bg-[var(--color-canvas)] rounded-[var(--radius-xl)] p-8 flex flex-col gap-4"
      >
        <div
          class="w-10 h-10 rounded-[var(--radius-md)] bg-[var(--color-primary)]/10 flex items-center justify-center"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 256 256" fill="var(--color-primary)"><path d="M224,128a8,8,0,0,1-8,8H136v80a8,8,0,0,1-16,0V136H40a8,8,0,0,1,0-16h80V40a8,8,0,0,1,16,0v80h80A8,8,0,0,1,224,128Z"/></svg>
        </div>
        <div>
          <p class="font-mono text-[11px] text-[var(--color-muted)] tracking-wider uppercase mb-2">Langkah 01</p>
          <h3 class="font-sans font-medium text-[var(--text-title-lg)] text-[var(--color-ink)] mb-2">
            Buat Room Escrow
          </h3>
          <p class="font-sans text-[var(--text-body-md)] text-[var(--color-body)] leading-relaxed">
            Seller membuat room dengan detail transaksi: nama barang, harga, dan mengundang buyer. Kode room unik dibuat otomatis.
          </p>
        </div>
      </div>

      <!-- Steps 2 & 3: stacked -->
      <div class="md:col-span-7 flex flex-col gap-6">
        <div
          class="bg-[var(--color-surface-dark)] rounded-[var(--radius-xl)] p-8 flex items-start gap-5"
        >
          <div
            class="w-10 h-10 rounded-[var(--radius-md)] bg-[var(--color-accent-teal)]/15 flex items-center justify-center shrink-0"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 256 256" fill="var(--color-accent-teal)"><path d="M216,72H56a8,8,0,0,1,0-16H192a8,8,0,0,0,0-16H56A24,24,0,0,0,32,64V192a24,24,0,0,0,24,24H216a16,16,0,0,0,16-16V88A16,16,0,0,0,216,72Zm0,128H56a8,8,0,0,1-8-8V86.63A23.84,23.84,0,0,0,56,88H216Zm-48-60a12,12,0,1,1,12,12A12,12,0,0,1,168,140Z"/></svg>
          </div>
          <div>
            <p class="font-mono text-[11px] text-[var(--color-on-dark-soft)] tracking-wider uppercase mb-2">Langkah 02</p>
            <h3 class="font-sans font-medium text-[var(--text-title-md)] text-[var(--color-on-dark)] mb-1">
              Dana Diamankan Otomatis
            </h3>
            <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-on-dark-soft)] leading-relaxed">
              Buyer mendanai room. Dana langsung masuk ke escrow kami, bukan ke seller. Seller baru bisa melihat dana setelah buyer konfirmasi.
            </p>
          </div>
        </div>

        <div
          class="bg-[var(--color-canvas)] border border-[var(--color-hairline)] rounded-[var(--radius-xl)] p-8 flex items-start gap-5"
        >
          <div
            class="w-10 h-10 rounded-[var(--radius-md)] bg-[var(--color-success)]/10 flex items-center justify-center shrink-0"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 256 256" fill="var(--color-success)"><path d="M173.66,98.34a8,8,0,0,1,0,11.32l-56,56a8,8,0,0,1-11.32,0l-24-24a8,8,0,0,1,11.32-11.32L112,148.69l50.34-50.35A8,8,0,0,1,173.66,98.34ZM232,128A104,104,0,1,1,128,24,104.11,104.11,0,0,1,232,128Zm-16,0a88,88,0,1,0-88,88A88.1,88.1,0,0,0,216,128Z"/></svg>
          </div>
          <div>
            <p class="font-mono text-[11px] text-[var(--color-muted)] tracking-wider uppercase mb-2">Langkah 03</p>
            <h3 class="font-sans font-medium text-[var(--text-title-md)] text-[var(--color-ink)] mb-1">
              Barang Diterima, Dana Dilepas
            </h3>
            <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-body)] leading-relaxed">
              Buyer konfirmasi penerimaan, dana otomatis dikirim ke seller. Jika ada masalah, buka dispute dan tim kami akan menengahi.
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</section>

<!-- Trust indicators: dark navy band -->
<section class="bg-[var(--color-surface-dark)] py-section">
  <div class="max-w-[1200px] mx-auto px-6">
    <div class="grid grid-cols-1 md:grid-cols-3 gap-12">
      <div>
        <p
          class="font-display text-[var(--text-display-lg)] text-[var(--color-on-dark)] leading-none tracking-[-0.02em]"
        >
          12.483
        </p>
        <p class="font-sans text-[var(--text-body-md)] text-[var(--color-on-dark-soft)] mt-2">
          transaksi berhasil diselesaikan
        </p>
      </div>
      <div>
        <p
          class="font-display text-[var(--text-display-lg)] text-[var(--color-on-dark)] leading-none tracking-[-0.02em]"
        >
          Rp 4,7M
        </p>
        <p class="font-sans text-[var(--text-body-md)] text-[var(--color-on-dark-soft)] mt-2">
          nilai escrow total yang diamankan
        </p>
      </div>
      <div>
        <p
          class="font-display text-[var(--text-display-lg)] text-[var(--color-on-dark)] leading-none tracking-[-0.02em]"
        >
          3,1%
        </p>
        <p class="font-sans text-[var(--text-body-md)] text-[var(--color-on-dark-soft)] mt-2">
          tingkat dispute, selalu diselesaikan adil
        </p>
      </div>
    </div>
  </div>
</section>

<!-- Security features: cream, varied layout -->
<section class="bg-[var(--color-canvas)] py-section">
  <div class="max-w-[1200px] mx-auto px-6">
    <div class="grid grid-cols-1 lg:grid-cols-12 gap-12 items-start">
      <div class="lg:col-span-4">
        <h2
          class="font-display text-[var(--text-display-md)] text-[var(--color-ink)] tracking-[-0.015em] mb-4"
        >
          Keamanan di setiap lapisan
        </h2>
        <p class="font-sans text-[var(--text-body-md)] text-[var(--color-body)] leading-relaxed">
          Kami membangun EyesOfPriestess dengan standar keamanan fintech. Setiap rupiah Anda terlindungi.
        </p>
      </div>
      <div class="lg:col-span-8 grid grid-cols-1 sm:grid-cols-2 gap-4">
        {#each [
          { title: 'PIN 6 Digit', desc: 'Setiap transaksi kritis memerlukan PIN. Salah 3x, akun terkunci otomatis 15 menit.' },
          { title: 'Insta-Ban System', desc: 'Akun mencurigakan diblokir instan via Redis blacklist tanpa menunggu token expire.' },
          { title: 'Auto-Release', desc: 'Dana otomatis dilepas ke seller setelah 24 jam jika buyer tidak merespons konfirmasi.' },
          { title: 'Dispute Resolution', desc: 'Tim moderator kami menengahi sengketa dan memutuskan berdasarkan bukti yang diunggah.' }
        ] as feature}
          <div
            class="bg-[var(--color-surface-card)] rounded-[var(--radius-lg)] p-6 hover:-translate-y-0.5 transition-transform duration-150"
          >
            <h3 class="font-sans font-medium text-[var(--text-title-sm)] text-[var(--color-ink)] mb-2">
              {feature.title}
            </h3>
            <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-body)] leading-relaxed">
              {feature.desc}
            </p>
          </div>
        {/each}
      </div>
    </div>
  </div>
</section>

<!-- CTA band: coral -->
<section class="bg-[var(--color-primary)] py-section">
  <div class="max-w-[1200px] mx-auto px-6 flex flex-col items-center text-center">
    <h2
      class="font-display text-[var(--text-display-md)] text-[var(--color-on-primary)] tracking-[-0.015em] mb-4 max-w-[560px]"
    >
      Siap bertransaksi dengan tenang?
    </h2>
    <p class="font-sans text-[var(--text-body-md)] text-white/80 mb-8 max-w-[440px]">
      Bergabung dengan ribuan pengguna yang sudah mempercayakan transaksinya kepada EyesOfPriestess.
    </p>
    <a
      href="/register"
      class="inline-flex items-center justify-center h-12 px-8 bg-[var(--color-canvas)] text-[var(--color-ink)] font-sans font-medium text-[15px] rounded-[var(--radius-md)] hover:bg-[var(--color-surface-soft)] active:scale-[0.98] transition-all duration-150"
    >
      Daftar Gratis
    </a>
  </div>
</section>

<!-- Footer: dark navy -->
<footer class="bg-[var(--color-surface-dark)] py-16">
  <div class="max-w-[1200px] mx-auto px-6">
    <div class="grid grid-cols-2 md:grid-cols-4 gap-8 mb-12">
      <div class="col-span-2 md:col-span-1">
        <span
          class="font-display text-[var(--text-display-sm)] text-[var(--color-on-dark)] tracking-tight"
        >
          Eyes<span class="text-[var(--color-primary)]">Of</span>Priestess
        </span>
        <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-on-dark-soft)] mt-3 leading-relaxed">
          Platform escrow P2P untuk transaksi digital yang aman dan terpercaya.
        </p>
      </div>
      {#each [
        { heading: 'Produk', links: ['Dashboard', 'Dompet', 'Room Escrow', 'Chat', 'Riwayat'] },
        { heading: 'Keamanan', links: ['Kebijakan Privasi', 'Syarat & Ketentuan', 'Keamanan Platform', 'Laporan Bug'] },
        { heading: 'Dukungan', links: ['Pusat Bantuan', 'Hubungi Kami', 'Status Layanan', 'Blog'] }
      ] as col}
        <div>
          <p class="font-sans font-medium text-[var(--text-body-sm)] text-[var(--color-on-dark)] mb-3">
            {col.heading}
          </p>
          <ul class="space-y-2">
            {#each col.links as link}
              <li>
                <a
                  href="/"
                  class="font-sans text-[var(--text-body-sm)] text-[var(--color-on-dark-soft)] hover:text-[var(--color-on-dark)] transition-colors"
                >
                  {link}
                </a>
              </li>
            {/each}
          </ul>
        </div>
      {/each}
    </div>
    <div class="border-t border-[var(--color-surface-dark-elevated)] pt-8 flex flex-col sm:flex-row items-center justify-between gap-4">
      <p class="font-sans text-[var(--text-caption)] text-[var(--color-on-dark-soft)]">
        &copy; 2026 EyesOfPriestess. Hak cipta dilindungi.
      </p>
      <p class="font-sans text-[var(--text-caption)] text-[var(--color-on-dark-soft)]">
        Dibuat dengan sepenuh hati untuk transaksi yang aman.
      </p>
    </div>
  </div>
</footer>
