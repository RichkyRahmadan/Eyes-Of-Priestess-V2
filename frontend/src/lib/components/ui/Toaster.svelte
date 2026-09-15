<script module>
  export { toasts } from '$lib/stores/notifications';
</script>

<script lang="ts">
  import { notifications, removeNotification } from '$lib/stores/notifications';
  import type { Notification } from '$lib/stores/notifications';

  const icons = {
    success: `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 256 256" fill="currentColor"><path d="M173.66,98.34a8,8,0,0,1,0,11.32l-56,56a8,8,0,0,1-11.32,0l-24-24a8,8,0,0,1,11.32-11.32L112,148.69l50.34-50.35A8,8,0,0,1,173.66,98.34ZM232,128A104,104,0,1,1,128,24,104.11,104.11,0,0,1,232,128Zm-16,0a88,88,0,1,0-88,88A88.1,88.1,0,0,0,216,128Z"/></svg>`,
    error: `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 256 256" fill="currentColor"><path d="M236.8,188.09,149.35,36.22a24.76,24.76,0,0,0-42.7,0L19.2,188.09a23.51,23.51,0,0,0,0,23.72A24.35,24.35,0,0,0,40.55,224h174.9a24.35,24.35,0,0,0,21.33-12.19A23.51,23.51,0,0,0,236.8,188.09ZM120,104a8,8,0,0,1,16,0v40a8,8,0,0,1-16,0Zm8,88a12,12,0,1,1,12-12A12,12,0,0,1,128,192Z"/></svg>`,
    warning: `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 256 256" fill="currentColor"><path d="M236.8,188.09,149.35,36.22a24.76,24.76,0,0,0-42.7,0L19.2,188.09a23.51,23.51,0,0,0,0,23.72A24.35,24.35,0,0,0,40.55,224h174.9a24.35,24.35,0,0,0,21.33-12.19A23.51,23.51,0,0,0,236.8,188.09ZM120,104a8,8,0,0,1,16,0v40a8,8,0,0,1-16,0Zm8,88a12,12,0,1,1,12-12A12,12,0,0,1,128,192Z"/></svg>`,
    info: `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 256 256" fill="currentColor"><path d="M128,24A104,104,0,1,0,232,128,104.11,104.11,0,0,0,128,24Zm0,192a88,88,0,1,1,88-88A88.1,88.1,0,0,1,128,216Zm16-40a8,8,0,0,1-8,8,16,16,0,0,1-16-16V128a8,8,0,0,1,0-16,16,16,0,0,1,16,16v40A8,8,0,0,1,144,176ZM112,84a12,12,0,1,1,12,12A12,12,0,0,1,112,84Z"/></svg>`
  };

  const colors: Record<Notification['type'], { bg: string; text: string; border: string }> = {
    success: {
      bg: 'bg-[var(--color-surface-card)]',
      text: 'text-[var(--color-success)]',
      border: 'border-l-4 border-l-[var(--color-success)]'
    },
    error: {
      bg: 'bg-[var(--color-surface-card)]',
      text: 'text-[var(--color-error)]',
      border: 'border-l-4 border-l-[var(--color-error)]'
    },
    warning: {
      bg: 'bg-[var(--color-surface-card)]',
      text: 'text-[var(--color-warning)]',
      border: 'border-l-4 border-l-[var(--color-warning)]'
    },
    info: {
      bg: 'bg-[var(--color-surface-card)]',
      text: 'text-[var(--color-primary)]',
      border: 'border-l-4 border-l-[var(--color-primary)]'
    }
  };
</script>

<div
  class="fixed top-4 right-4 z-[100] flex flex-col gap-2 w-80"
  aria-live="polite"
  aria-label="Notifikasi"
>
  {#each $notifications as notif (notif.id)}
    <div
      class="toast-enter {colors[notif.type].bg} {colors[notif.type].border} rounded-[var(--radius-md)] shadow-[var(--shadow-elevated)] p-4 flex items-start gap-3"
    >
      <span class="{colors[notif.type].text} shrink-0 mt-0.5">
        {@html icons[notif.type]}
      </span>
      <div class="flex-1 min-w-0">
        <p class="font-sans font-medium text-[14px] text-[var(--color-ink)]">{notif.title}</p>
        {#if notif.message}
          <p class="font-sans text-[13px] text-[var(--color-muted)] mt-0.5">{notif.message}</p>
        {/if}
      </div>
      <button
        onclick={() => removeNotification(notif.id)}
        class="text-[var(--color-muted-soft)] hover:text-[var(--color-ink)] transition-colors shrink-0"
        aria-label="Tutup notifikasi"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 256 256" fill="currentColor">
          <path d="M205.66,194.34a8,8,0,0,1-11.32,11.32L128,139.31,61.66,205.66a8,8,0,0,1-11.32-11.32L116.69,128,50.34,61.66A8,8,0,0,1,61.66,50.34L128,116.69l66.34-66.35a8,8,0,0,1,11.32,11.32L139.31,128Z"/>
        </svg>
      </button>
    </div>
  {/each}
</div>
