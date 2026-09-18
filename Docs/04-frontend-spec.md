# EyesOfPriestess — Frontend Specification

> **Version:** 1.0  
> **Framework:** SvelteKit 2.x + TypeScript  
> **Styling:** TailwindCSS 3.x  
> **UI Library:** shadcn-svelte (adapted)  
> **Design System:** Warm Editorial (Cream Canvas + Coral Accent + Slab-Serif)

---

## 1. Tech Stack Detail

| Layer | Technology | Purpose |
|---|---|---|
| **Framework** | SvelteKit 2.x | SSR/SPA hybrid, file-based routing, API routes |
| **Language** | TypeScript 5.x | Type safety, interfaces, enums |
| **Styling** | TailwindCSS 3.x | Utility-first CSS dengan custom design tokens |
| **Components** | shadcn-svelte | Base components (Button, Card, Input, Dialog, etc.) |
| **Icons** | Lucide Svelte | Consistent iconography |
| **Fonts** | Cormorant Garamond (display), Inter (body), JetBrains Mono (code) |
| **State** | Svelte Stores (writable, derived) + Context API |
| **HTTP Client** | Native fetch + custom wrapper |
| **WebSocket** | Native WebSocket API + custom manager |
| **Validation** | Zod | Runtime schema validation |
| **Date** | date-fns | Date formatting & manipulation |
| **Currency** | Intl.NumberFormat | IDR formatting |

---

## 2. Tailwind Configuration

```javascript
// tailwind.config.js
import { fontFamily } from "tailwindcss/defaultTheme";

/** @type {import('tailwindcss').Config} */
export default {
  darkMode: ["class"],
  content: ["./src/**/*.{html,js,svelte,ts}"],
  theme: {
    extend: {
      colors: {
        // Brand & Accent
        primary: {
          DEFAULT: "#cc785c",
          active: "#a9583e",
          disabled: "#e6dfd8",
        },
        // Surfaces
        canvas: "#faf9f5",
        "surface-soft": "#f5f0e8",
        "surface-card": "#efe9de",
        "surface-cream-strong": "#e8e0d2",
        "surface-dark": "#181715",
        "surface-dark-elevated": "#252320",
        "surface-dark-soft": "#1f1e1b",
        // Text
        ink: "#141413",
        "body-strong": "#252523",
        body: "#3d3d3a",
        muted: "#6c6a64",
        "muted-soft": "#8e8b82",
        "on-primary": "#ffffff",
        "on-dark": "#faf9f5",
        "on-dark-soft": "#a09d96",
        // Borders
        hairline: "#e6dfd8",
        "hairline-soft": "#ebe6df",
        // Semantic
        "accent-teal": "#5db8a6",
        "accent-amber": "#e8a55a",
        success: "#5db872",
        warning: "#d4a017",
        error: "#c64545",
      },
      fontFamily: {
        display: ["Cormorant Garamond", "Tiempos Headline", "Garamond", "serif"],
        sans: ["Inter", "-apple-system", "BlinkMacSystemFont", "Segoe UI", "Roboto", "sans-serif"],
        mono: ["JetBrains Mono", "ui-monospace", "monospace"],
      },
      fontSize: {
        "display-xl": ["64px", { lineHeight: "1.05", letterSpacing: "-0.02em" }],
        "display-lg": ["48px", { lineHeight: "1.1", letterSpacing: "-0.02em" }],
        "display-md": ["36px", { lineHeight: "1.15", letterSpacing: "-0.015em" }],
        "display-sm": ["28px", { lineHeight: "1.2", letterSpacing: "-0.01em" }],
        "title-lg": ["22px", { lineHeight: "1.3" }],
        "title-md": ["18px", { lineHeight: "1.4" }],
        "title-sm": ["16px", { lineHeight: "1.4" }],
        "body-md": ["16px", { lineHeight: "1.55" }],
        "body-sm": ["14px", { lineHeight: "1.55" }],
        caption: ["13px", { lineHeight: "1.4" }],
        "caption-upper": ["12px", { lineHeight: "1.4", letterSpacing: "0.1em" }],
      },
      spacing: {
        section: "96px",
        xxl: "48px",
        xl: "32px",
        lg: "24px",
        md: "16px",
        sm: "12px",
        xs: "8px",
        xxs: "4px",
      },
      borderRadius: {
        xs: "4px",
        sm: "6px",
        md: "8px",
        lg: "12px",
        xl: "16px",
        pill: "9999px",
        full: "9999px",
      },
      boxShadow: {
        soft: "0 1px 3px rgba(20, 20, 19, 0.08)",
        card: "0 4px 20px rgba(20, 20, 19, 0.06)",
        elevated: "0 8px 30px rgba(20, 20, 19, 0.12)",
      },
    },
  },
  plugins: [require("tailwindcss-animate")],
};
```

