# EyesOfPriestess — Architecture Overview

> **Version:** 1.0  
> **Date:** 2026-08-11  
> **Project:** EyesOfPriestess — Escrow E-Wallet for P2P Marketplace  
> **Design System:** Warm Editorial (Cream Canvas + Coral Accent + Slab-Serif)

---

## 1. Design Philosophy

EyesOfPriestess mengadopsi **warm editorial design language** yang berbeda dari e-wallet konvensional. Alih-alih cool blue / slate yang umum digunakan fintech, sistem ini menggunakan:

- **Cream Canvas** (`#faf9f5`) sebagai lantai dasar setiap halaman — hangat, humanist, berbeda dari pure white yang generic.
- **Coral Primary** (`#cc785c`) untuk CTA dan accent — warm, deliberately muted, tidak cyan/blue.
- **Slab-Serif Display** (Copernicus / Tiempos Headline / Cormorant Garamond) untuk headline — memberikan kesan literary, trustworthy, seperti membaca editorial majalah keuangan.
- **Humanist Sans** (StyreneB / Inter) untuk body text dan UI labels — readable, warm, tidak geometric dingin.
- **Dark Navy Surfaces** (`#181715`) untuk product chrome — dashboard cards, transaction tables, code blocks, terminal output.

Pacing halaman mengikuti ritme **cream → cream-card → dark-mockup → cream → coral-callout → dark-footer**.

---

## 2. Tech Stack

| Layer | Technology | Version | Notes |
|---|---|---|---|
| **Backend** | Quarkus | 3.x | Java 21, RESTEasy Reactive, Hibernate Reactive Panache, Mutiny Vert.x |
| **API Gateway** | Kong Gateway DB-less | 3.x | Port 8000: Reverse proxy, rate limiting (600 req/min), CORS |
| **Frontend** | SvelteKit | 2.x | TypeScript, Svelte 5 Runes, Vite dev proxy |
| **Styling** | Vanilla CSS + Tailwind | 3.x | Warm Editorial custom design tokens |
| **Database** | PostgreSQL | 15+ | Port 5435: 5 isolated schemas, 3NF, soft-deletes, 20+ seeds |
| **Message Broker** | RabbitMQ | 3.x | Port 5672: Async event exchange `eop.events` |
| **Cache & Session** | Redis | 7.x | Port 6379: Token blacklist, idempotency keys, rate limits |
| **Real-time** | WebSocket (native) | — | In-room chat + instant status updates |
| **Payment Gateway** | Xendit Sandbox | v2 | Virtual Account & QRIS callback webhook with token verification |
| **Container** | Docker Compose | 2.x | Containerized infrastructure orchestration |

---

## 3. Microservices Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           EYESOFPRIESTESS PLATFORM                          │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │                    SvelteKit SPA Client (:5173)                       │  │
│  │     • Svelte 5 Runes, In-Memory SWR Cache, In-Flight Deduplication    │  │
│  └──────────────────────────────────┬────────────────────────────────────┘  │
│                                     │ /api/v1 (Same-Origin Proxy)           │
│  ┌──────────────────────────────────▼────────────────────────────────────┐  │
│  │               Kong API Gateway DB-less (:8000)                        │  │
│  │     • Rate-Limiting: 600 req/min | 20,000 req/hr                      │  │
│  │     • Central Route Mapping & Sub-Millisecond Header Forwarding       │  │
│  └──────┬─────────────┬─────────────┬─────────────┬──────────────────────┘  │
│         │             │             │             │                         │
│  ┌──────┴──────┬──────┴──────┬──────┴──────┬──────┴──────┬────────────────┐ │
│  │             │             │             │             │                │ │
│  ▼             ▼             ▼             ▼             ▼                ▼ │
│ ┌─────┐    ┌─────┐     ┌─────────┐   ┌─────────┐   ┌─────────┐   ┌────────┐ │
│ │Auth │    │Wallet│     │  Room   │   │  Chat   │   │ Dispute │   │ Swagger│ │
│ │Svc  │    │Svc   │     │ Escrow  │   │  Svc    │   │  Svc    │   │ OpenAPI│ │
│ │:8081│    │:8082 │     │ :8083   │   │ :8084   │   │ :8085   │   │ /q/ui  │ │
│ └─────┘    └─────┘     └─────────┘   └─────────┘   └─────────┘   └────────┘ │
│     │          │            │             │             │                   │
│     └──────────┴────────────┼─────────────┴─────────────┘                   │
│                             │                                               │
│                   ┌─────────┴─────────┐                                     │
│                   │   Message Bus     │                                     │
│                   │ (RabbitMQ :5672)  │                                     │
│                   └───────────────────┘                                     │
│                             │                                               │
│        ┌────────────────────┼────────────────────┐                          │
│        ▼                    ▼                    ▼                          │
│   ┌─────────┐         ┌─────────┐         ┌─────────────┐                   │
│   │PostgreSQL│         │  Redis  │         │   Xendit    │                   │
│   │  :5435  │         │  :6379  │         │ Payment GW  │                   │
│   └─────────┘         └─────────┘         └─────────────┘                   │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Service Map

