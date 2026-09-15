<script lang="ts">
  import { currentUser } from '$lib/stores/auth';
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';
  import { page } from '$app/state';

  let { children } = $props();

  let isAuthorized = $derived(
    $currentUser?.role === 'ADMIN' || $currentUser?.role === 'MODERATOR'
  );

  onMount(() => {
    // If user is loaded and not admin/moderator, redirect to dashboard with alert
    if ($currentUser && !isAuthorized) {
      goto('/dashboard');
    }
  });

  const adminNav = [
    { href: '/admin', label: 'Ringkasan Platform' },
    { href: '/admin#disputes', label: 'Mediasi Sengketa' },
    { href: '/admin#users', label: 'Manajemen Pengguna' },
    { href: '/admin#logs', label: 'Audit Log' }
  ];
</script>

<svelte:head>
  <title>Arbiter Admin Sanctum - EyesOfPriestess</title>
</svelte:head>

{#if !isAuthorized && $currentUser}
  <!-- 403 Forbidden State -->
  <div class="bg-[var(--color-surface-card)] border border-[var(--color-error)]/30 rounded-[var(--radius-xl)] p-8 text-center max-w-lg mx-auto my-12">
    <div class="w-12 h-12 rounded-full bg-[var(--color-error)]/10 text-[var(--color-error)] flex items-center justify-center mx-auto text-xl font-bold mb-4">
      ✕
    </div>
    <h2 class="font-display text-[var(--text-title-lg)] text-[var(--color-ink)]">
      Akses Ditolak (403 Forbidden)
    </h2>
    <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)] mt-2 mb-6">
      Area ini dikhususkan bagi Administrator atau Arbiter terakreditasi. Akun Anda berstatus: <strong class="text-[var(--color-ink)]">{$currentUser.role}</strong>.
    </p>
    <a
      href="/dashboard"
      class="inline-flex items-center h-9 px-4 bg-[var(--color-primary)] text-white font-sans text-[13px] font-medium rounded-[var(--radius-md)] hover:bg-[var(--color-primary-active)] transition-colors"
    >
      Kembali ke Dashboard Pengguna
    </a>
  </div>
{:else}
  <div class="flex flex-col gap-6 pb-20 lg:pb-8">
    <!-- Admin Header Banner -->
    <div class="bg-[var(--color-surface-dark)] text-[var(--color-on-dark)] rounded-[var(--radius-xl)] p-6 sm:p-8 flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
      <div>
        <div class="flex items-center gap-2 mb-1">
          <span class="inline-flex items-center px-2 py-0.5 rounded text-[11px] font-mono uppercase font-semibold bg-[var(--color-primary)] text-white">
            {$currentUser?.role ?? 'ADMIN'} PORTAL
          </span>
          <span class="text-xs text-[var(--color-on-dark-soft)]">
            ID: {$currentUser?.id?.slice(0, 8) ?? 'SYSTEM'}
          </span>
        </div>
        <h1 class="font-display text-[var(--text-display-sm)] tracking-tight text-white">
          Arbiter & Admin Sanctum
        </h1>
        <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-on-dark-soft)] mt-1">
          Pusat kendali mediasi escrow, pengawasan likuiditas saldo, dan moderasi akun pengguna.
        </p>
      </div>

      <div class="flex gap-2 shrink-0">
        <a
          href="/dashboard"
          class="inline-flex items-center h-9 px-3.5 bg-white/10 hover:bg-white/20 text-white rounded-[var(--radius-md)] font-sans text-[13px] font-medium transition-colors"
        >
          ← Kembali ke App
        </a>
      </div>
    </div>

    <!-- Admin Nav Tabs -->
    <div class="flex gap-2 border-b border-[var(--color-hairline-soft)] pb-2 overflow-x-auto">
      {#each adminNav as tab}
        <a
          href={tab.href}
          class="h-9 px-4 rounded-[var(--radius-md)] font-sans text-[13px] font-medium inline-flex items-center whitespace-nowrap transition-colors {page.url.pathname + page.url.hash === tab.href || (tab.href === '/admin' && !page.url.hash) ? 'bg-[var(--color-surface-card)] text-[var(--color-ink)] shadow-sm' : 'text-[var(--color-muted)] hover:text-[var(--color-ink)]'}"
        >
          {tab.label}
        </a>
      {/each}
    </div>

    {@render children()}
  </div>
{/if}
