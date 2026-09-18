# EyesOfPriestess — System Flowchart & Architecture Diagrams

Dokumen ini berisi diagram alur (*flowcharts*) dan arsitektur sistem berbasis sintaks **Mermaid** untuk proyek Capstone S1 **EyesOfPriestess — The Covenant Protocol**. Seluruh diagram dirancang dengan hierarki bertingkat searah (*strict single-direction downward flow*) untuk memastikan **tidak ada garis panah yang saling tumpang tindih (*zero-crossing lines*)**.

---

## 1. Arsitektur Topologi Sistem (Tiered Architecture Flowchart)

Diagram bertingkat yang mengalirkan request secara bersih dari Client Layer, API Gateway, Microservices, Messaging/Integrasi, hingga Database Persistence Layer tanpa garis yang bersilangan.

```mermaid
flowchart TD
    subgraph T1["1. CLIENT TIER"]
        CLIENT["SvelteKit SPA Client (:5173)<br/>• Svelte 5 Runes & Responsive UI<br/>• LocalStorage JWT Persistence"]
    end

    subgraph T2["2. API GATEWAY TIER"]
        GATEWAY["Kong API Gateway DB-less (:8000)<br/>• Reverse Proxy & Central Route Mapping<br/>• Rate-Limiting: 600 req/min | CORS Enabled"]
    end

    subgraph T3["3. MICROSERVICES LAYER (Quarkus 3.x Reactive)"]
        direction LR
        AUTH["Auth Service (:8081)<br/>JWT RSA256, User & PIN"]
        WALLET["Wallet Service (:8082)<br/>Balance, Ledger & Accounts"]
        ROOM["Room Service (:8083)<br/>Escrow Covenant Engine"]
        CHAT["Chat Service (:8084)<br/>In-Room Chat & WebSocket"]
        DISPUTE["Dispute Service (:8085)<br/>Arbitration & Evidence"]
    end

    subgraph T4["4. MESSAGING, CACHE & EXTERNAL SERVICES"]
        direction LR
        REDIS["Redis 7 (:6379)<br/>Token Blacklist & Rate Limit"]
        XENDIT["Xendit Payment Gateway API<br/>Virtual Account & QRIS Callback"]
        RABBIT["RabbitMQ 3.x (:5672)<br/>Event Topic Exchange: eop.events"]
    end

    subgraph T5["5. PERSISTENCE LAYER (PostgreSQL 15 :5435)"]
        direction LR
        DB_AUTH[("Schema auth<br/>users, credentials, sessions")]
        DB_WALLET[("Schema wallet<br/>wallets, transactions, bank_accounts")]
        DB_ROOM[("Schema room<br/>rooms, delivery_proofs")]
        DB_CHAT[("Schema chat<br/>chat_rooms, messages")]
        DB_DISPUTE[("Schema dispute<br/>disputes, admin_decisions")]
    end

    %% Tier 1 ke Tier 2
    CLIENT -->|"HTTP / REST API & WebSocket Handshake"| GATEWAY

    %% Tier 2 ke Tier 3 (Rute sejajar tanpa silang)
    GATEWAY -->|"/api/v1/auth/*"| AUTH
    GATEWAY -->|"/api/v1/wallet/*"| WALLET
    GATEWAY -->|"/api/v1/room/*"| ROOM
    GATEWAY -->|"/api/v1/chat/*"| CHAT
    GATEWAY -->|"/api/v1/dispute/*"| DISPUTE

    %% Tier 3 ke Tier 4 (Integrasi sejajar langsung)
    AUTH -->|"Blacklist Check & Rate Limits"| REDIS
    WALLET -->|"Invoice Order & Payment Webhook"| XENDIT
    ROOM -->|"Publish: Room.fulfilled & Room.broken"| RABBIT
    CHAT -->|"Publish Message Events"| RABBIT
    DISPUTE -->|"Publish: judgment.resolved"| RABBIT

    %% Tier 3 ke Tier 5 (Database Schemas terisolasi)
    AUTH --> DB_AUTH
    WALLET --> DB_WALLET
    ROOM --> DB_ROOM
    CHAT --> DB_CHAT
    DISPUTE --> DB_DISPUTE
```