| Service | Port | Responsibility | Database Schema |
|---|---|---|---|
| **Kong Gateway** | 8000 | Reverse proxy, centralized routing, rate limiting, CORS | — |
| **Auth Service** | 8081 | Register, login, JWT RSA256, PIN management, blacklist check | `auth` |
| **Wallet Service** | 8082 | Balance, Xendit topup, bank accounts (soft-delete), ledger | `wallet` |
| **Room Escrow Service** | 8083 | Escrow room lifecycle, balance lock, delivery proofs, timeouts | `room` |
| **Chat Service** | 8084 | In-room real-time WebSocket messaging, message audit trail | `chat` |
| **Dispute Service** | 8085 | Dispute filing, evidence review, High Oracle arbitration | `dispute` |

---

## 4. Low-Latency & High Performance Architecture

Untuk menjamin performa sekelas produksi dan waktu tanggap instan (<50ms):

1. **Vite Same-Origin Dev Proxy (`/api` -> `http://127.0.0.1:8000`)**:
   - Menghilangkan *CORS preflight round trip* (`OPTIONS` request) yang sebelumnya menggandakan latensi jaringan browser.
   - Menggunakan IPv4 loopback eksplisit (`127.0.0.1`) guna menghindari penundaan negosiasi DNS dual-stack Windows IPv6 (`::1` fallback lag 180ms–430ms).
2. **In-Memory SWR (Stale-While-Revalidate) Read Cache**:
   - `client.ts` mengimplementasikan TTL cache (3000ms) untuk seluruh permintaan `GET` idempoten.
   - Navigasi antar halaman menyajikan data dari memori secara seketika (0ms waktu pemuatan).
   - Seluruh mutasi data (`POST`, `PUT`, `DELETE`, `PATCH`) otomatis memicu *cache invalidation* pada domain terkait.
3. **In-Flight Request Deduplication**:
   - Permintaan HTTP identik yang berjalan paralel secara otomatis digabungkan ke satu Promise bersama, mencegah *redundant backend hammering*.
4. **Optimistic Store Rendering**:
   - Halaman dashboard, room, dan wallet langsung merender data toko (*store*) tanpa animasi skeleton yang menghalangi pandangan jika data sudah tersedia.
5. **Reactive Non-Blocking Microservices (Quarkus & Mutiny)**:
   - Backend berjalan di atas Vert.x event loop dengan *reactive database client* dan *smallrye reactive messaging*, menghasilkan waktu pemrosesan internal di bawah 5ms.
6. **Kong Gateway Rate-Limiting Optimization**:
   - Threshold rate limit dinaikkan menjadi 600 req/menit dan 20.000 req/jam untuk mencegah throttling selama sesi navigasi cepat.

