<script lang="ts">
  interface Props {
    label?: string;
    type?: 'text' | 'email' | 'password' | 'number' | 'tel';
    value?: string | number;
    placeholder?: string;
    error?: string;
    disabled?: boolean;
    required?: boolean;
    id?: string;
    class?: string;
    oninput?: (e: Event) => void;
    onchange?: (e: Event) => void;
  }

  let {
    label = '',
    type = 'text',
    value = $bindable(''),
    placeholder = '',
    error = '',
    disabled = false,
    required = false,
    id = crypto.randomUUID(),
    class: className = '',
    oninput,
    onchange
  }: Props = $props();
</script>

<div class="flex flex-col gap-1.5 {className}">
  {#if label}
    <label
      for={id}
      class="font-sans text-[var(--text-caption)] font-medium text-[var(--color-body)]"
    >
      {label}
      {#if required}
        <span class="text-[var(--color-primary)]">*</span>
      {/if}
    </label>
  {/if}
  <input
    {id}
    {type}
    {disabled}
    {required}
    {placeholder}
    bind:value
    {oninput}
    {onchange}
    class="w-full h-10 px-3.5 py-2.5 bg-[var(--color-canvas)] border border-[var(--color-hairline)] rounded-[var(--radius-md)] font-sans text-[var(--text-body-md)] text-[var(--color-ink)] placeholder:text-[var(--color-muted-soft)] focus:outline-none focus:border-[var(--color-primary)] focus:ring-2 focus:ring-[var(--color-primary)]/15 transition-all duration-150 disabled:opacity-50 disabled:cursor-not-allowed {error ? 'border-[var(--color-error)] focus:border-[var(--color-error)] focus:ring-[var(--color-error)]/15' : ''}"
  />
  {#if error}
    <p class="font-sans text-[var(--text-caption)] text-[var(--color-error)]">{error}</p>
  {/if}
</div>
