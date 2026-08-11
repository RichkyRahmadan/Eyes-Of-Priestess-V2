# EyesOfPriestess — Feature Specifications

> **Version:** 1.0  
> **Project:** EyesOfPriestess — Escrow E-Wallet for P2P Marketplace  
> **Design System:** Warm Editorial (Cream Canvas + Coral Accent + Slab-Serif)

---

## Feature Matrix

| # | Feature | Category | Priority | Status |
|---|---------|----------|----------|--------|
| 1 | User Registration & Login | Auth | P0 | Planned |
| 2 | JWT Authentication with Refresh Token | Auth | P0 | Planned |
| 3 | 6-Digit PIN Setup & Verification | Auth | P0 | Planned |
| 4 | Insta-Ban System (Redis Blacklist) | Auth | P0 | Planned |
| 5 | Password Reset Flow | Auth | P1 | Planned |
| 6 | KTP Verification (Basic) | Auth | P2 | Planned |
| 7 | Wallet Balance Display | Wallet | P0 | Planned |
| 8 | Top-up via Virtual Account (Midtrans/Xendit) | Wallet | P0 | Planned |
| 9 | Withdraw to Bank Account | Wallet | P1 | Planned |
| 10 | P2P Direct Transfer | Wallet | P0 | Planned |
| 11 | Transaction History with Filters | Wallet | P0 | Planned |
| 12 | Bank Account Management | Wallet | P1 | Planned |
| 13 | Escrow Room Creation | Room | P0 | Planned |
| 14 | Room Funding (Buyer) | Room | P0 | Planned |
| 15 | Delivery Confirmation (Seller) | Room | P0 | Planned |
| 16 | Receipt Confirmation (Buyer) | Room | P0 | Planned |
| 17 | Auto-Release Timeout | Room | P0 | Planned |
| 18 | Room Cancellation | Room | P1 | Planned |
| 19 | Room Timeline / Audit Trail | Room | P1 | Planned |
| 20 | In-Room Chat (WebSocket) | Chat | P0 | Planned |
| 21 | Chat Message History | Chat | P0 | Planned |
| 22 | File/Image Sharing in Chat | Chat | P1 | Planned |
| 23 | Read Receipts | Chat | P2 | Planned |
| 24 | Dispute Filing | Dispute | P0 | Planned |
| 25 | Evidence Upload | Dispute | P0 | Planned |
| 26 | Admin Dispute Resolution | Dispute | P0 | Planned |
| 27 | Refund / Release Decision | Dispute | P0 | Planned |
| 28 | Push Notifications (WebSocket) | System | P1 | Planned |
| 29 | Dashboard with Summary | UI | P0 | Planned |
| 30 | Responsive Web Design | UI | P0 | Planned |
| 31 | Landing Page (Marketing) | UI | P1 | Planned |
| 32 | Admin Panel | Admin | P1 | Planned |

---

## 1. Authentication Features

### F-001: User Registration

**User Story:**  
Sebagai pengguna baru, saya ingin mendaftar akun agar dapat menggunakan platform EyesOfPriestess.

**Acceptance Criteria:**
- [ ] User dapat mendaftar dengan email, password, nama lengkap, nomor telepon, dan username
- [ ] Email harus valid dan unik
- [ ] Username harus unik, 3-50 karakter, alphanumeric + underscore
- [ ] Password minimal 8 karakter, mengandung huruf besar, huruf kecil, dan angka
- [ ] Nomor telepon validasi format Indonesia (+62)
- [ ] Setelah registrasi, wallet otomatis dibuat dengan balance 0
- [ ] Email verifikasi dikirim (simulated/mock untuk academic)

**UI/UX Notes:**
- Form di tengah halaman dengan card `surface-card` background
- Input fields dengan hairline border, focus ring coral
- Error messages muncul di bawah field dengan `text-error`
- Success state redirect ke halaman set PIN

---

### F-002: User Login

**User Story:**  
Sebagai pengguna terdaftar, saya ingin login agar dapat mengakses akun saya.

**Acceptance Criteria:**
- [ ] User dapat login dengan email dan password
- [ ] JWT access token (15 menit) dan refresh token (7 hari) diterima
- [ ] Failed login > 5 kali dalam 1 menit = rate limit 15 menit
- [ ] Jika akun di-ban, login langsung ditolak dengan pesan yang jelas
- [ ] "Remember me" option memperpanjang refresh token

**UI/UX Notes:**
- Login card sama style dengan register
- Link "Lupa password?" di bawah form
- Loading state pada tombol login

---

### F-003: 6-Digit PIN

**User Story:**  
Sebagai pengguna, saya ingin mengatur PIN 6 digit agar setiap transaksi kritis memerlukan verifikasi tambahan.

