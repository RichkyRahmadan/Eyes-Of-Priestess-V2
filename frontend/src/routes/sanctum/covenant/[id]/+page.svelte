<script lang="ts">
  import { page } from '$app/stores';
  import { onMount, onDestroy } from 'svelte';
  import { sealStore } from '$lib/stores/seal';
  import { covenantStore } from '$lib/stores/covenant';
  import { covenantApi } from '$lib/api/covenant';
  import { formatIDR, formatDateTime, statusBadgeClass, covenantStatusLabel } from '$lib/utils/formatters';
  import { toast } from 'svelte-sonner';
  import { ArrowLeft, Send, CheckCircle, Truck, AlertTriangle, XCircle, MessageSquare } from '@lucide/svelte';
  import type { CommunionMessage } from '$lib/types/communion';
  import LoadingCrystal from '$lib/components/shared/LoadingCrystal.svelte';

  const id = $derived($page.params.id);
  let loading   = $state(true);
  let actioning = $state(false);
  let messages  = $state<CommunionMessage[]>([]);
  let msgInput  = $state('');
  let ws        = $state<WebSocket | null>(null);

  const WS_BASE = import.meta.env.VITE_WS_URL ?? 'ws://localhost:8084/ws/communion';

  onMount(async () => {
    try {
      const cov = await covenantApi.getById(id);
      covenantStore.setCurrent(cov);
    } catch { toast.error('Gagal memuat covenant.'); }
    finally { loading = false; }

    // Open WebSocket
    const token = localStorage.getItem('accessSeal');
    if (token) {
      ws = new WebSocket(`${WS_BASE}/${id}?token=${token}`);
      ws.onmessage = (evt) => {
        try {
          const msg: CommunionMessage = JSON.parse(evt.data);
          msg.isMine = msg.senderId === $sealStore.pilgrim?.id;
          messages = [...messages, msg];
        } catch { /* ignore malformed */ }
      };
    }
  });

  onDestroy(() => { ws?.close(); });

  function sendMessage() {
    if (!msgInput.trim() || !ws || ws.readyState !== WebSocket.OPEN) return;
    ws.send(JSON.stringify({ type: 'TEXT', content: msgInput.trim() }));
    msgInput = '';
  }

  async function doAction(action: string) {
    if (actioning) return;
    actioning = true;
    try {
      let updated;
      if (action === 'seal')    updated = await covenantApi.seal(id);
      else if (action === 'deliver') updated = await covenantApi.deliver(id, {});
      else if (action === 'fulfill') updated = await covenantApi.fulfill(id);
      else if (action === 'cancel')  updated = await covenantApi.cancel(id);
      if (updated) covenantStore.setCurrent(updated);
      toast.success('Status covenant diperbarui!');
    } catch (e: unknown) { toast.error(e instanceof Error ? e.message : 'Gagal.'); }
    finally { actioning = false; }
  }

  const cov = $derived($covenantStore.current);
  const myId = $derived($sealStore.pilgrim?.id);
</script>

<svelte:head>
  <title>Covenant Chamber — EyesOfPriestess</title>
</svelte:head>

