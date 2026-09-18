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
      onkeydown={(e) => { if (e.key === 'Escape' && !loading) onCancel(); }}
      role="dialog"
      aria-modal="true"
      tabindex="-1"
    >
      <div class="flex items-start gap-4">
        {#if danger}
          <div class="w-10 h-10 rounded-full bg-[var(--color-error)]/10 text-[var(--color-error)] flex items-center justify-center shrink-0">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 256 256" fill="currentColor"><path d="M236.8,188.09,149.35,36.22h0a24.76,24.76,0,0,0-42.7,0L19.2,188.09a23.51,23.51,0,0,0,0,23.72A24.35,24.35,0,0,0,40.55,224h174.9a24.35,24.35,0,0,0,21.33-12.19A23.51,23.51,0,0,0,236.8,188.09ZM120,104a8,8,0,0,1,16,0v40a8,8,0,0,1-16,0Zm8,88a12,12,0,1,1,12-12A12,12,0,0,1,128,192Z"/></svg>
          </div>
        {:else}
          <div class="w-10 h-10 rounded-full bg-[var(--color-primary)]/10 text-[var(--color-primary)] flex items-center justify-center shrink-0">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 256 256" fill="currentColor"><path d="M128,24A104,104,0,1,0,232,128,104.11,104.11,0,0,0,128,24Zm0,192a88,88,0,1,1,88-88A88.1,88.1,0,0,1,128,216Zm16-40a8,8,0,0,1-8,8,16,16,0,0,1-16-16V128a8,8,0,0,1,0-16,16,16,0,0,1,16,16v40A8,8,0,0,1,144,176Zm-16-80a12,12,0,1,1,12-12A12,12,0,0,1,128,96Z"/></svg>
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