---

## 2. Alur Transaksi Escrow Covenant (Escrow Room Lifecycle)

Flowchart siklus transaksi escrow mulai dari pembuatan room, penguncian saldo, pengiriman bukti serah terima, hingga penyelesaian transaksi atau sengketa mediasi.

```mermaid
flowchart TD
    START([Mulai Transaksi Escrow]) --> CREATE[Pembeli & Penjual Membuat Room Kesepakatan]
    CREATE --> WAITING[Room Dibuat Status: WAITING_PAYMENT<br/>Kanal Chat Terenkripsi Terbuka]
    
    WAITING --> CHECK_SALDO{Pengecekan Saldo Pembeli}
    CHECK_SALDO -- Saldo Cukup --> FUND_ACTION[Pembeli Konfirmasi Penguncian Dana + Input PIN 6 Digit]
    CHECK_SALDO -- Saldo Kurang --> TOPUP_ACTION[Pembeli Melakukan Top-Up Saldo via VA/QRIS]
    TOPUP_ACTION --> FUND_ACTION

    FUND_ACTION --> FUNDED_STATE[Dana Masuk ke Escrow Balance<br/>Room Status: FUNDED]
    
    FUNDED_STATE --> SELLER_SEND[Penjual Menyerahkan Akun / Barang ke Pembeli]
    SELLER_SEND --> UPLOAD_PROOF[Penjual Unggah Bukti Serah Terima JPG/PNG/PDF]
    
    UPLOAD_PROOF --> DELIVERED_STATE[Room Status: DELIVERED<br/>Timer Proteksi Penjual 24 Jam Berjalan]

    DELIVERED_STATE --> BUYER_CHECK{Pemeriksaan Kondisi Barang oleh Pembeli}

    %% Cabang 1: Transaksi Normal (Puas)
    BUYER_CHECK -- Sesuai / Puas --> CONFIRM_ACTION[Pembeli Konfirmasi Penerimaan + Input PIN 6 Digit]
    DELIVERED_STATE -- 24 Jam Tanpa Sengketa --> AUTO_RELEASE[Auto-Release Sistem]
    AUTO_RELEASE --> CONFIRM_ACTION

    CONFIRM_ACTION --> EVENT_FULFILLED[RabbitMQ Publish: Room.fulfilled]
    EVENT_FULFILLED --> RELEASE_PAYOUT[Wallet Service Melepas Saldo Escrow ke Dompet Penjual]
    RELEASE_PAYOUT --> COMPLETED_STATE[Room Status: COMPLETED<br/>Transaksi Selesai & Berikan Ulasan]
    COMPLETED_STATE --> END_SUCCESS([Transaksi Selesai])

    %% Cabang 2: Pengajuan Sengketa
    BUYER_CHECK -- Bermasalah / Tidak Sesuai --> DISPUTE_ACTION[Ajukan Mediasi: Buka Modal Sengketa & Lampirkan Bukti]
    DISPUTE_ACTION --> DISPUTED_STATE[Dana Escrow Dibekukan Total<br/>Room Status: DISPUTED]
    
    DISPUTED_STATE --> ARBITER_REVIEW[Arbiter / Admin Sanctum Meninjau Bukti Dokumen & Log Obrolan]
    ARBITER_REVIEW --> JUDGMENT_CHOICE{Keputusan Arbiter Admin}

    JUDGMENT_CHOICE -- Refund Pembeli --> RESOLVE_BUYER[Keputusan: REFUND_BUYER<br/>Dana Escrow Dikembalikan Penuh ke Pembeli]
    JUDGMENT_CHOICE -- Lepas ke Penjual --> RESOLVE_SELLER[Keputusan: RELEASE_TO_SELLER<br/>Dana Escrow Dilepaskan ke Penjual]

    RESOLVE_BUYER --> CLOSED_STATE[Room Status: REFUNDED / CLOSED]
    RESOLVE_SELLER --> CLOSED_STATE
    CLOSED_STATE --> END_DISPUTE([Sengketa Tuntas])
```

---

## 3. Alur Autentikasi & Keamanan Akses (JWT & RBAC Guard)

