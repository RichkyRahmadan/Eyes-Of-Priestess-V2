// ─── Vault (Treasury & Payments) Types ───────────────────
export interface Treasury {
  availableTreasury: number;
  sealedTreasury: number;
  totalTreasury: number;
  currency: string;
}

export type ChronicleType =
  | 'OFFERING' | 'WITHDRAWAL' | 'TITHING_IN' | 'TITHING_OUT'
  | 'SEAL_HOLD' | 'SEAL_RELEASE' | 'SEAL_REFUND' | 'TITHE';

export type ChronicleStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';

export interface Chronicle {
  id: string;
  type: ChronicleType;
  amount: number;
  tithe: number;
  netAmount: number;
  status: ChronicleStatus;
  description: string;
  referenceId?: string;
  counterpartyName?: string;
  treasuryAfter: number;
  createdAt: string;
}

export interface OfferingRequest {
  amount: number;
  method: 'VIRTUAL_ACCOUNT' | 'E_WALLET' | 'BANK_TRANSFER';
  bank?: string;
  eWalletType?: string;
}

export interface OfferingResponse {
  offeringId: string;
  amount: number;
  method: string;
  paymentUrl?: string;
  vaNumber?: string;
  expiresAt: string;
  status: string;
}

export interface WithdrawalRequest {
  amount: number;
  bankCode: string;
  bankName: string;
  accountNumber: string;
  accountName: string;
  pin: string;
}

export interface TithingRequest {
  recipientPhone: string;
  amount: number;
  note?: string;
  pin: string;
}
