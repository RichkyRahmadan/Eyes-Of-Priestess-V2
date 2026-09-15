<script lang="ts">
  import { getInitials } from '$lib/utils/format';

  interface Props {
    name: string;
    src?: string;
    size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
    class?: string;
  }

  let { name, src, size = 'md', class: className = '' }: Props = $props();

  const sizes = {
    xs: 'w-6 h-6 text-[10px]',
    sm: 'w-8 h-8 text-[11px]',
    md: 'w-10 h-10 text-[13px]',
    lg: 'w-12 h-12 text-[15px]',
    xl: 'w-16 h-16 text-[18px]'
  };

  // Generate a deterministic warm hue from the name
  const hue = $derived(
    name.split('').reduce((acc, c) => acc + c.charCodeAt(0), 0) % 360
  );
</script>

<div
  class="rounded-[var(--radius-pill)] overflow-hidden flex items-center justify-center shrink-0 {sizes[size]} {className}"
  style="background: hsl({hue}, 35%, 72%); color: hsl({hue}, 35%, 28%);"
  aria-label={name}
>
  {#if src}
    <img {src} alt={name} class="w-full h-full object-cover" />
  {:else}
    <span class="font-sans font-medium leading-none">{getInitials(name)}</span>
  {/if}
</div>