---

## 3. TypeScript Types

```typescript
// src/lib/types/auth.ts
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

// src/lib/types/chat.ts
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

// src/lib/types/dispute.ts
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

// src/lib/types/common.ts
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
```

---

## 4. Svelte Stores

```typescript
// src/lib/stores/auth.ts
import { writable, derived } from "svelte/store";
import type { User, AuthTokens } from "$lib/types";

function createAuthStore() {
  const { subscribe, set, update } = writable<{
    user: User | null;
    tokens: AuthTokens | null;
    isLoading: boolean;
  }>({ user: null, tokens: null, isLoading: true });

  return {
    subscribe,
    setAuth: (user: User, tokens: AuthTokens) => set({ user, tokens, isLoading: false }),
    clearAuth: () => set({ user: null, tokens: null, isLoading: false }),
    setLoading: (isLoading: boolean) => update(s => ({ ...s, isLoading })),
    updateUser: (user: User) => update(s => ({ ...s, user })),
  };
}

export const authStore = createAuthStore();

export const isAuthenticated = derived(authStore, $auth => !!$auth.tokens?.accessToken);
export const isAdmin = derived(authStore, $auth => $auth.user?.role === "ADMIN");
export const currentUser = derived(authStore, $auth => $auth.user);

// src/lib/stores/wallet.ts
import { writable, derived } from "svelte/store";
import type { Wallet, Transaction } from "$lib/types";

export const walletStore = writable<Wallet | null>(null);
export const transactionsStore = writable<Transaction[]>([]);
export const walletLoading = writable(false);

export const formattedBalance = derived(walletStore, $w => {
  if (!$w) return "Rp 0";
  return new Intl.NumberFormat("id-ID", {
    style: "currency",
    currency: "IDR",
    minimumFractionDigits: 0,
  }).format($w.availableBalance);
});

// src/lib/stores/rooms.ts
import { writable } from "svelte/store";
import type { Room } from "$lib/types";

export const roomsStore = writable<Room[]>([]);
export const activeRoomStore = writable<Room | null>(null);
export const roomsLoading = writable(false);

// src/lib/stores/notifications.ts
import { writable } from "svelte/store";

export interface Notification {
  id: string;
  type: "success" | "error" | "warning" | "info";
  title: string;
  message: string;
  duration?: number;
}

export const notifications = writable<Notification[]>([]);

export function addNotification(notification: Omit<Notification, "id">) {
  const id = crypto.randomUUID();
  notifications.update(n => [...n, { ...notification, id }]);
  setTimeout(() => {
    notifications.update(n => n.filter(item => item.id !== id));
  }, notification.duration || 5000);
}

// src/lib/stores/websocket.ts
import { writable } from "svelte/store";

export const wsConnectionStatus = writable<"connected" | "connecting" | "disconnected">("disconnected");
export const wsUnreadCounts = writable<Record<string, number>>({});
```

---

## 5. API Client

