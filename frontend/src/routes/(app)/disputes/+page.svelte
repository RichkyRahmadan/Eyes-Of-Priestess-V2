<script lang="ts">
  import { AlertTriangle, ShieldCheck, Scale, FileText, Upload, ChevronRight } from '@lucide/svelte';

  let disputes = $state([
    {
      disputeId: 'dsp-01',
      roomCode: 'EOP-4012',
      title: 'Akun tidak sesuai deskripsi level 50',
      status: 'UNDER_REVIEW',
      initiator: 'Budi Santoso (Buyer)',
      counterparty: 'Rian (Seller)',
      amount: 450000,
      createdAt: '2026-08-10T11:00:00Z'
    }
  ]);
</script>

<div class="space-y-8">
  <div class="border-b border-hairline pb-6 space-y-1">
    <h1 class="font-display text-4xl font-bold text-ink">Pusat Arbitrase Dispute</h1>
    <p class="text-sm text-muted">Solusi penyelesaian sengketa transaksi yang adil dan diverifikasi secara obyektif oleh Tim Arbitrator.</p>
  </div>

  <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
    <div class="card-editorial p-6 space-y-2">
      <div class="text-xs font-semibold uppercase tracking-wider text-muted">Dispute Aktif</div>
      <div class="font-display text-4xl font-bold text-coral">1 Case</div>
    </div>
    <div class="card-editorial p-6 space-y-2">
      <div class="text-xs font-semibold uppercase tracking-wider text-muted">Dispute Selesai</div>
      <div class="font-display text-4xl font-bold text-success">0 Case</div>
    </div>
    <div class="card-dark-chrome p-6 space-y-2">
      <div class="text-xs font-semibold uppercase tracking-wider text-on-dark-soft">Peraturan Arbitrase</div>
      <div class="text-xs text-on-dark-soft leading-relaxed">
        Upload bukti tangkapan layar, rekaman video, atau log percakapan yang valid.
      </div>
    </div>
  </div>

  <div class="space-y-4">
    <h2 class="font-display text-2xl font-bold text-ink">Daftar Pengajuan Dispute</h2>

    <div class="space-y-4">
      {#each disputes as d (d.disputeId)}
        <div class="card-editorial p-6 space-y-4">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <span class="px-2.5 py-0.5 rounded-pill bg-error/10 text-error font-mono text-xs font-bold">
                #{d.roomCode}
              </span>
              <span class="text-xs text-muted">Dibuat: {new Date(d.createdAt).toLocaleString('id-ID')}</span>
            </div>
            <span class="px-3 py-1 rounded-pill bg-warning/15 text-warning font-semibold text-xs">
              {d.status}
            </span>
          </div>

          <div class="space-y-1">
            <h3 class="font-display text-2xl font-bold text-ink">{d.title}</h3>
            <p class="text-xs text-muted">Pengaju: <strong class="text-body">{d.initiator}</strong> vs <strong class="text-body">{d.counterparty}</strong></p>
          </div>

          <div class="pt-4 border-t border-hairline flex items-center justify-between">
            <span class="font-display text-xl font-bold text-ink">Nominal Sengketa: Rp {d.amount.toLocaleString('id-ID')}</span>
            <button class="btn-editorial-primary text-xs font-semibold">
              <Upload size={14} /> Unggah Bukti Tambahan
            </button>
          </div>
        </div>
      {/each}
    </div>
  </div>
</div>
