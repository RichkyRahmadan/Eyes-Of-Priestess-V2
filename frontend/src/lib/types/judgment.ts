// ─── Judgment (Dispute) Types ─────────────────────────────
export type CaseStatus = 'OPEN' | 'DELIBERATING' | 'RENDERED';
export type Resolution  = 'RELEASE' | 'REFUND' | 'SPLIT';

export interface JudgmentCase {
  id: string;
  covenantId: string;
  initiatorId: string;
  counterpartyId: string;
  reason: string;
  evidence: string[];
  status: CaseStatus;
  resolution?: Resolution;
  assignedOracleId?: string;
  oracleNotes?: string;
  openedAt: string;
  renderedAt?: string;
}
