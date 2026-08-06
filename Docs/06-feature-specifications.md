# EyesOfPriestess — Feature Specifications
## The Complete Chronicle of Features & User Stories

**Version:** 1.0  
**Platform:** Website (SvelteKit + Quarkus Microservices)  
**Scope:** Full MVP (Vault Core + Covenant Escrow + Communion + Judgment)

---

## 1. The Seal — Authentication & Authorization

### 1.1 Forge Identity (Registration)
**Priority:** P0  
**User Story:** Sebagai calon pilgrim, saya ingin menempa identitas dengan nomor HP agar bisa memasuki Sanctum.

**Acceptance Criteria:**
- [ ] Pilgrim mendaftar dengan nomor HP, nama lengkap, password, dan PIN 6 digit
- [ ] Validasi: nomor HP unik, format Indonesia (+62), minimal 10 digit
- [ ] Validasi: password minimal 8 karakter, mengandung huruf dan angka
- [ ] Validasi: PIN harus 6 digit numerik
- [ ] Omen (OTP) dikirim ke nomor HP (simulated: 123456 untuk development)
- [ ] Setelah verifikasi Omen, identitas aktif dan auto-login
- [ ] Treasury otomatis dibuat dengan saldo 0

**API Endpoints:**
- `POST /seal/forge`
- `POST /seal/omen/request`
- `POST /seal/omen/verify`

**Frontend:**
- [ ] Halaman `/rite/forge` dengan form step-by-step
- [ ] Halaman `/rite/attune` setelah verifikasi Omen
- [ ] Validasi real-time pada setiap field
- [ ] Loading state dan error handling

---

### 1.2 Rite of Return (Login)
**Priority:** P0  
**User Story:** Sebagai pilgrim, saya ingin melakukan Rite of Return dengan nomor HP dan password.

**Acceptance Criteria:**
- [ ] Login dengan nomor HP + password
- [ ] Sacred Seal (access: 15 menit) + Refresh Seal (7 hari) diterbitkan
- [ ] Device tracking: device_id dicatat
- [ ] Gagal 5x = account lock 30 menit
- [ ] Seal disimpan di localStorage
- [ ] Auto-redirect ke Observatory jika sudah login

**API Endpoints:**
- `POST /seal/rite`
- `POST /seal/renew`
- `POST /seal/sever`

**Frontend:**
- [ ] Halaman `/rite` dengan form phone + password
- [ ] "Remember me" option
- [ ] Redirect ke intended URL setelah login

---

### 1.3 PIN Sanctification
**Priority:** P0  
**User Story:** Sebagai pilgrim, saya ingin mengatur dan mengubah PIN agar transaksi saya aman.

**Acceptance Criteria:**
- [ ] PIN 6 digit wajib untuk semua transaksi kritis
- [ ] PIN di-hash dengan bcrypt (cost 12)
- [ ] Gagal PIN 5x = lock 30 menit
- [ ] Pilgrim dapat mengubah PIN dengan memasukkan PIN lama
- [ ] PIN tidak boleh sama dengan password login
- [ ] PIN confirmation pada: seal escrow, release, withdraw, top-up

**API Endpoints:**
- `PUT /seal/pin`

**Frontend:**
- [ ] PIN pad component (6 digit dots)
- [ ] Modal PIN confirmation sebelum transaksi kritis
- [ ] Halaman `/sanctum/profile` untuk ubah PIN

---

### 1.4 Sanction Seal — Insta-Ban
**Priority:** P1  
**User Story:** Sebagai oracle (admin), saya ingin mensanksi pilgrim secara instan tanpa menunggu Seal expired.

**Acceptance Criteria:**
- [ ] Oracle dapat mensanksi pilgrim via endpoint admin
- [ ] Semua active session pilgrim di-revoke
- [ ] Seal JTI dimasukkan ke Crystal (Redis) sanction list
- [ ] The Veil cek Crystal sebelum proses setiap request
- [ ] Pilgrim yang disanksi mendapat response 401 dengan code `SEAL_REVOKED`
- [ ] Event `pilgrim.sanctioned` dipublish ke RabbitMQ
- [ ] Pilgrim disanksi tidak bisa login kembali sampai unsanctioned

**API Endpoints:**
- `POST /seal/oracle/sanction`

