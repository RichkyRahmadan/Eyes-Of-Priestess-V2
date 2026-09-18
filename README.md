# EyesOfPriestess — The Sacred Covenant Escrow E-Wallet

> *"In the eyes of the Priestess, no transaction shall breach the sacred trust."*

[![Quarkus 3.x](https://img.shields.io/badge/Quarkus-3.x-red.svg)](https://quarkus.io/)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![SvelteKit 2.x](https://img.shields.io/badge/SvelteKit-2.x-ff3e00.svg)](https://kit.svelte.dev/)
[![TailwindCSS 3.x](https://img.shields.io/badge/TailwindCSS-3.x-38bdf8.svg)](https://tailwindcss.com/)
[![PostgreSQL 15](https://img.shields.io/badge/PostgreSQL-15-336791.svg)](https://www.postgresql.org/)
[![Kong Gateway 3.6](https://img.shields.io/badge/Kong-3.6-003459.svg)](https://konghq.com/)

**EyesOfPriestess** adalah platform escrow e-wallet berbasis arsitektur microservices terdistribusi untuk mengamankan transaksi peer-to-peer (P2P marketplace, akun game, produk digital, dan jasa). Aplikasi ini mengeliminasi kebutuhan jasa rekening bersama (*rekber*) manual pihak ketiga dengan menerapkan sistem **Covenant Room** otomatis, perlindungan saldo terkunci (*escrow balance*), sengketa (*dispute resolution*), dan obrolan instan terenkripsi.

Proyek ini dibangun sebagai implementasi Capstone Project S1 Teknik Informatika yang mematuhi standar arsitektur enterprise modern, keamanan finansial, dan norma desain humanis *Warm Editorial*.

---

## 🌟 Key Features Showcase

1. **Divine Escrow Covenant Rooms**
   - Siklus hidup transaksi escrow otomatis: `WAITING_PAYMENT` → `FUNDED` → `DELIVERED` → `COMPLETED`.
   - Validasi PIN 6 digit untuk setiap tindakan kritis (penguncian dana, konfirmasi pelepasan dana).
   - Pengunggahan bukti penyerahan barang/layanan berupa gambar, dokumen kontrak (PDF), atau tautan.
   - Timer pelepasan otomatis (*auto-release*) 24 jam dengan opsi pengajuan sengketa.

2. **Digital E-Wallet & P2P Balance Transfers**
   - Pemisahan saldo yang ketat: Saldo Tersedia (*Available Balance*) dan Saldo Escrow (*Escrow Balance*).
   - Penarikan dana (*withdraw*) ke rekening bank lokal terverifikasi (BCA, Mandiri, BRI, BNI, GoPay, Dana, OVO).
   - Riwayat transaksi real-time dengan pencarian kata kunci, filter multi-kriteria, pengurutan, dan paginasi standar.

3. **Dispute Resolution Sanctum & Arbiter Admin Panel**
   - Sistem pengajuan sengketa dengan unggah bukti pendukung dan riwayat log kronologis.
   - Panel khusus Administrator / Arbiter (`/admin`) untuk memediasi kasus, membekukan dana, dan memutuskan pengembalian dana (*refund*) atau pelepasan dana ke penjual (*release*).
   - Tindakan sanksi & banned akun (*Insta-Ban*) bagi pengguna yang melanggar ketentuan.

4. **Real-Time Dynamic Dashboard**
   - Ringkasan metrik finansial (Saldo Tersedia, Dana Escrow, Total Aset).
   - Counter total data interaktif (Total Transaksi, Room Sukses, Rasio Keberhasilan).
   - Grafik statistik visual tren volume transaksi mingguan dan distribusi status transaksi.
   - Log aktivitas transaksi terkini secara langsung dari API backend.

5. **Security & Role-Based Access Control (RBAC)**
   - Autentikasi JWT stateless dengan refresh token rotation dan blacklist token terintegrasi Redis.
   - Pengamanan sandi menggunakan *Argon2id* / *Bcrypt* dan hashing PIN terisolasi.
   - Pemisahan hak akses berbasis peran (`USER`, `MODERATOR`, `ADMIN`).

---

## 🛠️ Tech Stack Used

| Layer | Teknologi | Deskripsi / Catatan |
|---|---|---|
| **Frontend Framework** | SvelteKit 2.x | Svelte 5 runes, TypeScript, SSR/SPA Hybrid |
| **Styling & Design** | TailwindCSS 3.x | *Warm Editorial Design System* (Cream `#faf9f5`, Coral `#cc785c`, Dark `#181715`) |
| **Backend Services** | Quarkus 3.x | Java 21, RESTEasy Reactive, Hibernate Reactive Panache, SmallRye Mutiny |
| **API Gateway** | Kong 3.6 | DB-less Declarative Gateway, Auth & CORS Plugins, Rate Limiting, Request ID |
| **Database** | PostgreSQL 15 | 5 Domain Schemas (`auth`, `wallet`, `room`, `chat`, `dispute`), 3NF Normalized |
| **Cache & Session** | Redis 7 | Token Blacklist (Insta-Ban), Rate Limiting |
| **Message Broker** | RabbitMQ 3.x | Event-driven async messaging antar microservice |
| **API Docs** | SmallRye OpenAPI / Swagger | Dokumentasi endpoint interaktif bawaan Quarkus |
| **Containerization** | Docker & Docker Compose | Multi-container orchestration untuk seluruh ekosistem |

---

## 📁 Monorepo Directory Structure

```
EyesOfPriestessV2/
├── Docs/                               # Dokumentasi Teknis & Spesifikasi Sistem
│   ├── 01-architecture-overview.md     # Arsitektur sistem, topologi jaringan & flowchart
│   ├── 02-backend-api-spec.md          # Spesifikasi lengkap REST API & WebSocket
│   ├── 03-database-schema.md           # DDL PostgreSQL 5 domain schema & constraint
│   ├── 04-frontend-spec.md             # Panduan desain UI, token warna, & rute SvelteKit
│   ├── 05-project-setup.md             # Panduan setup environment & konfigurasi server
│   └── 06-feature-specifications.md    # Business rules, state machine room, & use cases
├── backend/                            # Sanctum Microservices (Quarkus Java 21)
│   ├── auth-service/                   # Port 8081: Identity, Auth JWT, Security PIN, RBAC
│   ├── wallet-service/                 # Port 8082: Saldo, Mutasi, Top-up, Withdraw, Bank Accounts
│   ├── room-service/                   # Port 8083: Escrow Covenant Room Lifecycle & State Machine
│   ├── chat-service/                   # Port 8084: Real-time In-Room WebSocket Messaging
│   ├── dispute-service/                # Port 8085: Penanganan Sengketa & Panel Keputusan Arbiter
│   ├── init-scripts/                   # PostgreSQL Initialization & Seeding Scripts
│   │   ├── 01-init-schemas.sql         # DDL Pembuatan Schema & Tabel Utama
│   │   └── 02-seed-data.sql            # Seeding 20+ records realistis per tabel
│   └── pom.xml                         # Root Maven multi-module configuration
├── frontend/                           # SvelteKit 2.x Web Application
│   ├── src/
│   │   ├── lib/                        # Shared components, stores, utilities, & API client
│   │   └── routes/
│   │       ├── (auth)/                 # Public: /login, /register, /forgot-password, /reset-password
│   │       ├── (app)/                  # Private: /dashboard, /wallet, /rooms, /history, /chat
│   │       │   └── admin/              # RBAC Protected: /admin (Dashboard, Disputes, Sanctions)
│   │       └── +error.svelte           # Unified Error Handling: 401, 403, 404, 500
│   ├── package.json
│   └── vite.config.ts
├── kong/                               # Kong API Gateway Config
│   └── kong.yaml                       # DB-less declarative routing & plugins
├── docker-compose.yml                  # Full stack orchestration (DB, Cache, Broker, Gateway)
└── README.md                           # Dokumentasi utama proyekan
```

---

## ⚙️ Installation & Setup Instructions (Local Running)

### Prasyarat Sistem
- **Docker** (v24.0+) & **Docker Compose** (v2.20+)
- **Node.js** (v20 LTS atau lebih baru) & **npm**
- **JDK 21** (Temurin / GraalVM) & **Maven 3.9+** *(opsional jika menjalankan backend langsung via Docker)*

---

### Langkah 1: Kloning Repositori & Persiapan Environment
```bash
git clone https://github.com/RichkyRahmadan/Eyes-Of-Priestess-V2.git
cd Eyes-Of-Priestess-V2
```

---

### Langkah 2: Jalankan Infrastruktur & Database (Docker Compose)
Jalankan PostgreSQL, Redis, RabbitMQ, dan Kong API Gateway:
```bash
docker compose up -d postgres redis rabbitmq kong
```
> **Catatan Seeding:** Pada pertama kali Postgres menyala, container akan mengeksekusi otomatis:
> 1. `01-init-schemas.sql` — Membuat schema `auth`, `wallet`, `room`, `chat`, `dispute`.
> 2. `02-seed-data.sql` — Mengisi minimal 20 records realistis untuk semua tabel utama (Users, Wallets, Transactions, Bank Accounts, Rooms, Chat Messages, Disputes).

Untuk memastikan container berjalan sehat:
```bash
docker compose ps
```

---

### Langkah 3: Menjalankan Backend Services (Quarkus)
Anda dapat menjalankan backend service menggunakan mode development Quarkus:

```bash
# Terminal 1 — Auth Service (Port 8081)
cd backend/auth-service
mvn quarkus:dev

# Terminal 2 — Wallet Service (Port 8082)
cd backend/wallet-service
mvn quarkus:dev

# Terminal 3 — Room Service (Port 8083)
cd backend/room-service
mvn quarkus:dev
```
*Atau jalankan seluruh service backend via Docker Compose:*
```bash
docker compose up -d --build
```

---

### Langkah 4: Menjalankan Frontend (SvelteKit)
Buka terminal baru di root folder:
```bash
cd frontend
npm install
npm run dev
```

Aplikasi frontend siap diakses di: **`http://localhost:5173`**  
Kong API Gateway beroperasi di: **`http://localhost:8000/api/v1`**

---

## 🔑 Demo Account Credentials

Untuk keperluan demonstrasi, evaluasi, dan pengujian fitur aplikasi, gunakan akun bawaan yang telah disediakan:

| Peran | Email | Kata Sandi | PIN Transaksi | Deskripsi Penggunaan |
|---|---|---|---|---|
| **ADMIN** | `admin@eyesofpriestess.com` | `Password123!` | `123456` | Hak akses penuh: Akses `/admin`, memutus sengketa, banned akun, monitoring saldo platform. |
| **MODERATOR** | `moderator@eyesofpriestess.com` | `Password123!` | `123456` | Hak akses moderasi: Meninjau bukti sengketa dan obrolan. |
| **USER 1 (Pembeli)** | `budi.santoso@gmail.com` | `Password123!` | `123456` | Akun pembeli aktif dengan saldo Rp 15.500.000, memiliki room aktif & riwayat transaksi. |
| **USER 2 (Penjual)** | `siti.rahmawati@gmail.com` | `Password123!` | `123456` | Akun merchant terverifikasi dengan saldo Rp 8.250.000, rekening bank BCA & BRI terdaftar. |
| **USER 3 (Traders)** | `dimas.prasetyo@gmail.com` | `Password123!` | `123456` | Akun trader digital dengan saldo Rp 42.000.000 dan room sengketa aktif. |

*(Seluruh 20+ akun pengguna demo lainnya di skrip seeding memiliki kata sandi default `Password123!` dan PIN `123456`)*.

---

## 🏛️ System Architecture & Flowchart

Diagram topologi arsitektur dan alur interaksi layanan microservices dapat dilihat secara detail pada dokumen:
📖 **[`Docs/01-architecture-overview.md`](./Docs/01-architecture-overview.md)**

### Diagram Alur Escrow Room (Covenant Lifecycle)
```
[Pembeli & Penjual]
        │
        ▼ Buat Room Escrow
[Status: WAITING_PAYMENT]
        │
        ▼ Pembeli Bayar (Saldo Escrow Terkunci)
[Status: FUNDED]
        │
        ▼ Penjual Menyerahkan Akun/Barang/Bukti
[Status: DELIVERED]
        │
   ┌────┴──────────────────────────┐
   ▼ (Pembeli Puas / Auto-Release)  ▼ (Timbul Masalah)
[Status: COMPLETED]              [Status: DISPUTED]
(Dana Cair ke Penjual)                  │
                                        ▼ Arbiter Admin Menengahi
                                 [Refund / Release]
```

### OpenAPI / Swagger Documentation
Setiap microservice mengekspos dokumentasi OpenAPI interaktif yang dapat diakses di:
- Auth Service: `http://localhost:8081/q/swagger-ui`
- Wallet Service: `http://localhost:8082/q/swagger-ui`
- Room Service: `http://localhost:8083/q/swagger-ui`
- Dispute Service: `http://localhost:8085/q/swagger-ui`
- Melalui Gateway: `http://localhost:8000/api/v1/{service}/q/swagger-ui`
