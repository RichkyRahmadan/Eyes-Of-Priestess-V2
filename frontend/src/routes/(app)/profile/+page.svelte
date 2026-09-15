<script lang="ts">
  import { currentUser, authStore } from '$lib/stores/auth';
  import { authApi } from '$lib/api';
  import { goto } from '$app/navigation';
  import Avatar from '$lib/components/ui/Avatar.svelte';
  import Button from '$lib/components/ui/Button.svelte';
  import { addNotification } from '$lib/stores/notifications';

  let loggingOut = $state(false);

  async function handleLogout() {
    loggingOut = true;
    try {
      await authApi.logout();
    } catch {
      // silent
    } finally {
      authStore.clearAuth();
      goto('/login');
    }
  }
</script>

<svelte:head>
  <title>Profil - EyesOfPriestess</title>
</svelte:head>

<div class="flex flex-col gap-6 pb-20 lg:pb-8 max-w-xl">
  <h1 class="font-display text-[var(--text-display-md)] text-[var(--color-ink)] tracking-[-0.015em]">
    Profil
  </h1>

  {#if $currentUser}
    <!-- Profile card -->
    <div class="bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] p-6 flex items-center gap-5">
      <Avatar name={$currentUser.fullName} src={$currentUser.avatarUrl} size="xl" />
      <div>
        <p class="font-display text-[var(--text-display-sm)] text-[var(--color-ink)] tracking-tight">
          {$currentUser.fullName}
        </p>
        <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)] mt-0.5">
          @{$currentUser.username}
        </p>
        <p class="font-sans text-[var(--text-caption)] text-[var(--color-muted-soft)] mt-1">
          {$currentUser.email}
        </p>
      </div>
    </div>

    <!-- Info rows -->
    <div class="bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] overflow-hidden">
      {#each [
        { label: 'Email', value: $currentUser.email },
        { label: 'Username', value: '@' + $currentUser.username },
        { label: 'Telepon', value: $currentUser.phoneNumber ?? '-' },
        { label: 'Status Verifikasi', value: $currentUser.isVerified ? 'Terverifikasi' : 'Belum diverifikasi' }
      ] as row, i}
        <div
          class="flex items-center justify-between px-5 py-4 {i < 3 ? 'border-b border-[var(--color-hairline-soft)]' : ''}"
        >
          <span class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)]">
            {row.label}
          </span>
          <span class="font-sans text-[var(--text-body-sm)] text-[var(--color-ink)] font-medium">
            {row.value}
          </span>
        </div>
      {/each}
    </div>

    <!-- Actions -->
    <div class="flex flex-col gap-2">
      <a
        href="/profile/change-pin"
        class="flex items-center justify-between px-5 py-4 bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] hover:shadow-[var(--shadow-soft)] transition-shadow"
      >
        <span class="font-sans font-medium text-[var(--text-body-sm)] text-[var(--color-ink)]">
          Ubah PIN
        </span>
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 256 256" fill="var(--color-muted-soft)"><path d="M221.66,133.66l-72,72a8,8,0,0,1-11.32-11.32L196.69,136H40a8,8,0,0,1,0-16H196.69L138.34,61.66a8,8,0,0,1,11.32-11.32l72,72A8,8,0,0,1,221.66,133.66Z"/></svg>
      </a>
      <a
        href="/wallet/bank-accounts"
        class="flex items-center justify-between px-5 py-4 bg-[var(--color-surface-card)] rounded-[var(--radius-xl)] hover:shadow-[var(--shadow-soft)] transition-shadow"
      >
        <span class="font-sans font-medium text-[var(--text-body-sm)] text-[var(--color-ink)]">
          Kelola Rekening Bank
        </span>
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 256 256" fill="var(--color-muted-soft)"><path d="M221.66,133.66l-72,72a8,8,0,0,1-11.32-11.32L196.69,136H40a8,8,0,0,1,0-16H196.69L138.34,61.66a8,8,0,0,1,11.32-11.32l72,72A8,8,0,0,1,221.66,133.66Z"/></svg>
      </a>
    </div>

    <!-- Logout -->
    <div class="pt-4 border-t border-[var(--color-hairline-soft)]">
      <Button variant="danger" loading={loggingOut} onclick={handleLogout} class="w-full">
        Keluar
      </Button>
    </div>
  {:else}
    <div class="p-12 text-center">
      <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)]">Memuat profil...</p>
    </div>
  {/if}
</div>
