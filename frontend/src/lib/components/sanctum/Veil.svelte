<script lang="ts">
  import { uiStore } from '$lib/stores/ui';
  import { Menu, Bell } from "@lucide/svelte";
  import { sealStore } from '$lib/stores/seal';

  let { title = '' }: { title?: string } = $props();
</script>

<header class="topbar">
  <div class="topbar-left">
    <button class="menu-btn" onclick={() => uiStore.toggleSidebar()} aria-label="Toggle sidebar">
      <Menu size={20} />
    </button>
    {#if title}
      <h1 class="topbar-title">{title}</h1>
    {/if}
  </div>

  <div class="topbar-right">
    <button class="icon-btn" aria-label="Notifications">
      <Bell size={18} />
    </button>
    <div class="avatar-sm">
      {$sealStore.pilgrim?.fullName.charAt(0).toUpperCase() ?? '?'}
    </div>
  </div>
</header>

<style>
.topbar {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1.5rem;
  background: var(--surface-800);
  border-bottom: 1px solid var(--border-subtle);
  position: sticky;
  top: 0;
  z-index: 30;
}

.topbar-left { display: flex; align-items: center; gap: 0.75rem; }
.topbar-right { display: flex; align-items: center; gap: 0.75rem; }

.menu-btn {
  display: none;
  width: 36px; height: 36px;
  align-items: center; justify-content: center;
  background: var(--surface-700);
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  transition: all var(--transition-fast);
}
.menu-btn:hover { color: var(--text-primary); background: var(--surface-600); }

.topbar-title {
  font-family: 'Cinzel', serif;
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.icon-btn {
  width: 36px; height: 36px;
  display: flex; align-items: center; justify-content: center;
  background: var(--surface-700);
  border-radius: var(--radius-md);
  color: var(--text-muted);
  transition: all var(--transition-fast);
}
.icon-btn:hover { color: var(--text-primary); background: var(--surface-600); }

.avatar-sm {
  width: 32px; height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--void-600), var(--void-800));
  display: flex; align-items: center; justify-content: center;
  font-size: 0.8125rem; font-weight: 700;
  color: var(--void-200);
}

@media (max-width: 1024px) {
  .menu-btn { display: flex; }
}
</style>

