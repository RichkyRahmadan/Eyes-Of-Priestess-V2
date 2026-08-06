# EyesOfPriestess — Frontend Specification
## The Sanctum Interface: SvelteKit Website

**Version:** 1.0  
**Framework:** SvelteKit  
**Language:** TypeScript  
**Styling:** TailwindCSS + shadcn-svelte  
**State:** Svelte Stores  
**Icons:** Lucide Svelte  
**HTTP:** Native Fetch with custom interceptors  

---

## 1. Tech Stack Detail

| Layer | Technology | Purpose |
|-------|------------|---------|
| Framework | SvelteKit | SSR + SPA, file-based routing |
| Language | TypeScript 5.x | Type safety |
| Styling | TailwindCSS 3.x | Utility-first CSS |
| Components | shadcn-svelte | Accessible, customizable UI primitives |
| Forms | Formsnap + Zod | Form handling + validation schema |
| Toast | svelte-sonner | Notifications (omens) |
| Charts | Chart.js + svelte-chartjs | Observatory analytics |
| Date | date-fns | Date formatting & manipulation |
| Currency | Intl.NumberFormat | IDR formatting |

---

## 2. Project Structure — The Sanctum

```
frontend/
├── src/
│   ├── app.html
│   ├── app.d.ts
│   ├── hooks.server.ts
│   ├── hooks.client.ts
│   │
│   ├── lib/
│   │   ├── components/
│   │   │   ├── ui/
│   │   │   ├── sanctum/
│   │   │   │   ├── Veil.svelte
│   │   │   │   ├── Sidebar.svelte
│   │   │   │   ├── SanctumFloor.svelte
│   │   │   │   ├── MobileNav.svelte
│   │   │   │   └── SealGuard.svelte
│   │   │   ├── vault/
│   │   │   │   ├── TreasuryCard.svelte
│   │   │   │   ├── ChronicleList.svelte
│   │   │   │   ├── ChronicleItem.svelte
│   │   │   │   ├── OfferingForm.svelte
│   │   │   │   ├── WithdrawalForm.svelte
│   │   │   │   ├── TithingForm.svelte
│   │   │   │   ├── BankSelector.svelte
│   │   │   │   └── VADisplay.svelte
│   │   │   ├── covenant/
│   │   │   │   ├── CovenantCard.svelte
│   │   │   │   ├── CovenantList.svelte
│   │   │   │   ├── CovenantTimeline.svelte
│   │   │   │   ├── CovenantStatusBadge.svelte
│   │   │   │   ├── ForgeCovenantForm.svelte
│   │   │   │   ├── CovenantActions.svelte
│   │   │   │   ├── OathProof.svelte
│   │   │   │   ├── JudgmentForm.svelte
│   │   │   │   └── CountdownTimer.svelte
│   │   │   ├── communion/
│   │   │   │   ├── CommunionBox.svelte
│   │   │   │   ├── CommunionMessage.svelte
│   │   │   │   ├── CommunionInput.svelte
│   │   │   │   ├── CommunionHeader.svelte
│   │   │   │   └── TypingIndicator.svelte
│   │   │   ├── seal/
│   │   │   │   ├── RiteForm.svelte
│   │   │   │   ├── ForgeIdentityForm.svelte
│   │   │   │   ├── OmenInput.svelte
│   │   │   │   ├── PinInput.svelte
│   │   │   │   ├── PinPad.svelte
│   │   │   │   └── PasswordInput.svelte
│   │   │   └── shared/
│   │   │       ├── LoadingCrystal.svelte
│   │   │       ├── EmptyVoid.svelte
│   │   │       ├── ErrorBoundary.svelte
│   │   │       ├── CurrencyDisplay.svelte
│   │   │       ├── CovenantScore.svelte
│   │   │       └── PriestessSeal.svelte
│   │   ├── stores/
│   │   │   ├── seal.ts
│   │   │   ├── vault.ts
│   │   │   ├── covenant.ts
│   │   │   ├── communion.ts
│   │   │   ├── ui.ts
│   │   │   └── omen.ts
│   │   ├── api/
│   │   │   ├── client.ts
│   │   │   ├── seal.ts
│   │   │   ├── vault.ts
│   │   │   ├── covenant.ts
│   │   │   ├── communion.ts
│   │   │   └── judgment.ts
│   │   ├── types/
│   │   │   ├── pilgrim.ts
│   │   │   ├── vault.ts
│   │   │   ├── covenant.ts
│   │   │   ├── communion.ts
│   │   │   ├── judgment.ts
│   │   │   └── api.ts
│   │   └── utils/
│   │       ├── formatters.ts
│   │       ├── validators.ts
│   │       ├── constants.ts
│   │       └── helpers.ts
│   ├── routes/
│   │   ├── +layout.svelte
│   │   ├── +layout.ts
│   │   ├── +page.svelte
│   │   ├── rite/
│   │   │   └── +page.svelte
│   │   ├── rite/forge/
│   │   │   └── +page.svelte
│   │   ├── rite/attune/
│   │   │   └── +page.svelte
│   │   ├── sanctum/
│   │   │   ├── +layout.svelte
│   │   │   ├── +layout.ts
│   │   │   ├── observatory/
│   │   │   │   └── +page.svelte
│   │   │   ├── vault/
│   │   │   │   ├── +page.svelte
│   │   │   │   ├── offering/
│   │   │   │   ├── withdrawal/
│   │   │   │   └── chronicles/
│   │   │   ├── tithing/
│   │   │   │   └── +page.svelte
│   │   │   ├── covenant/
│   │   │   │   ├── +page.svelte
│   │   │   │   ├── forge/
│   │   │   │   └── [id]/
│   │   │   ├── judgment/
│   │   │   │   └── +page.svelte
│   │   │   └── profile/
│   │   │       └── +page.svelte
│   │   └── api/
│   │       └── proxy/
│   │           └── [...path]/
│   │               └── +server.ts
│   ├── app.postcss
│   └── service-worker.ts
├── static/
│   ├── favicon.png
│   ├── priestess-seal.svg
│   ├── manifest.json
│   └── images/
├── package.json
├── svelte.config.js
├── vite.config.ts
├── tailwind.config.js
├── tsconfig.json
├── postcss.config.js
└── Dockerfile
```