**Backend:**
- [ ] Crystal sanction: `sanction:<jti>` → TTL = remaining expiry
- [ ] Session table: semua session di-set severed
- [ ] RabbitMQ event: `pilgrim.sanctioned`

---

### 1.5 KYC Attunement
**Priority:** P2  
**User Story:** Sebagai pilgrim, saya ingin melakukan attunement identitas untuk meningkatkan limit transaksi.

**Acceptance Criteria:**
- [ ] Pilgrim dapat submit NIK (16 digit)
- [ ] NIK di-hash, tidak disimpan plain text
- [ ] Status attunement: UNATTUNED → PENDING → ATTUNED/REJECTED
- [ ] ATTUNED = covenant score +0.5, limit transaksi naik
- [ ] Oracle panel untuk review (Phase 2)

**API Endpoints:**
- `PUT /seal/self/attune`
- `GET /seal/self`

---

## 2. The Vault — Wallet Core

### 2.1 Gaze Upon Treasury (Balance Inquiry)
**Priority:** P0  
**User Story:** Sebagai pilgrim, saya ingin melihat treasury saya yang tersedia dan yang di-seal di escrow.

**Acceptance Criteria:**
- [ ] Display: available treasury + sealed treasury + total treasury
- [ ] Update real-time setelah transaksi
- [ ] Currency: IDR (Rupiah Indonesia)
- [ ] Format: Rp 1.500.000,00
- [ ] Treasury tidak boleh negatif

**API Endpoints:**
- `GET /vault/treasury`

**Frontend:**
- [ ] Treasury card di Observatory dan Vault page
- [ ] Count-up animation saat treasury berubah
- [ ] Tooltip explaining sealed treasury

---

### 2.2 Make Offering (Top-up)
**Priority:** P0  
**User Story:** Sebagai pilgrim, saya ingin mengisi treasury via Virtual Account atau e-wallet.

**Acceptance Criteria:**
- [ ] Metode: VA (BCA, BNI, BRI, Mandiri), E-Wallet (GoPay, OVO, DANA), QRIS
- [ ] Minimal: Rp 10.000, Maksimal: Rp 10jt (unattuned), Rp 50jt (attuned)
- [ ] Generate VA number unik per offering
- [ ] VA expired dalam 24 jam
- [ ] Callback webhook dari Midtrans/Xendit
- [ ] Covenant-Key untuk prevent double offering
- [ ] Fee: 0% VA, 1.5% e-wallet

**API Endpoints:**
- `POST /vault/offering`
- `POST /vault/webhook/midtrans`

**Frontend:**
- [ ] Halaman `/sanctum/vault/offering`
- [ ] VA display dengan copy button
- [ ] Countdown timer expiry
- [ ] Status tracking: PENDING → ACCEPTED → COMPLETED

---

### 2.3 Withdrawal Ritual
**Priority:** P0  
**User Story:** Sebagai pilgrim, saya ingin menarik treasury ke rekening bank.

**Acceptance Criteria:**
- [ ] Withdraw ke bank (BCA, BNI, BRI, Mandiri)
- [ ] Minimal: Rp 50.000, Maksimal: Rp 25jt/hari
- [ ] Fee: Rp 6.500 per transaksi
- [ ] PIN confirmation wajib
- [ ] Rekening tersimpan untuk withdrawal berikutnya
- [ ] Status: PENDING → PROCESSING → COMPLETED

**API Endpoints:**
- `POST /vault/withdrawal`
- `GET /vault/banks`

**Frontend:**
- [ ] Halaman `/sanctum/vault/withdrawal`
- [ ] Dropdown bank selector
- [ ] Saved bank accounts list
- [ ] Summary: amount - fee = net
- [ ] PIN confirmation modal

---

### 2.4 P2P Tithing (Direct Transfer)
**Priority:** P0  
**User Story:** Sebagai pilgrim, saya ingin transfer treasury ke pilgrim lain secara instan.

**Acceptance Criteria:**
- [ ] Transfer via nomor HP atau username
- [ ] Validasi: penerima harus pilgrim aktif
- [ ] Minimal: Rp 1.000, Maksimal: Rp 10jt per transaksi
- [ ] Fee: 0% (promo) atau Rp 1.000 (normal)
- [ ] Instan: treasury penerima bertambah real-time
- [ ] PIN confirmation wajib
- [ ] Catatan opsional