**Acceptance Criteria:**
- [ ] PIN wajib di-set sebelum melakukan transaksi pertama kali
- [ ] PIN terdiri dari 6 digit angka
- [ ] PIN di-hash dengan Argon2
- [ ] 3x salah PIN = lock 15 menit
- [ ] PIN dapat diubah dengan memasukkan PIN lama
- [ ] History perubahan PIN disimpan (security audit)

**UI/UX Notes:**
- PIN input: 6 kotak terpisah, auto-focus next
- Numeric keypad virtual (mobile-friendly)
- Shake animation saat PIN salah

---

### F-004: Insta-Ban System

**User Story:**  
Sebagai admin, saya ingin dapat mem-ban akun secara instan tanpa menunggu JWT expire agar pelanggaran dapat dihentikan segera.

**Acceptance Criteria:**
- [ ] Admin dapat mem-ban user dengan reason dan tipe ban (FULL, TRANSACTION_ONLY, ROOM_ONLY)
- [ ] Ban langsung efektif di semua service via Redis blacklist
- [ ] API Gateway memeriksa blacklist pada setiap request (< 1ms)
- [ ] Token JWT yang valid tetapi user di-ban tetap ditolak
- [ ] User yang di-ban mendapat pesan error spesifik saat mencoba request
- [ ] Ban dapat di-unban oleh admin
- [ ] Event `user.banned` dipublish ke RabbitMQ untuk propagation

**UI/UX Notes:**
- Admin panel: tabel user dengan action ban (modal confirmation)
- Ban badge: coral pill pada profil user yang di-ban
- Toast notification saat ban/unban berhasil

---

## 2. Wallet Features

### F-005: Balance Display

**User Story:**  
Sebagai pengguna, saya ingin melihat saldo wallet saya yang tersedia dan yang tertahan di escrow.

**Acceptance Criteria:**
- [ ] Tampilkan available balance, escrow balance, dan total balance
- [ ] Update real-time saat ada transaksi baru (via WebSocket)
- [ ] Format currency IDR dengan pemisah ribuan
- [ ] Last updated timestamp

**UI/UX Notes:**
- Balance card: `display-md` Cormorant Garamond number
- Available balance: cream card
- Escrow balance: `surface-card` dengan icon lock
- Total balance: dark navy card (`surface-dark`) dengan cream text

---

### F-006: Top-up via Virtual Account

**User Story:**  
Sebagai pengguna, saya ingin mengisi saldo wallet melalui virtual account bank.

**Acceptance Criteria:**
- [ ] User memilih metode top-up: Virtual Account, E-Wallet, atau Retail
- [ ] Untuk VA: pilih bank (BCA, BNI, BRI, Mandiri, Permata)
- [ ] Sistem generate VA number via Midtrans/Xendit sandbox
- [ ] User membayar ke VA dalam waktu 24 jam
- [ ] Callback dari payment gateway otomatis update balance
- [ ] Status tracking: PENDING → PAID → SUCCESS
- [ ] Notifikasi WebSocket saat top-up berhasil

**UI/UX Notes:**
- Top-up flow: stepper (Pilih Metode → Detail → Bayar → Selesai)
- VA number dengan tombol copy
- Countdown timer untuk expiry
- Success animation (confetti) saat top-up berhasil

---

### F-007: Withdraw to Bank

**User Story:**  
Sebagai pengguna, saya ingin menarik saldo ke rekening bank.

**Acceptance Criteria:**
- [ ] User memilih rekening bank yang tersimpan
- [ ] Input amount (minimal Rp 10.000)
- [ ] Biaya admin ditampilkan transparan
- [ ] Konfirmasi PIN sebelum submit
- [ ] Status tracking: PENDING → PROCESSING → SUCCESS/FAILED
- [ ] Estimasi waktu kedatangan dana (1-2 hari kerja)

**UI/UX Notes:**
- Withdraw form dengan summary card (amount, fee, net amount)
- Fee breakdown transparan
- Bank account selector dengan logo bank

---

### F-008: P2P Direct Transfer

**User Story:**  
Sebagai pengguna, saya ingin transfer saldo ke user EyesOfPriestess lain secara instan.

**Acceptance Criteria:**
- [ ] Transfer by username atau email
- [ ] Validasi penerima exists dan active
- [ ] Input amount dan note (opsional)
- [ ] Konfirmasi PIN
- [ ] Transfer instan (tidak perlu escrow)
- [ ] Notifikasi real-time ke penerima

**UI/UX Notes:**
- Transfer form dengan user search (dropdown)
- Confirmation modal dengan detail transfer
- Success toast dengan tombol "Lihat Detail"

---

### F-009: Transaction History

**User Story:**  
Sebagai pengguna, saya ingin melihat riwayat transaksi saya dengan filter dan pencarian.

