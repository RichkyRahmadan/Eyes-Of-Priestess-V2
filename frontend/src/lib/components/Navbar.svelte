<script lang="ts">
  import { authStore, walletStore, formattedBalance } from '$lib/stores';
  import { Shield, Wallet, DoorOpen, MessageSquare, History, User, LogOut, Bell, PlusCircle } from '@lucide/svelte';
  import { goto } from '$app/navigation';

  let user = $derived($authStore.user);

  function handleLogout() {
    authStore.clearAuth();
    goto('/login');
  }
</script>

<header class="h-16 bg-canvas border-b border-hairline sticky top-0 z-40 px-6 flex items-center justify-between">
  <!-- Brand Logo -->
  <a href="/dashboard" class="flex items-center gap-2 text-decoration-none group">
    <div class="w-9 h-9 rounded-lg bg-primary-coral flex items-center justify-center text-white shadow-soft transition-transform group-hover:scale-105">
      <Shield size={20} />
    </div>
    <span class="font-display text-2xl font-bold tracking-tight text-ink">
      EyesOfPriestess
    </span>
  </a>

  <!-- Navigation Links -->
  <nav class="hidden md:flex items-center gap-1 bg-surface-soft p-1 rounded-lg border border-hairline">
    <a href="/dashboard" class="px-3.5 py-1.5 rounded-md text-sm font-medium text-body hover:text-ink hover:bg-canvas transition-all flex items-center gap-2">
      Dashboard
    </a>
    <a href="/wallet" class="px-3.5 py-1.5 rounded-md text-sm font-medium text-body hover:text-ink hover:bg-canvas transition-all flex items-center gap-2">
      <Wallet size={15} />
      Wallet
    </a>
    <a href="/rooms" class="px-3.5 py-1.5 rounded-md text-sm font-medium text-body hover:text-ink hover:bg-canvas transition-all flex items-center gap-2">
      <DoorOpen size={15} />
      Escrow Rooms
    </a>
    <a href="/chat" class="px-3.5 py-1.5 rounded-md text-sm font-medium text-body hover:text-ink hover:bg-canvas transition-all flex items-center gap-2">
      <MessageSquare size={15} />
      Chat
    </a>
    <a href="/disputes" class="px-3.5 py-1.5 rounded-md text-sm font-medium text-body hover:text-ink hover:bg-canvas transition-all flex items-center gap-2">
      Disputes
    </a>
  </nav>

  <!-- Right Section: Balance & User Profile -->
  <div class="flex items-center gap-4">
    <!-- Quick Create Room -->
    <a href="/rooms?create=true" class="hidden sm:inline-flex btn-editorial-primary text-xs font-semibold uppercase tracking-wider py-2">
      <PlusCircle size={15} />
      Buat Room
    </a>

    <!-- Wallet Balance Pill -->
    <a href="/wallet" class="hidden sm:flex items-center gap-2 px-3 py-1.5 rounded-pill bg-surface-card border border-hairline text-xs font-medium hover:bg-surface-cream-strong transition-colors">
      <span class="w-2 h-2 rounded-full bg-success"></span>
      <span class="font-mono text-ink font-semibold">{$formattedBalance}</span>
    </a>

    <!-- Profile Dropdown Button -->
    <div class="flex items-center gap-2 border-l border-hairline pl-4">
      <a href="/profile" class="flex items-center gap-2 text-ink hover:text-coral transition-colors">
        <div class="w-8 h-8 rounded-full bg-surface-dark text-on-dark flex items-center justify-center font-display font-bold text-sm">
          {user?.fullName?.charAt(0) || 'U'}
        </div>
        <span class="hidden lg:inline text-sm font-medium">{user?.fullName || 'Pilgrim'}</span>
      </a>

      <button onclick={handleLogout} class="p-1.5 text-muted hover:text-error transition-colors" title="Logout">
        <LogOut size={16} />
      </button>
    </div>
  </div>
</header>
