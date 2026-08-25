# EyesOfPriestess — The Sacred Covenant
> *"In the eyes of the Priestess, no transaction shall breach the sacred trust."*

EyesOfPriestess is a divine escrow e-wallet forged in the aesthetic of ancient civilizations and crystalline mysteries. Built for P2P marketplace transactions — Facebook Marketplace, game trading communities, and digital exchanges — it eliminates the need for *rekber* (rekening bersama) or human middleman through an automated **Covenant Room** system.

## 🏛️ Microservices Architecture — The Backend Sanctums

| Service | Port | Responsibility |
|---------|------|----------------|
| **auth-service** | 8081 | Authentication, User Registration, Security PIN, JWT Token & Redis Session Management |
| **wallet-service** | 8082 | E-Wallet Treasury, Top-Up via Xendit Gateway, Withdrawals, P2P Transfers, Audit Logs |
| **room-service** | 8083 | Escrow Covenant Room lifecycle (creation, acceptance, escrow lock, delivery, release) |
| **chat-service** | 8084 | Real-time Room Chat via WebSockets |
| **dispute-service** | 8085 | Dispute Adjudication, Evidence Upload, Admin Resolution |
| **kong (API Gateway)** | 8000 / 8001 | Public API Gateway Proxy, Auth Middleware, Rate Limiting & CORS |

## 🚀 Tech Stack
- **Backend:** Quarkus 3.x (Java 21, RESTEasy Reactive, Hibernate Reactive Panache)
- **API Gateway:** Kong 3.6 (DB-less mode)
- **Database:** PostgreSQL 15 (Logical Service Schemas)
- **Cache:** Redis 7 (Session Store & Blacklist Rate Limits)
- **Messaging:** RabbitMQ (Async Events between Services)

## 📌 Milestones & Progress
See [`PROJECT_MILESTONES.md`](./PROJECT_MILESTONES.md) for the detailed implementation roadmap.

## 📖 Documentation
Detailed technical specifications are available in the [`Docs/`](./Docs) folder:
- `Docs/01-architecture-overview.md` — Complete system architecture & tech stack
- `Docs/02-backend-api-spec.md` — REST API & Communion WebSocket specification
- `Docs/03-database-schema.md` — PostgreSQL schemas, tables & constraints
- `Docs/04-frontend-spec.md` — SvelteKit Sanctum UI design & route layout
- `Docs/05-project-setup.md` — Development environment & Docker setup guide
- `Docs/06-feature-specifications.md` — Complete user stories & business rules