---

## 5. Communication Patterns

### Synchronous (REST API)
- Client → Gateway → Service (internal REST)
- Gateway aggregates responses dari multiple services jika diperlukan
- Timeout: 10s default, 30s untuk payment callback

### Asynchronous (RabbitMQ)
| Event | Publisher | Consumer | Purpose |
|---|---|---|---|
| `user.registered` | Auth | Wallet | Auto-create wallet untuk user baru |
| `wallet.topped_up` | Wallet | Gateway | Push notification ke user |
| `room.created` | Room | Chat | Auto-create chat room |
| `room.funded` | Room | Wallet | Lock dana dari wallet ke escrow |
| `room.released` | Room | Wallet | Transfer dana ke seller wallet |
| `room.disputed` | Room | Dispute | Create dispute ticket |
| `dispute.resolved` | Dispute | Room | Release atau refund dana |
| `user.banned` | Auth | Gateway, Wallet, Room | Insta-ban propagation |

---

## 5. Security Architecture

### Authentication
- **JWT (SmallRye JWT)** — stateless, signed RS256
- Access token: 15 menit
- Refresh token: 7 hari (stored hashed di Redis)
- PIN: 6-digit, encrypted dengan Argon2, required untuk setiap transaksi kritis

### Insta-Ban Mechanism
- Akun yang terkena pelanggaran langsung dimasukkan ke **Redis blacklist set**
- API Gateway memeriksa blacklist pada setiap request (middleware, < 1ms latency)
- Token JWT yang masih valid tetapi user-nya di-ban akan langsung ditolak
- Event `user.banned` dipublish ke semua service untuk invalidate local cache

### Authorization
- RBAC: `USER`, `ADMIN`, `MODERATOR`
- Room-level: `BUYER`, `SELLER`, `OBSERVER`

### Data Protection
- PostgreSQL: SSL/TLS enforced
- Sensitive fields (PIN, KTP): AES-256-GCM encryption at rest
- Password: Argon2id hashing

---

## 6. Data Strategy

### PostgreSQL Schemas
```
auth        → users, credentials, refresh_tokens, pin_history, ban_logs
wallet      → wallets, transactions, topup_orders, withdraw_requests, bank_accounts
room        → rooms, room_participants, delivery_proofs, escrow_snapshots
chat        → chat_rooms, messages, message_reactions, chat_participants
dispute     → disputes, dispute_evidence, dispute_logs, admin_decisions
```

### Redis Keys
```
blacklist:jwt:{jti}       → TTL sesuai sisa token expiry
ban:user:{userId}         → permanent / TTL
rate_limit:ip:{ip}        → sliding window
session:{userId}          → active device list
pin_attempts:{userId}     → brute force protection
```

---

## 7. Real-Time Architecture

```
Client (SvelteKit) ←──WebSocket──→ API Gateway ←──WebSocket──→ Chat Service
                                           ↑
                                           └── Redis Pub/Sub (broadcast)
```

- WebSocket connection authenticated via JWT query param saat handshake
- Room-specific channels: `room:{roomId}`
- User-specific channels: `user:{userId}` (notifications)
- Heartbeat: 30s ping/pong
- Reconnection: exponential backoff

---

## 8. Design Tokens (EyesOfPriestess Adaptation)

### Color Palette
| Token | Hex | Usage |
|---|---|---|
| `--canvas` | `#faf9f5` | Page background, default floor |
| `--surface-card` | `#efe9de` | Feature cards, content cards |
| `--surface-dark` | `#181715` | Dashboard chrome, tables, footer |
| `--surface-dark-elevated` | `#252320` | Elevated cards inside dark bands |
| `--primary` | `#cc785c` | Primary CTA, coral accent, brand mark |
| `--primary-active` | `#a9583e` | Hover/pressed state |
| `--ink` | `#141413` | Headlines, primary text |
| `--body` | `#3d3d3a` | Running text |
| `--muted` | `#6c6a64` | Secondary text, breadcrumbs |
| `--on-primary` | `#ffffff` | Text on coral buttons |
| `--on-dark` | `#faf9f5` | Text on dark surfaces |
| `--success` | `#5db872` | Success states, available indicators |
| `--warning` | `#d4a017` | Warning callouts |
| `--error` | `#c64545` | Validation errors, dispute badges |
| `--hairline` | `#e6dfd8` | 1px borders on cream surfaces |

