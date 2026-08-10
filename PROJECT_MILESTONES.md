# EyesOfPriestess — Project Milestones & Roadmap
## The Covenant Protocol Development Plan

**Repository:** `https://github.com/RichkyRahmadan/Eyes-Of-Priestess-V2`  
**Platform:** Website (SvelteKit + Quarkus Microservices)  
**Theme:** Ancient Crystalline & Void Mystique (Deep Void Purples, Ancient Gold, Crystalline White)

---

## 🏛️ Project Architecture Overview

```
                                  PILGRIM (Browser)
                            The Sanctum (SvelteKit)
                                       │
                                       ▼ HTTPS
                             THE VEIL (API Gateway)
                                       │
       ┌──────────────┬────────────────┼────────────────┬──────────────┐
       ▼              ▼                ▼                ▼              ▼
 ┌──────────┐   ┌──────────┐    ┌─────────────┐   ┌───────────┐  ┌────────────┐
 │   Seal   │   │  Vault   │    │  Covenant   │   │ Communion │  │  Judgment  │
 │ Sanctum  │   │ Sanctum  │    │   Sanctum   │   │  Sanctum  │  │  Sanctum   │
 │  :8081   │   │  :8082   │    │    :8083    │   │   :8084   │  │   :8085    │
 └────┬─────┘   └────┬─────┘    └──────┬──────┘   └─────┬─────┘  └─────┬──────┘
      │              │                 │                │              │
      └──────────────┴─────────────────┼────────────────┴──────────────┘
                                       │
                         ┌─────────────┴─────────────┐
                         ▼                           ▼
                  The Archive (PostgreSQL)    The Crystal (Redis)
                  5 Logical DB Schemas        Sanction Seals & Cache
```

---

## 📌 Implementation Milestones

### 🚩 Milestone 0: Sacred Foundations & Repository Setup
- [x] Analyze and verify all documentation (`Docs/01-06`)
- [x] Create project structure, root `.gitignore`, and comprehensive `README.md`
- [x] Write `PROJECT_MILESTONES.md` roadmap
- [x] Initialize Git repository & attach remote `https://github.com/RichkyRahmadan/Eyes-Of-Priestess-V2`
- [x] **Git Commit & Push to GitHub**

---

### 🚩 Milestone 1: Environment & Infrastructure Orchestration ✅
- [x] Configure `docker-compose.yml` for PostgreSQL 15 (5 logical schemas: `seal`, `vault`, `covenant`, `communion`, `judgment`), Redis 7, and RabbitMQ
- [x] Create PostgreSQL initialization scripts (`backend/init-scripts/01-init-schemas.sql`) — all tables, indexes, triggers, views, seed Oracle pilgrim
- [x] Setup `.env.example` with all required environment variables
- [x] Setup Maven multi-module parent POM (`backend/pom.xml`) with Quarkus 3.8.4 BOM
- [x] Create skeleton `pom.xml` for all 6 Quarkus modules (the-veil, seal, vault, covenant, communion, judgment)
- [x] Create `application.properties` for each service with DB schema, JWT, Redis & RabbitMQ configs
- [x] **Git Commit & Push to GitHub**

---

### 🚩 Milestone 2: Seal Sanctum (Authentication & Identity) ✅
- [x] Entity layer: `Pilgrim.java`, `Session.java`, `SanctionRecord.java`
- [x] Repository layer: `PilgrimRepository.java`, `SessionRepository.java` (reactive Panache)
- [x] DTO layer: 8 request DTOs (Bean Validation), 3 response DTOs (ApiResponse wrapper, PilgrimResponse, SealResponse)
- [x] Service layer: `JwtService` (SmallRye JWT Build), `CrystalService` (Redis — sanction blacklist, OTP, rate limit), `OmenService` (OTP simulation), `SealService` (full business logic)
- [x] Resource layer: `SealResource` with all 10 documented REST endpoints
- [x] Exception handling: `SealException` (factory methods) + `SealExceptionMapper` (global JAX-RS handler)
- [x] Event: `PilgrimSanctionedEvent` for RabbitMQ Aether
- [x] Infrastructure: `Dockerfile.jvm`, `scripts/generate-jwt-keys.sh`
- [x] **Git Commit & Push to GitHub**

---

### 🚩 Milestone 3: Vault Sanctum (Treasury & Payments) ✅
- [x] Entity layer: `Treasury.java` (@Version optimistic locking), `Offering.java`, `Withdrawal.java`, `Chronicle.java` (immutable audit log)
- [x] Repository layer: `TreasuryRepository.java`, `OfferingRepository.java`, `WithdrawalRepository.java`, `ChronicleRepository.java`
- [x] Payment Gateway: Xendit integration (`XenditService.java` for Invoices & Disbursements APIs)
- [x] API Gateway Migration: Replaced custom gateway with **Kong 3.6 API Gateway** (DB-less declarative config `kong/kong.yaml` with CORS, rate-limiting, request-id, WebSocket)
- [x] DTO layer: Request & Response DTOs (`MakeOfferingRequest`, `WithdrawalRequest`, `TitheTransferRequest`, `XenditWebhookPayload`, `TreasuryResponse`, `OfferingResponse`, `WithdrawalResponse`, `ChronicleResponse`)
- [x] Service layer: `VaultService.java` (Treasury auto-creation, Xendit top-up, Xendit bank withdrawal, P2P tithing, Escrow hold/release/refund)
- [x] Event Messaging: `VaultEventConsumer.java` listening on RabbitMQ for `covenant-fulfilled` and `judgment-resolved` events
- [x] REST Resource: `VaultResource.java` with complete API routes, Xendit callback receiver, and internal Escrow endpoints
- [x] **Git Commit & Push to GitHub**

