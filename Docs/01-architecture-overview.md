# EyesOfPriestess — Architecture Overview
## The Sacred Covenant: Escrow E-Wallet for P2P Marketplace

**Version:** 1.0  
**Date:** August 2026  
**Platform:** Website (SvelteKit + Quarkus Microservices)  
**Database:** PostgreSQL  
**Auth:** JWT with Sanction Seal (Insta-Ban Token Revocation)  
**Payment Gateway:** Midtrans / Xendit Sandbox  
**Codename:** The Covenant Protocol

---

## 1. Executive Summary

> *"In the eyes of the Priestess, no transaction shall breach the sacred trust."*

**EyesOfPriestess** is a divine escrow e-wallet forged in the aesthetic of ancient civilizations and crystalline mysteries. Built for P2P marketplace transactions — Facebook Marketplace, game trading communities, and digital exchanges — it eliminates the need for *rekber* (rekening bersama) or midman through an automated **Covenant Room** system.

**Core Differentiator:** The **Covenant Room** — a sacred chamber where buyer funds are sealed in escrow by the Priestess's gaze, released only when both parties fulfill their oath. No third human required; the system itself is the impartial arbiter.

**Thematic Essence:** Deep void purples, ancient gold accents, crystalline whites, and the solemn elegance of the Priestess's eternal watch.

---

## 2. Tech Stack Final

### Backend — Microservices (Quarkus)
| Layer | Technology | Version | Notes |
|-------|------------|---------|-------|
| Framework | Quarkus | 3.x | Native compilation, low memory footprint |
| Programming | Java 21 | LTS | Virtual threads support |
| API Style | RESTEasy Reactive | — | Non-blocking, high throughput |
| Data Access | Hibernate Reactive Panache | — | Reactive ORM with Panache simplification |
| Database | PostgreSQL | 15+ | ACID, JSONB for metadata, full-text search ready |
| Cache | Redis | 7+ | Sanction Seal blacklist, session cache, rate limiting |
| Messaging | RabbitMQ | — | Async events between sanctums |
| Auth | SmallRye JWT + bcrypt | — | Sacred Seal access + refresh, PIN hash |
| Payment | Midtrans / Xendit | Sandbox | Offerings channel (VA, e-wallet top-up) |
| Build Tool | Maven | 3.9+ | — |
| Container | Docker | — | Each sanctum containerized |

### Frontend — The Sanctum (SvelteKit)
| Layer | Technology | Notes |
|-------|------------|-------|
| Framework | SvelteKit | SSR/SPA hybrid, file-based routing |
| Language | TypeScript | Type safety |
| Styling | TailwindCSS + shadcn-svelte | Utility-first, accessible components |
| State | Svelte Stores | Auth, vault, covenant states |
| HTTP Client | Fetch API + custom wrapper | Native fetch with interceptors |
| Real-time | WebSocket client | Per-covenant communion |
| Icons | Lucide Svelte | Clean, modern |
| Build | Vite | Fast HMR |

### Infrastructure
| Component | Technology |
|-----------|------------|
| API Gateway | Nginx / Kong / custom Quarkus gateway |
| Service Discovery | Docker Compose (dev) / Consul (prod ready) |
| Message Queue | RabbitMQ (async: notifications, auto-release cron) |
| Monitoring | Micrometer + Prometheus (Quarkus extension) |
| Logging | ELK Stack ready (Logstash pattern) |

---

## 3. Microservices Architecture — The Sanctum Network

```
┌─────────────────────────────────────────────────────────────────┐
│                     PILGRIM (Browser)                            │
│              The Sanctum Interface (SSR + SPA)                  │
└──────────────────────────┬──────────────────────────────────────┘
                           │ HTTPS
┌──────────────────────────▼──────────────────────────────────────┐
│                 THE VEIL (API Gateway)                           │
│     • SSL Termination    • Rate Limiting    • Route Proxy       │
└──┬────────────┬────────────┬────────────┬────────────┬──────────┘
   │            │            │            │            │
   ▼            ▼            ▼            ▼            ▼
┌──────┐   ┌──────┐   ┌──────┐   ┌──────┐   ┌──────────┐
│ Seal │   │ Vault│   │Covt. │   │Comm. │   │ Judgment │
│Svc   │   │Svc   │   │Svc   │   │Svc   │   │Svc       │
│:8081 │   │:8082 │   │:8083 │   │:8084 │   │:8085     │
└──┬───┘   └──┬───┘   └──┬───┘   └──┬───┘   └────┬─────┘
   │          │          │          │             │
   └──────────┴──────────┴──────────┴─────────────┘
                          │
              ┌───────────▼────────────┐
              │    The Archive (RDS)   │
              │  • Pilgrim DB (shared) │
              │  • Sanctum schemas      │
              │  • JSONB metadata       │
              └───────────┬────────────┘
                          │
              ┌───────────▼────────────┐
              │    The Crystal (Redis) │
              │  • Sanction Seals      │
              │  • Rate Limiting       │
              │  • Session Cache       │
              └────────────────────────┘
```