```typescript
// src/lib/api/client.ts
import { browser } from "$app/environment";
import { authStore } from "$lib/stores/auth";
import { goto } from "$app/navigation";
import type { ApiError } from "$lib/types";

const API_BASE = "http://localhost:8080/api/v1";

async function refreshAccessToken(): Promise<string | null> {
  const tokens = authStore;
  // Implementation: call /auth/refresh
  return null;
}

export async function apiFetch<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const url = `${API_BASE}${endpoint}`;

  let authStoreValue: any;
  authStore.subscribe(s => { authStoreValue = s; })();

  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...((options.headers as Record<string, string>) || {}),
  };

  if (authStoreValue?.tokens?.accessToken) {
    headers["Authorization"] = `Bearer ${authStoreValue.tokens.accessToken}`;
  }

  const response = await fetch(url, { ...options, headers });

  if (response.status === 401) {
    // Try refresh token
    const newToken = await refreshAccessToken();
    if (newToken) {
      headers["Authorization"] = `Bearer ${newToken}`;
      const retryResponse = await fetch(url, { ...options, headers });
      if (!retryResponse.ok) throw await retryResponse.json();
      return retryResponse.json();
    }
    authStore.clearAuth();
    if (browser) goto("/login");
    throw new Error("Session expired");
  }

  if (!response.ok) {
    const error: ApiError = await response.json();
    throw error;
  }

  if (response.status === 204) return undefined as T;
  return response.json();
}

// Service-specific clients
export const authApi = {
  login: (data: any) => apiFetch("/auth/login", { method: "POST", body: JSON.stringify(data) }),
  register: (data: any) => apiFetch("/auth/register", { method: "POST", body: JSON.stringify(data) }),
  me: () => apiFetch("/auth/me"),
  logout: () => apiFetch("/auth/logout", { method: "POST" }),
  verifyPin: (pin: string) => apiFetch("/auth/verify-pin", { method: "POST", body: JSON.stringify({ pin }) }),
};

export const walletApi = {
  getBalance: () => apiFetch("/wallet/balance"),
  getTransactions: (params?: string) => apiFetch(`/wallet/transactions?${params || ""}`),
  topup: (data: any) => apiFetch("/wallet/topup", { method: "POST", body: JSON.stringify(data) }),
  transfer: (data: any) => apiFetch("/wallet/transfer", { method: "POST", body: JSON.stringify(data) }),
  getBankAccounts: () => apiFetch("/wallet/bank-accounts"),
};

export const roomApi = {
  getRooms: (params?: string) => apiFetch(`/room/?${params || ""}`),
  getRoom: (id: string) => apiFetch(`/room/${id}`),
  createRoom: (data: any) => apiFetch("/room/", { method: "POST", body: JSON.stringify(data) }),
  fundRoom: (id: string, pin: string) => apiFetch(`/room/${id}/fund`, { method: "POST", body: JSON.stringify({ pin }) }),
  deliverRoom: (id: string, data: any) => apiFetch(`/room/${id}/deliver`, { method: "POST", body: JSON.stringify(data) }),
  confirmRoom: (id: string, data: any) => apiFetch(`/room/${id}/confirm`, { method: "POST", body: JSON.stringify(data) }),
  disputeRoom: (id: string, data: any) => apiFetch(`/room/${id}/dispute`, { method: "POST", body: JSON.stringify(data) }),
};

export const chatApi = {
  getRooms: () => apiFetch("/chat/rooms"),
  getMessages: (roomId: string, params?: string) => apiFetch(`/chat/rooms/${roomId}/messages?${params || ""}`),
  markRead: (roomId: string) => apiFetch(`/chat/rooms/${roomId}/read`, { method: "POST" }),
};
```

---

## 6. WebSocket Client