---

### 🚩 Milestone 4: Covenant Sanctum (Escrow Engine) ✅
- [x] Entity layer: `Covenant.java` (Escrow state machine: FORGED -> ACCEPTED -> SEALED -> DELIVERED -> FULFILLED | DISPUTED | CANCELLED), `CovenantInvitation.java`
- [x] Repository layer: `CovenantRepository.java`, `InvitationRepository.java`
- [x] DTO layer: Request & Response DTOs (`ForgeCovenantRequest`, `AcceptCovenantRequest`, `DeliverCovenantRequest`, `DisputeCovenantRequest`, `CovenantResponse`, `InvitationResponse`)
- [x] Downstream REST Client: `VaultClient.java` (invokes Vault Sanctum `/api/v1/vault/internal/escrow/*`)
- [x] Service layer: `CovenantService.java` (Room creation, invitation code generation, acceptance, escrow lock, delivery declaration, confirmation payout release, dispute initiation, cancellation)
- [x] Background Scheduler: `AutoReleaseScheduler.java` (Cron job for auto-fulfilling timed-out DELIVERED covenants)
- [x] Event Messaging: Outbound RabbitMQ events `covenant-fulfilled` and `covenant-broken`
- [x] REST Resource: `CovenantResource.java` (`/api/v1/covenants` with 9 endpoints)
- [x] **Git Commit & Push to GitHub**

---

### 🚩 Milestone 5: Communion & Judgment Sanctums ✅
**Communion Sanctum (Real-time WebSocket Chat):**
- [x] Entity: `Message.java` (USER | SYSTEM message types, per covenant room)
- [x] Repository: `MessageRepository.java` (paginated history, count queries)
- [x] DTO: `CommunionMessage.java` (WebSocket wire format — send & receive)
- [x] Service: `CommunionService.java` (persist user & system messages), `RoomRegistry.java` (in-memory session registry), `JwtValidator.java` (WebSocket JWT handshake)
- [x] WebSocket: `CommunionSocket.java` (`/ws/communion/{covenantId}?token=JWT`) — auth on connect, broadcast to room, persist on send
- [x] REST: `CommunionResource.java` (`GET /messages` history, `GET /status` active sessions)

**Judgment Sanctum (Dispute Resolution Engine):**
- [x] Entity: `JudgmentCase.java` (State machine: OPEN → DELIBERATING → RENDERED, resolution types: RELEASE | REFUND | SPLIT)
- [x] Repository: `JudgmentCaseRepository.java` (by covenant, by status, by pilgrim)
- [x] DTO: `SubmitEvidenceRequest`, `RenderJudgmentRequest`, `JudgmentCaseResponse`, `ApiResponse`
- [x] Events: `CovenantBrokenEvent` (consumed), `JudgmentResolvedEvent` (published)
- [x] Client: `VaultClient.java` (release, refund, split escrow settlement calls)
- [x] Consumer: `JudgmentEventConsumer.java` (auto-opens case on `covenant-broken` RabbitMQ event)
- [x] Service: `JudgmentService.java` (case lifecycle, evidence, Oracle assignment, verdict rendering & financial settlement trigger)
- [x] REST: `JudgmentResource.java` (7 endpoints: party & Oracle-only `@RolesAllowed("oracle")` panel)
- [x] Exception handling: `JudgmentException` + `JudgmentExceptionMapper`
- [x] **Git Commit & Push to GitHub**

---

### 🚩 Milestone 6: The Sanctum Frontend (SvelteKit Web Application) ✅
- [x] Design System implementation: Void Purple (`#0b0914`), Ancient Gold (`#d4af37`), Crystalline Glassmorphism
- [x] Build Rite forms (Login, Register, PIN modal)
- [x] Build Observatory Dashboard (Treasury summary, active covenants, stats, charts)
- [x] Build Vault UI (Offering, Withdrawal, Tithing, Chronicles list)
- [x] Build Covenant Chamber UI (Timeline, delivery proof, confirm release, real-time Communion chat)
- [x] Build Oracle Admin Panel & Judgment Sanctum (`/sanctum/judgment`)
- [x] Migrated to Svelte 5 Runes & configured Docker deployment with `@sveltejs/adapter-node`
- [x] **Git Commit & Push to GitHub**

---

### 🚩 Milestone 7: Integration, End-to-End Verification & Polish
- [ ] Perform complete end-to-end escrow transaction testing
- [ ] Validate performance (API response < 150ms, WebSocket < 50ms)
- [ ] Final UI/UX polish and responsiveness check
- [ ] **Git Commit & Push to GitHub**
