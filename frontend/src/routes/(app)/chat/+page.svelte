<script lang="ts">
  import { MessageSquare, Search, Send, Shield, Lock, DoorOpen } from '@lucide/svelte';

  let activeChatId = $state('c-1');

  let chats = $state([
    {
      id: 'c-1',
      roomCode: 'EOP-8821',
      title: 'Jual Beli Akun Game FF',
      lastMessage: 'Dana sebesar Rp 252.500 berhasil terkunci.',
      lastMessageTime: '10:05',
      unreadCount: 0,
      status: 'FUNDED'
    },
    {
      id: 'c-2',
      roomCode: 'EOP-9104',
      title: 'Lisensi Software Graphic Pro',
      lastMessage: 'Halo mas, order sudah siap dibayar.',
      lastMessageTime: '08:30',
      unreadCount: 1,
      status: 'WAITING_PAYMENT'
    }
  ]);
</script>

<div class="h-[650px] card-editorial p-0 overflow-hidden grid grid-cols-1 lg:grid-cols-12 border border-hairline">
  <!-- Left Side: Chat List (4 Cols) -->
  <div class="lg:col-span-4 border-r border-hairline bg-canvas flex flex-col justify-between">
    <div class="p-4 border-b border-hairline space-y-3">
      <h2 class="font-display text-2xl font-bold text-ink">Diskusi & Chat</h2>
      <div class="relative">
        <Search size={14} class="absolute left-3 top-3 text-muted" />
        <input
          type="text"
          placeholder="Cari chat room..."
          class="w-full bg-surface-soft border border-hairline rounded-lg pl-9 pr-3 py-2 text-xs text-ink focus:outline-none focus:border-coral"
        />
      </div>
    </div>

    <!-- Chat Rooms Stream -->
    <div class="flex-1 overflow-y-auto divide-y divide-hairline">
      {#each chats as chat (chat.id)}
        <button
          onclick={() => activeChatId = chat.id}
          class="w-full text-left p-4 hover:bg-surface-soft transition-colors flex items-start justify-between gap-3
            {activeChatId === chat.id ? 'bg-surface-card border-l-4 border-coral' : ''}
          "
        >
          <div class="space-y-1">
            <div class="flex items-center gap-2">
              <span class="font-mono text-[11px] font-bold text-coral">#{chat.roomCode}</span>
              {#if chat.unreadCount > 0}
                <span class="w-2 h-2 rounded-full bg-coral"></span>
              {/if}
            </div>
            <div class="text-sm font-bold text-ink line-clamp-1">{chat.title}</div>
            <div class="text-xs text-muted line-clamp-1">{chat.lastMessage}</div>
          </div>

          <div class="text-right shrink-0">
            <div class="text-[10px] text-muted">{chat.lastMessageTime}</div>
          </div>
        </button>
      {/each}
    </div>
  </div>

  <!-- Right Side: Dark Navy Active Chat (8 Cols) -->
  <div class="lg:col-span-8 bg-surface-dark text-on-dark flex flex-col justify-between">
    <div class="p-4 bg-surface-dark-elevated border-b border-surface-dark-soft flex items-center justify-between">
      <div>
        <div class="text-xs font-mono text-coral font-bold">#EOP-8821</div>
        <div class="text-sm font-bold text-on-dark">Jual Beli Akun Game FF Sultan Level 50</div>
      </div>
      <a href="/rooms/rm-1" class="btn-editorial-primary text-xs font-semibold py-1.5 px-3">
        <DoorOpen size={14} /> Detail Room
      </a>
    </div>

    <div class="flex-1 p-6 overflow-y-auto space-y-4 text-xs font-sans">
      <div class="p-3 rounded-lg bg-surface-dark-elevated text-center text-on-dark-soft border border-surface-dark-soft space-y-1">
        <div class="font-mono text-coral uppercase font-bold text-[10px]">Pemberitahuan Keamanan</div>
        <div class="text-on-dark">Jangan pernah bertransaksi di luar sistem EyesOfPriestess untuk menghindari penipuan.</div>
      </div>

      <div class="flex flex-col items-start">
        <div class="text-[10px] text-on-dark-soft mb-1">Andi Wijaya • 10:05</div>
        <div class="p-3 rounded-xl bg-surface-dark-elevated text-on-dark max-w-[80%]">
          Halo mas, dana escrow sudah aman ya di vault.
        </div>
      </div>
    </div>

    <div class="p-4 bg-surface-dark-elevated border-t border-surface-dark-soft flex items-center gap-2">
      <input
        type="text"
        placeholder="Pesan terenkripsi..."
        class="flex-1 bg-surface-dark border border-surface-dark-soft rounded-lg px-4 py-2.5 text-xs text-on-dark focus:outline-none focus:border-coral"
      />
      <button class="p-2.5 rounded-lg bg-primary-coral text-white hover:bg-primary-active">
        <Send size={16} />
      </button>
    </div>
  </div>
</div>
