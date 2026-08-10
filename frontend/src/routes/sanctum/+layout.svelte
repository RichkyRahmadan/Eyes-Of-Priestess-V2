<script lang="ts">
  import Sidebar from '$lib/components/sanctum/Sidebar.svelte';
  import Veil from '$lib/components/sanctum/Veil.svelte';
  import { uiStore } from '$lib/stores/ui';
  import { sealStore } from '$lib/stores/seal';
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';
  import LoadingCrystal from '$lib/components/shared/LoadingCrystal.svelte';
  import { page } from '$app/stores';

  let { children } = $props();

  // Route title map
  const titles: Record<string, string> = {
    '/sanctum/observatory': 'Observatory',
    '/sanctum/vault':       'Vault Sanctum',
    '/sanctum/covenant':    'Covenant Hall',
    '/sanctum/judgment':    'Judgment Sanctum',
    '/sanctum/profile':     'Profile'
  };

  const title = $derived(
    Object.entries(titles).find(([path]) => $page.url.pathname.startsWith(path))?.[1] ?? 'Sanctum'
  );

  onMount(() => {
    if (!$sealStore.isLoading && !$sealStore.isAuthenticated) {
      goto('/rite');
    }
  });
</script>

{#if $sealStore.isLoading}
  <div class="loading-screen">
    <LoadingCrystal />
  </div>
{:else if $sealStore.isAuthenticated}
  <div class="sanctum-layout">
    <!-- Sidebar -->
    <Sidebar open={$uiStore.sidebarOpen} />

    <!-- Main Content -->
    <div class="sanctum-main">
      <Veil {title} />
      <div class="sanctum-content page-enter">
        {@render children()}
      </div>
    </div>
  </div>
{/if}

<style>
.loading-screen {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--surface-900);
}
</style>

