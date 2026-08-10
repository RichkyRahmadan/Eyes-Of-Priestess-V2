<script lang="ts">
  import '../app.css';
  import { onMount } from 'svelte';
  import { sealStore } from '$lib/stores/seal';
  import { sealApi } from '$lib/api/seal';
  import { Toaster } from 'svelte-sonner';

  let { children } = $props();

  onMount(async () => {
    sealStore.init();
    // If there's a stored token, fetch current user profile
    if (typeof localStorage !== 'undefined' && localStorage.getItem('accessSeal')) {
      try {
        const pilgrim = await sealApi.me();
        sealStore.setPilgrim(pilgrim);
        sealStore.setLoading(false);
        // Mark as authenticated if we got pilgrim back
        sealStore.attune(
          pilgrim,
          localStorage.getItem('accessSeal')!,
          localStorage.getItem('refreshSeal') ?? ''
        );
      } catch {
        sealStore.sever();
      }
    } else {
      sealStore.setLoading(false);
    }
  });
</script>

<Toaster
  position="top-right"
  toastOptions={{
    style: 'background: var(--surface-700); color: var(--text-primary); border: 1px solid var(--border-default); border-radius: 10px;'
  }}
/>

{@render children()}