### Service Responsibilities — The Five Sanctums

| Sanctum | Port | Responsibility |
|---------|------|----------------|
| **seal-service** | 8081 | Pilgrim registration, Sacred Seal issuance, PIN sanctification, Sanction Seal (insta-ban), KYC attunement |
| **vault-service** | 8082 | Balance stewardship, offerings (top-up), withdrawals, P2P tithing, transaction chronicles, tithe calculation |
| **covenant-service** | 8083 | Covenant Room lifecycle (forge, accept, deliver, fulfill, break, auto-release), escrow seal/release, room state machine |
| **communion-service** | 8084 | Real-time communion (WebSocket) per room, message preservation, read receipts |
| **judgment-service** | 8085 | Dispute adjudication, evidence review, oracle panel, resolution (release/refund/split), notification |
| **herald-service** | 8086 | (Optional Phase 2) Push notifications, missives, omens via RabbitMQ async |
| **the-veil** | 8080 | Route proxy, auth middleware (Seal validation), rate limiting, CORS |

---

## 4. Communication Patterns

### Synchronous (REST API)
- The Veil → Sanctums (internal REST calls)
- Pilgrim → The Veil → Sanctums
- Used for: Seal, Vault queries, Covenant CRUD

### Asynchronous (RabbitMQ — The Aether)
- Sanctum → Sanctum (event-driven)
- Used for:
  - `covenant.fulfilled` → vault-service (release escrow)
  - `covenant.broken` → judgment-service (create case)
  - `pilgrim.sanctioned` → seal-service (revoke all seals)
  - `offering.accepted` → vault-service (top-up balance)
  - `covenant.expired` → covenant-service (auto-release cron)

### Real-time (WebSocket — The Communion)
- Pilgrim ↔ communion-service (persistent connection per room)
- Used for: In-covenant messaging, typing indicators, status updates

---

## 5. Security Architecture — The Sacred Seals

### 5.1 Authentication Flow — The Rite of Passage
```
1. Pilgrim offers credentials (phone + password) → seal-service
2. seal-service validates → issues Sacred Seal (access: 15min, refresh: 7days)
3. Pilgrim stores Seal in httpOnly cookie (or secure localStorage for SPA)
4. Every request: The Veil validates Seal signature + expiry
5. PIN required for: seal escrow, release, withdraw, top-up > threshold
```

### 5.2 Sanction Seal — Insta-Ban Mechanism
**Problem:** Sacred Seal is stateless — if a pilgrim is sanctioned, the seal remains valid until expiry.
**Solution:** The Sanction Seal
- Crystal blacklist: `sanction:<jti>` → TTL = remaining Seal expiry
- The Veil middleware: Check Crystal blacklist before processing any request
- On sanction: seal-service publishes `pilgrim.sanctioned` event → all sanctums invalidate cache
- Refresh seal also revoked and removed from Crystal whitelist

```java
// The Veil Seal validation pseudo
public void validateSeal(String seal) {
    DecodedJWT jwt = JWT.decode(seal);
    String jti = jwt.getId();
    if (crystal.exists("sanction:" + jti)) {
        throw new UnauthorizedException("Seal revoked by the Priestess");
    }
    // Continue validation...
}
```

### 5.3 PIN Sanctification
- PIN 6 digit, hashed with bcrypt (cost 12)
- PIN never equals password
- PIN required for all critical transactions (seal, release, withdraw)
- Rate limit: 5x wrong PIN = lock 30 minutes

### 5.4 Rate Limiting (Crystal)
| Endpoint | Limit |
|----------|-------|
| Rite of Passage (Login) | 5/minute per IP |
| Forge Covenant | 10/hour per pilgrim |
| Offerings (Top-up) | 5/day per pilgrim |
| API general | 100/minute per pilgrim |