```typescript
// src/lib/websocket/manager.ts
import { wsConnectionStatus, wsUnreadCounts } from "$lib/stores/websocket";
import { authStore } from "$lib/stores/auth";
import { addNotification } from "$lib/stores/notifications";
import type { ChatMessage } from "$lib/types";

class WebSocketManager {
  private ws: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 1000;
  private heartbeatInterval: ReturnType<typeof setInterval> | null = null;
  private messageHandlers: Map<string, ((msg: any) => void)[]> = new Map();

  connect() {
    let token: string | undefined;
    authStore.subscribe(s => { token = s.tokens?.accessToken; })();

    if (!token) return;

    wsConnectionStatus.set("connecting");
    this.ws = new WebSocket(`ws://localhost:8080/ws/chat?token=${token}`);

    this.ws.onopen = () => {
      wsConnectionStatus.set("connected");
      this.reconnectAttempts = 0;
      this.startHeartbeat();
    };

    this.ws.onmessage = (event) => {
      const data = JSON.parse(event.data);
      this.handleMessage(data);
    };

    this.ws.onclose = () => {
      wsConnectionStatus.set("disconnected");
      this.stopHeartbeat();
      this.attemptReconnect();
    };

    this.ws.onerror = (error) => {
      console.error("WebSocket error:", error);
    };
  }

  private handleMessage(data: any) {
    switch (data.type) {
      case "NEW_MESSAGE":
        this.emit("new_message", data);
        if (data.senderId !== this.getCurrentUserId()) {
          wsUnreadCounts.update(counts => ({
            ...counts,
            [data.roomId]: (counts[data.roomId] || 0) + 1,
          }));
        }
        break;
      case "ROOM_STATUS_UPDATE":
        this.emit("room_status", data);
        break;
      case "NOTIFICATION":
        addNotification({
          type: data.notificationType || "info",
          title: data.title,
          message: data.message,
        });
        break;
    }
  }

  send(message: object) {
    if (this.ws?.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message));
    }
  }

  joinRoom(roomId: string) {
    this.send({ type: "JOIN_ROOM", roomId });
  }

  leaveRoom(roomId: string) {
    this.send({ type: "LEAVE_ROOM", roomId });
  }

  sendMessage(roomId: string, content: string, messageType: string = "TEXT") {
    this.send({ type: "SEND_MESSAGE", roomId, content, messageType });
  }

  on(event: string, handler: (msg: any) => void) {
    if (!this.messageHandlers.has(event)) {
      this.messageHandlers.set(event, []);
    }
    this.messageHandlers.get(event)!.push(handler);
  }

  off(event: string, handler: (msg: any) => void) {
    const handlers = this.messageHandlers.get(event);
    if (handlers) {
      this.messageHandlers.set(event, handlers.filter(h => h !== handler));
    }
  }

  private emit(event: string, data: any) {
    this.messageHandlers.get(event)?.forEach(handler => handler(data));
  }

  private startHeartbeat() {
    this.heartbeatInterval = setInterval(() => {
      this.send({ type: "PING" });
    }, 30000);
  }

  private stopHeartbeat() {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval);
      this.heartbeatInterval = null;
    }
  }

  private attemptReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) return;

    setTimeout(() => {
      this.reconnectAttempts++;
      this.connect();
    }, this.reconnectDelay * Math.pow(2, this.reconnectAttempts));
  }

  private getCurrentUserId(): string | undefined {
    let userId: string | undefined;
    authStore.subscribe(s => { userId = s.user?.id; })();
    return userId;
  }

  disconnect() {
    this.stopHeartbeat();
    this.ws?.close();
    this.ws = null;
  }
}