{#if loading}
  <LoadingCrystal />
{:else if cov}
<div class="chamber animate-fade-in">
  <!-- Back + Header -->
  <div class="chamber-header">
    <a href="/sanctum/covenant" class="btn btn-ghost btn-sm">
      <ArrowLeft size={16} /> Covenant Hall
    </a>
    <div class="ch-info">
      <h2 class="ch-title">{cov.title}</h2>
      <div class="ch-meta">
        <span class="badge {statusBadgeClass(cov.status)}">{covenantStatusLabel(cov.status)}</span>
        {#if cov.invitationCode}
          <span class="inv-code">🔑 {cov.invitationCode}</span>
        {/if}
        <span class="text-sm text-muted">{formatIDR(cov.amount)}</span>
      </div>
    </div>
  </div>

  <div class="chamber-grid">
    <!-- Left: Details + Actions -->
    <div class="chamber-left">
      <!-- Details Card -->
      <div class="card">
        <h3 class="card-title text-sm text-muted uppercase mb-3">Detail Covenant</h3>
        {#if cov.description}
          <p class="text-sm mb-3">{cov.description}</p>
        {/if}
        <div class="detail-rows">
          <div class="detail-row">
            <span class="text-muted text-xs">Nilai</span>
            <span class="gradient-gold font-bold">{formatIDR(cov.amount)}</span>
          </div>
          <div class="detail-row">
            <span class="text-muted text-xs">Status</span>
            <span class="badge {statusBadgeClass(cov.status)}">{covenantStatusLabel(cov.status)}</span>
          </div>
          <div class="detail-row">
            <span class="text-muted text-xs">Peran saya</span>
            <span class="text-sm font-semibold">
              {cov.initiatorId === myId ? (cov.initiatorRole === 'BUYER' ? '🛒 Buyer (Initiator)' : '📦 Seller (Initiator)') : 'Counterparty'}
            </span>
          </div>
          {#if cov.autoReleaseAt}
            <div class="detail-row">
              <span class="text-muted text-xs">Auto-Release</span>
              <span class="text-sm">{formatDateTime(cov.autoReleaseAt)}</span>
            </div>
          {/if}
        </div>
      </div>

      <!-- Actions Card -->
      <div class="card">
        <h3 class="card-title text-sm text-muted uppercase mb-3">Tindakan</h3>
        <div class="action-btns">
          {#if cov.status === 'ACCEPTED'}
            <button class="btn btn-gold btn-full" onclick={() => doAction('seal')} disabled={actioning}>
              <CheckCircle size={16} /> Segel & Deposit
            </button>
          {/if}
          {#if cov.status === 'SEALED' && (cov.initiatorRole === 'SELLER' ? cov.initiatorId === myId : cov.buyerId !== myId)}
            <button class="btn btn-primary btn-full" onclick={() => doAction('deliver')} disabled={actioning}>
              <Truck size={16} /> Konfirmasi Pengiriman
            </button>
          {/if}
          {#if cov.status === 'DELIVERED'}
            <button class="btn btn-gold btn-full" onclick={() => doAction('fulfill')} disabled={actioning}>
              <CheckCircle size={16} /> Konfirmasi Penerimaan
            </button>
            <button class="btn btn-ghost btn-full" onclick={() => doAction('dispute')} disabled={actioning} style="color:var(--danger)">
              <AlertTriangle size={16} /> Buka Sengketa
            </button>
          {/if}
          {#if ['FORGED', 'ACCEPTED'].includes(cov.status)}
            <button class="btn btn-ghost btn-full" onclick={() => doAction('cancel')} disabled={actioning} style="color:var(--danger)">
              <XCircle size={16} /> Batalkan Covenant
            </button>
          {/if}
        </div>
      </div>
    </div>

    <!-- Right: Communion Chat -->
    <div class="chamber-right">
      <div class="communion-box card">
        <div class="communion-header">
          <MessageSquare size={18} color="var(--void-400)" />
          <h3 class="text-sm font-semibold">Communion Chamber</h3>
          <div class="ws-status" class:connected={ws?.readyState === 1}></div>
        </div>

        <div class="communion-messages scroll-thin">
          {#if messages.length === 0}
            <div class="no-msgs">
              <MessageSquare size={32} color="var(--text-muted)" />
              <p class="text-sm text-muted">Belum ada pesan. Mulai percakapan!</p>
            </div>
          {:else}
            {#each messages as msg}
              <div class="msg-wrapper" class:mine={msg.isMine}>
                {#if !msg.isMine && msg.type !== 'SYSTEM'}
                  <div class="msg-avatar">{msg.senderDisplay.charAt(0)}</div>
                {/if}
                <div class="msg-bubble" class:system={msg.type === 'SYSTEM'}>
                  {#if !msg.isMine && msg.type !== 'SYSTEM'}
                    <p class="msg-sender">{msg.senderDisplay}</p>
                  {/if}
                  <p class="msg-content">{msg.content}</p>
                  <p class="msg-time">{formatDateTime(msg.sentAt)}</p>
                </div>
              </div>
            {/each}
          {/if}
        </div>

        <form class="communion-input" onsubmit={(e) => { e.preventDefault(); sendMessage(); }}>
          <input
            type="text"
            placeholder="Tulis pesan…"
            bind:value={msgInput}
            style="border-radius: var(--radius-md); flex:1;"
          />
          <button type="submit" class="btn btn-primary btn-icon" disabled={!msgInput.trim()}>
            <Send size={16} />
          </button>
        </form>
      </div>
    </div>
  </div>
</div>
{:else}
  <div class="empty-state" style="padding:4rem; text-align:center;">
    <p class="text-muted">Covenant tidak ditemukan.</p>
    <a href="/sanctum/covenant" class="btn btn-ghost btn-sm" style="margin-top:1rem;">← Kembali</a>
  </div>
{/if}

<style>
.chamber { display: flex; flex-direction: column; gap: 1.5rem; }

.chamber-header { display: flex; align-items: flex-start; gap: 1rem; flex-wrap: wrap; }
.ch-info { flex: 1; }
.ch-title { font-size: 1.25rem; font-weight: 700; margin-bottom: 0.5rem; }
.ch-meta  { display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap; }
.inv-code { font-size: 0.8125rem; font-family: monospace; color: var(--gold-400); font-weight: 700; letter-spacing: 0.1em; }

.chamber-grid {
  display: grid; grid-template-columns: 360px 1fr; gap: 1.5rem;
}
@media (max-width: 900px) { .chamber-grid { grid-template-columns: 1fr; } }

.chamber-left { display: flex; flex-direction: column; gap: 1.25rem; }

.card-title { font-size: 0.9375rem; font-weight: 600; }
.uppercase { text-transform: uppercase; letter-spacing: 0.08em; }
.mb-3 { margin-bottom: 0.75rem; }
.mb-0 { margin-bottom: 0; }
.font-bold { font-weight: 700; }
.font-semibold { font-weight: 600; }

.detail-rows { display: flex; flex-direction: column; gap: 0.75rem; }
.detail-row { display: flex; align-items: center; justify-content: space-between; }

.action-btns { display: flex; flex-direction: column; gap: 0.625rem; }

/* Communion */
.communion-box { display: flex; flex-direction: column; height: 520px; padding: 0; overflow: hidden; }
.communion-header {
  display: flex; align-items: center; gap: 0.625rem;
  padding: 1rem 1.25rem; border-bottom: 1px solid var(--border-subtle);
}
.ws-status {
  width: 8px; height: 8px; border-radius: 50%; background: var(--border-default); margin-left: auto;
}
.ws-status.connected { background: var(--success); }

.communion-messages {
  flex: 1; overflow-y: auto; padding: 1rem;
  display: flex; flex-direction: column; gap: 0.75rem;
}

.no-msgs {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 0.75rem;
}

.msg-wrapper { display: flex; align-items: flex-end; gap: 0.5rem; }
.msg-wrapper.mine { flex-direction: row-reverse; }

.msg-avatar {
  width: 28px; height: 28px; border-radius: 50%;
  background: var(--void-700); display: flex; align-items: center; justify-content: center;
  font-size: 0.75rem; font-weight: 700; color: var(--void-200); flex-shrink: 0;
}

.msg-bubble {
  max-width: 75%; padding: 0.625rem 0.875rem;
  border-radius: var(--radius-lg); background: var(--surface-700); border: 1px solid var(--border-subtle);
}

.msg-wrapper.mine .msg-bubble { background: rgba(109,40,217,0.2); border-color: rgba(139,92,246,0.3); }
.msg-bubble.system { background: var(--surface-600); opacity: 0.8; text-align: center; max-width: 100%; }

.msg-sender { font-size: 0.6875rem; color: var(--void-400); font-weight: 600; margin-bottom: 0.25rem; }
.msg-content { font-size: 0.875rem; line-height: 1.5; }
.msg-time    { font-size: 0.625rem; color: var(--text-muted); margin-top: 0.25rem; text-align: right; }

.communion-input {
  display: flex; gap: 0.5rem;
  padding: 0.75rem 1rem; border-top: 1px solid var(--border-subtle);
}
</style>