Diagram proses autentikasi akun baru, login kredensial, penerbitan sepasang token JWT asimetris, dan validasi hak akses pengguna vs admin.

```mermaid
flowchart TD
    START_AUTH([Akses Aplikasi]) --> CHK_TOKEN{Pengecekan Status Login Client}

    %% Jalur Belum Login
    CHK_TOKEN -- Belum Login --> AUTH_SCREEN[Tampilkan Halaman Login / Registrasi]
    
    AUTH_SCREEN --> ACTION_TYPE{Aksi Pengguna}

    ACTION_TYPE -- Daftar Akun Baru --> FORM_REG[Input: Nama, Email, No HP, Password, PIN]
    FORM_REG --> VALIDATE_REG[Validasi Payload & Bcrypt Hash Kredensial]
    VALIDATE_REG --> INSERT_DB[Simpan ke auth.users & Buat Dompet Baru di wallet.wallets]
    INSERT_DB --> REG_DONE[Registrasi Berhasil HTTP 201] --> FORM_LOGIN

    ACTION_TYPE -- Masuk Akun --> FORM_LOGIN[Input Email/No HP & Password]
    FORM_LOGIN --> VERIFY_PASS[Verifikasi Password Hash di auth.credentials]
    
    VERIFY_PASS --> PASS_CHECK{Kredensial Cocok?}
    PASS_CHECK -- Salah --> ERR_LOGIN[HTTP 401: Kredensial Tidak Sah / Akun Terkunci]
    
    PASS_CHECK -- Benar --> ISSUE_TOKEN[Terbitkan Token JWT RSA256 & Refresh Token JTI]
    ISSUE_TOKEN --> STORE_JWT[Simpan Access Token di LocalStorage Client]
    STORE_JWT --> REDIRECT_DASH[Arahkan Pengguna ke /dashboard]

    %% Jalur Sudah Login
    CHK_TOKEN -- Sudah Memiliki Token --> CALL_API[Request Endpoint Terproteksi]
    CALL_API --> CHECK_REDIS[Periksa JTI di Redis Token Blacklist]
    
    CHECK_REDIS --> BL_CHECK{Terdaftar di Blacklist?}
    BL_CHECK -- Ya --> LOGOUT_FORCE[HTTP 401: Sesi Dicabut ➔ Auto Logout]
    
    BL_CHECK -- Tidak --> CHECK_ROLES{Evaluasi Hak Akses Role}
    CHECK_ROLES -- Role: USER --> USER_PAGES[Izinkan Akses: Dashboard, Dompet, Room, Obrolan]
    CHECK_ROLES -- Role: ADMIN / ORACLE --> ADMIN_PAGES[Izinkan Akses: Dashboard Admin, Mediasi Sengketa, Ban Akun]
    
    USER_PAGES -- Percobaan Akses ke /admin/* --> FORBIDDEN[HTTP 403: Akses Ditolak Khusus Administrator]
```

---

## 4. Alur Pembayaran & Webhook Payment Gateway (Xendit)

Diagram alur permintaan top-up, pembuatan invoice pembayaran, verifikasi token callback, dan jaminan idempotensi saldo.

