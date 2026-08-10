// ─── Pilgrim (Auth) Types ─────────────────────────────────
export interface Pilgrim {
  id: string;
  phone: string;
  email?: string;
  fullName: string;
  profilePhoto?: string;
  attunementStatus: 'UNATTUNED' | 'PENDING' | 'ATTUNED' | 'REJECTED';
  covenantScore: number;
  role: 'PILGRIM' | 'ORACLE';
  status: 'ACTIVE' | 'SUSPENDED' | 'SANCTIONED';
  attuned: boolean;
  createdAt: string;
}

export interface SealState {
  pilgrim: Pilgrim | null;
  accessSeal: string | null;
  refreshSeal: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}

export interface RiteCredentials {
  phone: string;
  password: string;
}

export interface ForgeIdentityRequest {
  phone: string;
  email?: string;
  fullName: string;
  password: string;
  pin: string;
}