**API Endpoints:**
- `POST /vault/tithing`

**Frontend:**
- [ ] Halaman `/sanctum/tithing`
- [ ] Auto-complete pencarian pilgrim
- [ ] Confirmation screen
- [ ] PIN confirmation modal
- [ ] Success screen

---

### 2.5 Read Chronicles (Transaction History)
**Priority:** P0  
**User Story:** Sebagai pilgrim, saya ingin melihat riwayat semua transaksi saya.

**Acceptance Criteria:**
- [ ] List semua transaksi: offering, withdrawal, tithing, escrow, fee
- [ ] Filter by type dan date range
- [ ] Pagination: 20 per page
- [ ] Sort: newest first
- [ ] Detail: amount, fee, net, status, reference, timestamp
- [ ] Treasury snapshot (before/after)

**API Endpoints:**
- `GET /vault/chronicles?page=&limit=&filter=&from=&to=`
- `GET /vault/chronicles/:id`

**Frontend:**
- [ ] Halaman `/sanctum/vault/chronicles`
- [ ] Chronicle item card dengan icon type + color
- [ ] Infinite scroll atau pagination
- [ ] Detail modal on click
- [ ] Empty void untuk pilgrim baru

---

## 3. The Covenant — Escrow Room

### 3.1 Forge Covenant
**Priority:** P0  
**User Story:** Sebagai initiator, saya ingin menempa covenant agar dana saya aman.

**Acceptance Criteria:**
- [ ] Initiator input: counterpart phone/username, item name, description, category, amount, deadline
- [ ] Validasi: counterpart harus pilgrim aktif, tidak boleh diri sendiri
- [ ] Validasi: amount > 0, minimal Rp 10.000
- [ ] Validasi: initiator treasury >= amount (hold dana langsung)
- [ ] Deadline: default 72 jam, max 168 jam
- [ ] Generate covenant code unik: CVN-XXXXX
- [ ] Dana di-hold dari available_treasury ke sealed_treasury
- [ ] Status: FORGED
- [ ] Notifikasi ke counterpart
- [ ] Covenant-Key untuk prevent double creation

**API Endpoints:**
- `POST /covenant`
- `GET /covenant?role=&status=`

**Frontend:**
- [ ] Halaman `/sanctum/covenant/forge` multi-step form
- [ ] Step 1: Cari counterpart
- [ ] Step 2: Detail item
- [ ] Step 3: Deadline + review
- [ ] Step 4: PIN confirmation
- [ ] Success: redirect ke covenant chamber

---

### 3.2 Accept/Reject Covenant (Counterpart)
**Priority:** P0  
**User Story:** Sebagai counterpart, saya ingin menerima atau menolak covenant.

**Acceptance Criteria:**
- [ ] Counterpart menerima notifikasi covenant baru
- [ ] Counterpart dapat review detail: item, amount, initiator covenant score
- [ ] Accept: status → ACCEPTED, deadline countdown mulai
- [ ] Reject: status → BROKEN, dana kembali ke initiator
- [ ] Tolak hanya saat status FORGED
- [ ] PIN confirmation untuk accept

**API Endpoints:**
- `POST /covenant/:id/accept`
- `POST /covenant/:id/reject`

**Frontend:**
- [ ] Covenant card di Observatory dengan action buttons
- [ ] Covenant chamber dengan Accept/Reject buttons
- [ ] Confirmation modal
- [ ] Notifikasi real-time via Communion

---

### 3.3 Fulfill Oath (Counterpart Deliver)
**Priority:** P0  
**User Story:** Sebagai counterpart, saya ingin mengirim barang dan upload bukti pengiriman.

**Acceptance Criteria:**
- [ ] Upload bukti: foto resi, screenshot, atau file
- [ ] Max file size: 5MB
- [ ] Supported: JPG, PNG, PDF
- [ ] Catatan pengiriman opsional
- [ ] Status → DELIVERED
- [ ] Initiator menerima notifikasi
- [ ] PIN confirmation wajib
- [ ] Auto-release countdown mulai

**API Endpoints:**
- `POST /covenant/:id/fulfill`

