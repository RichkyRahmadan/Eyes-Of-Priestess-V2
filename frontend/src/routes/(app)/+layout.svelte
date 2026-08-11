<script lang="ts">
  import Navbar from '$lib/components/Navbar.svelte';
  import Sidebar from '$lib/components/Sidebar.svelte';
  import { onMount } from 'svelte';
  import { wsManager } from '$lib/websocket/manager';

  let { children } = $props();

  onMount(() => {
    wsManager.connect();
    return () => wsManager.disconnect();
  });
</script>

<div class="min-h-screen bg-canvas text-ink flex flex-col">
  <!-- Top Navigation Header -->
  <Navbar />

  <!-- Main Body Layout (Sidebar + Content) -->
  <div class="flex-1 flex">
    <Sidebar />

    <main class="flex-1 p-6 lg:p-10 max-w-7xl mx-auto w-full space-y-8">
      {@render children()}
    </main>
  </div>
</div>
