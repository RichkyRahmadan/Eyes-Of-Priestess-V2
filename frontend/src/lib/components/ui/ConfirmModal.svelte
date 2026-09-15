<script lang="ts">
  interface Props {
    open: boolean;
    title: string;
    description: string;
    confirmLabel?: string;
    cancelLabel?: string;
    danger?: boolean;
    loading?: boolean;
    onConfirm: () => void;
    onCancel: () => void;
  }

  let {
    open = false,
    title,
    description,
    confirmLabel = 'Konfirmasi',
    cancelLabel = 'Batal',
    danger = false,
    loading = false,
    onConfirm,
    onCancel
  }: Props = $props();

  function handleKeydown(e: KeyboardEvent) {
    if (e.key === 'Escape' && open && !loading) {
      onCancel();
    }
  }
</script>

<svelte:window onkeydown={handleKeydown} />

{#if open}
  <!-- Backdrop -->
  <div
    class="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 transition-opacity duration-200"
    onclick={() => { if (!loading) onCancel(); }}
    role="presentation"
  >
    <!-- Modal Card -->
    <div
      class="bg-[var(--color-surface-card)] border border-[var(--color-hairline)] rounded-[var(--radius-xl)] max-w-md w-full p-6 shadow-xl flex flex-col gap-4 transform transition-all duration-200 scale-100"
      onclick={(e) => e.stopPropagation()}
      role="dialog"
      aria-modal="true"
      tabindex="-1"
    >
      <div class="flex items-start gap-4">
        {#if danger}
          <div class="w-10 h-10 rounded-full bg-[var(--color-error)]/10 text-[var(--color-error)] flex items-center justify-center shrink-0 font-bold text-lg">
            ⚠
          </div>
        {:else}
          <div class="w-10 h-10 rounded-full bg-[var(--color-primary)]/10 text-[var(--color-primary)] flex items-center justify-center shrink-0 font-bold text-lg">
            ?
          </div>
        {/if}

        <div class="flex-1 min-w-0">
          <h3 class="font-display text-[var(--text-title-md)] text-[var(--color-ink)] leading-snug">
            {title}
          </h3>
          <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)] mt-1.5 leading-relaxed">
            {description}
          </p>
        </div>
      </div>

      <div class="flex items-center justify-end gap-2.5 pt-3 border-t border-[var(--color-hairline-soft)] mt-2">
        <button
          type="button"
          onclick={onCancel}
          disabled={loading}
          class="h-9 px-4 rounded-[var(--radius-md)] border border-[var(--color-hairline)] bg-[var(--color-canvas)] hover:bg-[var(--color-surface-soft)] text-[var(--color-ink)] font-sans text-xs font-medium transition-colors disabled:opacity-50"
        >
          {cancelLabel}
        </button>

        <button
          type="button"
          onclick={onConfirm}
          disabled={loading}
          class="h-9 px-4 rounded-[var(--radius-md)] font-sans text-xs font-medium text-white transition-colors disabled:opacity-50 inline-flex items-center gap-1.5 {danger ? 'bg-[var(--color-error)] hover:bg-[var(--color-error)]/90' : 'bg-[var(--color-primary)] hover:bg-[var(--color-primary-active)]'}"
        >
          {#if loading}
            <span class="inline-block w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
          {/if}
          {confirmLabel}
        </button>
      </div>
    </div>
  </div>
{/if}