```mermaid
flowchart TD
    T_START([Pengguna Mengisi Saldo]) --> T_FORM[Buka Menu Top-Up: Pilih Nominal & Metode VA / QRIS]
    T_FORM --> T_POST[POST /api/v1/wallet/topup]
    
    T_POST --> T_XENDIT_API[Wallet Service Memanggil API Xendit Sandbox]
    T_XENDIT_API --> T_INVOICE[Xendit Menghasilkan Nomor VA / QR String Pembayaran]
    
    T_INVOICE --> T_SAVE_DB[Simpan Order di wallet.topup_orders Status: PENDING]
    T_SAVE_DB --> T_UI_DISPLAY[Tampilkan Nomor Virtual Account & Timer Kedaluwarsa di Layar]
    
    T_UI_DISPLAY --> T_USER_PAY[Nasabah Melakukan Transfer Bank / Scan QRIS via M-Banking]
    T_USER_PAY --> T_BANK_SETTLE[Sistem Perbankan Mengonfirmasi Dana Masuk ke Rekening Escrow]
    
    T_BANK_SETTLE --> T_WEBHOOK_CALL[Xendit Mengirim Callback Webhook: POST /api/v1/wallet/webhook]
    T_WEBHOOK_CALL --> T_VERIFY_TOKEN{Validasi Header X-Callback-Token}
    
    T_VERIFY_TOKEN -- Tidak Valid --> T_REJECT[HTTP 403: Forbidden Callback Ditolak]
    
    T_VERIFY_TOKEN -- Valid --> T_IDEMPOTENCY{Pengecekan Kunci Idempotensi di Redis}
    T_IDEMPOTENCY -- Sudah Pernah Diproses --> T_ACK[HTTP 200: Abaikan Duplikasi Callback]
    
    T_IDEMPOTENCY -- Request Baru --> T_DB_TX[Buka Database Transaction PostgreSQL]
    T_DB_TX --> T_UPDATE_ORDER[Update Status Order Menjadi PAID / SUCCESS]
    T_UPDATE_ORDER --> T_CREDIT_WALLET[Tambah Saldo Tersedia di wallet.wallets]
    T_CREDIT_WALLET --> T_LEDGER[Catat Mutasi di wallet.transactions: TOPUP / IN]
    T_LEDGER --> T_COMMIT[Commit Database Transaction & Simpan Idempotency Key]
    
    T_COMMIT --> T_REALTIME[Push Notifikasi Saldo Masuk ke Dashboard Pengguna]
    T_REALTIME --> T_END([Saldo Masuk & Siap Digunakan])
```

---

## 5. Alur Mediasi & Putusan Sengketa (Dispute Resolution Flow)

Diagram alur terperinci penanganan perselisihan transaksi escrow di bawah wewenang *High Oracle Administrator*.

```mermaid
flowchart TD
    D_START([Pengguna Mengalami Masalah]) --> D_MODAL[Klik 'Laporkan Sengketa' di Halaman Room]
    D_MODAL --> D_INPUT[Pilih Kategori Masalah & Tuliskan Kronologi Rinci]
    D_INPUT --> D_UPLOAD[Lampirkan Bukti Tangkapan Layar / Dokumen PDF via FileUpload]
    D_UPLOAD --> D_SUBMIT[Kirim Sengketa: POST /api/v1/room/{id}/dispute]

    D_SUBMIT --> D_FREEZE[Dana Escrow Dibekukan Total & Room Berstatus: DISPUTED]
    D_FREEZE --> D_EVENT[Publish Event: Room.broken Melalui RabbitMQ]
    
    D_EVENT --> D_CASE[Dispute Service Membuka Kasus Baru di dispute.disputes]
    D_CASE --> D_QUEUE[Kasus Masuk ke Antrean Mediasi /admin#disputes]
    
    D_QUEUE --> D_ADMIN_VIEW[Arbiter Administrator Memeriksa Berkas Kasus]
    D_ADMIN_VIEW --> D_ANALYZE[Pemeriksaan: Bukti Serah Terima, Log Obrolan, dan Kredensial]
    
    D_ANALYZE --> D_DECISION{Pertimbangan & Putusan Arbiter}

    D_DECISION -- Pelanggaran oleh Penjual --> D_REFUND[Eksekusi Putusan: REFUND_BUYER]
    D_DECISION -- Kewajiban Penjual Terbukti Sah --> D_RELEASE[Eksekusi Putusan: RELEASE_TO_SELLER]

    D_REFUND --> D_PUB_RESOLVED[Publish Event: judgment.resolved ke RabbitMQ]
    D_RELEASE --> D_PUB_RESOLVED

    D_PUB_RESOLVED --> D_WALLET_SETTLE[Wallet Service Mengeksekusi Rekonsiliasi Saldo]
    D_WALLET_SETTLE --> D_TRANSFER_WINNER[Pencairan Saldo Otomatis ke Dompet Pihak yang Berhak]
    
    D_TRANSFER_WINNER --> D_CLOSE_CASE[Kasus Status: RESOLVED | Room Status: CLOSED]
    D_CLOSE_CASE --> D_NOTIF[Kirim Toast & Notifikasi Keputusan Resmi ke Kedua Pengguna]
    D_NOTIF --> D_END([Sengketa Ditutup Adil])
```