---

## 3. Type Definitions

### 3.1 Pilgrim Types
```typescript
export interface Pilgrim {
  id: string;
  phone: string;
  email?: string;
  fullName: string;
  profilePhoto?: string;
  attunementStatus: 'UNATTUNED' | 'PENDING' | 'ATTUNED' | 'REJECTED';
  covenantScore: number;
  status: 'ATTUNED' | 'SUSPENDED' | 'SANCTIONED';
  attunedAt: string;
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

export interface ForgeIdentityData {
  phone: string;
  email?: string;
  fullName: string;
  password: string;
  pin: string;
}
```

### 3.2 Vault Types
```typescript
export interface Treasury {
  availableTreasury: number;
  sealedTreasury: number;
  totalTreasury: number;
  currency: string;
}

export interface Chronicle {
  id: string;
  type: ChronicleType;
  amount: number;
  tithe: number;
  netAmount: number;
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';
  description: string;
  referenceId?: string;
  counterpartyName?: string;
  treasuryAfter: number;
  createdAt: string;
}

export type ChronicleType = 
  | 'OFFERING' | 'WITHDRAWAL' | 'TITHING_IN' | 'TITHING_OUT' 
  | 'SEAL_HOLD' | 'SEAL_RELEASE' | 'SEAL_REFUND' | 'TITHE';

export interface OfferingRequest {
  amount: number;
  method: 'VIRTUAL_ACCOUNT' | 'E_WALLET' | 'BANK_TRANSFER';
  bank?: string;
  eWalletType?: string;
}

export interface WithdrawalRequest {
  amount: number;
  bankAccount: BankAccount;
  pin: string;
}

export interface BankAccount {
  bankCode: string;
  bankName: string;
  accountNumber: string;
  accountName: string;
}

export interface TithingRequest {
  recipientPhone: string;
  amount: number;
  note?: string;
  pin: string;
}
```

