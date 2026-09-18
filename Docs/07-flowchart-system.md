# EyesOfPriestess — System Flowchart & Architecture Diagrams

Dokumen ini berisi diagram alur (*flowcharts*) dan arsitektur sistem berbasis sintaks **Mermaid** untuk proyek Capstone S1 **EyesOfPriestess — The Covenant Protocol**.

---

## 1. Arsitektur Topologi Sistem & Komunikasi Layanan

Diagram arsitektur microservices terdistribusi terhubung melalui Kong API Gateway, asynchronous message broker (RabbitMQ), cache & rate-limiter (Redis), database multi-schema (PostgreSQL), dan integrasi payment gateway (Xendit).

```mermaid
flowchart TB
    subgraph CLIENT["Client Layer (Frontend)"]
        WEB["SvelteKit SPA Client<br/>(TailwindCSS + Svelte 5)<br/>:5173"]
    end

    subgraph GATEWAY["API Gateway Layer"]
        KONG["Kong API Gateway (DB-less)<br/>Reverse Proxy, CORS, Rate Limit, Auth<br/>:8000 (HTTP) / :8001 (Admin)"]
    end

    subgraph MICROSERVICES["Microservices Layer (Quarkus 3.x Reactive)"]
        AUTH["Auth Service (:8081)<br/>JWT RSA256, Identity & PIN"]
        WALLET["Wallet Service (:8082)<br/>Balance, Ledger, Xendit PG"]
        ROOM["Room Service (:8083)<br/>Escrow Covenant Engine"]
        CHAT["Chat Service (:8084)<br/>In-Room Messaging & WebSocket"]
        DISPUTE["Dispute Service (:8085)<br/>Arbitration & Evidence"]
    end

    subgraph BROKER["Event Bus & Cache"]
        RABBIT["RabbitMQ 3.x (:5672)<br/>Topic Exchange: eop.events"]
        REDIS["Redis 7 (:6379)<br/>Token Blacklist, Idempotency, Rate-Limits"]
    end

    subgraph STORAGE["Persistence Layer"]
        subgraph PG["PostgreSQL 15 (:5435)"]
            DB_AUTH[("Schema: auth<br/>users, credentials, sessions")]
            DB_WALLET[("Schema: wallet<br/>wallets, transactions, bank_accounts")]
            DB_ROOM[("Schema: room<br/>rooms, participants, delivery_proofs")]
            DB_CHAT[("Schema: chat<br/>chat_rooms, messages, reactions")]
            DB_DISPUTE[("Schema: dispute<br/>disputes, evidence, admin_decisions")]
        end
    end

    subgraph EXTERNAL["External Integrations"]
        XENDIT["Xendit Payment Gateway API<br/>(Virtual Account, QRIS, E-Wallet)"]
    end

    %% Client to Gateway
    WEB -->|"REST API (/api/v1/*)<br/>WebSocket (/api/v1/chat/*)"| KONG

    %% Gateway Routing
    KONG -->|"/api/v1/auth/*"| AUTH
    KONG -->|"/api/v1/wallet/*"| WALLET
    KONG -->|"/api/v1/room/*"| ROOM
    KONG -->|"/api/v1/chat/*"| CHAT
    KONG -->|"/api/v1/dispute/*"| DISPUTE

    %% Service to Message Broker
    ROOM -.->|"Publish: Room.created, Room.funded,<br/>Room.delivered, Room.fulfilled, Room.broken"| RABBIT
    RABBIT -.->|"Consume: Room.fulfilled"| WALLET
    RABBIT -.->|"Consume: Room.broken"| DISPUTE
    DISPUTE -.->|"Publish: judgment.resolved"| RABBIT
    RABBIT -.->|"Consume: judgment.resolved"| WALLET

    %% Service to Redis
    AUTH -->|"Blacklist check & rate-limits"| REDIS
    WALLET -->|"Idempotency keys"| REDIS

    %% Services to DB Schemas
    AUTH --> DB_AUTH
    WALLET --> DB_WALLET
    ROOM --> DB_ROOM
    CHAT --> DB_CHAT
    DISPUTE --> DB_DISPUTE

    %% External
    WALLET <-->|"Payment Orders & Webhooks"| XENDIT
```

---

## 2. Alur Transaksi Escrow Covenant (Escrow Room Lifecycle)

Flowchart siklus hidup transaksi escrow mulai dari pembuatan kesepakatan, penguncian dana, serah terima, hingga penyelesaian transaksi atau sengketa mediasi.