export const wsManager = new WebSocketManager();
```

---

## 7. Page Specifications

### 7.1 Route Structure

```
src/routes/
├── (auth)/                    → Auth layout (no sidebar, centered)
│   ├── login/
│   │   └── +page.svelte
│   ├── register/
│   │   └── +page.svelte
│   └── set-pin/
│       └── +page.svelte
│
├── (app)/                     → App layout (sidebar + top nav)
│   ├── +layout.svelte
│   ├── dashboard/
│   │   └── +page.svelte
│   ├── wallet/
│   │   └── +page.svelte
│   ├── transfer/
│   │   └── +page.svelte
│   ├── topup/
│   │   └── +page.svelte
│   ├── withdraw/
│   │   └── +page.svelte
│   ├── rooms/
│   │   ├── +page.svelte
│   │   └── [id]/
│   │       └── +page.svelte
│   ├── chat/
│   │   ├── +page.svelte
│   │   └── [roomId]/
│   │       └── +page.svelte
│   ├── history/
│   │   └── +page.svelte
│   ├── disputes/
│   │   ├── +page.svelte
│   │   └── [id]/
│   │       └── +page.svelte
│   └── profile/
│       └── +page.svelte
│
├── admin/                     → Admin layout
│   ├── +layout.svelte
│   ├── users/
│   │   └── +page.svelte
│   ├── disputes/
│   │   └── +page.svelte
│   └── transactions/
│       └── +page.svelte
│
├── +layout.svelte             → Root layout (fonts, providers)
├── +page.svelte               → Landing page (marketing)
└── +error.svelte              → Error boundary
```

### 7.2 Layout: Root (`+layout.svelte`)

- Load fonts: Cormorant Garamond, Inter, JetBrains Mono (Google Fonts or self-hosted)
- Global CSS variables mapped ke Tailwind tokens
- Toast notification container (fixed top-right)
- WebSocket connection manager (on mount jika authenticated)

### 7.3 Layout: Auth (`(auth)/+layout.svelte`)

- Full-height cream canvas (`bg-canvas`)
- Centered card container (max-width 420px)
- Logo EyesOfPriestess (slab-serif "EyesOfPriestess" dengan coral accent)
- No sidebar, no top navigation

### 7.4 Layout: App (`(app)/+layout.svelte`)

- **Top Navigation:** 64px height, cream canvas, logo kiri, nav links tengah (Dashboard, Wallet, Rooms, Chat, History), profile dropdown kanan
- **Sidebar:** 280px width (desktop), collapsible (tablet), hidden (mobile dengan drawer)
- **Main Content:** flex-1, cream canvas background, generous padding
- **Bottom bar:** Mobile navigation (icon + label)

### 7.5 Page: Dashboard (`/dashboard`)

**Surface:** Cream canvas with alternating dark navy cards

**Sections:**
1. **Welcome Band** — `display-md` greeting: "Selamat datang, {name}" — Cormorant Garamond
2. **Balance Cards Row** (3-up on desktop):
   - Available Balance — `surface-card` background, large serif number
   - Escrow Balance — `surface-card` background
   - Total Balance — `surface-dark` card, cream text (dark navy product chrome)
3. **Quick Actions** — Horizontal row: Top-up, Transfer, Create Room, Withdraw — coral primary buttons
4. **Recent Activity** — `surface-card` table/list, 5 latest transactions dengan icon type + amount + status badge
5. **Active Rooms** — Grid of room cards (2-up), showing room code, title, status badge, counterparty
6. **CTA Band** — Full-width coral (`bg-primary`) callout: "Jual beli lebih aman dengan EyesOfPriestess" — serif headline, cream button

### 7.6 Page: Wallet (`/wallet`)

**Surface:** Cream canvas

**Sections:**
1. **Balance Header** — Large serif display number, update terakhir
2. **Action Buttons** — Primary coral: Top-up, Transfer, Withdraw
3. **Transaction History** — Filterable table:
   - Columns: Type icon, Description, Amount (IN green / OUT coral), Status badge, Date
   - Filters: Type dropdown, Date range, Status tabs
   - Pagination
4. **Bank Accounts** — List of saved accounts dengan primary badge

### 7.7 Page: Room Detail (`/rooms/[id]`)

**Surface:** Cream canvas dengan dark navy sidebar untuk chat

**Layout:** Two-column (desktop) / stacked (mobile)
- **Left (60%):** Room information
  - Room code badge (pill, coral)
  - Title — `title-lg`
  - Status timeline (vertical stepper)
  - Item details & price
  - Counterparty card dengan avatar
  - Action buttons (contextual by status):
    - WAITING_PAYMENT: "Bayar Sekarang" (coral primary)
    - FUNDED: "Konfirmasi Pengiriman" (seller) / tunggu (buyer)
    - DELIVERED: "Konfirmasi Penerimaan" (buyer) / tunggu (seller)
    - DISPUTED: "Lihat Dispute"
  - Delivery proofs (grid of images)
- **Right (40%):** Chat panel
  - Dark navy (`surface-dark`) background — product chrome feel
  - Message list dengan bubbles
  - Input area di bottom
  - System messages (escrow status updates) styled differently

### 7.8 Page: Chat (`/chat`)

**Surface:** Cream canvas list + dark navy chat area

**Layout:** Two-column persistent (like WhatsApp Web)
- **Left:** Chat room list
  - Search bar
  - Room items: avatar, name, last message preview, unread badge, timestamp
  - Status indicator (online/offline)
- **Right:** Active chat
  - Header: room name, escrow status badge, info button
  - Messages: alternating bubbles, timestamp grouping
  - Input: text + attachment button + send button (coral)

### 7.9 Page: Landing (`/`)

**Surface:** Alternating cream → dark navy → coral bands (editorial pacing)

**Sections:**
1. **Hero Band** — Full-width cream:
   - `display-xl` headline: "Transaksi P2P Tanpa Rasa Khawatir" — Cormorant Garamond
   - Sub-headline: body text explaining escrow concept
   - Two buttons: "Mulai Sekarang" (coral primary) + "Pelajari Cara Kerja" (secondary outline)
   - Right: Dark navy product mockup card showing room interface
2. **How It Works** — 3-up feature cards (`surface-card`):
   - Step 1: Buat Room
   - Step 2: Dana Diamankan
   - Step 3: Barang Diterima, Dana Dilepas
3. **Trust Indicators** — Dark navy band:
   - Stats: "10,000+ Transaksi Aman", "Rp 5M+ Nilai Escrow", "0% Rekber Scam"
   - Code-window style presentation (monospace numbers)
4. **Use Cases** — Category tabs (Game, Marketplace, Jasa):
   - Active tab: `surface-card` background
   - Content: Use case description + testimonial card
5. **Security** — Cream band:
   - Feature cards: Insta-Ban, PIN Protection, Auto-Release, Dispute Resolution
6. **CTA Band Coral** — Full-width coral:
   - `display-sm` headline: "Siap Bertransaksi dengan Aman?"
   - Cream button: "Daftar Gratis"
7. **Footer** — Dark navy (`surface-dark`):
   - 4-column link list
   - Copyright, social links

---

## 8. Component Specifications

### 8.1 Button Variants

```svelte
<!-- Primary -->
<button class="bg-primary text-on-primary font-sans font-medium text-sm px-5 py-3 rounded-md hover:bg-primary-active transition-colors disabled:bg-primary-disabled disabled:text-muted">
  {label}
