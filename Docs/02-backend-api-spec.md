# EyesOfPriestess — Backend API Specification

> **Version:** 1.0  
> **Base URL:** `http://localhost:8000` (Kong API Gateway)  
> **Auth:** Bearer JWT (RS256)  
> **Content-Type:** `application/json`  
> **Design System:** Warm Editorial (Cream Canvas + Coral Accent)

---

## 1. API Gateway Routing

| Route Pattern | Target Service | Port |
|---|---|---|
| `/api/v1/auth/**` | Auth Service | 8081 |
| `/api/v1/wallet/**` | Wallet Service | 8082 |
| `/api/v1/room/**` | Room Escrow Service | 8083 |
| `/api/v1/chat/**` | Chat Service | 8084 |
| `/api/v1/dispute/**` | Dispute Service | 8085 |
| `/ws/**` | Chat Service (WebSocket) | 8084 |

---

## 2. Auth Service (`/api/v1/auth`)

### 2.1 POST `/register`
Register new user.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "fullName": "Budi Santoso",
  "phoneNumber": "+6281234567890",
  "username": "budisantoso"
}
```

**Response 201:**
```json
{
  "id": "uuid",
  "email": "user@example.com",
  "fullName": "Budi Santoso",
  "username": "budisantoso",
  "createdAt": "2026-08-11T10:00:00Z",
  "walletId": "uuid"
}
```

**Errors:**
- `400` — Validation error (email format, password strength, phone format)
- `409` — Email atau username sudah terdaftar
- `429` — Rate limit exceeded

---

### 2.2 POST `/login`
Authenticate user.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

**Response 200:**
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIs...",
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2g...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "fullName": "Budi Santoso",
    "username": "budisantoso",
    "role": "USER"
  }
}
```

**Errors:**
- `401` — Invalid credentials
- `403` — Account banned (insta-ban active)
- `429` — Too many login attempts

---

### 2.3 POST `/refresh`
Refresh access token.

**Request:**
```json
{
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2g..."
}
```