```mermaid
flowchart TD
    START([Mulai Transaksi]) --> CREATE[Pembeli atau Penjual Membuat Room Escrow]
    CREATE --> ROOM_INIT[Room Berstatus: WAITING_PAYMENT<br/>Kanal Chat Terenkripsi Dibuat Otomatis]
    
    ROOM_INIT --> CHECK_BUYER{Pembeli Memiliki<br/>Saldo Cukup?}
    CHECK_BUYER -- Tidak --> TOPUP_ACTION[Pembeli Melakukan Top-Up Saldo Dompet]
    TOPUP_ACTION --> CHECK_BUYER
    CHECK_BUYER -- Ya --> FUND[Pembeli Memasukkan PIN 6 Digit & Konfirmasi Kunci Dana]

    FUND --> LOCK_BALANCE[Sistem Memindahkan Dana:<br/>Available Balance ➔ Escrow Balance<br/>Room Berstatus: FUNDED]

    LOCK_BALANCE --> CHAT_COMMUNICATION[Kedua Pihak Berkomunikasi via In-Room Chat]
    CHAT_COMMUNICATION --> SELLER_DELIVER[Penjual Menyerahkan Akun/Barang &<br/>Mengunggah Bukti Serah Terima JPG/PNG/PDF]
    
    SELLER_DELIVER --> DELIVERED[Room Berstatus: DELIVERED<br/>Timer Otomatis 24 Jam Aktif]

    DELIVERED --> BUYER_INSPECT{Pembeli Memeriksa<br/>Kondisi Barang}

    %% Happy Path
    BUYER_INSPECT -- Sesuai / Puas --> BUYER_CONFIRM[Pembeli Konfirmasi Penerimaan<br/>Input PIN 6 Digit]
    BUYER_CONFIRM --> RELEASE_FUNDS[Event: Room.fulfilled Terkirim<br/>Dana Escrow Dilepas ke Saldo Penjual]
    RELEASE_FUNDS --> COMPLETED[Room Berstatus: COMPLETED<br/>Transaksi Berhasil & Beri Rating]
    COMPLETED --> FINISH([Selesai])

    %% Dispute Path
    BUYER_INSPECT -- Bermasalah / Tidak Sesuai --> OPEN_DISPUTE[Salah Satu Pihak Buka Sengketa:<br/>Input Alasan & Unggah Bukti Pendukung]
    DELIVERED -- Batas Waktu Habis Tanpa Respons --> AUTO_RELEASE[Auto-Release Proteksi Penjual]
    AUTO_RELEASE --> RELEASE_FUNDS

    OPEN_DISPUTE --> DISPUTED[Room Berstatus: DISPUTED<br/>Dana Tetap Terkunci di Escrow<br/>Kasus Masuk ke Antrean Arbiter Admin]

    DISPUTED --> ARBITRATION[Arbiter / Admin Meninjau Bukti & Chat Log]
    ARBITRATION --> DECISION{Keputusan Arbiter}

    DECISION -- Refund Pembeli --> REFUND_ACTION[Event: judgment.resolved<br/>Winner: BUYER<br/>Dana Escrow Dikembalikan ke Pembeli]
    DECISION -- Lepas ke Penjual --> RELEASE_ACTION[Event: judgment.resolved<br/>Winner: SELLER<br/>Dana Escrow Diberikan ke Penjual]
    DECISION -- Pembagian Adil Split --> SPLIT_ACTION[Event: judgment.resolved<br/>Winner: SPLIT<br/>Dana Dibagi Proporsional]

    REFUND_ACTION --> RESOLVED_CLOSE[Status Room: REFUNDED / CLOSED]
    RELEASE_ACTION --> RESOLVED_CLOSE
    SPLIT_ACTION --> RESOLVED_CLOSE
    RESOLVED_CLOSE --> FINISH
```

---

## 3. Alur Autentikasi, Keamanan JWT, & RBAC Guard

Flowchart proses registrasi, login, penerbitan token JWT asimetris RSA256, rotasi refresh token, dan pengecekan otorisasi Role-Based Access Control.

