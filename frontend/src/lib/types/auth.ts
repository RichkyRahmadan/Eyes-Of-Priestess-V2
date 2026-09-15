// src/lib/types/auth.ts
export interface User {
  id: string;
  email: string;
  username: string;
  fullName: string;
  phoneNumber?: string;
  role: 'USER' | 'ADMIN' | 'MODERATOR';
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
