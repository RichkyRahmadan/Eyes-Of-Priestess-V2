<script lang="ts">
  import { page } from '$app/state';
  import { authStore, currentUser, isAuthenticated } from '$lib/stores/auth';
  import { wsManager } from '$lib/websocket/manager';
  import { wsUnreadCounts } from '$lib/stores/websocket';
  import Avatar from '$lib/components/ui/Avatar.svelte';
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';

  let { children } = $props();

  let mobileMenuOpen = $state(false);

  onMount(() => {
    if (!$isAuthenticated) {
      goto('/login');
      return;
    }
    wsManager.connect();
    return () => wsManager.disconnect();
  });

  const navItems = [
    { href: '/dashboard', label: 'Dashboard', icon: 'house' },
    { href: '/wallet', label: 'Dompet', icon: 'wallet' },
    { href: '/rooms', label: 'Room', icon: 'shield-check' },
    { href: '/chat', label: 'Chat', icon: 'chat-circle' },
    { href: '/history', label: 'Riwayat', icon: 'clock-counter-clockwise' }
  ];

  const totalUnread = $derived(
    Object.values($wsUnreadCounts).reduce((a, b) => a + b, 0)
  );

  function isActive(href: string): boolean {
    return page.url.pathname === href || page.url.pathname.startsWith(href + '/');
  }
</script>

