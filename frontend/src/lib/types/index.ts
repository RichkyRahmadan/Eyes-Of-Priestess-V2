// Auth Types
export interface User {
  id: string;
  email: string;
  username: string;
  fullName: string;
  phoneNumber?: string;
  role: "USER" | "ADMIN" | "MODERATOR";
  isVerified: boolean;
  isPinSet: boolean;
  avatarUrl?: string;
  createdAt: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface LoginCredentials {
  email: string;
  password: string;
}

export interface RegisterData {
  email: string;
  password: string;
  fullName: string;
  phoneNumber: string;
  username: string;
}

// Wallet Types
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
  direction: "IN" | "OUT" | "NEUTRAL";
  amount: number;
  fee: number;
  status: TransactionStatus;
  description: string;
  counterpartyName?: string;
  counterpartyAvatar?: string;
  createdAt: string;
}

export type TransactionType = 
  | "TOPUP" | "WITHDRAW" | "P2P_TRANSFER" 
  | "ESCROW_HOLD" | "ESCROW_RELEASE" | "ESCROW_REFUND" | "FEE";

export type TransactionStatus = "PENDING" | "SUCCESS" | "FAILED";

export interface TopUpOrder {
  orderId: string;
  amount: number;
  method: string;
  bankCode?: string;
  virtualAccountNumber?: string;
  status: string;
  expiryTime: string;
}

export interface BankAccount {
  id: string;
  bankCode: string;
  bankName: string;
  accountNumber: string;
  accountHolderName: string;
  isPrimary: boolean;
}

// Room Types
export interface Room {
  roomId: string;
  roomCode: string;
  title: string;
  description: string;
  itemCategory: ItemCategory;
  itemPrice: number;
  fee: number;
  totalAmount: number;
  status: RoomStatus;
  buyer: RoomParticipant;
  seller: RoomParticipant;
  autoReleaseAt: string;
  chatRoomId: string;
  timeline: RoomTimelineEvent[];
  createdAt: string;
}

export type ItemCategory = 
  | "GAME_ACCOUNT" | "GAME_ITEM" | "DIGITAL_PRODUCT" 
  | "PHYSICAL_PRODUCT" | "SERVICE" | "OTHER";

export type RoomStatus = 
  | "WAITING_PAYMENT" | "FUNDED" | "DELIVERED" 
  | "COMPLETED" | "DISPUTED" | "CANCELLED" | "REFUNDED";

export interface RoomParticipant {
  id: string;
  name: string;
  avatar?: string;
}

export interface RoomTimelineEvent {
  status: string;
  timestamp: string;
  actor: string;
}

export interface DeliveryProof {
  id: string;
  proofType: "IMAGE" | "VIDEO" | "DOCUMENT" | "LINK";
  proofUrl: string;
  thumbnailUrl?: string;
  description?: string;
  uploadedAt: string;
}

// Chat Types
export interface ChatRoom {
  chatRoomId: string;
  roomName: string;
  roomCode: string;
  lastMessage?: string;
  lastMessageAt?: string;
  unreadCount: number;
  participantCount: number;
  escrowStatus: RoomStatus;
}

export interface ChatMessage {
  messageId: string;
  chatRoomId: string;
  senderId: string;
  senderName: string;
  senderAvatar?: string;
  content: string;
  messageType: "TEXT" | "IMAGE" | "FILE" | "SYSTEM" | "ESCROW_STATUS";
  fileUrl?: string;
  fileName?: string;
  fileSize?: number;
  sentAt: string;
  isEdited: boolean;
}

// Dispute Types
export interface Dispute {
  disputeId: string;
  roomId: string;
  roomCode: string;
  title: string;
  description: string;
  status: "OPEN" | "UNDER_REVIEW" | "RESOLVED" | "CLOSED";
  initiatedBy: string;
  initiatorRole: "BUYER" | "SELLER";
  buyer: { id: string; name: string };
  seller: { id: string; name: string };
  evidence: DisputeEvidence[];
  decision?: string;
  createdAt: string;
  updatedAt: string;
}

export interface DisputeEvidence {
  id: string;
  type: "IMAGE" | "VIDEO" | "DOCUMENT" | "CHAT_LOG";
  url: string;
  description?: string;
  uploadedBy: string;
  uploadedAt: string;
}

// Common Types
export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  details?: Array<{ field: string; message: string }>;
  traceId: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
