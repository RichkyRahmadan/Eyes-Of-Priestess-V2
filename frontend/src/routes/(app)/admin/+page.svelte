<script lang="ts">
  import { onMount } from 'svelte';
  import Badge from '$lib/components/ui/Badge.svelte';
  import Button from '$lib/components/ui/Button.svelte';
  import { toasts } from '$lib/components/ui/Toaster.svelte';
  import { formatIDR, formatDate } from '$lib/utils/format';

  // Sample dynamic admin data seeded in 02-seed-data.sql
  let disputes = $state([
    {
      id: 'd0000000-0000-0000-0000-000000000001',
      roomCode: 'COV-829111',
      title: 'Akun PUBG Belum Diberikan Akses Penuh',
      buyer: 'Budi Santoso',
      seller: 'Dimas Prasetyo',
      amount: 3800000,
      status: 'OPEN',
      date: new Date(Date.now() - 4 * 86400000).toISOString()
    },
    {
      id: 'd0000000-0000-0000-0000-000000000002',
      roomCode: 'COV-829112',
      title: 'Keterlambatan Deliverable Ilustrasi Vtuber',
      buyer: 'Dewi Anggraini',
      seller: 'Mega Utami',
      amount: 2100000,
      status: 'UNDER_REVIEW',
      date: new Date(Date.now() - 3 * 86400000).toISOString()
    }
  ]);

  let users = $state([
    { id: 'a0000000-0000-0000-0000-000000000003', name: 'Budi Santoso', email: 'budi.santoso@gmail.com', role: 'USER', status: 'ACTIVE', score: 4.9 },
    { id: 'a0000000-0000-0000-0000-000000000004', name: 'Siti Rahmawati', email: 'siti.rahmawati@gmail.com', role: 'USER', status: 'ACTIVE', score: 5.0 },
    { id: 'a0000000-0000-0000-0000-000000000005', name: 'Dimas Prasetyo', email: 'dimas.prasetyo@gmail.com', role: 'USER', status: 'REPORTED', score: 3.8 },
    { id: 'a0000000-0000-0000-0000-000000000006', name: 'Anisa Tri Lestari', email: 'anisa.lestari@gmail.com', role: 'USER', status: 'ACTIVE', score: 4.8 },
    { id: 'a0000000-0000-0000-0000-000000000007', name: 'Reza Aditya Wardhana', email: 'reza.aditya@gmail.com', role: 'USER', status: 'ACTIVE', score: 4.7 }
  ]);

  let resolvingId = $state<string | null>(null);

  function handleDecision(disputeId: string, decision: 'REFUND' | 'RELEASE') {
    resolvingId = disputeId;
    setTimeout(() => {
      disputes = disputes.filter((d) => d.id !== disputeId);
      resolvingId = null;
      toasts.success(
        decision === 'REFUND'
          ? 'Keputusan Selesai: Dana berhasil di-refund kembali ke pembeli.'
          : 'Keputusan Selesai: Dana dilepaskan ke dompet penjual.'
      );
    }, 600);
  }

  function handleToggleBan(userId: string) {
    users = users.map((u) => {
      if (u.id === userId) {
        const nextStatus = u.status === 'BANNED' ? 'ACTIVE' : 'BANNED';
        toasts.info(`Status pengguna ${u.name} diubah menjadi ${nextStatus}.`);
        return { ...u, status: nextStatus };
      }
      return u;
    });
  }
</script>

<svelte:head>
  <title>Dashboard Admin - EyesOfPriestess</title>
</svelte:head>

