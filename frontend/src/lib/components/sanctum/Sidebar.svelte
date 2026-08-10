<script lang="ts">
  import { sealStore } from '$lib/stores/seal';
  import { uiStore } from '$lib/stores/ui';
  import { goto } from '$app/navigation';
  import { page } from '$app/stores';
  import { sealApi } from '$lib/api/seal';
  import {
    LayoutDashboard, Wallet, Handshake, MessageSquare,
    Scale, User, LogOut, ChevronRight, Gem, Shield, Menu, X
  } from "@lucide/svelte";

  interface NavItem {
    href: string;
    label: string;
    icon: typeof LayoutDashboard;
    oracleOnly?: boolean;
  }

  const navItems: NavItem[] = [
    { href: '/sanctum/observatory', label: 'Observatory',     icon: LayoutDashboard },
    { href: '/sanctum/vault',       label: 'Vault Sanctum',  icon: Wallet },
    { href: '/sanctum/covenant',    label: 'Covenant Hall',  icon: Handshake },
    { href: '/sanctum/judgment',    label: 'Judgment',       icon: Scale },
    { href: '/sanctum/profile',     label: 'Profile',        icon: User }
  ];

  let { open = false }: { open?: boolean } = $props();

  const isActive = (href: string) => $page.url.pathname.startsWith(href);

  async function handleLogout() {
    try { await sealApi.sever(); } catch { /* ignore */ }
    sealStore.sever();
    goto('/rite');
  }
</script>

<!-- Overlay -->
{#if open}
  <button
    class="sidebar-overlay"
    onclick={() => uiStore.closeSidebar()}
    aria-label="Close sidebar"
  ></button>
{/if}

<aside class="sidebar" class:open>
  <!-- Branding -->
  <div class="sidebar-brand">
    <div class="brand-icon">
      <Gem size={20} color="var(--gold-400)" />
    </div>
    <div>
      <p class="brand-title">EyesOfPriestess</p>
      <p class="brand-sub">The Covenant Protocol</p>
    </div>
  </div>

  <!-- Pilgrim Mini Card -->
  {#if $sealStore.pilgrim}
    <div class="pilgrim-card">
      <div class="pilgrim-avatar">
        {$sealStore.pilgrim.fullName.charAt(0).toUpperCase()}
      </div>
      <div class="pilgrim-info">
        <p class="pilgrim-name">{$sealStore.pilgrim.fullName}</p>
        <p class="pilgrim-phone">{$sealStore.pilgrim.phone}</p>
      </div>
      {#if $sealStore.pilgrim.role === 'ORACLE'}
        <span class="oracle-badge">
          <Shield size={10} /> Oracle
        </span>
      {/if}
    </div>
  {/if}

  <!-- Navigation -->
  <nav class="sidebar-nav">
    <p class="nav-section-label">NAVIGATION</p>
    {#each navItems as item}
      {#if !item.oracleOnly || $sealStore.pilgrim?.role === 'ORACLE'}
        <a href={item.href} class="nav-item" class:active={isActive(item.href)}>
          <item.icon size={18} />
          <span>{item.label}</span>
          {#if isActive(item.href)}
            <ChevronRight size={14} class="ml-auto" />
          {/if}
        </a>
      {/if}
    {/each}
  </nav>

  <!-- Bottom actions -->
  <div class="sidebar-footer">
    <button class="nav-item logout-btn" onclick={handleLogout}>
      <LogOut size={18} />
      <span>Sever Seal</span>
    </button>
  </div>
</aside>

<style>
.sidebar-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.6);
  backdrop-filter: blur(4px);
  z-index: 49;
  border: none;
  cursor: default;
}

.sidebar {
  width: var(--sidebar-width);
  height: 100vh;
  background: var(--surface-800);
  border-right: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
  padding: 1.25rem 0;
  overflow-y: auto;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0 1.25rem 1.25rem;
  border-bottom: 1px solid var(--border-subtle);
  margin-bottom: 0.75rem;
}

.brand-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, var(--void-700), var(--void-900));
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--border-gold);
  flex-shrink: 0;
}

.brand-title {
  font-family: 'Cinzel', serif;
  font-size: 0.8125rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.brand-sub {
  font-size: 0.6875rem;
  color: var(--text-muted);
}

.pilgrim-card {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  padding: 0.75rem 1.25rem;
  background: var(--surface-700);
  margin: 0 0.75rem 0.75rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-subtle);
}

.pilgrim-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--void-600), var(--void-800));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8125rem;
  font-weight: 700;
  color: var(--void-200);
  flex-shrink: 0;
}

.pilgrim-info { flex: 1; min-width: 0; }

.pilgrim-name {
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pilgrim-phone {
  font-size: 0.6875rem;
  color: var(--text-muted);
}

.oracle-badge {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 0.6rem;
  padding: 2px 6px;
  background: rgba(245,158,11,0.15);
  color: var(--gold-400);
  border: 1px solid rgba(245,158,11,0.3);
  border-radius: var(--radius-full);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  font-weight: 700;
  white-space: nowrap;
}

.sidebar-nav {
  flex: 1;
  padding: 0 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-section-label {
  font-size: 0.625rem;
  font-weight: 700;
  letter-spacing: 0.1em;
  color: var(--text-muted);
  padding: 0.5rem 0.5rem 0.25rem;
  text-transform: uppercase;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  padding: 0.625rem 0.75rem;
  border-radius: var(--radius-md);
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-muted);
  text-decoration: none;
  transition: all var(--transition-fast);
  cursor: pointer;
  border: none;
  background: none;
  width: 100%;
  text-align: left;
}

.nav-item:hover {
  color: var(--text-primary);
  background: var(--surface-700);
}

.nav-item.active {
  color: var(--void-300);
  background: rgba(139,92,246,0.12);
  border: 1px solid rgba(139,92,246,0.2);
}

.sidebar-footer {
  padding: 0.75rem;
  border-top: 1px solid var(--border-subtle);
  margin-top: 0.5rem;
}

.logout-btn {
  color: var(--danger) !important;
}

.logout-btn:hover {
  background: rgba(239,68,68,0.08) !important;
}

@media (max-width: 1024px) {
  .sidebar {
    position: fixed;
    top: 0;
    left: 0;
    z-index: 50;
    transform: translateX(-100%);
    transition: transform var(--transition-normal);
    box-shadow: var(--shadow-lg);
  }
  .sidebar.open {
    transform: translateX(0);
  }
}
</style>