**Response 200:**
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIs...",
  "expiresIn": 900
}
```

**Errors:**
- `401` — Invalid or expired refresh token

---

### 2.4 POST `/logout`
Invalidate tokens.

**Headers:** `Authorization: Bearer {accessToken}`

**Request:**
```json
{
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2g...",
  "allDevices": false
}
```

**Response 204:** No content

---

### 2.5 POST `/set-pin`
Set 6-digit transaction PIN (first time).

**Headers:** `Authorization: Bearer {accessToken}`

**Request:**
```json
{
  "pin": "123456",
  "confirmPin": "123456"
}
```

**Response 200:**
```json
{
  "message": "PIN set successfully",
  "pinSetAt": "2026-08-11T10:05:00Z"
}
```

**Errors:**
- `400` — PIN tidak cocok atau tidak 6 digit
- `409` — PIN sudah pernah di-set

---

### 2.6 POST `/change-pin`
Change existing PIN.

**Headers:** `Authorization: Bearer {accessToken}`

**Request:**
```json
{
  "oldPin": "123456",
  "newPin": "654321",
  "confirmNewPin": "654321"
}
```

**Response 200:** `{ "message": "PIN changed successfully" }`

---

### 2.7 POST `/verify-pin`
Verify PIN untuk transaksi kritis.

**Headers:** `Authorization: Bearer {accessToken}`

**Request:**
```json
{
  "pin": "123456"
}
```

**Response 200:**
```json
{
  "valid": true,
  "verifiedAt": "2026-08-11T10:10:00Z"
}
```

**Errors:**
- `403` — Invalid PIN (3 attempts = 15 min lock)

---

### 2.8 GET `/me`
Get current user profile.

**Headers:** `Authorization: Bearer {accessToken}`

**Response 200:**
```json
{
  "id": "uuid",
  "email": "user@example.com",
  "fullName": "Budi Santoso",
  "username": "budisantoso",
  "phoneNumber": "+6281234567890",
  "role": "USER",
  "isPinSet": true,
  "isVerified": true,
  "createdAt": "2026-08-11T10:00:00Z",
  "updatedAt": "2026-08-11T10:05:00Z"
}
```

---

### 2.9 PUT `/me`
Update profile.

**Headers:** `Authorization: Bearer {accessToken}`

**Request:**
```json
{
  "fullName": "Budi Santoso Updated",
  "phoneNumber": "+6289876543210"
}
```

**Response 200:** Updated user object

---

### 2.10 POST `/forgot-password`
Request password reset.

**Request:**
```json
{
  "email": "user@example.com"
}
```

**Response 200:** `{ "message": "Reset link sent if email exists" }`

---

### 2.11 POST `/reset-password`
Reset password dengan token.

**Request:**
```json
{
  "token": "reset-token-string",
  "newPassword": "NewSecurePass123!",
  "confirmPassword": "NewSecurePass123!"
}
```

**Response 200:** `{ "message": "Password reset successful" }`

---

### 2.12 POST `/admin/ban` (ADMIN only)
Insta-ban user.

**Headers:** `Authorization: Bearer {adminToken}`

**Request:**
```json
{
  "userId": "uuid",
  "reason": "Fraudulent activity detected",
  "duration": "PERMANENT",
  "banType": "FULL"
}
```

**Response 200:**
```json
{
  "message": "User banned successfully",
  "banId": "uuid",
  "bannedAt": "2026-08-11T10:15:00Z",
  "expiresAt": null
}
```

**Duration options:** `PERMANENT`, `TEMPORARY` (with `durationHours`)
**Ban types:** `FULL`, `TRANSACTION_ONLY`, `ROOM_ONLY`

---

### 2.13 POST `/admin/unban` (ADMIN only)
Unban user.

**Request:**
```json
{
  "userId": "uuid",
  "reason": "Appeal approved"
}
```

**Response 200:** `{ "message": "User unbanned successfully" }`

---

## 3. Wallet Service (`/api/v1/wallet`)

### 3.1 GET `/balance`
Get wallet balance.

**Headers:** `Authorization: Bearer {accessToken}`

**Response 200:**
```json
{
  "walletId": "uuid",
  "availableBalance": 1500000,
  "escrowBalance": 500000,
  "totalBalance": 2000000,
  "currency": "IDR",
  "lastUpdated": "2026-08-11T10:00:00Z"
}
```

---

### 3.2 POST `/topup`
Create top-up order.

**Headers:** `Authorization: Bearer {accessToken}`

**Request:**
```json
{
  "amount": 100000,
  "method": "VIRTUAL_ACCOUNT",
  "bankCode": "BCA",
  "pin": "123456"
}
```

**Response 201:**
```json
{
  "orderId": "uuid",
  "amount": 100000,
  "method": "VIRTUAL_ACCOUNT",
  "bankCode": "BCA",
  "virtualAccountNumber": "9881234567890123",
  "status": "PENDING",
  "expiryTime": "2026-08-11T22:00:00Z",
  "createdAt": "2026-08-11T10:00:00Z"
}
```

**Methods:** `VIRTUAL_ACCOUNT`, `E_WALLET` (OVO, DANA, LinkAja), `RETAIL`

---

### 3.3 GET `/topup/{orderId}`
Get top-up order status.

**Response 200:**
```json
{
  "orderId": "uuid",
  "amount": 100000,
  "status": "SUCCESS",
  "paidAt": "2026-08-11T10:30:00Z",
  "settledAt": "2026-08-11T10:31:00Z"
}
```

**Status:** `PENDING`, `PAID`, `SUCCESS`, `FAILED`, `EXPIRED`

---

### 3.4 POST `/topup/callback` (Webhook)
Payment gateway callback.

**Headers:** `X-Callback-Token: {secret}`

**Request:**
```json
{
  "orderId": "uuid",
  "transactionStatus": "settlement",
  "paymentType": "bank_transfer",
  "grossAmount": 100000,
  "transactionTime": "2026-08-11T10:30:00Z"
}
```

**Response 200:** `{ "message": "Callback processed" }`

---

### 3.5 POST `/withdraw`
Create withdraw request.

**Headers:** `Authorization: Bearer {accessToken}`

**Request:**
```json
{
  "amount": 500000,
  "bankAccountId": "uuid",
  "pin": "123456"
}
```

**Response 201:**
```json
{
  "withdrawId": "uuid",
  "amount": 500000,
  "fee": 6500,
  "netAmount": 493500,
  "status": "PENDING",
  "estimatedArrival": "2026-08-12T10:00:00Z",
  "createdAt": "2026-08-11T10:00:00Z"
}
```

---

### 3.6 POST `/transfer`
P2P transfer ke user lain.

**Headers:** `Authorization: Bearer {accessToken}`

**Request:**
```json
{
  "toUserId": "uuid",
  "amount": 100000,
  "note": "Pembayaran invoice #123",
  "pin": "123456"
}
```

**Response 201:**
```json
{
  "transactionId": "uuid",
  "fromUserId": "uuid",
  "toUserId": "uuid",
  "amount": 100000,
  "fee": 0,
  "type": "P2P_TRANSFER",
  "status": "SUCCESS",
  "note": "Pembayaran invoice #123",
  "createdAt": "2026-08-11T10:00:00Z"
}
```

---

### 3.7 GET `/transactions`
Get transaction history.

**Headers:** `Authorization: Bearer {accessToken}`

**Query Params:**
- `page` (int, default: 0)
- `size` (int, default: 20)
- `type` (enum: TOPUP, WITHDRAW, P2P_TRANSFER, ESCROW_HOLD, ESCROW_RELEASE, ESCROW_REFUND, FEE)
- `status` (enum: PENDING, SUCCESS, FAILED)
- `fromDate` (ISO 8601)
- `toDate` (ISO 8601)

**Response 200:**
```json
{
  "content": [
    {
      "transactionId": "uuid",
      "type": "P2P_TRANSFER",
      "amount": 100000,
      "fee": 0,
      "status": "SUCCESS",
      "description": "Transfer to budisantoso",
      "counterpartyName": "Budi Santoso",
      "counterpartyAvatar": "https://...",
      "createdAt": "2026-08-11T10:00:00Z",
      "direction": "OUT"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8
}
```

---

### 3.8 GET `/transactions/{transactionId}`
Get transaction detail.

**Response 200:**
```json
{
  "transactionId": "uuid",
  "type": "ESCROW_HOLD",
  "amount": 500000,
  "fee": 5000,
  "status": "SUCCESS",
  "description": "Escrow hold for room #ROOM-123",
  "roomId": "uuid",
  "metadata": {},
  "createdAt": "2026-08-11T10:00:00Z",
  "settledAt": "2026-08-11T10:01:00Z"
}
```

---

### 3.9 GET `/bank-accounts`
Get saved bank accounts.

**Headers:** `Authorization: Bearer {accessToken}`

**Response 200:**
```json
{
  "accounts": [
    {
      "id": "uuid",
      "bankCode": "BCA",
      "bankName": "Bank Central Asia",
      "accountNumber": "1234567890",
      "accountHolderName": "BUDI SANTOSO",
      "isPrimary": true
    }
  ]
}
```

---

### 3.10 POST `/bank-accounts`
Add bank account.

**Request:**
```json
{
  "bankCode": "BCA",
  "accountNumber": "1234567890",
  "accountHolderName": "BUDI SANTOSO",
  "pin": "123456"
}
```

**Response 201:** Created bank account object

---

## 4. Room Escrow Service (`/api/v1/room`)

### 4.1 POST `/`
Create escrow room.

**Headers:** `Authorization: Bearer {accessToken}`

**Request:**
```json
{
  "title": "Jual Beli Akun Game FF",
  "description": "Akun sultan, level 50, skin lengkap",
  "itemCategory": "GAME_ACCOUNT",
  "itemPrice": 250000,
  "sellerId": "uuid",
  "autoReleaseHours": 24
}
```

**Response 201:**
```json
{
  "roomId": "uuid",
  "roomCode": "EOP-ABC123",
  "title": "Jual Beli Akun Game FF",
  "description": "Akun sultan, level 50, skin lengkap",
  "itemCategory": "GAME_ACCOUNT",
  "itemPrice": 250000,
  "fee": 2500,
  "totalAmount": 252500,
  "status": "WAITING_PAYMENT",
  "buyerId": "uuid",
  "sellerId": "uuid",
  "autoReleaseAt": "2026-08-12T10:00:00Z",
  "createdAt": "2026-08-11T10:00:00Z"
}
```

**Status lifecycle:** `WAITING_PAYMENT` → `FUNDED` → `DELIVERED` → `COMPLETED` / `DISPUTED` / `CANCELLED`

---

### 4.2 POST `/{roomId}/fund`
Buyer mendanai room (lock dana ke escrow).

**Headers:** `Authorization: Bearer {accessToken}`

**Request:**
```json
{
  "pin": "123456"
}
```

**Response 200:**
```json
{
  "roomId": "uuid",
  "status": "FUNDED",
  "fundedAt": "2026-08-11T10:05:00Z",
  "escrowTransactionId": "uuid"
}
```

---

### 4.3 POST `/{roomId}/deliver`
Seller konfirmasi pengiriman barang/jasa.

**Headers:** `Authorization: Bearer {accessToken}`

**Request:**
```json
{
  "deliveryNotes": "Data akun sudah dikirim via chat",
  "proofImages": ["url1", "url2"]
}
```

**Response 200:**
```json
{
  "roomId": "uuid",
  "status": "DELIVERED",
  "deliveredAt": "2026-08-11T10:10:00Z",
  "deliveryNotes": "Data akun sudah dikirim via chat"
}
```

---

### 4.4 POST `/{roomId}/confirm`
Buyer konfirmasi penerimaan.

**Headers:** `Authorization: Bearer {accessToken}`

**Request:**
```json
{
  "pin": "123456",
  "rating": 5,
  "review": "Seller amanah, proses cepat"
}
```

**Response 200:**
```json
{
  "roomId": "uuid",
  "status": "COMPLETED",
  "completedAt": "2026-08-11T10:15:00Z",
  "releasedAmount": 250000,
  "sellerReceived": 247500
}
```

---

### 4.5 POST `/{roomId}/dispute`
Buyer atau seller buka dispute.

**Headers:** `Authorization: Bearer {accessToken}`

**Request:**
```json
{
  "reason": "Barang tidak sesuai deskripsi",
  "description": "Akun yang diberikan levelnya hanya 30, bukan 50",
  "evidenceImages": ["url1", "url2"]
}
```

**Response 201:**
```json
{
  "roomId": "uuid",
  "status": "DISPUTED",
  "disputeId": "uuid",
  "disputedAt": "2026-08-11T10:20:00Z"
}
```

---

### 4.6 POST `/{roomId}/cancel`
Cancel room (hanya jika status WAITING_PAYMENT).

**Headers:** `Authorization: Bearer {accessToken}`

**Response 200:**
```json
{
  "roomId": "uuid",
  "status": "CANCELLED",
  "cancelledAt": "2026-08-11T10:00:00Z"
}
```

---

### 4.7 GET `/`
List rooms untuk user.

**Headers:** `Authorization: Bearer {accessToken}`

**Query Params:**
- `role` (BUYER, SELLER, ALL)
- `status` (WAITING_PAYMENT, FUNDED, DELIVERED, COMPLETED, DISPUTED, CANCELLED)
- `page`, `size`

**Response 200:** Paginated list of RoomSummary

---

### 4.8 GET `/{roomId}`
Get room detail.

**Response 200:**
```json
{
  "roomId": "uuid",
  "roomCode": "EOP-ABC123",
  "title": "Jual Beli Akun Game FF",
  "description": "Akun sultan, level 50, skin lengkap",
  "itemCategory": "GAME_ACCOUNT",
  "itemPrice": 250000,
  "fee": 2500,
  "totalAmount": 252500,
  "status": "FUNDED",
  "buyer": { "id": "uuid", "name": "Andi Wijaya", "avatar": "url" },
  "seller": { "id": "uuid", "name": "Budi Santoso", "avatar": "url" },
  "autoReleaseAt": "2026-08-12T10:00:00Z",
  "timeline": [
    { "status": "CREATED", "timestamp": "2026-08-11T10:00:00Z", "actor": "Budi Santoso" },
    { "status": "FUNDED", "timestamp": "2026-08-11T10:05:00Z", "actor": "Andi Wijaya" }
  ],
  "chatRoomId": "uuid",
  "createdAt": "2026-08-11T10:00:00Z"
}
```

---

### 4.9 POST `/{roomId}/extend-auto-release`
Extend auto-release timer (buyer only, max 72 jam).

**Request:**
```json
{
  "additionalHours": 24
}
```

**Response 200:** Updated room dengan `autoReleaseAt` baru

---

## 5. Chat Service (`/api/v1/chat`)

### 5.1 WebSocket Connection
```
ws://localhost:8080/ws/chat?token={jwt}
```

**Connection lifecycle:**
1. Client connect dengan JWT di query param
2. Server validate JWT + cek blacklist
3. Server subscribe client ke channel `user:{userId}`
4. Saat masuk room, client send: `{ "type": "JOIN_ROOM", "roomId": "uuid" }`
5. Server subscribe ke `room:{roomId}`

**Message format (client → server):**
```json
{
  "type": "SEND_MESSAGE",
  "roomId": "uuid",
  "content": "Halo, sudah saya transfer ya",
  "messageType": "TEXT"
}
```

**Message format (server → client):**
```json
{
  "type": "NEW_MESSAGE",
  "messageId": "uuid",
  "roomId": "uuid",
  "senderId": "uuid",
  "senderName": "Budi Santoso",
  "senderAvatar": "url",
  "content": "Halo, sudah saya transfer ya",
  "messageType": "TEXT",
  "sentAt": "2026-08-11T10:00:00Z"
}
```

**Message types:** `TEXT`, `IMAGE`, `FILE`, `SYSTEM` (auto-generated status updates)

---

### 5.2 GET `/rooms`
Get chat rooms untuk user.

**Headers:** `Authorization: Bearer {accessToken}`

**Response 200:**
```json
{
  "rooms": [
    {
      "chatRoomId": "uuid",
      "roomName": "Jual Beli Akun Game FF",
      "roomCode": "EOP-ABC123",
      "lastMessage": "Data akun sudah dikirim",
      "lastMessageAt": "2026-08-11T10:10:00Z",
      "unreadCount": 3,
      "participantCount": 2,
      "escrowStatus": "FUNDED"
    }
  ]
}
```

---

### 5.3 GET `/rooms/{chatRoomId}/messages`
Get message history.

**Query Params:** `page`, `size` (default 50)

**Response 200:** Paginated messages (newest first)

---

### 5.4 POST `/rooms/{chatRoomId}/read`
Mark messages as read.

**Response 204:** No content

---

## 6. Dispute Service (`/api/v1/dispute`)

### 6.1 GET `/`
List disputes (user melihat dispute-nya sendiri, admin melihat semua).

**Headers:** `Authorization: Bearer {accessToken}`

**Query Params:**
- `status` (OPEN, UNDER_REVIEW, RESOLVED, CLOSED)
- `role` (BUYER, SELLER, ADMIN)
- `page`, `size`

**Response 200:** Paginated disputes

---

### 6.2 GET `/{disputeId}`
Get dispute detail.

**Response 200:**
```json
{
  "disputeId": "uuid",
  "roomId": "uuid",
  "roomCode": "EOP-ABC123",
  "title": "Barang tidak sesuai deskripsi",
  "description": "Akun yang diberikan levelnya hanya 30, bukan 50",
  "status": "UNDER_REVIEW",
  "initiatedBy": "BUYER",
  "buyer": { "id": "uuid", "name": "Andi Wijaya" },
  "seller": { "id": "uuid", "name": "Budi Santoso" },
  "evidence": [
    { "id": "uuid", "type": "IMAGE", "url": "url", "uploadedBy": "uuid", "uploadedAt": "..." }
  ],
  "decision": null,
  "createdAt": "2026-08-11T10:20:00Z",
  "updatedAt": "2026-08-11T10:25:00Z"
}
```

---

### 6.3 POST `/{disputeId}/evidence`
Upload evidence.

**Headers:** `Authorization: Bearer {accessToken}`  
**Content-Type:** `multipart/form-data`

**Form fields:**
- `file` (image/pdf, max 5MB)
- `description` (string)

**Response 201:** Evidence object

---

### 6.4 POST `/{disputeId}/resolve` (ADMIN only)
Resolve dispute.

**Headers:** `Authorization: Bearer {adminToken}`

**Request:**
```json
{
  "decision": "REFUND_BUYER",
  "reason": "Bukti menunjukkan barang tidak sesuai deskripsi",
  "refundAmount": 250000
}
```

**Decision options:** `RELEASE_TO_SELLER`, `REFUND_BUYER`, `PARTIAL_REFUND`, `CANCEL`

**Response 200:**
```json
{
  "disputeId": "uuid",
  "status": "RESOLVED",
  "decision": "REFUND_BUYER",
  "resolvedAt": "2026-08-11T11:00:00Z",
  "refundTransactionId": "uuid"
}
```

---

### 6.5 POST `/{disputeId}/comment`
Add comment ke dispute.

**Request:**
```json
{
  "content": "Saya sudah upload bukti screenshot akun asli"
}
```

**Response 201:** Comment object

---

## 7. Error Response Standard

```json
{
  "timestamp": "2026-08-11T10:00:00Z",
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Validation failed",
  "path": "/api/v1/wallet/transfer",
  "details": [
    {
      "field": "amount",
      "message": "Amount must be at least 10000"
    }
  ],
  "traceId": "abc-def-123"
}
```

### Error Codes

| Code | HTTP | Meaning |
|---|---|---|
| `BAD_REQUEST` | 400 | Invalid request body or parameters |
| `UNAUTHORIZED` | 401 | Missing or invalid JWT |
| `FORBIDDEN` | 403 | Insufficient permissions atau account banned |
| `NOT_FOUND` | 404 | Resource tidak ditemukan |
| `CONFLICT` | 409 | Resource sudah ada atau state tidak valid |
| `UNPROCESSABLE` | 422 | Business logic violation |
| `TOO_MANY_REQUESTS` | 429 | Rate limit exceeded |
| `INTERNAL_ERROR` | 500 | Server error |
| `SERVICE_UNAVAILABLE` | 503 | Downstream service error |

---

## 8. Rate Limiting

| Endpoint | Limit |
|---|---|
| `/auth/login` | 10 requests / minute / IP |
| `/auth/register` | 5 requests / hour / IP |
| `/auth/verify-pin` | 5 attempts / 15 min / user |
| `/wallet/transfer` | 30 requests / minute / user |
| `/room/` (create) | 30 requests / hour / user |
| Gateway General Limit | 600 requests / minute (DB-less Kong) |

---

## 9. Interactive API Documentation (Swagger / OpenAPI)

Setiap microservice mengekspos OpenAPI 3.0 specification dan antarmuka Swagger UI interaktif untuk pengujian endpoint secara langsung:

| Service | Direct Swagger UI URL | Kong Gateway Proxied URL |
|---|---|---|
| **Auth Service** | `http://localhost:8081/q/swagger-ui` | `http://localhost:8000/api/v1/auth/q/swagger-ui` |
| **Wallet Service** | `http://localhost:8082/q/swagger-ui` | `http://localhost:8000/api/v1/wallet/q/swagger-ui` |
| **Room Escrow Service** | `http://localhost:8083/q/swagger-ui` | `http://localhost:8000/api/v1/room/q/swagger-ui` |
| **Dispute Service** | `http://localhost:8085/q/swagger-ui` | `http://localhost:8000/api/v1/dispute/q/swagger-ui` |

---

*End of Backend API Specification*