### 3.3 Covenant Types
```typescript
export type CovenantStatus = 
  | 'FORGED' | 'ACCEPTED' | 'DELIVERED' | 'FULFILLED' 
  | 'JUDGMENT' | 'BROKEN' | 'EXPIRED';

export type CovenantCategory = 'GAME' | 'MARKETPLACE' | 'SERVICE';

export interface CovenantParty {
  id: string;
  fullName: string;
  phone: string;
  covenantScore: number;
  profilePhoto?: string;
}

export interface CovenantTimelineEvent {
  status: CovenantStatus;
  timestamp: string;
  actor: string;
  note: string;
  proofUrl?: string;
}

export interface Covenant {
  covenantId: string;
  covenantCode: string;
  initiator: CovenantParty;
  counterpart: CovenantParty;
  itemName: string;
  itemDescription?: string;
  category: CovenantCategory;
  amount: number;
  status: CovenantStatus;
  timeline: CovenantTimelineEvent[];
  deadlineAt: string;
  canFulfill: boolean;
  canBreak: boolean;
  canSever: boolean;
  escrow: { sealId: string; amount: number; status: string };
  createdAt: string;
  updatedAt: string;
}

export interface CovenantListItem {
  covenantId: string;
  covenantCode: string;
  role: 'INITIATOR' | 'COUNTERPART';
  itemName: string;
  amount: number;
  status: CovenantStatus;
  otherParty: CovenantParty;
  deadlineAt: string;
  updatedAt: string;
}

export interface ForgeCovenantRequest {
  counterpartPhone: string;
  itemName: string;
  itemDescription?: string;
  category: CovenantCategory;
  amount: number;
  deadlineHours: number;
  pin: string;
}
```

### 3.4 Communion Types
```typescript
export interface CommunionMessage {
  id: string;
  senderId: string;
  senderName: string;
  senderAvatar?: string;
  message: string;
  messageType: 'TEXT' | 'IMAGE' | 'FILE' | 'SYSTEM';
  fileUrl?: string;
  fileName?: string;
  createdAt: string;
  isRead: boolean;
  isMine: boolean;
}

export interface WSMessage {
  type: 'NEW_MESSAGE' | 'SYSTEM_NOTIFICATION' | 'TYPING' | 'READ_RECEIPT';
  payload: unknown;
}
```

### 3.5 Judgment Types
```typescript
export type JudgmentReason = 'ITEM_NOT_AS_DESCRIBED' | 'NOT_DELIVERED' | 'OTHER';
export type JudgmentStatus = 'OPEN' | 'UNDER_REVIEW' | 'RESOLVED' | 'REJECTED';
export type JudgmentResolution = 'RELEASE_TO_COUNTERPART' | 'REFUND_TO_INITIATOR' | 'SPLIT';

export interface Judgment {
  judgmentId: string;
  covenantId: string;
  covenantCode: string;
  itemName: string;
  amount: number;
  reason: JudgmentReason;
  description: string;
  evidenceUrls: string[];
  status: JudgmentStatus;
  raisedByMe: boolean;
  otherParty: { id: string; fullName: string };
  resolution?: JudgmentResolution;
  oracleNotes?: string;
  createdAt: string;
  updatedAt: string;
}
```

### 3.6 API Response Types
```typescript
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: ApiError;
}

export interface ApiError {
  code: string;
  message: string;
  details?: Record<string, string>;
  timestamp: string;
  path: string;
  requestId: string;
}
```

---

## 4. Store Architecture

### 4.1 Seal Store
```typescript
import { writable, derived } from 'svelte/store';
import type { Pilgrim, SealState } from '$lib/types/pilgrim';

const createSealStore = () => {
  const { subscribe, set, update } = writable<SealState>({
    pilgrim: null, accessSeal: null, refreshSeal: null,
    isAuthenticated: false, isLoading: true
  });

  return {
    subscribe,
    attune: (pilgrim: Pilgrim, accessSeal: string, refreshSeal: string) => {
      localStorage.setItem('accessSeal', accessSeal);
      localStorage.setItem('refreshSeal', refreshSeal);
      set({ pilgrim, accessSeal, refreshSeal, isAuthenticated: true, isLoading: false });
    },
    sever: () => {
      localStorage.removeItem('accessSeal');
      localStorage.removeItem('refreshSeal');
      set({ pilgrim: null, accessSeal: null, refreshSeal: null, isAuthenticated: false, isLoading: false });
    },
    setPilgrim: (pilgrim: Pilgrim) => update(s => ({ ...s, pilgrim })),
    setLoading: (isLoading: boolean) => update(s => ({ ...s, isLoading })),
    init: () => {
      const seal = localStorage.getItem('accessSeal');
      if (seal) { /* validate */ }
      update(s => ({ ...s, isLoading: false }));
    }
  };
};

export const sealStore = createSealStore();
export const isAuthenticated = derived(sealStore, $seal => $seal.isAuthenticated);
export const currentPilgrim = derived(sealStore, $seal => $seal.pilgrim);
```