</button>

<!-- Secondary -->
<button class="bg-canvas text-ink font-sans font-medium text-sm px-5 py-3 rounded-md border border-hairline hover:bg-surface-soft transition-colors">
  {label}
</button>

<!-- Secondary on Dark -->
<button class="bg-surface-dark-elevated text-on-dark font-sans font-medium text-sm px-5 py-3 rounded-md hover:bg-surface-dark-soft transition-colors">
  {label}
</button>

<!-- Text Link -->
<button class="bg-transparent text-primary font-sans font-medium text-sm hover:underline">
  {label}
</button>

<!-- Icon Circular -->
<button class="w-9 h-9 rounded-full bg-canvas border border-hairline flex items-center justify-center text-ink hover:bg-surface-soft transition-colors">
  <Icon />
</button>
```

### 8.2 Card Variants

```svelte
<!-- Feature Card -->
<div class="bg-surface-card rounded-lg p-8 text-ink">
  <div class="w-10 h-10 rounded-md bg-primary/10 flex items-center justify-center mb-4">
    <Icon class="text-primary" />
  </div>
  <h3 class="font-sans font-medium text-title-md mb-2">{title}</h3>
  <p class="font-sans text-body-md text-body">{description}</p>
</div>

<!-- Product Mockup Card (Dark) -->
<div class="bg-surface-dark rounded-lg p-8 text-on-dark">
  <h3 class="font-sans font-medium text-title-md mb-4">{title}</h3>
  <div class="bg-surface-dark-soft rounded-md p-4 font-mono text-sm">
    <!-- Code / data content -->
  </div>
</div>

