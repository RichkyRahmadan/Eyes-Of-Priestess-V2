<script lang="ts">
  import { sealStore } from '$lib/stores/seal';
  import { goto } from '$app/navigation';
  import { Gem, Shield, Handshake, Scale, ArrowRight, Check } from "@lucide/svelte";
  import { onMount } from 'svelte';

  onMount(() => {
    if ($sealStore.isAuthenticated) goto('/sanctum/observatory');
  });

  const features = [
    {
      icon: Handshake,
      title: 'Covenant Forge',
      desc: 'Buat perjanjian escrow terenkripsi dengan kode undangan unik dan mekanisme auto-release.'
    },
    {
      icon: Shield,
      title: 'Seal Sanctum',
      desc: 'Otentikasi multi-lapisan dengan JWT berdurasi singkat dan refresh seal terenkripsi.'
    },
    {
      icon: Scale,
      title: 'Judgment Sanctum',
      desc: 'Panel Oracle eksklusif untuk resolusi sengketa — Release, Refund, atau Split.'
    }
  ];

  const steps = [
    { num: '01', title: 'Forge',   desc: 'Buat Covenant dengan nilai dan peran (Buyer/Seller)' },
    { num: '02', title: 'Accept',  desc: 'Counterparty bergabung lewat kode undangan' },
    { num: '03', title: 'Seal',    desc: 'Buyer melakukan deposit ke Vault Escrow' },
    { num: '04', title: 'Fulfill', desc: 'Seller kirim barang, Buyer konfirmasi — dana dilepas' }
  ];
</script>

<svelte:head>
  <title>EyesOfPriestess — The Covenant Protocol</title>
  <meta name="description" content="Platform escrow mystical berbasis Covenant untuk transaksi peer-to-peer yang aman dan terpercaya." />
</svelte:head>

<div class="landing">
  <!-- Navigation -->
  <nav class="landing-nav">
    <div class="nav-brand">
      <Gem size={20} color="var(--gold-400)" />
      <span>EyesOfPriestess</span>
    </div>
    <div class="nav-actions">
      <a href="/rite" class="btn btn-ghost btn-sm">Masuk</a>
      <a href="/rite/forge" class="btn btn-gold btn-sm">Daftar</a>
    </div>
  </nav>

  <!-- Hero -->
  <section class="hero">
    <div class="hero-glow hero-glow-1"></div>
    <div class="hero-glow hero-glow-2"></div>

    <div class="hero-content animate-fade-in">
      <div class="hero-eyebrow">
        <span class="eyebrow-dot"></span>
        The Covenant Protocol
      </div>

      <h1 class="hero-title">
        <span class="gradient-gold">The Eyes</span><br />
        of the Priestess<br />
        <span class="text-secondary" style="font-size:0.7em">See All Transactions</span>
      </h1>

      <p class="hero-desc">
        Platform escrow mystical yang menjaga setiap transaksi peer-to-peer
        di bawah tatapan abadi sang Priestess. Aman, transparan, dan adil.
      </p>

      <div class="hero-cta">
        <a href="/rite/forge" class="btn btn-gold btn-lg">
          Attuning Your Seal <ArrowRight size={18} />
        </a>
        <a href="/rite" class="btn btn-ghost btn-lg">
          Perform the Rite
        </a>
      </div>

      <div class="hero-stats">
        <div class="stat">
          <p class="stat-value gradient-gold">100%</p>
          <p class="stat-label">Escrow Secured</p>
        </div>
        <div class="stat-sep"></div>
        <div class="stat">
          <p class="stat-value gradient-void">&lt; 150ms</p>
          <p class="stat-label">API Response</p>
        </div>
        <div class="stat-sep"></div>
        <div class="stat">
          <p class="stat-value" style="color:var(--success)">Auto-Release</p>
          <p class="stat-label">48h Protection</p>
        </div>
      </div>
    </div>
  </section>

  <!-- Features -->
  <section class="section">
    <div class="section-header">
      <p class="section-eyebrow">Sanctum Features</p>
      <h2 class="section-title">The Five Sanctums of Power</h2>
    </div>
    <div class="features-grid">
      {#each features as f}
        <div class="feature-card glass-card">
          <div class="feature-icon">
            <f.icon size={24} color="var(--gold-400)" />
          </div>
          <h3 class="feature-title">{f.title}</h3>
          <p class="feature-desc">{f.desc}</p>
        </div>
      {/each}
    </div>
  </section>

  <!-- How it works -->
  <section class="section section-alt">
    <div class="section-header">
      <p class="section-eyebrow">The Sacred Ritual</p>
      <h2 class="section-title">How the Covenant Works</h2>
    </div>
    <div class="steps-row">
      {#each steps as step, i}
        <div class="step-card">
          <div class="step-num gradient-gold">{step.num}</div>
          <h3 class="step-title">{step.title}</h3>
          <p class="step-desc">{step.desc}</p>
        </div>
        {#if i < steps.length - 1}
          <div class="step-arrow"><ArrowRight size={18} color="var(--border-strong)" /></div>
        {/if}
      {/each}
    </div>
  </section>

  <!-- CTA Banner -->
  <section class="cta-section">
    <div class="cta-inner">
      <h2 class="cta-title gradient-gold">Ready to Forge a Covenant?</h2>
      <p class="cta-desc">Bergabunglah dan mulai transaksi di bawah perlindungan Priestess.</p>
      <a href="/rite/forge" class="btn btn-gold btn-lg">
        Forge Your Identity <ArrowRight size={18} />
      </a>
    </div>
  </section>

  <!-- Footer -->
  <footer class="landing-footer">
    <div class="footer-brand">
      <Gem size={16} color="var(--gold-400)" />
      <span>EyesOfPriestess</span>
    </div>
    <p class="footer-copy">© 2025 The Covenant Protocol. All transactions are sealed.</p>
  </footer>
</div>

<style>
.landing {
  min-height: 100vh;
  background: var(--surface-900);
  overflow-x: hidden;
}

/* Nav */
.landing-nav {
  position: sticky;
  top: 0;
  z-index: 40;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 clamp(1.5rem, 5vw, 5rem);
  height: 64px;
  background: rgba(11,9,20,0.85);
  backdrop-filter: blur(16px);
  border-bottom: 1px solid var(--border-subtle);
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-family: 'Cinzel', serif;
  font-weight: 700;
  font-size: 1rem;
  color: var(--text-primary);
}

.nav-actions { display: flex; align-items: center; gap: 0.75rem; }

/* Hero */
.hero {
  position: relative;
  min-height: calc(100vh - 64px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4rem clamp(1.5rem, 5vw, 5rem);
  overflow: hidden;
}

.hero-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
}

.hero-glow-1 {
  width: 600px; height: 600px;
  top: -100px; left: -100px;
  background: radial-gradient(circle, rgba(109,40,217,0.15) 0%, transparent 70%);
}

.hero-glow-2 {
  width: 400px; height: 400px;
  bottom: 0; right: 0;
  background: radial-gradient(circle, rgba(245,158,11,0.08) 0%, transparent 70%);
}

.hero-content {
  max-width: 720px;
  text-align: center;
  position: relative;
  z-index: 1;
}

.hero-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8125rem;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--gold-400);
  margin-bottom: 1.5rem;
}

.eyebrow-dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: var(--gold-400);
  animation: pulse-glow 2s ease-in-out infinite;
}