### 4.2 Vault Store
```typescript
import { writable, derived } from 'svelte/store';
import type { Treasury, Chronicle } from '$lib/types/vault';

const createVaultStore = () => {
  const { subscribe, set, update } = writable<{
    treasury: Treasury | null;
    chronicles: Chronicle[];
    isLoading: boolean;
  }>({ treasury: null, chronicles: [], isLoading: false });

  return {
    subscribe,
    setTreasury: (treasury: Treasury) => update(s => ({ ...s, treasury })),
    setChronicles: (chronicles: Chronicle[]) => update(s => ({ ...s, chronicles })),
    addChronicle: (chronicle: Chronicle) => update(s => ({ ...s, chronicles: [chronicle, ...s.chronicles] })),
    setLoading: (isLoading: boolean) => update(s => ({ ...s, isLoading })),
    updateTreasury: (delta: number) => update(s => s.treasury ? {
      ...s, treasury: { ...s.treasury, availableTreasury: s.treasury.availableTreasury + delta }
    } : s)
  };
};

export const vaultStore = createVaultStore();
```

### 4.3 Covenant Store
```typescript
import { writable } from 'svelte/store';
import type { Covenant, CovenantListItem } from '$lib/types/covenant';

const createCovenantStore = () => {
  const { subscribe, set, update } = writable<{
    covenants: CovenantListItem[];
    currentCovenant: Covenant | null;
    isLoading: boolean;
    activeTab: 'INITIATOR' | 'COUNTERPART' | 'ALL';
  }>({ covenants: [], currentCovenant: null, isLoading: false, activeTab: 'ALL' });

  return {
    subscribe,
    setCovenants: (covenants: CovenantListItem[]) => update(s => ({ ...s, covenants })),
    setCurrentCovenant: (covenant: Covenant | null) => update(s => ({ ...s, currentCovenant: covenant })),
    updateCovenantStatus: (covenantId: string, status: string) => update(s => ({
      ...s,
      covenants: s.covenants.map(c => c.covenantId === covenantId ? { ...c, status } : c),
      currentCovenant: s.currentCovenant?.covenantId === covenantId ? { ...s.currentCovenant, status } : s.currentCovenant
    })),
    setActiveTab: (tab: 'INITIATOR' | 'COUNTERPART' | 'ALL') => update(s => ({ ...s, activeTab: tab })),
    setLoading: (isLoading: boolean) => update(s => ({ ...s, isLoading }))
  };
};

export const covenantStore = createCovenantStore();
```

---

## 5. API Client — The Seal Bearer

```typescript
const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

class SealBearer {
  private async invoke<T>(endpoint: string, options: RequestInit = {}): Promise<ApiResponse<T>> {
    const seal = localStorage.getItem('accessSeal');
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(seal && { 'Authorization': `SacredSeal ${seal}` }),
      ...((options.body && !(options.body instanceof FormData)) && { 'Content-Type': 'application/json' }),
      ...(options.headers as Record<string, string>)
    };

    const response = await fetch(`${API_BASE}${endpoint}`, { ...options, headers });
    if (response.status === 401) {
      const renewed = await this.renewSeal();
      if (renewed) return this.invoke(endpoint, options);
      sealStore.sever();
      throw new Error('Your seal has faded. Please perform the Rite of Return.');
    }
    const data = await response.json();
    if (!data.success) throw new ApiError(data.error);
    return data;
  }

  async renewSeal(): Promise<boolean> {
    const refreshSeal = localStorage.getItem('refreshSeal');
    if (!refreshSeal) return false;
    try {
      const res = await fetch(`${API_BASE}/seal/renew`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshSeal })
      });
      const data = await res.json();
      if (data.success) { localStorage.setItem('accessSeal', data.data.accessSeal); return true; }
    } catch { return false; }
    return false;
  }

  get<T>(endpoint: string) { return this.invoke<T>(endpoint, { method: 'GET' }); }
  post<T>(endpoint: string, body: unknown, covenantKey?: string) {
    const headers: Record<string, string> = {};
    if (covenantKey) headers['Covenant-Key'] = covenantKey;
    return this.invoke<T>(endpoint, { method: 'POST', body: JSON.stringify(body), headers });
  }
  put<T>(endpoint: string, body: unknown) { return this.invoke<T>(endpoint, { method: 'PUT', body: JSON.stringify(body) }); }
  delete<T>(endpoint: string) { return this.invoke<T>(endpoint, { method: 'DELETE' }); }
}

export const bearer = new SealBearer();
```