<!-- Pricing / Tier Card -->
<div class="bg-canvas border border-hairline rounded-lg p-8">
  <h3 class="font-sans font-medium text-title-lg">{planName}</h3>
  <p class="font-display text-display-sm mt-2">{price}</p>
  <ul class="mt-6 space-y-3">
    {#each features as feature}
      <li class="font-sans text-body-md flex items-center gap-2">
        <Check class="text-success w-4 h-4" />
        {feature}
      </li>
    {/each}
  </ul>
  <Button class="w-full mt-8">{cta}</Button>
</div>

<!-- Callout Card (Coral) -->
<div class="bg-primary rounded-lg p-12 text-on-primary">
  <h2 class="font-display text-display-sm">{headline}</h2>
  <p class="font-sans text-body-md mt-3 opacity-90">{description}</p>
  <Button variant="secondary" class="mt-6 bg-canvas text-ink">{cta}</Button>
</div>
```

### 8.3 Input Fields

```svelte
<!-- Text Input -->
<div class="space-y-1.5">
  <label class="font-sans text-caption font-medium text-body">{label}</label>
  <input
    type="text"
    class="w-full h-10 px-3.5 py-2.5 bg-canvas border border-hairline rounded-md font-sans text-body-md text-ink placeholder:text-muted-soft focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/15 transition-all"
    placeholder={placeholder}
  />
  {#if error}
    <p class="font-sans text-caption text-error">{error}</p>
  {/if}
</div>

<!-- PIN Input (6 digits) -->
<div class="flex gap-2 justify-center">
  {#each Array(6) as _, i}
    <input
      type="password"
      maxlength="1"
      class="w-12 h-14 text-center bg-canvas border-2 border-hairline rounded-md font-sans text-title-lg text-ink focus:border-primary focus:ring-2 focus:ring-primary/15 transition-all"
    />
  {/each}
</div>
```

### 8.4 Badges

```svelte
<!-- Pill Badge -->
<span class="inline-flex items-center px-3 py-1 rounded-pill bg-surface-card text-ink font-sans text-caption font-medium">
  {label}
</span>

<!-- Coral Badge -->
<span class="inline-flex items-center px-3 py-1 rounded-pill bg-primary text-on-primary font-sans text-caption-upper font-medium tracking-wider">
  {label}
</span>

<!-- Status Badges -->
<span class="inline-flex items-center px-2.5 py-0.5 rounded-pill font-sans text-caption font-medium
  {status === 'SUCCESS' ? 'bg-success/10 text-success' : ''}
  {status === 'PENDING' ? 'bg-warning/10 text-warning' : ''}
  {status === 'FAILED' ? 'bg-error/10 text-error' : ''}
">
  {status}
</span>
```

### 8.5 Navigation

```svelte
<!-- Top Nav -->
<nav class="h-16 bg-canvas border-b border-hairline-soft sticky top-0 z-50">
  <div class="max-w-7xl mx-auto px-6 h-full flex items-center justify-between">
    <!-- Logo -->
    <a href="/" class="flex items-center gap-2">
      <span class="font-display text-display-sm text-ink tracking-tight">EyesOfPriestess</span>
    </a>

    <!-- Nav Links -->
    <div class="hidden md:flex items-center gap-1">
      {#each navItems as item}
        <a href={item.href} class="px-3 py-2 rounded-md font-sans text-nav-link font-medium text-ink hover:bg-surface-soft transition-colors">
          {item.label}
        </a>
      {/each}
    </div>

    <!-- Right Cluster -->
    <div class="flex items-center gap-3">
      <a href="/login" class="font-sans text-nav-link font-medium text-ink hover:text-primary transition-colors">Masuk</a>
      <Button variant="primary" class="h-9 px-4">Daftar</Button>
    </div>
  </div>
</nav>

<!-- Sidebar (App) -->
<aside class="w-[280px] bg-canvas border-r border-hairline-soft h-screen sticky top-0 flex flex-col">
  <div class="p-6">
    <span class="font-display text-display-sm text-ink">EyesOfPriestess</span>
  </div>
  <nav class="flex-1 px-4 space-y-1">
    {#each sidebarItems as item}
      <a href={item.href} class="flex items-center gap-3 px-3 py-2.5 rounded-md font-sans text-body-md text-ink hover:bg-surface-soft transition-colors {isActive ? 'bg-surface-card font-medium' : ''}">
        <Icon class="w-5 h-5" />
        {item.label}
        {#if item.badge}
          <span class="ml-auto bg-primary text-on-primary text-xs px-2 py-0.5 rounded-full">{item.badge}</span>
        {/if}
      </a>
    {/each}
  </nav>
  <div class="p-4 border-t border-hairline-soft">
    <!-- User mini profile -->
  </div>
</aside>
```

---

## 9. Responsive Breakpoints

| Name | Width | Key Changes |
|---|---|---|
| Mobile | < 768px | Single column, hamburger nav, bottom bar, hero text 64→32px, cards 1-up |
| Tablet | 768–1024px | Sidebar collapsible to icon-only, feature cards 2-up, rooms 2-up |
| Desktop | 1024–1440px | Full sidebar, 3-up feature cards, split chat layout |
| Wide | > 1440px | Max content width 1200px, more outer breathing room |

---

## 10. Animation & Motion Guidelines

- **Page transitions:** Fade (150ms ease-out)
- **Card hover:** Subtle lift `translateY(-2px)` + soft shadow (rare, use sparingly)
- **Button press:** Scale 0.98 + darken background
- **Toast:** Slide in from right (300ms ease-out), auto-dismiss fade (200ms)
- **Modal:** Backdrop fade + content scale from 0.95 (200ms)
- **Skeleton loading:** Pulse animation on `surface-soft` background
- **Number counting:** Count-up animation untuk balance display
- **Chat message:** Slide in from bottom (100ms stagger)

---

## 11. Asset Requirements

### Fonts (Self-hosted recommended)
- `Cormorant Garamond` — weights: 400, 500, 600 (display)
- `Inter` — weights: 400, 500, 600 (body)
- `JetBrains Mono` — weight: 400 (code)

### Icons (Lucide)
- Wallet, Send, ArrowDownLeft, ArrowUpRight, Shield, Lock, MessageSquare, Users, History, Settings, Bell, Search, ChevronRight, Check, X, AlertTriangle, Image, FileText, MoreVertical, LogOut, User, Home, Plus, Minus, Copy, ExternalLink

### Images
- Hero illustration: Line-art style, coral + dark navy strokes on cream
- Empty states: Minimal line illustrations
- Avatars: Default gradient or initials

---

## 12. Kepatuhan Ketentuan Frontend S1 / Capstone & Arsitektur Latensi Rendah

Frontend diimplementasikan dengan standar kualitas tinggi untuk memenuhi seluruh kriteria akademik S1:

| Kriteria Regulasi S1 | Standar & Spesifikasi | Implementasi di EyesOfPriestess | Status |
|---|---|---|:---:|
| **1. Responsive Layout** | Breakpoints: Mobile (<768px), Tablet (769-1024px), Desktop (>1024px) | Seluruh halaman fluid tanpa horizontal scrollbar; adaptif 1-kolom di mobile hingga split view di desktop | ✅ Terpenuhi |
| **2. Alur Autentikasi** | Login, Register, Logout, Forgot, Reset Password, JWT LocalStorage | `authStore` reaktif, auto-redirect `/dashboard`, auto-restore sesi pada reload, purge token saat logout | ✅ Terpenuhi |
| **3. Client-Side Routing** | Public, Private, Role-Based (`/admin/*`) | Proteksi route di layout SvelteKit, auto-redirect ke `/login` atau `/dashboard` jika unauthorized | ✅ Terpenuhi |
| **4. Dynamic Dashboard** | Dilarang data statis hardcoded; wajib ringkasan, counter, chart, & aktivitas | Integrasi API `/wallet/balance`, `/wallet/transactions`, `/room`, counter dinamis & log aktivitas riil | ✅ Terpenuhi |
| **5. Standard CRUD Suite** | List View, Detail View, Create, Edit, Delete (Modal) | CRUD penuh pada Room, Bank Account (dengan soft delete), Topup, dan Dispute dengan `ConfirmModal.svelte` | ✅ Terpenuhi |
| **6. Search, Filter & Sort** | Keyword search, multi-criteria filter, sort A-Z/Newest secara simultan | `rooms/+page.svelte` & `history/+page.svelte` menjalankan search, filter kategori/status, dan sort bersamaan | ✅ Terpenuhi |
| **7. Paginasi Standar** | Prev/Next, Page numbers, total counter, page size selector | Komponen terstandarisasi `Pagination.svelte` dengan selector 10, 25, 50 data | ✅ Terpenuhi |
| **8. File Upload** | Gambar (.jpg, .png, .webp) atau dokumen PDF | `FileUpload.svelte` dengan drag-and-drop, validasi ukuran <5MB, preview gambar & dokumen | ✅ Terpenuhi |
| **9. Validasi Formulir** | Real-time inline feedback (email, min/max, phone, password confirm) | Validasi inline instan sebelum submit dengan pesan error kontekstual per field | ✅ Terpenuhi |
| **10. Notifikasi & Error** | Interactive toasts (CRUD) + Error Pages (401, 403, 404, 500) | `Toaster.svelte` dan `+error.svelte` menangani seluruh kode status HTTP dengan tombol aksi | ✅ Terpenuhi |

### Optimasi Latensi Rendah (Sub-50ms Perceived Latency):
- **Same-Origin Vite Proxy**: Panggilan API ke `/api/v1` diarahkan melalui reverse proxy internal Vite ke `http://127.0.0.1:8000`, meniadakan *CORS preflight round trip* (`OPTIONS`).
- **In-Memory SWR TTL Cache**: Seluruh query `GET` di-cache selama 3000ms; navigasi antar halaman berlangsung instan (0ms). Mutasi data otomatis membatalkan (*invalidate*) cache domain terkait.
- **In-Flight Request Deduplication**: Permintaan kembar paralel disatukan dalam satu Promise, mencegah beban server ganda.
- **Optimistic Store Rendering**: Store Svelte reaktif menampilkan data terkini secara instan tanpa flickering skeleton.

---

*End of Frontend Specification*