### Typography
| Token | Font | Size | Weight | Line Height | Letter Spacing |
|---|---|---|---|---|---|
| `display-xl` | Cormorant Garamond | 64px | 500 | 1.05 | -0.02em |
| `display-lg` | Cormorant Garamond | 48px | 500 | 1.1 | -0.02em |
| `display-md` | Cormorant Garamond | 36px | 500 | 1.15 | -0.015em |
| `title-lg` | Inter | 22px | 500 | 1.3 | 0 |
| `title-md` | Inter | 18px | 500 | 1.4 | 0 |
| `body-md` | Inter | 16px | 400 | 1.55 | 0 |
| `body-sm` | Inter | 14px | 400 | 1.55 | 0 |
| `caption` | Inter | 13px | 500 | 1.4 | 0 |
| `code` | JetBrains Mono | 14px | 400 | 1.6 | 0 |

### Spacing
| Token | Value |
|---|---|
| `section` | 96px |
| `xxl` | 48px |
| `xl` | 32px |
| `lg` | 24px |
| `md` | 16px |
| `sm` | 12px |
| `xs` | 8px |
| `xxs` | 4px |

### Border Radius
| Token | Value | Usage |
|---|---|---|
| `md` | 8px | Buttons, inputs, tabs |
| `lg` | 12px | Content cards, feature cards |
| `xl` | 16px | Hero containers, dashboard panels |
| `pill` | 9999px | Badges, status pills |

---

## 9. Deployment Architecture (Dev)

```yaml
# docker-compose.yml (simplified)
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: eyesofpriestess
      POSTGRES_USER: eop
      POSTGRES_PASSWORD: eop_dev
    ports: ["5432:5432"]
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports: ["6379:6379"]

  rabbitmq:
    image: rabbitmq:3-management
    ports: ["5672:5672", "15672:15672"]

  auth-service:
    build: ./backend/auth-service
    ports: ["8081:8081"]
    depends_on: [postgres, redis, rabbitmq]

  wallet-service:
    build: ./backend/wallet-service
    ports: ["8082:8082"]
    depends_on: [postgres, redis, rabbitmq]

  room-service:
    build: ./backend/room-service
    ports: ["8083:8083"]
    depends_on: [postgres, redis, rabbitmq]

  chat-service:
    build: ./backend/chat-service
    ports: ["8084:8084"]
    depends_on: [postgres, redis, rabbitmq]

  dispute-service:
    build: ./backend/dispute-service
    ports: ["8085:8085"]
    depends_on: [postgres, redis, rabbitmq]

  gateway:
    build: ./backend/gateway
    ports: ["8080:8080"]
    depends_on: [auth-service, wallet-service, room-service, chat-service, dispute-service]

  frontend:
    build: ./frontend
    ports: ["3000:3000"]
    depends_on: [gateway]
```

---

## 10. Folder Structure (Monorepo)

