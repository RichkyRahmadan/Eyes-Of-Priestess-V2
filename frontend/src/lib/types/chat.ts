// src/lib/types/chat.ts
import type { RoomStatus } from './room.js';

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
  messageType: 'TEXT' | 'IMAGE' | 'FILE' | 'SYSTEM' | 'ESCROW_STATUS';
  fileUrl?: string;
  fileName?: string;
  fileSize?: number;
  sentAt: string;
  isEdited: boolean;
}