---

## 6. Page Specifications

### 6.1 Sanctum Entrance (`/`)
- Hero: "The Eyes of the Priestess see all transactions"
- Feature highlights: The Covenant, The Seal, The Judgment
- How it works: 4 steps (Forge → Accept → Fulfill → Confirm)
- CTA: "Attune Your Seal"

### 6.2 Observatory (`/sanctum/observatory`)
- Treasury card, quick actions, stats, recent covenants/chronicles, chart

### 6.3 Vault (`/sanctum/vault`)
- Treasury display, actions (Offering, Withdrawal, Tithing), chronicle history

### 6.4 Covenant Hall (`/sanctum/covenant`)
- Tabs, filter, covenant cards, "Forge New Covenant" CTA

### 6.5 Covenant Chamber (`/sanctum/covenant/[id]`)
- Header, parties, timeline, communion panel, action panel, countdown

### 6.6 Forge Covenant (`/sanctum/covenant/forge`)
- Multi-step: find counterpart → item details → deadline → PIN → success

### 6.7 Profile (`/sanctum/profile`)
- Pilgrim card, stats, settings (transmute PIN, omens, theme), bank accounts, sever seal

---

## 7. Design System — The Priestess's Aesthetic

### 7.1 Color Palette
```css
:root {
  --primary-50: #f5f3ff; --primary-100: #ede9fe; --primary-200: #ddd6fe;
  --primary-300: #c4b5fd; --primary-400: #a78bfa; --primary-500: #8b5cf6;
  --primary-600: #7c3aed; --primary-700: #6d28d9; --primary-800: #5b21b6;
  --primary-900: #4c1d95;
  --gold-400: #fbbf24; --gold-500: #f59e0b; --gold-600: #d97706;
  --void-900: #0f0a1a; --void-800: #1a1025; --void-700: #2d1b4e;
  --canvas: #faf8ff; --canvas-soft: #f0ecfa; --canvas-softer: #f5f3ff;
  --surface-pressed: #e9e5f5;
  --ink: #1a1025; --body: #5e5e5e; --mute: #9ca3af;
  --on-dark: #ffffff; --hairline-mid: #6b7280;
  --success: #10b981; --warning: #f59e0b; --danger: #ef4444;
}
```

### 7.2 Status Colors
| Status | Color |
|--------|-------|
| FORGED | amber |
| ACCEPTED | primary-600 |
| DELIVERED | purple |
| FULFILLED | success |
| JUDGMENT | danger |
| BROKEN | gray |
| EXPIRED | gray |

### 7.3 Typography
- Font: Inter (Google Fonts)
- Display: weight 700, tight line-height
- Scale: Display 3rem, H1 2.25rem, H2 1.5rem, H3 1.25rem, Body 1rem, Small 0.875rem, Caption 0.75rem

### 7.4 Spacing
- Base: 4px, Scale: 4,8,12,16,20,24,32,40,48,64,80,96
- Radius: 4px(sm), 8px(md), 12px(lg), 16px(xl), 9999px(full)

---

## 8. Responsive Breakpoints

| Breakpoint | Width | Layout |
|------------|-------|--------|
| Mobile | < 640px | Single column, bottom nav |
| Tablet | 640-1024px | Two columns, collapsible sidebar |
| Desktop | > 1024px | Three columns, fixed sidebar |

---

## 9. Animations
- Page transitions: fade 200ms
- Modal: scale + fade 150ms
- Toast (Omen): slide from right 300ms
- Loading: crystal pulse
- Treasury: count-up
- Status change: color transition 300ms
- Communion: slide from bottom 150ms
- Countdown: pulse last 10 min

---

*Next: Read `05-project-setup.md` for attunement instructions.*
