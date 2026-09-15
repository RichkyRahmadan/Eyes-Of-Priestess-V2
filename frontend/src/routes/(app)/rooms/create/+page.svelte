<script lang="ts">
  import Input from '$lib/components/ui/Input.svelte';
  import Button from '$lib/components/ui/Button.svelte';
  import PinInput from '$lib/components/ui/PinInput.svelte';
  import { roomApi } from '$lib/api';
  import { goto } from '$app/navigation';
  import { addNotification } from '$lib/stores/notifications';
  import { formatIDR } from '$lib/utils/format';
  import type { ItemCategory, Room } from '$lib/types';

  let form = $state({
    title: '',
    description: '',
    itemCategory: 'DIGITAL_PRODUCT' as ItemCategory,
    itemPrice: '',
    buyerUsername: '',
    role: 'SELLER' as 'SELLER' | 'BUYER'
  });

  let loading = $state(false);
  let errors = $state<Record<string, string>>({});
  let showPinModal = $state(false);
  let pinValues = $state(Array(6).fill(''));
  let pinError = $state('');

  const numPrice = $derived(Number(form.itemPrice.replace(/\D/g, '')) || 0);
  const fee = $derived(Math.round(numPrice * 0.01)); // 1% fee
  const totalAmount = $derived(numPrice + fee);

  const categories: { value: ItemCategory; label: string }[] = [
    { value: 'DIGITAL_PRODUCT', label: 'Produk Digital (Lisensi, Account, Key)' },
    { value: 'GAME_ACCOUNT', label: 'Akun Game' },
    { value: 'GAME_ITEM', label: 'Item / Currency Game' },
    { value: 'PHYSICAL_PRODUCT', label: 'Produk Fisik' },
    { value: 'SERVICE', label: 'Jasa / Freelance' },
    { value: 'OTHER', label: 'Lainnya' }
  ];

  function validate() {
    const errs: Record<string, string> = {};
    if (!form.title.trim()) errs.title = 'Judul room wajib diisi';
    if (!form.description.trim()) errs.description = 'Deskripsi transaksi wajib diisi';
    if (!numPrice || numPrice < 10000) errs.itemPrice = 'Harga minimal Rp 10.000';
    if (!form.buyerUsername.trim()) errs.buyerUsername = 'Username pihak lawan wajib diisi';
    return errs;
  }

  function handlePreSubmit(e: Event) {
    e.preventDefault();
    const v = validate();
    if (Object.keys(v).length > 0) {
      errors = v;
      return;
    }
    errors = {};
    showPinModal = true;
  }

  async function handleFinalSubmit() {
    const pin = pinValues.join('');
    if (pin.length !== 6) {
      pinError = 'Masukkan 6 digit PIN';
      return;
    }

    loading = true;
    pinError = '';
    try {
      const room = await roomApi.createRoom({
        title: form.title,
        description: form.description,
        itemCategory: form.itemCategory,
        itemPrice: numPrice,
        counterpartyUsername: form.buyerUsername,
        myRole: form.role,
        pin
      }) as Room;

      addNotification({
        type: 'success',
        title: 'Room Berhasil Dibuat',
        message: `Kode Room: ${room.roomCode}`
      });
      goto(`/rooms/${room.roomId}`);
    } catch (err: unknown) {
      const apiErr = err as { message?: string };
      pinError = apiErr?.message ?? 'PIN salah atau gagal membuat room';
    } finally {
      loading = false;
    }
  }
</script>

<svelte:head>
  <title>Buat Room Escrow - EyesOfPriestess</title>
</svelte:head>