---

## 6. Data Strategy — The Archives

### 6.1 Database per Sanctum (Logical Separation)
All sanctums connect to **one PostgreSQL instance** with **separate schemas** for maintainability:
- `seal.pilgrims`, `seal.sessions`
- `vault.treasuries`, `vault.chronicles`
- `covenant.rooms`, `covenant.escrow_seals`
- `communion.messages`
- `judgment.cases`, `judgment.evidence`

**Rationale:** Logical separation for future extraction to physical databases.

### 6.2 Shared Data
- `pilgrims` table: seal-service is the single source of truth. Other sanctums query via internal REST or replicated read replica.

### 6.3 Event Chronicle (Lightweight)
- `covenant_events` table: Record every state change (FORGED, ACCEPTED, DELIVERED, etc.)
- Audit trail, debugging, judgment evidence

---

## 7. Deployment Architecture (Development)

```yaml
# docker-compose.yml (simplified)
version: '3.8'
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: eyesofpriestess
      POSTGRES_USER: priestess
      POSTGRES_PASSWORD: originium_seal
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672"

  seal-service:
    build: ./backend/seal-service
    ports:
      - "8081:8081"
    environment:
      - DB_URL=jdbc:postgresql://postgres:5432/eyesofpriestess
      - REDIS_URL=redis://redis:6379
    depends_on:
      - postgres
      - redis

  vault-service:
    build: ./backend/vault-service
    ports:
      - "8082:8082"
    environment:
      - DB_URL=jdbc:postgresql://postgres:5432/eyesofpriestess
      - REDIS_URL=redis://redis:6379
      - MIDTRANS_SERVER_KEY=${MIDTRANS_SERVER_KEY}
    depends_on:
      - postgres
      - redis
      - rabbitmq

  covenant-service:
    build: ./backend/covenant-service
    ports:
      - "8083:8083"
    environment:
      - DB_URL=jdbc:postgresql://postgres:5432/eyesofpriestess
      - REDIS_URL=redis://redis:6379
    depends_on:
      - postgres
      - redis
      - rabbitmq

  communion-service:
    build: ./backend/communion-service
    ports:
      - "8084:8084"
    environment:
      - DB_URL=jdbc:postgresql://postgres:5432/eyesofpriestess
      - REDIS_URL=redis://redis:6379
    depends_on:
      - postgres
      - redis

  judgment-service:
    build: ./backend/judgment-service
    ports:
      - "8085:8085"
    environment:
      - DB_URL=jdbc:postgresql://postgres:5432/eyesofpriestess
      - REDIS_URL=redis://redis:6379
    depends_on:
      - postgres
      - redis

  the-veil:
    build: ./backend/the-veil
    ports:
      - "8080:8080"
    environment:
      - SEAL_SERVICE_URL=http://seal-service:8081
      - VAULT_SERVICE_URL=http://vault-service:8082
      - COVENANT_SERVICE_URL=http://covenant-service:8083
      - COMMUNION_SERVICE_URL=http://communion-service:8084
      - JUDGMENT_SERVICE_URL=http://judgment-service:8085
      - REDIS_URL=redis://redis:6379
    depends_on:
      - seal-service
      - vault-service
      - covenant-service
      - communion-service
      - judgment-service

  the-sanctum:
    build: ./frontend
    ports:
      - "3000:3000"
    environment:
      - PUBLIC_API_URL=http://localhost:8080
    depends_on:
      - the-veil

volumes:
  postgres_data:
```

---

## 8. Key Design Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Reactive vs Imperative | **Reactive** | Non-blocking I/O, Quarkus native image support, fits SvelteKit async |
| Monolith vs Microservices | **Microservices (logical)** | Clear sanctum boundaries, impressive portfolio, scalable per domain |
| DB per service vs Shared | **Shared DB, separate schemas** | Pragmatic for academic, logically separated for future extraction |
| JWT storage | **httpOnly cookie** | XSS protection, proper CORS handling |
| Payment | **Midtrans/Xendit Sandbox** | Real integration experience, production-ready pattern |
| Communion | **WebSocket native** | Real-time, low latency, superior to polling |
| Auto-release | **Cron job + RabbitMQ** | Scheduled task, reliable, retry-able |

---

## 9. Performance Targets