**Frontend:**
- [ ] Upload component dengan drag-and-drop
- [ ] Preview file
- [ ] Progress bar
- [ ] Delivery notes textarea
- [ ] PIN confirmation modal

---

### 3.4 Confirm Fulfillment (Initiator)
**Priority:** P0  
**User Story:** Sebagai initiator, saya ingin konfirmasi penerimaan agar dana release ke counterpart.

**Acceptance Criteria:**
- [ ] Initiator dapat konfirmasi terima barang
- [ ] Status → FULFILLED
- [ ] Dana release dari escrow ke counterpart treasury (minus fee 1%)
- [ ] Fee masuk ke treasury system
- [ ] Rating 1-5 dan review opsional
- [ ] PIN confirmation wajib
- [ ] Receipt generated
- [ ] Covenant score update: +0.1 untuk successful transaction

**API Endpoints:**
- `POST /covenant/:id/confirm`

**Frontend:**
- [ ] "Confirm Fulfillment" button di covenant chamber
- [ ] Rating stars component
- [ ] Review textarea
- [ ] PIN confirmation modal
- [ ] Success animation + receipt download

---

### 3.5 Auto-Release Timeout
**Priority:** P0  
**User Story:** Sebagai sistem, saya ingin auto-release dana jika initiator tidak merespons.

**Acceptance Criteria:**
- [ ] Cron job setiap jam cek covenant DELIVERED dengan deadline lewat
- [ ] Auto-release: dana ke counterpart (minus fee)
- [ ] Status → FULFILLED
- [ ] Event log: AUTO_RELEASED
- [ ] Notifikasi ke initiator dan counterpart
- [ ] Bisa di-disable jika judgment sedang berlangsung

**Backend:**
- [ ] Quarkus Scheduler: `@Scheduled(every = "1h")`
- [ ] Query: `SELECT * FROM covenants WHERE status = 'DELIVERED' AND deadline_at <= NOW()`
- [ ] Atomic transaction

---

### 3.6 Sever Covenant (Initiator)
**Priority:** P1  
**User Story:** Sebagai initiator, saya ingin membatalkan covenant sebelum counterpart accept.

**Acceptance Criteria:**
- [ ] Sever hanya saat status FORGED
- [ ] Status → BROKEN
- [ ] Dana kembali ke initiator treasury
- [ ] Notifikasi ke counterpart
- [ ] Tidak ada fee untuk sever

**API Endpoints:**
- `POST /covenant/:id/sever`

---

### 3.7 Covenant Timeline
**Priority:** P1  
**User Story:** Sebagai pilgrim, saya ingin melihat timeline progress covenant.

**Acceptance Criteria:**
- [ ] Vertical stepper: FORGED → ACCEPTED → DELIVERED → FULFILLED
- [ ] Setiap step: timestamp, actor, note
- [ ] Step aktif: highlighted
- [ ] Step completed: checkmark icon
- [ ] Step pending: grayed out
- [ ] Proof links clickable
- [ ] Mobile-friendly

**Frontend:**
- [ ] Timeline component di covenant chamber
- [ ] Color-coded steps
- [ ] Animated transitions

---

## 4. The Communion — In-Room Chat

### 4.1 Real-time Communion
**Priority:** P1  
**User Story:** Sebagai pilgrim, saya ingin berkomunikasi dengan lawan transaksi dalam covenant.

**Acceptance Criteria:**
- [ ] WebSocket connection per covenant
- [ ] Send/receive text messages real-time
- [ ] Message persistence
- [ ] Display sender name, avatar, timestamp
- [ ] Auto-scroll ke message terbaru
- [ ] Typing indicator
- [ ] Read receipts
- [ ] System messages: status changes, delivery notifications
- [ ] Reconnect otomatis
- [ ] Max 1000 messages per covenant

**API Endpoints:**
- `WS /ws/communion?seal=&covenantId=`
- `GET /communion/covenant/:id/messages?cursor=&limit=`

**Frontend:**
- [ ] Communion panel di covenant chamber
- [ ] Message bubble component
- [ ] Communion input dengan send button
- [ ] Typing indicator dots
- [ ] Connection status indicator
- [ ] Load more on scroll up

---

## 5. The Judgment — Dispute System

### 5.1 Invoke Judgment
**Priority:** P1  
**User Story:** Sebagai pilgrim, saya ingin mengajukan judgment jika ada masalah.

