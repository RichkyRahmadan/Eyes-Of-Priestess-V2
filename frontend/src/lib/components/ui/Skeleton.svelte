<script lang="ts">
  interface Props {
    rows?: number;
    class?: string;
  }

  let { rows = 1, class: className = '' }: Props = $props();
</script>

<div class="flex flex-col gap-3 {className}">
  {#each Array(rows) as _, i}
    <div class="flex flex-col gap-2">
      <div class="skeleton h-4 w-2/3"></div>
      <div class="skeleton h-3 w-full"></div>
      {#if i === 0}
        <div class="skeleton h-3 w-4/5"></div>
      {/if}
    </div>
  {/each}
</div>
