// ─── Covenant (Escrow) Types ──────────────────────────────
export type CovenantStatus =
  | 'FORGED' | 'ACCEPTED' | 'SEALED' | 'DELIVERED' | 'FULFILLED'
  | 'DISPUTED' | 'CANCELLED';

export interface CovenantResponse {
  id: string;
  title: string;
  description?: string;
  initiatorId: string;
  initiatorRole: 'BUYER' | 'SELLER';
  counterpartyId?: string;
  buyerId?: string;
  sellerId?: string;
  amount: number;
  status: CovenantStatus;
  invitationCode?: string;
  autoReleaseHours: number;
  autoReleaseAt?: string;
  expiresAt: string;
  sealedAt?: string;
  deliveredAt?: string;
  fulfilledAt?: string;
  disputedAt?: string;
  cancelledAt?: string;
  createdAt: string;
}

export interface ForgeCovenantRequest {
  title: string;
  description?: string;
  initiatorRole: 'BUYER' | 'SELLER';
  amount: number;
  autoReleaseHours?: number;
}

export interface DeliverCovenantRequest {
  proofNote?: string;
}

export interface DisputeCovenantRequest {
  reason: string;
}