| Metric | Target |
|--------|--------|
| API Response (p95) | < 150ms |
| Communion latency | < 50ms |
| Page Load (First Contentful Paint) | < 1.5s |
| Time to Interactive | < 3s |
| Concurrent Pilgrims | 5,000+ |
| Database Connections | Pool 20 per sanctum |

---

## 10. Folder Structure (Monorepo)

```
eyesofpriestess/
├── README.md
├── docker-compose.yml
├── .env.example
├── backend/
│   ├── the-veil/
│   │   ├── src/main/java/com/eyesofpriestess/veil/
│   │   ├── src/main/resources/
│   │   ├── pom.xml
│   │   └── Dockerfile
│   ├── seal-service/
│   │   ├── src/main/java/com/eyesofpriestess/seal/
│   │   │   ├── entity/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   ├── resource/
│   │   │   ├── dto/
│   │   │   ├── mapper/
│   │   │   ├── security/
│   │   │   └── event/
│   │   ├── src/main/resources/
│   │   ├── pom.xml
│   │   └── Dockerfile
│   ├── vault-service/
│   │   └── ... (same structure)
│   ├── covenant-service/
│   │   └── ... (same structure)
│   ├── communion-service/
│   │   └── ... (same structure)
│   └── judgment-service/
│       └── ... (same structure)
├── frontend/
│   ├── src/
│   │   ├── lib/
│   │   │   ├── components/
│   │   │   │   ├── ui/           # shadcn-svelte components
│   │   │   │   ├── sanctum/      # Navbar, Sidebar, Footer
│   │   │   │   ├── vault/        # BalanceCard, ChronicleList
│   │   │   │   ├── covenant/     # RoomCard, CovenantTimeline, CommunionBox
│   │   │   │   └── seal/         # LoginForm, PinInput
│   │   │   ├── stores/
│   │   │   │   ├── seal.ts       # Pilgrim state, Sacred Seal
│   │   │   │   ├── vault.ts      # Treasury, chronicles
│   │   │   │   └── covenant.ts   # Active covenants, current room
│   │   │   ├── api/
│   │   │   │   ├── client.ts     # Fetch wrapper with Seal
│   │   │   │   ├── seal.ts       # Seal API
│   │   │   │   ├── vault.ts      # Vault API
│   │   │   │   ├── covenant.ts   # Covenant API
│   │   │   │   └── communion.ts  # WebSocket + message API
│   │   │   ├── types/
│   │   │   │   ├── pilgrim.ts
│   │   │   │   ├── vault.ts
│   │   │   │   ├── covenant.ts
│   │   │   │   └── communion.ts
│   │   │   └── utils/
│   │   │       ├── formatters.ts # Currency, date
│   │   │       └── validators.ts # Form validation
│   │   ├── routes/
│   │   │   ├── +layout.svelte
│   │   │   ├── +page.svelte              # Landing / Sanctum Entrance
│   │   │   ├── rite/
│   │   │   │   ├── +page.svelte          # Login (Rite of Return)
│   │   │   │   └── forge/
│   │   │   │       └── +page.svelte      # Register (Forge Identity)
│   │   │   ├── sanctum/
│   │   │   │   ├── +layout.svelte
│   │   │   │   ├── +page.svelte          # Dashboard (The Observatory)
│   │   │   │   ├── vault/
│   │   │   │   │   ├── +page.svelte
│   │   │   │   │   ├── offering/
│   │   │   │   │   ├── withdrawal/
│   │   │   │   │   └── chronicles/
│   │   │   │   ├── covenant/
│   │   │   │   │   ├── +page.svelte
│   │   │   │   │   ├── forge/
│   │   │   │   │   └── [id]/
│   │   │   │   ├── tithing/
│   │   │   │   │   └── +page.svelte      # P2P transfer
│   │   │   │   └── profile/
│   │   │   │       └── +page.svelte
│   │   ├── app.html
│   │   └── app.d.ts
│   ├── static/
│   │   ├── favicon.png
│   │   ├── priestess-seal.svg
│   │   └── images/
│   ├── package.json
│   ├── svelte.config.js
│   ├── tailwind.config.js
│   ├── vite.config.ts
│   └── Dockerfile
└── docs/
    ├── 01-architecture-overview.md
    ├── 02-backend-api-spec.md
    ├── 03-database-schema.md
    ├── 04-frontend-spec.md
    ├── 05-project-setup.md
    └── 06-feature-specifications.md
```

---

*Next: Read `02-backend-api-spec.md` for detailed API contracts of the Five Sanctums.*
