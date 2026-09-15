<script lang="ts">
  interface Props {
    pin?: string[];
    error?: string;
    class?: string;
    onchange?: (pin: string) => void;
  }

  let {
    pin = $bindable(Array(6).fill('')),
    error = '',
    class: className = '',
    onchange
  }: Props = $props();

  let inputs: HTMLInputElement[] = [];
  let shaking = $state(false);

  $effect(() => {
    if (error) {
      shaking = true;
      setTimeout(() => (shaking = false), 400);
    }
  });

  function handleInput(index: number, e: Event) {
    const target = e.target as HTMLInputElement;
    const val = target.value.replace(/\D/g, '').slice(-1);
    pin[index] = val;
    if (val && index < 5) {
      inputs[index + 1]?.focus();
    }
    onchange?.(pin.join(''));
  }

  function handleKeydown(index: number, e: KeyboardEvent) {
    if (e.key === 'Backspace' && !pin[index] && index > 0) {
      pin[index - 1] = '';
      inputs[index - 1]?.focus();
    }
  }

  function handlePaste(e: ClipboardEvent) {
    e.preventDefault();
    const text = e.clipboardData?.getData('text').replace(/\D/g, '').slice(0, 6) ?? '';
    text.split('').forEach((char, i) => {
      if (i < 6) pin[i] = char;
    });
    inputs[Math.min(text.length, 5)]?.focus();
    onchange?.(pin.join(''));
  }
</script>

<div class="flex flex-col items-center gap-4 {className}">
  <div
    class="flex gap-2 {shaking ? 'pin-shake' : ''}"
    role="group"
    aria-label="PIN input"
  >
    {#each Array(6) as _, i}
      <input
        bind:this={inputs[i]}
        type="password"
        inputmode="numeric"
        maxlength="2"
        value={pin[i]}
        oninput={(e) => handleInput(i, e)}
        onkeydown={(e) => handleKeydown(i, e)}
        onpaste={i === 0 ? handlePaste : undefined}
        class="w-11 h-14 text-center bg-[var(--color-canvas)] border-2 border-[var(--color-hairline)] rounded-[var(--radius-md)] font-sans text-[var(--text-title-lg)] text-[var(--color-ink)] focus:border-[var(--color-primary)] focus:outline-none transition-all duration-150 {error ? 'border-[var(--color-error)]' : ''} {pin[i] ? 'border-[var(--color-primary-active)]' : ''}"
        aria-label="PIN digit {i + 1}"
      />
    {/each}
  </div>
  {#if error}
    <p class="font-sans text-[var(--text-caption)] text-[var(--color-error)]">{error}</p>
  {/if}
</div>
