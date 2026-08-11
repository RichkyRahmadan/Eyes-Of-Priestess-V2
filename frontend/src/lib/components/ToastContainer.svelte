<script lang="ts">
  import { notifications } from '$lib/stores';
  import { CheckCircle2, AlertCircle, AlertTriangle, Info, X } from '@lucide/svelte';

  function removeNotification(id: string) {
    notifications.update(n => n.filter(item => item.id !== id));
  }
</script>

<div class="fixed top-20 right-6 z-50 flex flex-col gap-3 max-w-sm w-full pointer-events-none">
  {#each $notifications as toast (toast.id)}
    <div class="pointer-events-auto flex items-start gap-3 p-4 rounded-xl border bg-canvas shadow-elevated transition-all animate-in slide-in-from-top-2 duration-200
      {toast.type === 'success' ? 'border-success text-ink' : ''}
      {toast.type === 'error' ? 'border-error text-ink' : ''}
      {toast.type === 'warning' ? 'border-warning text-ink' : ''}
      {toast.type === 'info' ? 'border-hairline text-ink' : ''}
    ">
      {#if toast.type === 'success'}
        <CheckCircle2 class="text-success shrink-0 mt-0.5" size={18} />
      {:else if toast.type === 'error'}
        <AlertCircle class="text-error shrink-0 mt-0.5" size={18} />
      {:else if toast.type === 'warning'}
        <AlertTriangle class="text-warning shrink-0 mt-0.5" size={18} />
      {:else}
        <Info class="text-coral shrink-0 mt-0.5" size={18} />
      {/if}

      <div class="flex-1 space-y-0.5">
        <h4 class="text-sm font-semibold">{toast.title}</h4>
        <p class="text-xs text-muted leading-relaxed">{toast.message}</p>
      </div>

      <button onclick={() => removeNotification(toast.id)} class="text-muted hover:text-ink">
        <X size={14} />
      </button>
    </div>
  {/each}
</div>
