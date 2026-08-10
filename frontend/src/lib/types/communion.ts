// ─── Communion (Chat) Types ───────────────────────────────
export interface CommunionMessage {
  id: string;
  covenantId: string;
  senderId: string;
  senderDisplay: string;
  type: 'USER' | 'SYSTEM';
  content: string;
  sentAt: string;
  isMine?: boolean;
}
