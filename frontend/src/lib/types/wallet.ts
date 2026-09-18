// src/lib/types/wallet.ts
export interface Wallet {
  walletId: string;
  availableBalance: number;
  escrowBalance: number;
  totalBalance: number;
  currency: string;
  lastUpdated: string;
}

export interface Transaction {
  transactionId: string;
  type: TransactionType;
  direction: 'IN' | 'OUT' | 'NEUTRAL';
  amount: number;
  fee: number;
  status: TransactionStatus;
  description: string;
  counterpartyName?: string;
  counterpartyAvatar?: string;
  createdAt: string;
}

export type TransactionType =
  | 'TOPUP'
  | 'WITHDRAW'
  | 'P2P_TRANSFER'
  | 'ESCROW_HOLD'
  | 'ESCROW_RELEASE'
  | 'ESCROW_REFUND'
  | 'FEE';

export type TransactionStatus = 'PENDING' | 'SUCCESS' | 'FAILED';

export interface TopUpOrder {
  id?: string;
  orderId: string;
  amount: number;
  method: string;
  bankCode?: string;
  virtualAccountNumber?: string;
  status: string;
  expiryTime: string;
  invoiceUrl?: string;
}

export interface BankAccount {
  id: string;
  bankCode: string;
  bankName: string;
  accountNumber: string;
  accountHolderName: string;
  isPrimary: boolean;
}
