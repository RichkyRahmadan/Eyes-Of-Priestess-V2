<script lang="ts">
  interface Props {
    variant?: 'primary' | 'secondary' | 'ghost' | 'danger';
    size?: 'sm' | 'md' | 'lg';
    disabled?: boolean;
    loading?: boolean;
    type?: 'button' | 'submit' | 'reset';
    class?: string;
    onclick?: () => void;
    children?: import('svelte').Snippet;
  }

  let {
    variant = 'primary',
    size = 'md',
    disabled = false,
    loading = false,
    type = 'button',
    class: className = '',
    onclick,
    children
  }: Props = $props();

  const base =
    'inline-flex items-center justify-center gap-2 font-sans font-medium rounded-[var(--radius-md)] transition-all duration-150 cursor-pointer select-none';

  const variants = {
    primary:
      'bg-[var(--color-primary)] text-[var(--color-on-primary)] hover:bg-[var(--color-primary-active)] active:scale-[0.98] disabled:bg-[var(--color-primary-disabled)] disabled:text-[var(--color-muted)] disabled:cursor-not-allowed',
    secondary:
      'bg-[var(--color-canvas)] text-[var(--color-ink)] border border-[var(--color-hairline)] hover:bg-[var(--color-surface-soft)] active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed',
    ghost:
      'bg-transparent text-[var(--color-primary)] hover:bg-[var(--color-surface-soft)] active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed',
    danger:
      'bg-[var(--color-error)] text-white hover:opacity-90 active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed'
  };

  const sizes = {
    sm: 'h-8 px-3 text-[13px]',
    md: 'h-10 px-5 text-[14px]',
    lg: 'h-12 px-6 text-[15px]'
  };
</script>

<button
  {type}
  class="{base} {variants[variant]} {sizes[size]} {className}"
  disabled={disabled || loading}
  {onclick}
>
  {#if loading}
    <svg
      class="animate-spin w-4 h-4"
      xmlns="http://www.w3.org/2000/svg"
      fill="none"
      viewBox="0 0 24 24"
    >
      <circle
        class="opacity-25"
        cx="12"
        cy="12"
        r="10"
        stroke="currentColor"
        stroke-width="4"
      ></circle>
      <path
        class="opacity-75"
        fill="currentColor"
        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"
      ></path>
    </svg>
  {/if}
  {@render children?.()}
</button>
