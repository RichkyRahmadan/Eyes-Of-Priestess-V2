<script lang="ts">
  import { onMount, onDestroy } from 'svelte';
  import { page } from '$app/state';
  import { chatApi } from '$lib/api';
  import { wsManager } from '$lib/websocket/manager';
  import { currentUser } from '$lib/stores/auth';
  import Avatar from '$lib/components/ui/Avatar.svelte';
  import Skeleton from '$lib/components/ui/Skeleton.svelte';
  import { formatDateShort } from '$lib/utils/format';
  import type { ChatRoom, ChatMessage } from '$lib/types';

  let chatRooms = $state<ChatRoom[]>([]);
  let messages = $state<ChatMessage[]>([]);
  let loadingRooms = $state(true);
  let loadingMessages = $state(false);
  let selectedRoomId = $state<string | null>(null);
  let inputText = $state('');
  let messagesEl = $state<HTMLElement | null>(null);

  $effect(() => {
    const roomParam = page.url.searchParams.get('room');
    if (roomParam && chatRooms.find((r) => r.chatRoomId === roomParam)) {
      selectRoom(roomParam);
    }
  });

  const selectedRoom = $derived(chatRooms.find((r) => r.chatRoomId === selectedRoomId));

  onMount(() => {
    (async () => {
      try {
        chatRooms = (await chatApi.getRooms()) as ChatRoom[];
      } finally {
        loadingRooms = false;
      }
    })();

    const handleMsg = (msg: unknown) => {
      const data = msg as ChatMessage;
      if (data.chatRoomId === selectedRoomId) {
        messages = [...messages, data];
        scrollToBottom();
      }
      chatRooms = chatRooms.map((r) =>
        r.chatRoomId === data.chatRoomId
          ? { ...r, lastMessage: data.content, lastMessageAt: data.sentAt }
          : r
      );
    };

    wsManager.on('new_message', handleMsg);
    return () => wsManager.off('new_message', handleMsg);
  });

  async function selectRoom(roomId: string) {
    if (selectedRoomId === roomId) return;
    if (selectedRoomId) wsManager.leaveRoom(selectedRoomId);

    selectedRoomId = roomId;
    loadingMessages = true;
    messages = [];

    wsManager.joinRoom(roomId);
    try {
      const res = await chatApi.getMessages(roomId, 'size=50') as import('$lib/types').PaginatedResponse<ChatMessage>;
      messages = res.content;
      await chatApi.markRead(roomId);
    } finally {
      loadingMessages = false;
      scrollToBottom();
    }
  }

  function scrollToBottom() {
    setTimeout(() => {
      if (messagesEl) messagesEl.scrollTop = messagesEl.scrollHeight;
    }, 50);
  }

  function sendMessage() {
    const text = inputText.trim();
    if (!text || !selectedRoomId) return;
    wsManager.sendMessage(selectedRoomId, text);
    inputText = '';
  }

  function handleKeydown(e: KeyboardEvent) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  }

  onDestroy(() => {
    if (selectedRoomId) wsManager.leaveRoom(selectedRoomId);
  });
</script>

<svelte:head>
  <title>Chat - EyesOfPriestess</title>
</svelte:head>

