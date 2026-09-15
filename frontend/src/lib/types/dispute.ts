// src/lib/types/dispute.ts
export interface Dispute {
  disputeId: string;
  roomId: string;
  roomCode: string;
  title: string;
  description: string;
  status: 'OPEN' | 'UNDER_REVIEW' | 'RESOLVED' | 'CLOSED';
  initiatedBy: string;
  initiatorRole: 'BUYER' | 'SELLER';
  buyer: { id: string; name: string };
  seller: { id: string; name: string };
  evidence: DisputeEvidence[];
  decision?: string;
  createdAt: string;
  updatedAt: string;
}

export interface DisputeEvidence {
  id: string;
  type: 'IMAGE' | 'VIDEO' | 'DOCUMENT' | 'CHAT_LOG';
  url: string;
  description?: string;
  uploadedBy: string;
  uploadedAt: string;
}