.hero-title {
  font-size: clamp(2.5rem, 7vw, 4.5rem);
  font-weight: 700;
  line-height: 1.05;
  margin-bottom: 1.5rem;
}

.hero-desc {
  font-size: 1.125rem;
  color: var(--text-secondary);
  max-width: 520px;
  margin: 0 auto 2.5rem;
  line-height: 1.7;
}

.hero-cta {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: 3rem;
}

.hero-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2rem;
  flex-wrap: wrap;
}

.stat { text-align: center; }
.stat-value { font-size: 1.25rem; font-weight: 800; line-height: 1; margin-bottom: 0.25rem; }
.stat-label { font-size: 0.75rem; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.08em; }
.stat-sep { width: 1px; height: 40px; background: var(--border-default); }

/* Sections */
.section {
  padding: 5rem clamp(1.5rem, 5vw, 5rem);
}

.section-alt { background: var(--surface-800); }

.section-header { text-align: center; margin-bottom: 3rem; }
.section-eyebrow {
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.15em;
  text-transform: uppercase;
  color: var(--gold-400);
  margin-bottom: 0.75rem;
}
.section-title { font-size: clamp(1.5rem, 4vw, 2.25rem); }

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
  max-width: 1100px;
  margin: 0 auto;
}

.feature-card { transition: transform var(--transition-normal), box-shadow var(--transition-normal); }
.feature-card:hover { transform: translateY(-4px); box-shadow: var(--shadow-glow); }

.feature-icon {
  width: 52px; height: 52px;
  border-radius: var(--radius-lg);
  background: rgba(245,158,11,0.08);
  border: 1px solid var(--border-gold);
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 1rem;
}

.feature-title { font-size: 1.125rem; margin-bottom: 0.5rem; }
.feature-desc  { font-size: 0.9rem; color: var(--text-muted); line-height: 1.6; }

/* Steps */
.steps-row {
  display: flex;
  align-items: flex-start;
  justify-content: center;
  gap: 1rem;
  max-width: 1000px;
  margin: 0 auto;
  flex-wrap: wrap;
}

.step-card {
  flex: 1;
  min-width: 180px;
  max-width: 220px;
  text-align: center;
  padding: 1.5rem 1rem;
}

.step-num { font-size: 2rem; font-weight: 800; line-height: 1; margin-bottom: 0.75rem; }
.step-title { font-size: 1rem; font-weight: 600; margin-bottom: 0.5rem; }
.step-desc  { font-size: 0.8125rem; color: var(--text-muted); line-height: 1.5; }

.step-arrow { align-self: center; opacity: 0.4; }

/* CTA */
.cta-section {
  padding: 5rem clamp(1.5rem, 5vw, 5rem);
  text-align: center;
  background: linear-gradient(135deg, rgba(109,40,217,0.08), rgba(245,158,11,0.04));
}

.cta-inner { max-width: 600px; margin: 0 auto; }
.cta-title { font-size: clamp(1.75rem, 4vw, 2.5rem); margin-bottom: 1rem; }
.cta-desc  { color: var(--text-secondary); margin-bottom: 2rem; font-size: 1.0625rem; }

/* Footer */
.landing-footer {
  padding: 2rem clamp(1.5rem, 5vw, 5rem);
  border-top: 1px solid var(--border-subtle);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 1rem;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-family: 'Cinzel', serif;
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.footer-copy { font-size: 0.8125rem; color: var(--text-muted); }

@keyframes pulse-glow {
  0%, 100% { box-shadow: 0 0 4px rgba(251,191,36,0.3); }
  50%       { box-shadow: 0 0 12px rgba(251,191,36,0.7); }
}
</style>