<div class="flex min-h-[100dvh] bg-[var(--color-canvas)]">
  <!-- Sidebar (desktop) -->
  <aside
    class="hidden lg:flex w-[280px] bg-[var(--color-canvas)] border-r border-[var(--color-hairline-soft)] flex-col sticky top-0 h-screen shrink-0"
  >
    <!-- Logo -->
    <div class="p-6 border-b border-[var(--color-hairline-soft)]">
      <a href="/dashboard">
        <span
          class="font-display text-[var(--text-display-sm)] text-[var(--color-ink)] tracking-tight leading-none"
        >
          Eyes<span class="text-[var(--color-primary)]">Of</span>Priestess
        </span>
      </a>
    </div>

    <!-- Nav -->
    <nav class="flex-1 px-4 py-4 space-y-0.5">
      {#each navItems as item}
        {@const active = isActive(item.href)}
        <a
          href={item.href}
          class="flex items-center gap-3 px-3 py-2.5 rounded-[var(--radius-md)] font-sans text-[var(--text-body-md)] transition-colors duration-150 {active ? 'bg-[var(--color-surface-card)] text-[var(--color-ink)] font-medium' : 'text-[var(--color-muted)] hover:bg-[var(--color-surface-soft)] hover:text-[var(--color-ink)]'}"
        >
          <!-- Icon placeholder (SVG inline) -->
          <span class="w-5 h-5 flex items-center justify-center opacity-70">
            {#if item.icon === 'house'}
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 256 256" fill="currentColor"><path d="M219.31,108.68l-80-80a16,16,0,0,0-22.62,0l-80,80A15.87,15.87,0,0,0,32,120v96a8,8,0,0,0,8,8h64a8,8,0,0,0,8-8V168h32v48a8,8,0,0,0,8,8h64a8,8,0,0,0,8-8V120A15.87,15.87,0,0,0,219.31,108.68ZM208,208H160V160a8,8,0,0,0-8-8H104a8,8,0,0,0-8,8v48H48V120l80-80,80,80Z"/></svg>
            {:else if item.icon === 'wallet'}
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 256 256" fill="currentColor"><path d="M216,72H56a8,8,0,0,1,0-16H192a8,8,0,0,0,0-16H56A24,24,0,0,0,32,64V192a24,24,0,0,0,24,24H216a16,16,0,0,0,16-16V88A16,16,0,0,0,216,72Zm0,128H56a8,8,0,0,1-8-8V86.63A23.84,23.84,0,0,0,56,88H216Zm-48-60a12,12,0,1,1,12,12A12,12,0,0,1,168,140Z"/></svg>
            {:else if item.icon === 'shield-check'}
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 256 256" fill="currentColor"><path d="M208,40H48A16,16,0,0,0,32,56V120c0,88,88,128,88,128s88-40,88-128V56A16,16,0,0,0,208,40Zm0,80c0,68.9-62.6,104.66-80,113.31C110.6,224.66,48,189,48,120V56H208ZM82.34,130.34,104,152l69.66-69.66a8,8,0,0,1,11.32,11.32l-75.32,75.32a8,8,0,0,1-11.32,0l-27.32-27.32a8,8,0,0,1,11.32-11.32Z"/></svg>
            {:else if item.icon === 'chat-circle'}
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 256 256" fill="currentColor"><path d="M116,128a12,12,0,1,1,12,12A12,12,0,0,1,116,128Zm-44,0a12,12,0,1,0,12-12A12,12,0,0,0,72,128Zm104,0a12,12,0,1,0-12,12A12,12,0,0,0,176,128Zm52,0A100,100,0,1,1,128,28,100.11,100.11,0,0,1,228,128Zm-16,0a84,84,0,1,0-84,84A84.09,84.09,0,0,0,212,128Z"/></svg>
            {:else if item.icon === 'clock-counter-clockwise'}
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 256 256" fill="currentColor"><path d="M136,80v43.47l36.12,21.67a8,8,0,0,1-8.24,13.72l-40-24A8,8,0,0,1,120,128V80a8,8,0,0,1,16,0ZM232,48v48a8,8,0,0,1-8,8H176a8,8,0,0,1,0-16h28.69L188.4,71.71C168.17,51.48,140.9,40,112,40a88,88,0,0,0,0,176c36.24,0,68.3-19.84,85.51-50.76a8,8,0,0,1,14,7.52A104,104,0,1,1,112,24c35.54,0,68.58,13.69,93,38.66L220,48a8,8,0,0,1,12,0Z"/></svg>
            {/if}
          </span>
          <span class="flex-1">{item.label}</span>
          {#if item.icon === 'chat-circle' && totalUnread > 0}
            <span
              class="bg-[var(--color-primary)] text-[var(--color-on-primary)] text-[11px] font-medium px-1.5 py-0.5 rounded-[var(--radius-pill)] min-w-[18px] text-center"
            >
              {totalUnread > 99 ? '99+' : totalUnread}
            </span>
          {/if}
        </a>
      {/each}

      {#if $currentUser?.role === 'ADMIN' || $currentUser?.role === 'MODERATOR'}
        <div class="pt-2 mt-2 border-t border-[var(--color-hairline-soft)]">
          <a
            href="/admin"
            class="flex items-center gap-3 px-3 py-2.5 rounded-[var(--radius-md)] font-sans text-[var(--text-body-md)] transition-colors duration-150 {isActive('/admin') ? 'bg-[var(--color-primary)] text-white font-medium' : 'text-[var(--color-primary)] hover:bg-[var(--color-primary)]/10 font-medium'}"
          >
            <span class="w-5 h-5 flex items-center justify-center">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 256 256" fill="currentColor"><path d="M208,40H48A16,16,0,0,0,32,56V120c0,88,88,128,88,128s88-40,88-128V56A16,16,0,0,0,208,40Zm-80,188c-24.31-13.62-64-44.59-64-108V56H128Z"/></svg>
            </span>
            <span class="flex-1">Admin Sanctum</span>
            <span class="text-[10px] uppercase font-mono px-1.5 py-0.5 rounded bg-black/10">{$currentUser.role}</span>
          </a>
        </div>
      {/if}
    </nav>

    <!-- User profile -->
    <div class="p-4 border-t border-[var(--color-hairline-soft)]">
      {#if $currentUser}
        <div class="flex items-center gap-3 px-2 py-2">
          <Avatar name={$currentUser.fullName} src={$currentUser.avatarUrl} size="sm" />
          <div class="flex-1 min-w-0">
            <p
              class="font-sans font-medium text-[var(--text-body-sm)] text-[var(--color-ink)] truncate"
            >
              {$currentUser.fullName}
            </p>
            <p
              class="font-sans text-[var(--text-caption)] text-[var(--color-muted)] truncate"
            >
              @{$currentUser.username}
            </p>
          </div>
          <a
            href="/profile"
            class="text-[var(--color-muted-soft)] hover:text-[var(--color-ink)] transition-colors"
            aria-label="Profil"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 256 256" fill="currentColor"><path d="M128,24A104,104,0,1,0,232,128,104.11,104.11,0,0,0,128,24Zm-4,48a12,12,0,1,1-12,12A12,12,0,0,1,124,72Zm12,112H120a8,8,0,0,1,0-16h4V128h-4a8,8,0,0,1,0-16h8a8,8,0,0,1,8,8v48h4a8,8,0,0,1,0,16Z"/></svg>
          </a>
        </div>
      {/if}
    </div>
  </aside>

  <!-- Main content -->
  <div class="flex-1 flex flex-col min-w-0">
    <!-- Top navigation (tablet/mobile) -->
    <header
      class="lg:hidden h-16 bg-[var(--color-canvas)] border-b border-[var(--color-hairline-soft)] sticky top-0 z-50 flex items-center justify-between px-4"
    >
      <a href="/dashboard">
        <span
          class="font-display text-[var(--text-display-sm)] text-[var(--color-ink)] tracking-tight"
        >
          Eyes<span class="text-[var(--color-primary)]">Of</span>Priestess
        </span>
      </a>
      <button
        onclick={() => (mobileMenuOpen = !mobileMenuOpen)}
        class="w-9 h-9 flex items-center justify-center rounded-[var(--radius-md)] text-[var(--color-ink)] hover:bg-[var(--color-surface-soft)] transition-colors"
        aria-label="Menu"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 256 256" fill="currentColor">
          {#if mobileMenuOpen}
            <path d="M205.66,194.34a8,8,0,0,1-11.32,11.32L128,139.31,61.66,205.66a8,8,0,0,1-11.32-11.32L116.69,128,50.34,61.66A8,8,0,0,1,61.66,50.34L128,116.69l66.34-66.35a8,8,0,0,1,11.32,11.32L139.31,128Z"/>
          {:else}
            <path d="M224,128a8,8,0,0,1-8,8H40a8,8,0,0,1,0-16H216A8,8,0,0,1,224,128ZM40,72H216a8,8,0,0,0,0-16H40a8,8,0,0,0,0,16ZM216,184H40a8,8,0,0,0,0,16H216a8,8,0,0,0,0-16Z"/>
          {/if}
        </svg>
      </button>
    </header>

    <!-- Mobile menu drawer -->
    {#if mobileMenuOpen}
      <div
        class="lg:hidden fixed inset-0 z-40 bg-[var(--color-ink)]/20"
        onclick={() => (mobileMenuOpen = false)}
        role="presentation"
      ></div>
      <nav
        class="lg:hidden fixed top-16 left-0 right-0 z-40 bg-[var(--color-canvas)] border-b border-[var(--color-hairline-soft)] px-4 py-3 flex flex-col gap-0.5"
      >
        {#each navItems as item}
          <a
            href={item.href}
            onclick={() => (mobileMenuOpen = false)}
            class="px-3 py-3 rounded-[var(--radius-md)] font-sans text-[var(--text-body-md)] {isActive(item.href) ? 'bg-[var(--color-surface-card)] text-[var(--color-ink)] font-medium' : 'text-[var(--color-muted)] hover:bg-[var(--color-surface-soft)]'}"
          >
            {item.label}
          </a>
        {/each}
        {#if $currentUser?.role === 'ADMIN' || $currentUser?.role === 'MODERATOR'}
          <a
            href="/admin"
            onclick={() => (mobileMenuOpen = false)}
            class="px-3 py-3 rounded-[var(--radius-md)] font-sans text-[var(--text-body-md)] text-[var(--color-primary)] font-medium hover:bg-[var(--color-surface-soft)] flex items-center justify-between"
          >
            <span class="flex items-center gap-2">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 256 256" fill="currentColor"><path d="M208,40H48A16,16,0,0,0,32,56V120c0,88,88,128,88,128s88-40,88-128V56A16,16,0,0,0,208,40Zm-80,188c-24.31-13.62-64-44.59-64-108V56H128Z"/></svg>
              Admin Sanctum
            </span>
            <span class="text-[10px] uppercase font-mono px-1.5 py-0.5 rounded bg-black/10">{$currentUser.role}</span>
          </a>
        {/if}
        <div class="border-t border-[var(--color-hairline-soft)] mt-2 pt-2">
          <a
            href="/profile"
            onclick={() => (mobileMenuOpen = false)}
            class="px-3 py-3 rounded-[var(--radius-md)] font-sans text-[var(--text-body-md)] text-[var(--color-muted)] hover:bg-[var(--color-surface-soft)] block"
          >
            Profil
          </a>
        </div>
      </nav>
    {/if}

    <!-- Page content -->
    <main class="flex-1 px-4 py-6 lg:px-8 lg:py-8 max-w-[1200px] w-full mx-auto">
      {@render children()}
    </main>
  </div>
</div>

<!-- Bottom nav bar (mobile) -->
<nav
  class="lg:hidden fixed bottom-0 left-0 right-0 bg-[var(--color-canvas)] border-t border-[var(--color-hairline-soft)] flex z-30"
>
  {#each navItems as item}
    {@const active = isActive(item.href)}
    <a
      href={item.href}
      class="flex-1 flex flex-col items-center gap-0.5 py-2 transition-colors {active ? 'text-[var(--color-primary)]' : 'text-[var(--color-muted-soft)]'}"
    >
      <span class="w-5 h-5 flex items-center justify-center relative">
        {#if item.icon === 'house'}
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 256 256" fill="currentColor"><path d="M219.31,108.68l-80-80a16,16,0,0,0-22.62,0l-80,80A15.87,15.87,0,0,0,32,120v96a8,8,0,0,0,8,8h64a8,8,0,0,0,8-8V168h32v48a8,8,0,0,0,8,8h64a8,8,0,0,0,8-8V120A15.87,15.87,0,0,0,219.31,108.68ZM208,208H160V160a8,8,0,0,0-8-8H104a8,8,0,0,0-8,8v48H48V120l80-80,80,80Z"/></svg>
        {:else if item.icon === 'wallet'}
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 256 256" fill="currentColor"><path d="M216,72H56a8,8,0,0,1,0-16H192a8,8,0,0,0,0-16H56A24,24,0,0,0,32,64V192a24,24,0,0,0,24,24H216a16,16,0,0,0,16-16V88A16,16,0,0,0,216,72Zm0,128H56a8,8,0,0,1-8-8V86.63A23.84,23.84,0,0,0,56,88H216Zm-48-60a12,12,0,1,1,12,12A12,12,0,0,1,168,140Z"/></svg>
        {:else if item.icon === 'shield-check'}
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 256 256" fill="currentColor"><path d="M208,40H48A16,16,0,0,0,32,56V120c0,88,88,128,88,128s88-40,88-128V56A16,16,0,0,0,208,40Zm0,80c0,68.9-62.6,104.66-80,113.31C110.6,224.66,48,189,48,120V56H208ZM82.34,130.34,104,152l69.66-69.66a8,8,0,0,1,11.32,11.32l-75.32,75.32a8,8,0,0,1-11.32,0l-27.32-27.32a8,8,0,0,1,11.32-11.32Z"/></svg>
        {:else if item.icon === 'chat-circle'}
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 256 256" fill="currentColor"><path d="M116,128a12,12,0,1,1,12,12A12,12,0,0,1,116,128Zm-44,0a12,12,0,1,0,12-12A12,12,0,0,0,72,128Zm104,0a12,12,0,1,0-12,12A12,12,0,0,0,176,128Zm52,0A100,100,0,1,1,128,28,100.11,100.11,0,0,1,228,128Zm-16,0a84,84,0,1,0-84,84A84.09,84.09,0,0,0,212,128Z"/></svg>
        {:else if item.icon === 'clock-counter-clockwise'}
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 256 256" fill="currentColor"><path d="M136,80v43.47l36.12,21.67a8,8,0,0,1-8.24,13.72l-40-24A8,8,0,0,1,120,128V80a8,8,0,0,1,16,0ZM232,48v48a8,8,0,0,1-8,8H176a8,8,0,0,1,0-16h28.69L188.4,71.71C168.17,51.48,140.9,40,112,40a88,88,0,0,0,0,176c36.24,0,68.3-19.84,85.51-50.76a8,8,0,0,1,14,7.52A104,104,0,1,1,112,24c35.54,0,68.58,13.69,93,38.66L220,48a8,8,0,0,1,12,0Z"/></svg>
        {/if}
        {#if item.icon === 'chat-circle' && totalUnread > 0}
          <span
            class="absolute -top-1 -right-1 w-4 h-4 bg-[var(--color-primary)] text-white text-[9px] font-medium rounded-full flex items-center justify-center"
          >
            {totalUnread > 9 ? '9+' : totalUnread}
          </span>
        {/if}
      </span>
      <span class="text-[10px] font-sans font-medium">{item.label}</span>
    </a>
  {/each}
</nav>
