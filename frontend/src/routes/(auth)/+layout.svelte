<script lang="ts">
  let { children } = $props();
</script>

<div class="min-h-[100dvh] bg-[var(--color-canvas)] flex items-center justify-center px-4 py-12">
  <div class="w-full max-w-[420px]">
    <!-- Logo -->
    <div class="text-center mb-8">
      <a href="/" class="inline-block">
        <span
          class="font-display text-[var(--text-display-sm)] text-[var(--color-ink)] tracking-tight leading-none"
        >
          Eyes<span class="text-[var(--color-primary)]">Of</span>Priestess
        </span>
      </a>
    </div>

    <!-- Auth card -->
    <div
      class="bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] p-8 shadow-[var(--shadow-card)]"
    >
      {@render children()}
    </div>
  </div>
</div>