```
eyesofpriestess/
├── README.md
├── docker-compose.yml
├── .gitignore
│
├── backend/
│   ├── gateway/                 → API Gateway (Quarkus)
│   │   ├── src/main/java/com/eyesofpriestess/gateway/
│   │   ├── src/main/resources/
│   │   ├── pom.xml
│   │   └── Dockerfile
│   │
│   ├── auth-service/            → Auth Service (Quarkus)
│   │   ├── src/main/java/com/eyesofpriestess/auth/
│   │   │   ├── entity/
│   │   │   ├── repository/
│   │   │   ├── resource/
│   │   │   ├── service/
│   │   │   ├── dto/
│   │   │   ├── mapper/
│   │   │   ├── security/
│   │   │   └── messaging/
│   │   ├── src/main/resources/
│   │   ├── pom.xml
│   │   └── Dockerfile
│   │
│   ├── wallet-service/          → Wallet Service (Quarkus)
│   │   ├── src/main/java/com/eyesofpriestess/wallet/
│   │   └── ...
│   │
│   ├── room-service/            → Room Escrow Service (Quarkus)
│   │   ├── src/main/java/com/eyesofpriestess/room/
│   │   └── ...
│   │
│   ├── chat-service/            → Chat Service (Quarkus)
│   │   ├── src/main/java/com/eyesofpriestess/chat/
│   │   └── ...
│   │
│   └── dispute-service/         → Dispute Service (Quarkus)
│       ├── src/main/java/com/eyesofpriestess/dispute/
│       └── ...
│
├── frontend/                    → SvelteKit + TailwindCSS
│   ├── src/
│   │   ├── app.html
│   │   ├── app.css
│   │   ├── routes/
│   │   │   ├── (auth)/
│   │   │   │   ├── login/+page.svelte
│   │   │   │   ├── register/+page.svelte
│   │   │   │   └── set-pin/+page.svelte
│   │   │   ├── (app)/
│   │   │   │   ├── dashboard/+page.svelte
│   │   │   │   ├── wallet/+page.svelte
│   │   │   │   ├── rooms/+page.svelte
│   │   │   │   ├── rooms/[id]/+page.svelte
│   │   │   │   ├── chat/+page.svelte
│   │   │   │   ├── history/+page.svelte
│   │   │   │   └── profile/+page.svelte
│   │   │   └── +layout.svelte
│   │   ├── lib/
│   │   │   ├── components/      → Reusable UI (Button, Card, Input, Badge, etc.)
│   │   │   ├── stores/          → Svelte stores (auth, wallet, rooms, notifications)
│   │   │   ├── api/             → API client modules per service
│   │   │   ├── websocket/       → WebSocket client manager
│   │   │   ├── types/           → TypeScript interfaces
│   │   │   └── utils/           → Helpers, formatters, validators
│   │   └── static/
│   │       ├── fonts/           → Cormorant Garamond, Inter (self-hosted)
│   │       └── images/
│   ├── tailwind.config.js
│   ├── svelte.config.js
│   ├── vite.config.ts
│   ├── tsconfig.json
│   ├── package.json
│   └── Dockerfile
│
└── docs/                        → Documentation (this file set)
    ├── 01-architecture-overview.md
    ├── 02-backend-api-spec.md
    ├── 03-database-schema.md
    ├── 04-frontend-spec.md
    ├── 05-project-setup.md
    └── 06-feature-specifications.md
```

---

## 11. Key Design Decisions

| Decision | Rationale |
|---|---|
| **Quarkus Reactive** | Non-blocking I/O, low memory footprint, cocok untuk microservices finansial dengan concurrency tinggi |
| **SvelteKit** | Zero virtual DOM overhead, SSR untuk SEO, performa maksimal untuk web app |
| **PostgreSQL per schema** | ACID rigid untuk transaksi finansial, JSONB untuk metadata fleksibel |
| **Microservices** | Setiap domain (auth, wallet, room, chat, dispute) bisa di-scale dan di-deploy independently |
| **Warm Editorial UI** | Differentiasi dari e-wallet kompetitor yang menggunakan cool blue/slate — menciptakan trust melalui kesan humanist, literary, dan "priestess-like" wisdom |
| **Insta-Ban via Redis** | Keamanan real-time tanpa menunggu JWT expire — critical untuk platform escrow |
| **Midtrans/Xendit Sandbox** | Real payment integration untuk portofolio, menunjukkan kemampuan production-ready |

---

*End of Architecture Overview*