<div class="max-w-2xl mx-auto pb-20 lg:pb-8">
  <div class="mb-6">
    <a href="/rooms" class="font-sans text-[13px] text-[var(--color-muted)] hover:text-[var(--color-primary)] transition-colors">
      &larr; Kembali ke daftar room
    </a>
    <h1 class="font-display text-[var(--text-display-md)] text-[var(--color-ink)] tracking-[-0.015em] mt-1">
      Buat Room Escrow Baru
    </h1>
    <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)] mt-1">
      Atur kesepakatan transaksi dengan aman. Escrow akan memegang dana hingga barang diterima.
    </p>
  </div>

  <form onsubmit={handlePreSubmit} class="flex flex-col gap-6">
    <!-- Role selection -->
    <div class="bg-[var(--color-surface-card)] p-5 rounded-[var(--radius-xl)]">
      <label class="font-sans text-[13px] font-medium text-[var(--color-ink)] mb-3 block">
        Peran Anda dalam Transaksi Ini
      </label>
      <div class="grid grid-cols-2 gap-3">
        <button
          type="button"
          onclick={() => (form.role = 'SELLER')}
          class="p-4 rounded-[var(--radius-lg)] border text-left transition-all duration-150 {form.role === 'SELLER' ? 'border-[var(--color-primary)] bg-[var(--color-primary)]/5 text-[var(--color-ink)]' : 'border-[var(--color-hairline)] bg-[var(--color-canvas)] text-[var(--color-muted)] hover:border-[var(--color-muted)]'}"
        >
          <span class="font-sans font-medium text-[14px] block">Penjual (Seller)</span>
          <span class="font-sans text-[12px] text-[var(--color-muted)]">Anda yang menyediakan barang/jasa</span>
        </button>
        <button
          type="button"
          onclick={() => (form.role = 'BUYER')}
          class="p-4 rounded-[var(--radius-lg)] border text-left transition-all duration-150 {form.role === 'BUYER' ? 'border-[var(--color-primary)] bg-[var(--color-primary)]/5 text-[var(--color-ink)]' : 'border-[var(--color-hairline)] bg-[var(--color-canvas)] text-[var(--color-muted)] hover:border-[var(--color-muted)]'}"
        >
          <span class="font-sans font-medium text-[14px] block">Pembeli (Buyer)</span>
          <span class="font-sans text-[12px] text-[var(--color-muted)]">Anda yang menyetor dana escrow</span>
        </button>
      </div>
    </div>

    <!-- Details -->
    <div class="bg-[var(--color-surface-card)] p-6 rounded-[var(--radius-xl)] flex flex-col gap-4">
      <Input
        label="Judul Transaksi"
        bind:value={form.title}
        placeholder="Contoh: Akun Mobile Legends Diamond 5000"
        error={errors.title}
        required
        id="room-title"
      />

      <div class="flex flex-col gap-1.5">
        <label for="room-cat" class="font-sans text-[13px] font-medium text-[var(--color-ink)]">
          Kategori Barang / Jasa
        </label>
        <select
          id="room-cat"
          bind:value={form.itemCategory}
          class="h-10 px-3 bg-[var(--color-canvas)] border border-[var(--color-hairline)] rounded-[var(--radius-md)] font-sans text-[14px] text-[var(--color-ink)] focus:outline-none focus:border-[var(--color-primary)]"
        >
          {#each categories as cat}
            <option value={cat.value}>{cat.label}</option>
          {/each}
        </select>
      </div>

      <div class="flex flex-col gap-1.5">
        <label for="room-desc" class="font-sans text-[13px] font-medium text-[var(--color-ink)]">
          Deskripsi & Syarat Transaksi
        </label>
        <textarea
          id="room-desc"
          bind:value={form.description}
          rows="3"
          placeholder="Tuliskan spesifikasi barang, garansi, atau ketentuan penyerahan secara mendetail..."
          class="p-3 bg-[var(--color-canvas)] border border-[var(--color-hairline)] rounded-[var(--radius-md)] font-sans text-[14px] text-[var(--color-ink)] focus:outline-none focus:border-[var(--color-primary)]"
        ></textarea>
        {#if errors.description}
          <span class="font-sans text-[12px] text-[var(--color-error)]">{errors.description}</span>
        {/if}
      </div>

      <Input
        label="Username Pihak Lawan ({form.role === 'SELLER' ? 'Pembeli' : 'Penjual'})"
        bind:value={form.buyerUsername}
        placeholder="Username pengguna EyesOfPriestess"
        error={errors.buyerUsername}
        required
        id="room-counterparty"
      />

      <Input
        label="Harga Barang / Jasa (Rp)"
        type="number"
        bind:value={form.itemPrice}
        placeholder="100000"
        error={errors.itemPrice}
        required
        id="room-price"
      />
    </div>

    <!-- Summary Box -->
    <div class="bg-[var(--color-surface-soft)] p-5 rounded-[var(--radius-xl)] space-y-2">
      <div class="flex justify-between items-center text-[13px] text-[var(--color-body)]">
        <span>Harga Barang</span>
        <span class="font-medium text-[var(--color-ink)]">{formatIDR(numPrice)}</span>
      </div>
      <div class="flex justify-between items-center text-[13px] text-[var(--color-body)]">
        <span>Biaya Escrow (1%)</span>
        <span class="text-[var(--color-muted)]">{formatIDR(fee)}</span>
      </div>
      <div class="flex justify-between items-center text-[15px] font-semibold text-[var(--color-ink)] pt-2 border-t border-[var(--color-hairline-soft)]">
        <span>Total yang didanai Escrow</span>
        <span class="font-display text-[20px] text-[var(--color-primary)]">{formatIDR(totalAmount)}</span>
      </div>
    </div>

    <Button variant="primary" type="submit" size="lg" class="w-full">
      Lanjut & Verifikasi PIN
    </Button>
  </form>

  <!-- PIN Modal -->
  {#if showPinModal}
    <div
      class="fixed inset-0 z-50 flex items-center justify-center p-4"
      role="dialog"
      aria-modal="true"
    >
      <div
        class="absolute inset-0 bg-[var(--color-ink)]/40"
        onclick={() => (showPinModal = false)}
        role="presentation"
      ></div>
      <div
        class="relative bg-[var(--color-canvas)] rounded-[var(--radius-xl)] p-8 w-full max-w-sm text-center shadow-[var(--shadow-elevated)]"
      >
        <h2 class="font-display text-[var(--text-display-sm)] text-[var(--color-ink)] mb-2">
          Verifikasi PIN
        </h2>
        <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)] mb-6">
          Masukkan PIN 6-digit untuk mengonfirmasi pembuatan room.
        </p>
        <PinInput bind:pin={pinValues} error={pinError} />
        <div class="mt-6 flex gap-3">
          <Button variant="secondary" class="flex-1" onclick={() => (showPinModal = false)}>
            Batal
          </Button>
          <Button variant="primary" class="flex-1" {loading} onclick={handleFinalSubmit}>
            Buat Room
          </Button>
        </div>
      </div>
    </div>
  {/if}
</div>