**Acceptance Criteria:**
- [ ] List transaksi dengan pagination (20 per page)
- [ ] Filter by type: Top-up, Withdraw, Transfer, Escrow
- [ ] Filter by status: Success, Pending, Failed
- [ ] Filter by date range
- [ ] Search by description atau counterparty name
- [ ] Export ke CSV (P2)

**UI/UX Notes:**
- Table/list dengan icon type (color-coded)
- IN = green (`text-success`), OUT = coral (`text-primary`)
- Status badge: pill style
- Filter bar sticky di atas list

---

## 3. Room Escrow Features

### F-010: Create Escrow Room

**User Story:**  
Sebagai seller, saya ingin membuat room escrow untuk transaksi dengan buyer.

**Acceptance Criteria:**
- [ ] Seller input: title, description, item category, price
- [ ] Seller input buyer username/email (atau generate link invite)
- [ ] Room code auto-generate: EOP-XXX123 format
- [ ] Fee dihitung otomatis (1% dari item price, min Rp 2.500)
- [ ] Chat room otomatis dibuat
- [ ] Notifikasi ke buyer

**UI/UX Notes:**
- Create room form: stepper atau single page
- Price input dengan IDR formatting
- Category selector: icon + label
- Room code displayed prominently (copyable)

---

### F-011: Fund Room (Buyer)

**User Story:**  
Sebagai buyer, saya ingin mendanai room escrow agar transaksi dapat berlangsung.

**Acceptance Criteria:**
- [ ] Buyer melihat detail room dan total amount (price + fee)
- [ ] Konfirmasi PIN
- [ ] Dana dipindahkan dari available balance ke escrow balance
- [ ] Status room berubah ke FUNDED
- [ ] Seller menerima notifikasi
- [ ] Timeline diupdate

**UI/UX Notes:**
- Funding confirmation modal dengan detail breakdown
- Balance check: warning jika insufficient balance
- Success animation

---

### F-012: Delivery Confirmation (Seller)

**User Story:**  
Sebagai seller, saya ingin mengkonfirmasi pengiriman barang/jasa dengan bukti.

**Acceptance Criteria:**
- [ ] Seller upload proof (image, video, document, atau link)
- [ ] Optional delivery notes
- [ ] Multiple proof files allowed (max 5, 5MB each)
- [ ] Status room berubah ke DELIVERED
- [ ] Auto-release timer mulai berjalan (default 24 jam)
- [ ] Buyer menerima notifikasi

**UI/UX Notes:**
- File upload drag-and-drop area
- Preview thumbnail untuk images
- Notes textarea

---

### F-013: Receipt Confirmation (Buyer)

**User Story:**  
Sebagai buyer, saya ingin mengkonfirmasi penerimaan barang/jasa.

**Acceptance Criteria:**
- [ ] Buyer melihat delivery proofs
- [ ] Konfirmasi PIN
- [ ] Optional: rating (1-5) dan review
- [ ] Dana dilepas ke seller (minus fee)
- [ ] Status room berubah ke COMPLETED
- [ ] Review disimpan dan ditampilkan pada profil seller

**UI/UX Notes:**
- Star rating component (interactive)
- Review textarea
- Confirmation modal: "Apakah Anda yakin barang sudah diterima?"

---

### F-014: Auto-Release Timeout

**User Story:**  
Sebagai sistem, saya ingin otomatis melepaskan dana ke seller jika buyer tidak merespons dalam waktu tertentu.

**Acceptance Criteria:**
- [ ] Timer berjalan sejak status DELIVERED
- [ ] Default: 24 jam (dapat di-extend oleh buyer, max 72 jam total)
- [ ] Background job (Quarkus Scheduler) cek setiap 5 menit
- [ ] Jika timeout tercapai: auto-release ke seller
- [ ] Notifikasi ke both parties
- [ ] Timeline diupdate dengan actor "SYSTEM"

**UI/UX Notes:**
- Countdown timer pada room detail (prominent)
- Warning color saat < 1 jam tersisa
- Extend button dengan modal confirmation

---

### F-015: Open Dispute

**User Story:**  
Sebagai buyer atau seller, saya ingin membuka dispute jika ada masalah dengan transaksi.

**Acceptance Criteria:**
- [ ] Dispute dapat dibuka oleh buyer atau seller
- [ ] Hanya dapat dibuka saat status FUNDED atau DELIVERED
- [ ] Input: reason, description, evidence files
- [ ] Status room berubah ke DISPUTED
- [ ] Dana tetap di-hold sampai dispute resolved
- [ ] Admin menerima notifikasi

**UI/UX Notes:**
- Dispute button: warning style (`text-warning`)
- Form dengan reason dropdown + custom description
- Evidence upload (sama dengan delivery proof)

---

## 4. Chat Features

### F-016: In-Room Chat

**User Story:**  
Sebagai buyer/seller, saya ingin berkomunikasi dalam room escrow secara real-time.

