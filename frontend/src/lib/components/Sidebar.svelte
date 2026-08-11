<script lang="ts">
  import { page } from '$app/state';
  import { LayoutDashboard, Wallet, DoorOpen, MessageSquare, History, AlertTriangle, User, ArrowUpRight, ArrowDownLeft, ShieldCheck } from '@lucide/svelte';

  let currentPath = $derived(page.url.pathname);
</script>

<aside class="w-64 bg-canvas border-r border-hairline min-h-[calc(100vh-4rem)] p-4 flex flex-col justify-between hidden lg:flex">
  <div class="space-y-6">
    <!-- Menu Section -->
    <div>
      <div class="px-3 mb-2 text-xs font-semibold uppercase tracking-wider text-muted font-sans">
        Utama
      </div>
      <nav class="space-y-1">
        <a href="/dashboard" class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all {currentPath === '/dashboard' ? 'bg-surface-card text-coral font-semibold shadow-soft' : 'text-body hover:bg-surface-soft hover:text-ink'}">
          <LayoutDashboard size={18} />
          Dashboard
        </a>
        <a href="/wallet" class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all {currentPath.startsWith('/wallet') ? 'bg-surface-card text-coral font-semibold shadow-soft' : 'text-body hover:bg-surface-soft hover:text-ink'}">
          <Wallet size={18} />
          Wallet
        </a>
        <a href="/rooms" class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all {currentPath.startsWith('/rooms') ? 'bg-surface-card text-coral font-semibold shadow-soft' : 'text-body hover:bg-surface-soft hover:text-ink'}">
          <DoorOpen size={18} />
          Escrow Rooms
        </a>
        <a href="/chat" class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all {currentPath.startsWith('/chat') ? 'bg-surface-card text-coral font-semibold shadow-soft' : 'text-body hover:bg-surface-soft hover:text-ink'}">
          <MessageSquare size={18} />
          Chat & Diskusi
        </a>
      </nav>
    </div>

    <!-- Transaksi & Dispute -->
    <div>
      <div class="px-3 mb-2 text-xs font-semibold uppercase tracking-wider text-muted font-sans">
        Aktivitas
      </div>
      <nav class="space-y-1">
        <a href="/topup" class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm text-body hover:bg-surface-soft hover:text-ink">
          <ArrowDownLeft size={16} class="text-success" />
          Top-Up Saldo
        </a>
        <a href="/withdraw" class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm text-body hover:bg-surface-soft hover:text-ink">
          <ArrowUpRight size={16} class="text-coral" />
          Tarik Saldo
        </a>
        <a href="/transfer" class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm text-body hover:bg-surface-soft hover:text-ink">
          <Wallet size={16} />
          Transfer P2P
        </a>
        <a href="/disputes" class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all {currentPath.startsWith('/disputes') ? 'bg-surface-card text-coral font-semibold' : 'text-body hover:bg-surface-soft hover:text-ink'}">
          <AlertTriangle size={18} />
          Pusat Dispute
        </a>
      </nav>
    </div>
  </div>

  <!-- Escrow Security Assurance Card -->
  <div class="card-dark-chrome p-4 space-y-2">
    <div class="flex items-center gap-2 text-primary-coral">
      <ShieldCheck size={20} />
      <span class="font-display font-semibold text-sm">Escrow Protection</span>
    </div>
    <p class="text-xs text-on-dark-soft leading-relaxed">
      Dana disimpan dengan aman di smart hold vault hingga transaksi selesai dikonfirmasi.
    </p>
  </div>
</aside>
