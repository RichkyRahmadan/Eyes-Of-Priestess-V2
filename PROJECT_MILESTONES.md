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

### 🚩 Milestone 1: Environment & Infrastructure Orchestration
- [ ] Configure `docker-compose.yml` for PostgreSQL 15 (5 logical schemas: `seal`, `vault`, `covenant`, `communion`, `judgment`), Redis 7, and RabbitMQ
- [ ] Create PostgreSQL initialization scripts (`init-schemas.sql`)
- [ ] Setup base Maven multi-module / Quarkus service structure for all 5 Sanctums & The Veil gateway
- [ ] **Git Commit & Push to GitHub**

---

### 🚩 Milestone 2: Seal Sanctum (Authentication & Identity)
- [ ] Implement Pilgrim registration (`POST /seal/forge`) & Omen OTP simulation (`/seal/omen/*`)
- [ ] Implement Rite of Return login (`POST /seal/rite`), Sacred Seal (JWT) issuing & renewing
- [ ] Implement PIN sanctification & bcrypt hashing
- [ ] Implement Sanction Seal (Insta-Ban token revocation with Redis Crystal blacklist)
- [ ] **Git Commit & Push to GitHub**

---

### 🚩 Milestone 3: Vault Sanctum (Treasury Core)
- [ ] Implement Treasury balance inquiry (`GET /vault/treasury`)
- [ ] Implement Offerings top-up flow (`POST /vault/offering` + Midtrans/Xendit Sandbox webhook handling)
- [ ] Implement Withdrawal Ritual (`POST /vault/withdrawal`)
- [ ] Implement P2P Tithing direct transfer (`POST /vault/tithing`)
- [ ] Implement Chronicles transaction history (`GET /vault/chronicles`)
- [ ] **Git Commit & Push to GitHub**

---

### 🚩 Milestone 4: Covenant Sanctum (Automated Escrow Engine)
- [ ] Implement Covenant creation (`POST /covenant`), locking treasury from available to sealed
- [ ] Implement Accept/Reject Covenant flow
- [ ] Implement Oath Fulfillment (delivery proof upload & status transition)
- [ ] Implement Confirm Fulfillment (escrow release to seller treasury minus 1% fee)
- [ ] Implement Auto-Release timeout cron job (Quarkus Scheduled task)
- [ ] **Git Commit & Push to GitHub**

---

### 🚩 Milestone 5: Communion & Judgment Sanctums (Real-Time Chat & Disputes)
- [ ] Implement WebSocket Communion endpoint (`ws://localhost:8080/ws/communion/{covenantId}`)
- [ ] Implement message persistence & history
- [ ] Implement Dispute/Judgment invocation (`POST /covenant/:id/judgment`), freezing escrow
- [ ] Implement Oracle resolution panel (`POST /judgment/:id/render` for release/refund/split)
- [ ] **Git Commit & Push to GitHub**

---

### 6️⃣ Milestone 6: The Sanctum Frontend (SvelteKit Web Application)
- [ ] Design System implementation: Void Purple (`#0b0914`), Ancient Gold (`#d4af37`), Crystalline Glassmorphism
- [ ] Build Rite forms (Login, Register, PIN modal)
- [ ] Build Observatory Dashboard (Treasury summary, active covenants, stats, charts)
- [ ] Build Vault UI (Offering, Withdrawal, Tithing, Chronicles list)
- [ ] Build Covenant Chamber UI (Timeline, delivery proof, confirm release, real-time Communion chat)
- [ ] Build Oracle Admin Panel (`/oracle`)
- [ ] **Git Commit & Push to GitHub**

---

### 🚩 Milestone 7: Integration, End-to-End Verification & Polish
- [ ] Perform complete end-to-end escrow transaction testing
- [ ] Validate performance (API response < 150ms, WebSocket < 50ms)
- [ ] Final UI/UX polish and responsiveness check
- [ ] **Git Commit & Push to GitHub**
