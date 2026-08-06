# EyesOfPriestess — The Sacred Covenant
> *"In the eyes of the Priestess, no transaction shall breach the sacred trust."*

EyesOfPriestess is a divine escrow e-wallet forged in the aesthetic of ancient civilizations and crystalline mysteries. Built for P2P marketplace transactions — Facebook Marketplace, game trading communities, and digital exchanges — it eliminates the need for *rekber* (rekening bersama) or human middleman through an automated **Covenant Room** system.

## 🏛️ Microservices Architecture — The Five Sanctums

| Sanctum | Port | Responsibility |
|---------|------|----------------|
| **seal-service** | 8081 | Pilgrim registration, Sacred Seal (JWT) issuance, PIN sanctification, Sanction Seal (insta-ban) |
| **vault-service** | 8082 | Treasury stewardship, Offerings (top-up), withdrawals, P2P tithing, transaction chronicles |
| **covenant-service** | 8083 | Covenant Room lifecycle (forge, accept, deliver, fulfill, break, auto-release), escrow hold/release |
| **communion-service** | 8084 | Real-time communion (WebSocket) per room, message preservation |
| **judgment-service** | 8085 | Dispute adjudication, evidence review, oracle panel, resolution |
| **the-veil** | 8080 | API Gateway route proxy, Seal auth middleware, rate limiting, CORS |
| **the-sanctum** | 3000 | Frontend SvelteKit Website |

## 🚀 Tech Stack
- **Backend:** Quarkus 3.x (Java 21 Virtual Threads, RESTEasy Reactive, Hibernate Reactive Panache)
- **Frontend:** SvelteKit + TypeScript + TailwindCSS + Lucide Icons
- **Database:** PostgreSQL 15 (Logical Service Schemas)
- **Cache:** Redis 7 (The Crystal — Sanction Seals & Rate Limits)
- **Messaging:** RabbitMQ (Async Events between Sanctums)

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