<div class="flex gap-0 h-[calc(100dvh-7rem)] lg:h-[calc(100dvh-4rem)] -mx-4 lg:-mx-8 -mt-6 lg:-mt-8 rounded-[var(--radius-xl)] overflow-hidden border border-[var(--color-hairline-soft)]">
  <!-- Room list sidebar -->
  <div
    class="{selectedRoomId ? 'hidden lg:flex' : 'flex'} w-full lg:w-[320px] border-r border-[var(--color-hairline-soft)] flex-col bg-[var(--color-canvas)] shrink-0"
  >
    <div class="p-4 border-b border-[var(--color-hairline-soft)]">
      <h1 class="font-sans font-medium text-[var(--text-title-sm)] text-[var(--color-ink)]">
        Chat Room
      </h1>
    </div>

    <div class="flex-1 overflow-y-auto">
      {#if loadingRooms}
        <div class="p-4">
          <Skeleton rows={3} />
        </div>
      {:else if chatRooms.length === 0}
        <div class="p-8 text-center">
          <p class="font-sans text-[var(--text-body-sm)] text-[var(--color-muted)]">
            Belum ada chat room
          </p>
        </div>
      {:else}
        {#each chatRooms as room}
          <button
            onclick={() => selectRoom(room.chatRoomId)}
            class="w-full flex items-start gap-3 px-4 py-3.5 text-left hover:bg-[var(--color-surface-soft)] transition-colors duration-100 {selectedRoomId === room.chatRoomId ? 'bg-[var(--color-surface-card)]' : ''} border-b border-[var(--color-hairline-soft)]"
          >
            <div
              class="w-10 h-10 rounded-[var(--radius-md)] bg-[var(--color-primary)]/10 flex items-center justify-center shrink-0 font-mono text-[11px] text-[var(--color-primary)] font-medium"
            >
              {room.roomCode.slice(-3)}
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center justify-between gap-2">
                <p
                  class="font-sans font-medium text-[var(--text-body-sm)] text-[var(--color-ink)] truncate"
                >
                  {room.roomName}
                </p>
                {#if room.lastMessageAt}
                  <span class="font-sans text-[var(--text-caption)] text-[var(--color-muted-soft)] shrink-0">
                    {formatDateShort(room.lastMessageAt)}
                  </span>
                {/if}
              </div>
              <div class="flex items-center justify-between gap-2 mt-0.5">
                <p class="font-sans text-[var(--text-caption)] text-[var(--color-muted)] truncate">
                  {room.lastMessage ?? 'Belum ada pesan'}
                </p>
                {#if room.unreadCount > 0}
                  <span
                    class="min-w-[18px] h-[18px] bg-[var(--color-primary)] text-white text-[10px] font-medium rounded-full flex items-center justify-center px-1 shrink-0"
                  >
                    {room.unreadCount > 99 ? '99+' : room.unreadCount}
                  </span>
                {/if}
              </div>
            </div>
          </button>
        {/each}
      {/if}
    </div>
  </div>

  <!-- Chat pane -->
  <div
    class="{selectedRoomId ? 'flex' : 'hidden lg:flex'} flex-1 flex-col bg-[var(--color-surface-dark)] min-w-0"
  >
    {#if !selectedRoomId}
      <div class="flex-1 flex items-center justify-center">
        <div class="text-center">
          <p class="font-sans text-[var(--text-body-md)] text-[var(--color-on-dark-soft)]">
            Pilih room untuk mulai chat
          </p>
        </div>
      </div>
    {:else}
      <!-- Chat header -->
      <div
        class="px-5 py-4 border-b border-[var(--color-surface-dark-elevated)] flex items-center gap-3"
      >
        <button
          onclick={() => (selectedRoomId = null)}
          class="lg:hidden text-[var(--color-on-dark-soft)] hover:text-[var(--color-on-dark)] transition-colors"
          aria-label="Kembali"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 256 256" fill="currentColor"><path d="M228,128a8,8,0,0,1-8,8H59.31l58.35,58.34a8,8,0,0,1-11.32,11.32l-72-72a8,8,0,0,1,0-11.32l72-72a8,8,0,0,1,11.32,11.32L59.31,120H220A8,8,0,0,1,228,128Z"/></svg>
        </button>
        <div class="flex-1 min-w-0">
          <p class="font-sans font-medium text-[var(--text-body-sm)] text-[var(--color-on-dark)] truncate">
            {selectedRoom?.roomName ?? 'Loading...'}
          </p>
          {#if selectedRoom?.escrowStatus}
            <p class="font-sans text-[var(--text-caption)] text-[var(--color-on-dark-soft)]">
              {selectedRoom.escrowStatus}
            </p>
          {/if}
        </div>
        <a
          href="/rooms/{selectedRoom?.roomCode}"
          class="text-[var(--color-on-dark-soft)] hover:text-[var(--color-on-dark)] transition-colors"
          aria-label="Lihat room"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 256 256" fill="currentColor"><path d="M128,24A104,104,0,1,0,232,128,104.11,104.11,0,0,0,128,24Zm0,192a88,88,0,1,1,88-88A88.1,88.1,0,0,1,128,216Zm16-40a8,8,0,0,1-8,8,16,16,0,0,1-16-16V128a8,8,0,0,1,0-16,16,16,0,0,1,16,16v40A8,8,0,0,1,144,176ZM112,84a12,12,0,1,1,12,12A12,12,0,0,1,112,84Z"/></svg>
        </a>
      </div>

      <!-- Messages -->
      <div
        bind:this={messagesEl}
        class="flex-1 overflow-y-auto px-5 py-5 flex flex-col gap-3"
      >
        {#if loadingMessages}
          <div class="space-y-3">
            {#each Array(5) as _}
              <div class="flex gap-3">
                <div class="skeleton w-8 h-8 rounded-full opacity-20"></div>
                <div class="skeleton h-10 w-48 rounded-[var(--radius-lg)] opacity-20"></div>
              </div>
            {/each}
          </div>
        {:else}
          {#each messages as msg}
            {#if msg.messageType === 'SYSTEM' || msg.messageType === 'ESCROW_STATUS'}
              <div class="flex justify-center py-1">
                <span
                  class="font-sans text-[var(--text-caption)] text-[var(--color-on-dark-soft)] bg-[var(--color-surface-dark-elevated)] px-3 py-1 rounded-[var(--radius-pill)] italic"
                >
                  {msg.content}
                </span>
              </div>
            {:else}
              {@const isOwn = msg.senderId === $currentUser?.id}
              <div
                class="flex items-end gap-2 {isOwn ? 'flex-row-reverse' : ''}"
              >
                {#if !isOwn}
                  <Avatar name={msg.senderName} src={msg.senderAvatar} size="xs" />
                {/if}
                <div class="flex flex-col gap-1 max-w-[72%] {isOwn ? 'items-end' : 'items-start'}">
                  {#if !isOwn}
                    <span class="font-sans text-[10px] text-[var(--color-on-dark-soft)] ml-1">
                      {msg.senderName}
                    </span>
                  {/if}
                  <div
                    class="px-4 py-2.5 rounded-[var(--radius-lg)] font-sans text-[var(--text-body-sm)] leading-relaxed {isOwn ? 'bg-[var(--color-primary)] text-white rounded-br-[var(--radius-sm)]' : 'bg-[var(--color-surface-dark-elevated)] text-[var(--color-on-dark)] rounded-bl-[var(--radius-sm)]'}"
                  >
                    {msg.content}
                  </div>
                  <span class="font-sans text-[10px] text-[var(--color-on-dark-soft)] mx-1">
                    {formatDateShort(msg.sentAt)}
                  </span>
                </div>
              </div>
            {/if}
          {/each}
        {/if}
      </div>

      <!-- Input area -->
      <div class="px-4 py-3 border-t border-[var(--color-surface-dark-elevated)] flex items-end gap-2">
        <textarea
          bind:value={inputText}
          onkeydown={handleKeydown}
          placeholder="Ketik pesan..."
          rows="1"
          class="flex-1 resize-none bg-[var(--color-surface-dark-elevated)] text-[var(--color-on-dark)] placeholder:text-[var(--color-on-dark-soft)] font-sans text-[var(--text-body-sm)] px-4 py-2.5 rounded-[var(--radius-lg)] focus:outline-none focus:ring-1 focus:ring-[var(--color-primary)]/40 transition-all"
          style="max-height: 120px;"
        ></textarea>
        <button
          onclick={sendMessage}
          disabled={!inputText.trim()}
          class="w-10 h-10 rounded-[var(--radius-md)] bg-[var(--color-primary)] text-white flex items-center justify-center hover:bg-[var(--color-primary-active)] active:scale-[0.95] transition-all duration-150 disabled:opacity-40 disabled:cursor-not-allowed shrink-0"
          aria-label="Kirim pesan"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 256 256" fill="currentColor"><path d="M231.87,114l-168-95.89A16,16,0,0,0,40.92,37l15.66,65.49L66.39,128l-9.81,25.49L40.92,219A16,16,0,0,0,56,240a16.14,16.14,0,0,0,7.92-2.1l168-95.89a16,16,0,0,0,0-28ZM56,224a.56.56,0,0,1,0-.22l9.54-39.94L112,128l-46.42-55.86L56.08,32.28A.56.56,0,0,1,56,32l168,95.85Z"/></svg>
        </button>
      </div>
    {/if}
  </div>
</div>