**Acceptance Criteria:**
- [ ] Judgment hanya saat status DELIVERED
- [ ] Alasan: ITEM_NOT_AS_DESCRIBED, NOT_DELIVERED, OTHER
- [ ] Deskripsi detail wajib (min 20 karakter)
- [ ] Upload evidence: max 5 files, 5MB each
- [ ] Status covenant → JUDGMENT
- [ ] Dana di-frozen
- [ ] PIN confirmation wajib
- [ ] Notifikasi ke lawan transaksi

**API Endpoints:**
- `POST /covenant/:id/judgment`

**Frontend:**
- [ ] "Invoke Judgment" button di covenant chamber
- [ ] Judgment form: reason, description, file upload
- [ ] Evidence preview grid
- [ ] PIN confirmation modal

---

### 5.2 Render Judgment (Oracle)
**Priority:** P1  
**User Story:** Sebagai oracle, saya ingin menyelesaikan judgment dengan adil.

**Acceptance Criteria:**
- [ ] Oracle panel: list semua judgment dengan filter
- [ ] Detail judgment: covenant info, evidence, communion history, timeline
- [ ] Keputusan: RELEASE_TO_COUNTERPART, REFUND_TO_INITIATOR, SPLIT
- [ ] Jika SPLIT: input amount ke initiator
- [ ] Oracle notes wajib
- [ ] Status judgment → RESOLVED
- [ ] Dana diproses sesuai keputusan
- [ ] Notifikasi ke kedua belah pihak
- [ ] Covenant score penalty untuk pihak yang kalah (-0.5)

**API Endpoints:**
- `GET /judgment/oracle/all`
- `POST /judgment/:id/render`

**Frontend:**
- [ ] Halaman `/sanctum/judgment` (oracle view: all, pilgrim view: my judgments)
- [ ] Judgment detail: evidence gallery, decision panel
- [ ] Resolution form
- [ ] Confirmation modal

---

## 6. The Observatory — Dashboard & Analytics

### 6.1 Pilgrim Observatory
**Priority:** P1  
**User Story:** Sebagai pilgrim, saya ingin melihat overview akun di satu halaman.

**Acceptance Criteria:**
- [ ] Treasury card: available + sealed
- [ ] Quick actions: Make Offering, Forge Covenant, Bestow Tithing
- [ ] Stats: total covenants, fulfillment rate, active covenants, covenant score
- [ ] Recent covenants: 5 terakhir
- [ ] Recent chronicles: 5 terakhir
- [ ] Chart: weekly covenant volume
- [ ] Notifications: unread count
- [ ] Responsive layout

**Frontend:**
- [ ] Halaman `/sanctum/observatory`
- [ ] Grid layout dengan cards
- [ ] Chart.js line chart
- [ ] Skeleton loading
- [ ] Auto-refresh setiap 30 detik

---

## 7. Omens — Notifications

### 7.1 Push Notifications
**Priority:** P1  
**User Story:** Sebagai pilgrim, saya ingin menerima notifikasi real-time.

**Acceptance Criteria:**
- [ ] Notifikasi untuk: covenant forged, accepted, delivered, fulfilled, judgment, broken
- [ ] Notifikasi untuk: offering accepted, withdrawal completed, tithing received
- [ ] Via: in-app (Communion), browser push (Phase 2), email (Phase 2)
- [ ] Notification bell dengan unread count
- [ ] Notification list: timestamp, type, message, link
- [ ] Mark as read / mark all as read

**Backend:**
- [ ] RabbitMQ consumer untuk setiap event type
- [ ] Notification table: pilgrim_id, type, message, is_read, created_at

**Frontend:**
- [ ] Notification bell di Veil (navbar)
- [ ] Dropdown notification list
- [ ] Toast notification (svelte-sonner)
- [ ] Badge count

---

## 8. Profile & Settings

### 8.1 Pilgrim Profile
**Priority:** P1  
**User Story:** Sebagai pilgrim, saya ingin melihat dan mengatur profil saya.

**Acceptance Criteria:**
- [ ] Display: photo, name, phone, email, covenant score, attunement status
- [ ] Edit: name, email, photo
- [ ] Stats: total covenants, judgment rate, attuned since
- [ ] Saved bank accounts: list, add, delete, set primary
- [ ] Settings: transmute PIN, omen preferences, theme
- [ ] Sever Seal button