**Acceptance Criteria:**
- [ ] WebSocket connection untuk real-time messaging
- [ ] Message types: TEXT, IMAGE, FILE, SYSTEM
- [ ] System messages auto-generated saat status room berubah
- [ ] Message history tersimpan dan dapat di-load
- [ ] Soft delete (tampil "pesan dihapus")

**UI/UX Notes:**
- Dark navy chat area (`surface-dark`) — product chrome feel
- Message bubbles: buyer (coral tint), seller (cream tint)
- System messages: centered, muted text, italic
- Typing indicator

---

### F-017: Chat Room List

**User Story:**  
Sebagai pengguna, saya ingin melihat daftar chat room saya.

**Acceptance Criteria:**
- [ ] List semua chat rooms yang diikuti user
- [ ] Sort by last message time (newest first)
- [ ] Unread count badge
- [ ] Last message preview
- [ ] Escrow status indicator

**UI/UX Notes:**
- WhatsApp-style list: avatar, name, preview, time, badge
- Search bar di atas list
- Active room highlight: `surface-card` background

---

## 5. Dispute Resolution Features

### F-018: Admin Dispute Dashboard

**User Story:**  
Sebagai admin, saya ingin melihat dan mengelola semua dispute.

**Acceptance Criteria:**
- [ ] Tabel dispute dengan filter: status, date range
- [ ] Detail view: evidence, chat history, room timeline
- [ ] Action: resolve dengan decision
- [ ] Decision options: RELEASE_TO_SELLER, REFUND_BUYER, PARTIAL_REFUND

**UI/UX Notes:**
- Admin panel: dark sidebar, cream content area
- Evidence gallery: lightbox view
- Decision form dengan reason textarea

---

### F-019: Dispute Resolution

**User Story:**  
Sebagai admin, saya ingin memutuskan dispute dan mengeksekusi hasil keputusan.

**Acceptance Criteria:**
- [ ] Admin memilih decision dan input reason
- [ ] Untuk PARTIAL_REFUND: input refund amount
- [ ] Konfirmasi modal sebelum eksekusi
- [ ] Eksekusi: create transaction (release/refund)
- [ ] Status room diupdate (COMPLETED atau REFUNDED)
- [ ] Notifikasi ke both parties

**UI/UX Notes:**
- Decision cards: 4 options dengan icon
- Amount input untuk partial refund (slider + number)
- Final confirmation dengan summary

---

## 6. System Features

### F-020: Push Notifications

**User Story:**  
Sebagai pengguna, saya ingin menerima notifikasi real-time untuk aktivitas penting.

**Acceptance Criteria:**
- [ ] Notifikasi via WebSocket
- [ ] Types: transaction, room status, chat message, dispute update
- [ ] Browser notification API (jika diizinkan)
- [ ] Notification center: list semua notifikasi
-- [ ] Mark as read / clear all

**UI/UX Notes:**
- Toast: slide in dari kanan atas, auto-dismiss 5s
- Notification bell icon dengan unread badge (coral)
- Notification drawer: slide from right

---

### F-021: Dashboard

**User Story:**  
Sebagai pengguna, saya ingin melihat ringkasan aktivitas saya dalam satu halaman.

**Acceptance Criteria:**
- [ ] Balance summary cards
- [ ] Quick action buttons
- [ ] Recent transactions (5 items)
- [ ] Active rooms (max 4)
- [ ] Unread chat count
- [ ] Notification summary

**UI/UX Notes:**
- Editorial layout: generous whitespace
- Cards: alternating cream and dark navy
- Numbers: Cormorant Garamond, large

---

### F-022: Landing Page

**User Story:**  
Sebagai pengunjung baru, saya ingin memahami apa itu EyesOfPriestess dan mengapa saya harus menggunakannya.

**Acceptance Criteria:**
- [ ] Hero section dengan headline editorial
- [ ] How it works (3 steps)
- [ ] Trust indicators / stats
- [ ] Use cases dengan tabs
- [ ] Security features
- [ ] CTA sections (coral + dark navy)
- [ ] Footer dengan links

**UI/UX Notes:**
- Pacing: cream → cream-card → dark-mockup → cream → coral-callout → dark-footer
- Headlines: Cormorant Garamond, negative tracking
- Product mockup cards: dark navy dengan code/data chrome

---

## 7. KPIs & Success Metrics

| Metric | Target |
|---|---|
| User Registration Conversion | > 60% dari landing page visit |
| Room Creation to Funding | > 70% dalam 24 jam |
| Dispute Rate | < 5% dari total transaksi |
| Auto-Release Trigger Rate | < 10% (artinya mostly manual confirm) |
| Average Resolution Time | < 48 jam untuk dispute |
| Page Load Time | < 2s untuk dashboard |
| WebSocket Latency | < 100ms untuk chat |

---

*End of Feature Specifications*