```mermaid
flowchart TD
    USER_REQ([Pengguna Mengakses Sistem]) --> IS_AUTH{Memiliki Token JWT Valid?}

    IS_AUTH -- Tidak --> LOGIN_REGISTER{Aksi Pengguna}
    
    LOGIN_REGISTER -- Register --> REG_FORM[Pengguna Isi Form:<br/>Email, No HP, Nama, Password, PIN 6 Digit]
    REG_FORM --> REG_VALIDATE{Validasi Input & Format}
    REG_VALIDATE -- Gagal --> REG_ERROR[Inline Feedback Error 400/422] --> REG_FORM
    REG_VALIDATE -- Berhasil --> HASH_PASS[Bcrypt Hash Password & PIN]
    HASH_PASS --> INSERT_USER[Simpan ke auth.users & wallet.wallets Otomatis]
    INSERT_USER --> REG_SUCCESS[Registrasi Sukses (HTTP 201) ➔ Arahkan ke Login]

    LOGIN_REGISTER -- Login --> LOGIN_FORM[Pengguna Input Email/No HP & Password]
    LOGIN_FORM --> VERIFY_PASS{Cek Hash Password di auth.credentials}
    VERIFY_PASS -- Salah --> INC_ATTEMPTS[Tambah Hitungan Gagal<br/>Jika >= 5 Kunci Akun Sementara] --> LOGIN_ERROR[HTTP 401: Kredensial Tidak Sah]
    VERIFY_PASS -- Benar --> GEN_JWT[Penerbitan Sepasang Token:<br/>1. Access Token RSA256 (Exp: 15 Menit)<br/>2. Refresh Token JTI (Exp: 7 Hari)]
    GEN_JWT --> SAVE_STORAGE[Simpan Token di LocalStorage Client]
    SAVE_STORAGE --> REDIRECT_DASH[Redirect Otomatis ke /dashboard]

    IS_AUTH -- Ya --> REQ_RESOURCE[Request Mengakses Endpoint Private]
    REQ_RESOURCE --> CHECK_BLACKLIST{Cek Token JTI di Redis Blacklist?}
    CHECK_BLACKLIST -- Masuk Blacklist/Insta-Ban --> FORBIDDEN_401[HTTP 401: Sesi Dibatalkan ➔ Logout]
    CHECK_BLACKLIST -- Aman --> CHECK_EXPIRY{Token Kedaluwarsa?}

    CHECK_EXPIRY -- Ya --> AUTO_REFRESH[Kirim Refresh Token ke /api/v1/auth/renew]
    AUTO_REFRESH --> REFRESH_OK{Refresh Token Sah?}
    REFRESH_OK -- Ya --> ISSUE_NEW[Terbitkan Access Token Baru] --> REQ_RESOURCE
    REFRESH_OK -- Tidak --> PURGE_SESSION[Purge Storage & Redirect ke /login]

    CHECK_EXPIRY -- Tidak --> RBAC_CHECK{Memeriksa Peran Pengguna}
    RBAC_CHECK -- Role: USER --> ALLOW_USER[Akses Halaman Pengguna: Dashboard, Dompet, Room, Chat]
    RBAC_CHECK -- Role: ADMIN --> ALLOW_ADMIN[Akses Penuh: Dashboard Admin, Mediasi Sengketa, Ban Akun]
    ALLOW_USER -- Mencoba Akses /admin/* --> DENY_403[HTTP 403 Forbidden: Akses Khusus Arbiter]
```

---

## 4. Alur Top-Up & Pembayaran Gateway (Xendit Lifecycle)

Flowchart integrasi pembayaran otomatis dari request order, pembuatan invoice Virtual Account / QRIS, proses pembayaran oleh nasabah, hingga konfirmasi webhook asinkron.

```mermaid
flowchart TD
    START_TOPUP([Pengguna Ingin Tambah Saldo]) --> OPEN_TOPUP[Buka Halaman /topup]
    OPEN_TOPUP --> INPUT_AMOUNT[Pilih Metode: Virtual Account / QRIS / E-Wallet<br/>Masukkan Nominal Top-Up]
    INPUT_AMOUNT --> POST_ORDER[POST /api/v1/wallet/topup]
    
    POST_ORDER --> CALL_XENDIT[Wallet Service Menghubungi API Xendit Sandbox]
    CALL_XENDIT --> XENDIT_RESP[Xendit Mengembalikan:<br/>Nomor VA / QR String / Invoice URL]
    XENDIT_RESP --> SAVE_PENDING[Simpan ke wallet.topup_orders (Status: PENDING)]
    SAVE_PENDING --> SHOW_PAYMENT[Tampilkan Detail Pembayaran & Countdown Kedaluwarsa di UI]

    SHOW_PAYMENT --> USER_PAY[Pengguna Melakukan Transfer Bank / Scan QRIS]
    USER_PAY --> XENDIT_DETECT[Sistem Bank / Xendit Mendeteksi Dana Masuk]
    
    XENDIT_DETECT --> WEBHOOK_POST[Xendit Mengirim Webhook Callback:<br/>POST /api/v1/wallet/webhook]
    WEBHOOK_POST --> VERIFY_TOKEN{Validasi X-Callback-Token}
    
    VERIFY_TOKEN -- Palsu / Tidak Sah --> REJECT_403[HTTP 403: Forbidden Callback]
    VERIFY_TOKEN -- Sah --> CHECK_IDEMPOTENCY{Cek Idempotency Key di Redis}

    CHECK_IDEMPOTENCY -- Sudah Diproses Sebelumnya --> ACK_SUCCESS[Acknowledge HTTP 200 (No-Op)]
    CHECK_IDEMPOTENCY -- Request Baru --> TX_BEGIN[Buka Database Transaction PostgreSQL]

    TX_BEGIN --> UPDATE_ORDER[Update Status Order: SUCCESS / PAID]
    UPDATE_ORDER --> CREDIT_WALLET[Tambah Saldo: wallet.wallets.available_balance]
    CREDIT_WALLET --> INSERT_LEDGER[Catat Transaksi: wallet.transactions (Type: TOPUP, Direction: IN)]
    INSERT_LEDGER --> COMMIT_TX[Commit Transaksi & Simpan Key di Redis]
    COMMIT_TX --> NOTIFY_USER[WebSocket / Realtime Update Saldo ke Frontend Pengguna]
    NOTIFY_USER --> SUCCESS_END([Saldo Masuk & Siap Digunakan])
```

