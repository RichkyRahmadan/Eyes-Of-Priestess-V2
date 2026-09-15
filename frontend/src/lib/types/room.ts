// src/lib/types/room.ts
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
  | 'GAME_ACCOUNT'
  | 'GAME_ITEM'
  | 'DIGITAL_PRODUCT'
  | 'PHYSICAL_PRODUCT'
  | 'SERVICE'
  | 'OTHER';

export type RoomStatus =
  | 'WAITING_PAYMENT'
  | 'FUNDED'
  | 'DELIVERED'
  | 'COMPLETED'
  | 'DISPUTED'
  | 'CANCELLED'
  | 'REFUNDED';

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
  proofType: 'IMAGE' | 'VIDEO' | 'DOCUMENT' | 'LINK';
  proofUrl: string;
  thumbnailUrl?: string;
  description?: string;
  uploadedAt: string;
}