<div class="flex flex-col gap-8">
  <!-- Key Platform Metrics -->
  <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
    <div class="bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] p-5 flex flex-col gap-2">
      <span class="font-sans text-[var(--text-caption)] text-[var(--color-muted)] font-medium">
        Total Dana Custody Escrow
      </span>
      <p class="font-display text-[var(--text-title-lg)] text-[var(--color-ink)] leading-none">
        Rp 128.450.000
      </p>
      <span class="font-sans text-[11px] text-[var(--color-success)] flex items-center gap-1">
        ↑ +12.4% dari bulan lalu
      </span>
    </div>

    <div class="bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] p-5 flex flex-col gap-2">
      <span class="font-sans text-[var(--text-caption)] text-[var(--color-muted)] font-medium">
        Pengguna Terdaftar
      </span>
      <p class="font-display text-[var(--text-title-lg)] text-[var(--color-ink)] leading-none">
        1,420 Akun
      </p>
      <span class="font-sans text-[11px] text-[var(--color-muted-soft)]">
        22 akun aktif di seeder demo
      </span>
    </div>

    <div class="bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] p-5 flex flex-col gap-2">
      <span class="font-sans text-[var(--text-caption)] text-[var(--color-muted)] font-medium">
        Room Escrow Aktif
      </span>
      <p class="font-display text-[var(--text-title-lg)] text-[var(--color-ink)] leading-none">
        48 Covenant
      </p>
      <span class="font-sans text-[11px] text-[var(--color-primary)]">
        16 menanti pelepasan
      </span>
    </div>

    <div class="bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] p-5 flex flex-col gap-2">
      <span class="font-sans text-[var(--text-caption)] text-[var(--color-muted)] font-medium">
        Sengketa Terbuka
      </span>
      <p class="font-display text-[var(--text-title-lg)] text-[var(--color-warning)] leading-none">
        {disputes.length} Kasus
      </p>
      <span class="font-sans text-[11px] text-[var(--color-warning)]">
        Butuh atensi Arbiter
      </span>
    </div>
  </div>

  <!-- Dispute Resolution Section -->
  <div id="disputes" class="flex flex-col gap-4">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="font-sans font-medium text-[var(--text-title-md)] text-[var(--color-ink)]">
          Antrean Mediasi Sengketa (Dispute Resolution)
        </h2>
        <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)]">
          Tinjau laporan dan putuskan alokasi dana escrow secara objektif
        </p>
      </div>
      <span class="px-2.5 py-1 bg-[var(--color-surface-card)] border border-[var(--color-hairline)] rounded-full text-xs font-mono font-medium">
        {disputes.length} Kasus Aktif
      </span>
    </div>

    <div class="bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] overflow-hidden border border-[var(--color-hairline-soft)]">
      {#if disputes.length === 0}
        <div class="p-12 text-center text-[var(--color-muted)] text-[var(--text-body-sm)]">
          ✓ Seluruh sengketa telah diselesaikan oleh Arbiter.
        </div>
      {:else}
        <div class="divide-y divide-[var(--color-hairline-soft)]">
          {#each disputes as dispute}
            <div class="p-5 flex flex-col md:flex-row md:items-center justify-between gap-4">
              <div class="flex-1">
                <div class="flex items-center gap-2 mb-1.5">
                  <span class="font-mono text-xs font-semibold px-2 py-0.5 bg-[var(--color-surface-soft)] rounded">
                    {dispute.roomCode}
                  </span>
                  <span class="px-2 py-0.5 rounded text-[11px] font-semibold {dispute.status === 'OPEN' ? 'bg-[var(--color-error)]/10 text-[var(--color-error)]' : 'bg-[var(--color-warning)]/10 text-[var(--color-warning)]'}">
                    {dispute.status}
                  </span>
                  <span class="text-xs text-[var(--color-muted-soft)]">
                    {formatDate(dispute.date)}
                  </span>
                </div>
                <h3 class="font-sans font-medium text-[var(--text-body-md)] text-[var(--color-ink)]">
                  {dispute.title}
                </h3>
                <p class="font-sans text-[var(--text-caption)] text-[var(--color-muted)] mt-1">
                  Penggugat: <strong class="text-[var(--color-ink)]">{dispute.buyer}</strong> (Pembeli) · Tergugat: <strong class="text-[var(--color-ink)]">{dispute.seller}</strong> (Penjual) · Nilai: <strong class="text-[var(--color-ink)]">{formatIDR(dispute.amount)}</strong>
                </p>
              </div>

              <!-- Action buttons -->
              <div class="flex items-center gap-2 shrink-0">
                <button
                  onclick={() => handleDecision(dispute.id, 'REFUND')}
                  disabled={resolvingId === dispute.id}
                  class="h-8 px-3 bg-[var(--color-error)] hover:bg-[var(--color-error)]/90 text-white rounded-[var(--radius-md)] text-xs font-sans font-medium transition-colors disabled:opacity-50"
                >
                  ↩ Refund Pembeli
                </button>
                <button
                  onclick={() => handleDecision(dispute.id, 'RELEASE')}
                  disabled={resolvingId === dispute.id}
                  class="h-8 px-3 bg-[var(--color-success)] hover:bg-[var(--color-success)]/90 text-white rounded-[var(--radius-md)] text-xs font-sans font-medium transition-colors disabled:opacity-50"
                >
                  ✓ Lepas ke Penjual
                </button>
              </div>
            </div>
          {/each}
        </div>
      {/if}
    </div>
  </div>

  <!-- User Moderation Section -->
  <div id="users" class="flex flex-col gap-4">
    <div>
      <h2 class="font-sans font-medium text-[var(--text-title-md)] text-[var(--color-ink)]">
        Manajemen Akun & Sanksi Pengguna
      </h2>
      <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)]">
        Pantau skor integritas covenant dan lakukan suspend/banned akun bermasalah
      </p>
    </div>

    <div class="bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] overflow-hidden border border-[var(--color-hairline-soft)]">
      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse font-sans text-[var(--text-body-sm)]">
          <thead>
            <tr class="border-b border-[var(--color-hairline-soft)] bg-[var(--color-surface-soft)]/50 text-[var(--color-muted)] text-[12px] uppercase">
              <th class="py-3 px-4 font-semibold">Nama Pengguna</th>
              <th class="py-3 px-4 font-semibold">Email</th>
              <th class="py-3 px-4 font-semibold">Peran</th>
              <th class="py-3 px-4 font-semibold">Skor Covenant</th>
              <th class="py-3 px-4 font-semibold">Status</th>
              <th class="py-3 px-4 font-semibold text-right">Aksi</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-[var(--color-hairline-soft)]">
            {#each users as u}
              <tr class="hover:bg-[var(--color-surface-soft)]/30 transition-colors">
                <td class="py-3 px-4 font-medium text-[var(--color-ink)]">{u.name}</td>
                <td class="py-3 px-4 text-[var(--color-muted)] font-mono text-xs">{u.email}</td>
                <td class="py-3 px-4">
                  <span class="inline-block px-2 py-0.5 rounded text-[11px] font-mono font-medium bg-[var(--color-surface-soft)] text-[var(--color-ink)]">
                    {u.role}
                  </span>
                </td>
                <td class="py-3 px-4 font-mono font-medium text-[var(--color-ink)]">
                  ★ {u.score}
                </td>
                <td class="py-3 px-4">
                  <span class="inline-block px-2 py-0.5 rounded text-[11px] font-semibold {u.status === 'ACTIVE' ? 'bg-[var(--color-success)]/10 text-[var(--color-success)]' : u.status === 'REPORTED' ? 'bg-[var(--color-warning)]/10 text-[var(--color-warning)]' : 'bg-[var(--color-error)]/10 text-[var(--color-error)]'}">
                    {u.status}
                  </span>
                </td>
                <td class="py-3 px-4 text-right">
                  <button
                    onclick={() => handleToggleBan(u.id)}
                    class="h-7 px-2.5 rounded text-xs font-medium transition-colors {u.status === 'BANNED' ? 'bg-[var(--color-surface-soft)] text-[var(--color-ink)] hover:bg-[var(--color-hairline)]' : 'bg-[var(--color-error)]/10 text-[var(--color-error)] hover:bg-[var(--color-error)] hover:text-white'}"
                  >
                    {u.status === 'BANNED' ? 'Unban Akun' : 'Ban Akun'}
                  </button>
                </td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    </div>
  </div>
</div>