---

## 5. Alur Mediasi Sengketa (Dispute & Arbitration Flow)

Flowchart penyelesaian perselisihan transaksi di bawah kendali *High Oracle Administrator* / *Arbiter Sanctum*.

```mermaid
flowchart TD
    DISPUTE_TRIGGER([Pembeli / Penjual Menemukan Masalah]) --> FORM_DISPUTE[Buka Modal 'Laporkan Sengketa' di Room Escrow]
    FORM_DISPUTE --> FILL_REASON[Pilih Kategori Kendala & Tulis Kronologi]
    FILL_REASON --> ATTACH_FILES[Unggah File Bukti screenshot/PDF via FileUpload]
    ATTACH_FILES --> SUBMIT_DISPUTE[POST /api/v1/room/{id}/dispute]

    SUBMIT_DISPUTE --> EVENT_BROKEN[Publish Event: Room.broken]
    EVENT_BROKEN --> DISPUTE_SERVICE[Dispute Service Menangkap Event]
    DISPUTE_SERVICE --> OPEN_CASE[Buka Kasus di dispute.disputes (Status: OPEN)]
    OPEN_CASE --> ROOM_STATUS_UPDATE[Room Berubah ke Status: DISPUTED<br/>Dana Escrow Dibekukan Total]

    ROOM_STATUS_UPDATE --> ADMIN_NOTIF[Notifikasi Masuk ke Antrean /admin#disputes]
    ADMIN_NOTIF --> ADMIN_REVIEW[Arbiter Admin Membuka Detail Kasus & Bukti]

    ADMIN_REVIEW --> INVESTIGATE[Arbiter Meneliti:<br/>1. Bukti Gambar & Dokumen Serah Terima<br/>2. Jejak Riwayat Pesan di In-Room Chat<br/>3. Kredensial & Batas Waktu]
    
    INVESTIGATE --> HEAR_PARTIES{Perlu Bukti Tambahan?}
    HEAR_PARTIES -- Ya --> REQUEST_MORE[Admin Mengirim Pesan Peringatan Resmi di Chat] --> INVESTIGATE
    HEAR_PARTIES -- Cukup --> JUDGMENT_ACTION[Arbiter Memilih Tindakan Resolusi]

    JUDGMENT_ACTION -- Refund Pembeli --> RESOLVE_BUYER[POST /api/v1/dispute/{id}/resolve<br/>Decision: REFUND_BUYER]
    JUDGMENT_ACTION -- Release Penjual --> RESOLVE_SELLER[POST /api/v1/dispute/{id}/resolve<br/>Decision: RELEASE_TO_SELLER]

    RESOLVE_BUYER --> RABBIT_RESOLVED[Publish Event: judgment.resolved]
    RESOLVE_SELLER --> RABBIT_RESOLVED

    RABBIT_RESOLVED --> WALLET_EXECUTE[Wallet Service Mengeksekusi Ledger]:
    WALLET_EXECUTE --> UNLOCK_FINANCE[Pencairan Dana Otomatis ke Dompet Pemenang Sengketa]
    UNLOCK_FINANCE --> CASE_CLOSED[Status Sengketa: RESOLVED & Status Room: COMPLETED/REFUNDED]
    CASE_CLOSED --> TOAST_NOTIF[Toast Notifikasi Hasil Keputusan Muncul di Akun Kedua Pengguna]
    TOAST_NOTIF --> END_DISPUTE([Kasus Selesai])
```