**API Endpoints:**
- `GET /seal/self`
- `PUT /seal/self`
- `PUT /seal/pin`

**Frontend:**
- [ ] Halaman `/sanctum/profile`
- [ ] Profile card dengan avatar upload
- [ ] Tabs: Profile, Bank Accounts, Settings
- [ ] Form validation dengan Zod

---

## 9. Oracle Features — Admin

### 9.1 Oracle Panel
**Priority:** P2  
**User Story:** Sebagai oracle, saya ingin mengelola Sanctum.

**Acceptance Criteria:**
- [ ] Dashboard: total pilgrims, total covenants, active covenants, judgments
- [ ] Pilgrim management: list, search, sanction/unsanction, view profile
- [ ] Judgment management: list, assign, render
- [ ] Attunement review: list, approve/reject
- [ ] Covenant monitoring: list, filter, force sever (emergency)

**Frontend:**
- [ ] Route `/oracle` (protected, oracle only)
- [ ] Sidebar navigation
- [ ] Data tables dengan sorting, filtering, pagination
- [ ] Charts: daily active pilgrims, covenant volume, judgment rate

---

## 10. Non-Functional Requirements

### 10.1 Performance
- [ ] API response < 150ms (p95)
- [ ] Communion latency < 50ms
- [ ] Page load FCP < 1.5s
- [ ] TTI < 3s
- [ ] Support 5,000 concurrent pilgrims

### 10.2 Security
- [ ] OWASP Top 10 compliance
- [ ] SQL injection prevention
- [ ] XSS prevention
- [ ] CSRF protection
- [ ] Rate limiting
- [ ] Input validation (Zod + Bean Validation)
- [ ] HTTPS only (production)
- [ ] Secure headers

### 10.3 Accessibility
- [ ] WCAG 2.1 AA compliance
- [ ] Keyboard navigation
- [ ] Screen reader support
- [ ] Color contrast >= 4.5:1
- [ ] Focus indicators
- [ ] Alt text for images

### 10.4 Responsive
- [ ] Mobile-first
- [ ] Breakpoints: sm 640px, md 768px, lg 1024px, xl 1280px
- [ ] Touch-friendly (min 44x44px)
- [ ] Bottom navigation for mobile
- [ ] Sidebar collapsible for tablet/desktop

---

## 11. Feature Priority Matrix

| Feature | Priority | Complexity | Value |
|---------|----------|------------|-------|
| Forge Identity / Rite of Return | P0 | Medium | Critical |
| PIN Sanctification | P0 | Low | Critical |
| Gaze Upon Treasury | P0 | Low | Critical |
| Make Offering | P0 | High | Critical |
| Withdrawal Ritual | P0 | High | Critical |
| P2P Tithing | P0 | Medium | Critical |
| Read Chronicles | P0 | Medium | High |
| Forge Covenant | P0 | High | Critical |
| Accept/Reject Covenant | P0 | Medium | Critical |
| Fulfill Oath | P0 | Medium | Critical |
| Confirm Fulfillment | P0 | Medium | Critical |
| Auto-Release | P0 | Medium | Critical |
| Sanction Seal | P1 | Medium | High |
| Real-time Communion | P1 | High | High |
| Invoke/Render Judgment | P1 | High | High |
| Observatory | P1 | Medium | High |
| Omens (Notifications) | P1 | Medium | High |
| Pilgrim Profile | P1 | Low | Medium |
| KYC Attunement | P2 | Medium | Medium |
| Oracle Panel | P2 | High | Medium |
| Rating & Review | P2 | Low | Medium |

---

## 12. Success Metrics (KPIs)

| Metric | Target |
|--------|--------|
| Pilgrim Registration Conversion | > 70% |
| Covenant Forge to Fulfillment | > 80% |
| Judgment Rate | < 5% |
| Average Covenant Completion Time | < 24 hours |
| Pilgrim Satisfaction (NPS) | > 50 |
| App Load Time (FCP) | < 1.5s |
| API Response Time (p95) | < 150ms |
| Uptime | > 99.9% |

---

*End of the Chronicle. Refer to other scrolls for technical implementation details.*
