<script lang="ts">
  import { page } from '$app/state';
  import Input from '$lib/components/ui/Input.svelte';
  import Button from '$lib/components/ui/Button.svelte';
  import { roomApi } from '$lib/api';
  import { addNotification } from '$lib/stores/notifications';
  import { goto } from '$app/navigation';
  import type { Room } from '$lib/types';

  let notes = $state('');
  let proofUrls = $state<string[]>(['']);
  let loading = $state(false);
  let error = $state('');

  function addUrlField() {
    if (proofUrls.length < 5) {
      proofUrls = [...proofUrls, ''];
    }
  }

  function removeUrlField(index: number) {
    if (proofUrls.length > 1) {
      proofUrls = proofUrls.filter((_, i) => i !== index);
    }
  }

  async function handleSubmit(e: Event) {
    e.preventDefault();
    error = '';
    const filteredUrls = proofUrls.map((u) => u.trim()).filter(Boolean);

    if (!notes.trim()) {
      error = 'Catatan pengiriman wajib diisi';
      return;
    }

    loading = true;
    try {
      const roomId = page.params.id;
      const updated = await roomApi.deliverRoom(roomId, {
        notes,
        proofUrls: filteredUrls
      }) as Room;

      addNotification({
        type: 'success',
        title: 'Bukti Pengiriman Terkirim',
        message: 'Status room telah diperbarui ke DELEVERED'
      });
      goto(`/rooms/${roomId}`);
    } catch (err: unknown) {
      const apiErr = err as { message?: string };
      error = apiErr?.message ?? 'Gagal mengunggah bukti pengiriman';
    } finally {
      loading = false;
    }
  }
</script>

<svelte:head>
  <title>Upload Bukti Pengiriman - EyesOfPriestess</title>
</svelte:head>

<div class="max-w-xl mx-auto pb-20 lg:pb-8">
  <div class="mb-6">
    <a href="/rooms/{page.params.id}" class="font-sans text-[13px] text-[var(--color-muted)] hover:text-[var(--color-primary)] transition-colors">
      &larr; Kembali ke Room
    </a>
    <h1 class="font-display text-[var(--text-display-md)] text-[var(--color-ink)] tracking-[-0.015em] mt-1">
      Upload Bukti Pengiriman
    </h1>
    <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)] mt-1">
      Kirimkan detail, link tangkapan layar, atau akun kepada pembeli.
    </p>
  </div>

  <form onsubmit={handleSubmit} class="flex flex-col gap-6">
    {#if error}
      <div class="bg-[var(--color-error)]/8 border border-[var(--color-error)]/20 rounded-[var(--radius-md)] px-4 py-3">
        <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-error)]">{error}</p>
      </div>
    {/if}

    <div class="bg-[var(--color-surface-card)] p-6 rounded-[var(--radius-xl)] flex flex-col gap-4">
      <div class="flex flex-col gap-1.5">
        <label for="deliver-notes" class="font-sans text-[13px] font-medium text-[var(--color-ink)]">
          Catatan Pengiriman / Informasi Kredensial
        </label>
        <textarea
          id="deliver-notes"
          bind:value={notes}
          rows="4"
          placeholder="Tuliskan data akun, nomor resi pengiriman, atau petunjuk penggunaan secara lengkap..."
          class="p-3 bg-[var(--color-canvas)] border border-[var(--color-hairline)] rounded-[var(--radius-md)] font-sans text-[14px] text-[var(--color-ink)] focus:outline-none focus:border-[var(--color-primary)]"
          required
        ></textarea>
      </div>

      <!-- Proof URLs -->
      <div class="flex flex-col gap-2">
        <label class="font-sans text-[13px] font-medium text-[var(--color-ink)] block">
          URL Bukti Gambar / Tangkapan Layar (Opsional)
        </label>

        {#each proofUrls as url, idx}
          <div class="flex gap-2">
            <Input
              bind:value={proofUrls[idx]}
              placeholder="https://imgur.com/screenshot.png"
              id="proof-url-{idx}"
            />
            {#if proofUrls.length > 1}
              <button
                type="button"
                onclick={() => removeUrlField(idx)}
                class="px-3 text-[var(--color-error)] hover:bg-[var(--color-error)]/10 rounded-[var(--radius-md)] transition-colors"
              >
                Hapus
              </button>
            {/if}
          </div>
        {/each}

        {#if proofUrls.length < 5}
          <button
            type="button"
            onclick={addUrlField}
            class="self-start font-sans text-[12px] text-[var(--color-primary)] hover:underline font-medium mt-1"
          >
            + Tambah Link Bukti Lagi
          </button>
        {/if}
      </div>
    </div>

    <Button variant="primary" type="submit" size="lg" {loading} class="w-full">
      Kirim Bukti Pengiriman
    </Button>
  </form>
</div>
