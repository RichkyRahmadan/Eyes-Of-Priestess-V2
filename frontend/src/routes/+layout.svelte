<script lang="ts">
  import '../app.css';
  import Toaster from '$lib/components/ui/Toaster.svelte';

  let { children } = $props();
</script>

<svelte:head>
  <title>EyesOfPriestess - Escrow P2P Aman</title>
  <meta
    name="description"
    content="Platform escrow peer-to-peer yang aman untuk transaksi digital. Dana Anda terlindungi sampai barang atau jasa diterima."
  />
</svelte:head>

<Toaster />
{@render children()}
