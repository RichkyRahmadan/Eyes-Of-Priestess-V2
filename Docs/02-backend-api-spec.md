# EyesOfPriestess — Backend API Specification
## The Five Sanctums: REST API + Communion WebSocket

**Base URL (via The Veil):** `http://localhost:8080/api/v1`  
**Content-Type:** `application/json`  
**Auth:** `Authorization: SacredSeal <jwt_access_token>`  
**Idempotency:** `Covenant-Key: <uuid>` (for POST/PUT)  

---

## 1. Seal Sanctum (`/api/v1/seal`)

### 1.1 Forge Identity (Register)
```http
POST /seal/forge
Content-Type: application/json

Request:
{
  "phone": "+6281234567890",
  "email": "pilgrim@sanctum.id",      // optional
  "fullName": "Wanderer of the Void",
  "password": "OriginiumSeal123!",
  "pin": "123456"                   // 6 digit, numeric only
}

Response 201:
{
  "success": true,
  "data": {
    "pilgrimId": "550e8400-e29b-41d4-a716-446655440000",
    "phone": "+6281234567890",
    "status": "attuned",
    "attunedAt": "2026-08-06T20:00:00Z"
  }
}

Response 400:
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "This resonance is already attuned to another pilgrim",
    "field": "phone"
  }
}
```

### 1.2 Rite of Return (Login)
```http
POST /seal/rite
Content-Type: application/json

Request:
{
  "phone": "+6281234567890",
  "password": "OriginiumSeal123!",
  "deviceId": "device-uuid-123"     // for device tracking
}

Response 200:
{
  "success": true,
  "data": {
    "accessSeal": "eyJhbGciOiJSUzI1NiIs...",
    "refreshSeal": "eyJhbGciOiJSUzI1NiIs...",
    "expiresIn": 900,                // 15 minutes
    "sealType": "Sacred",
    "pilgrim": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "phone": "+6281234567890",
      "fullName": "Wanderer of the Void",
      "attunementStatus": "unattuned",
      "isSanctioned": false
    }
  }
}

Response 401:
{
  "success": false,
  "error": {
    "code": "INVALID_CREDENTIALS",
    "message": "The resonance does not match the seal"
  }
}
```

### 1.3 Renew Seal (Refresh Token)
```http
POST /seal/renew
Content-Type: application/json

Request:
{
  "refreshSeal": "eyJhbGciOiJSUzI1NiIs..."
}

Response 200:
{
  "success": true,
  "data": {
    "accessSeal": "eyJhbGciOiJSUzI1NiIs...",
    "expiresIn": 900
  }
}
```

### 1.4 Sever Seal (Logout)
```http
POST /seal/sever
Authorization: SacredSeal <access_seal>

Request:
{
  "refreshSeal": "eyJhbGciOiJSUzI1NiIs..."
}

Response 200:
{
  "success": true,
  "data": {
    "message": "Your seal has been severed. Return safely, pilgrim."
  }
}
// Note: Adds both access and refresh seal jti to Crystal sanction list
```

### 1.5 Request Omen (OTP)
```http
POST /seal/omen/request
Content-Type: application/json

Request:
{
  "phone": "+6281234567890",
  "purpose": "FORGE"              // FORGE | RESET_PASSWORD | CHANGE_PHONE
}

Response 200:
{
  "success": true,
  "data": {
    "omenToken": "omen-verification-token-123",
    "expiresIn": 300,                // 5 minutes
    "message": "An omen has been sent to your resonance (SIMULATED: 123456)"
  }
}
```

### 1.6 Verify Omen
```http
POST /seal/omen/verify
Content-Type: application/json

Request:
{
  "omenToken": "omen-verification-token-123",
  "omenCode": "123456"
}

Response 200:
{
  "success": true,
  "data": {
    "verified": true,
    "verificationToken": "verified-token-456"
  }
}
```

### 1.7 Transmute PIN
```http
PUT /seal/pin
Authorization: SacredSeal <access_seal>
Content-Type: application/json

Request:
{
  "oldPin": "123456",
  "newPin": "654321"
}

Response 200:
{
  "success": true,
  "data": {
    "message": "Your seal has been transmuted successfully"
  }
}
```

### 1.8 Gaze Upon Self (Get Current Pilgrim)
```http
GET /seal/self
Authorization: SacredSeal <access_seal>

Response 200:
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "phone": "+6281234567890",
    "email": "pilgrim@sanctum.id",
    "fullName": "Wanderer of the Void",
    "profilePhoto": "https://cdn.eyesofpriestess.id/avatars/550e8400.jpg",
    "attunementStatus": "attuned",
    "attunementIdHash": "3175********1234",    // masked
    "attunedAt": "2026-08-01T10:00:00Z",
    "isSanctioned": false,
    "covenantScore": 4.8                      // 0-5, based on transaction history
  }
}
```

### 1.9 Update Self
```http
PUT /seal/self
Authorization: SacredSeal <access_seal>
Content-Type: application/json

Request:
{
  "fullName": "Wanderer of the Void, Updated",
  "email": "newpath@sanctum.id"
}

Response 200: { ...updated pilgrim object }
```

### 1.10 Oracle: Sanction Pilgrim (Insta-Ban)
```http
POST /seal/oracle/sanction
Authorization: SacredSeal <oracle_access_seal>
Content-Type: application/json

Request:
{
  "pilgrimId": "550e8400-e29b-41d4-a716-446655440000",
  "reason": "FRAUDULENT_ACTIVITY",
  "description": "Multiple broken covenants with evidence of deception",
  "sanctionDuration": "ETERNAL"           // ETERNAL | TEMPORARY (days)
}

Response 200:
{
  "success": true,
  "data": {
    "pilgrimId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "sanctioned",
    "sanctionedAt": "2026-08-06T20:00:00Z",
    "reason": "FRAUDULENT_ACTIVITY",
    "sealsSevered": 3                  // number of active sessions revoked
  }
}
// Note: Publishes pilgrim.sanctioned event to RabbitMQ, all sanctums invalidate cache
```

---

## 2. Vault Sanctum (`/api/v1/vault`)

### 2.1 Gaze Upon Treasury (Get Balance)
```http
GET /vault/treasury
Authorization: SacredSeal <access_seal>

Response 200:
{
  "success": true,
  "data": {
    "availableTreasury": 1500000.00,      // IDR
    "sealedTreasury": 500000.00,          // dana yang sedang di-hold dalam escrow
    "totalTreasury": 2000000.00,
    "currency": "IDR"
  }
}
```

### 2.2 Make Offering (Top-up)
```http
POST /vault/offering
Authorization: SacredSeal <access_seal>
Content-Type: application/json
Covenant-Key: <uuid>

Request:
{
  "amount": 100000.00,
  "method": "VIRTUAL_ACCOUNT",         // VIRTUAL_ACCOUNT | E_WALLET | BANK_TRANSFER
  "bank": "BCA",                       // for VA: BCA | BNI | BRI | MANDIRI
  "eWalletType": null                  // for E_WALLET: GOPAY | OVO | DANA
}

Response 201:
{
  "success": true,
  "data": {
    "offeringId": "offering-uuid-789",
    "amount": 100000.00,
    "status": "PENDING",
    "paymentDetails": {
      "method": "VIRTUAL_ACCOUNT",
      "vaNumber": "8899500012345678",
      "bank": "BCA",
      "expiryTime": "2026-08-06T23:59:59Z"
    },
    "createdAt": "2026-08-06T20:00:00Z"
  }
}
```

### 2.3 Offering Callback (Midtrans/Xendit Webhook)
```http
POST /vault/webhook/midtrans
Content-Type: application/json

Request:
{
  "orderId": "offering-uuid-789",
  "transactionStatus": "settlement",
  "grossAmount": "100000.00",
  "paymentType": "bank_transfer",
  "transactionTime": "2026-08-06T20:15:00Z"
}

Response 200: { "success": true }
```

### 2.4 Withdrawal Ritual
```http
POST /vault/withdrawal
Authorization: SacredSeal <access_seal>
Content-Type: application/json
Covenant-Key: <uuid>

Request:
{
  "amount": 500000.00,
  "bankAccount": {
    "bankCode": "BCA",
    "accountNumber": "1234567890",
    "accountName": "Wanderer of the Void"
  },
  "pin": "123456"
}

Response 201:
{
  "success": true,
  "data": {
    "withdrawalId": "wdr-uuid-456",
    "amount": 500000.00,
    "tithe": 6500.00,
    "netAmount": 493500.00,
    "status": "PROCESSING",
    "bankAccount": {
      "bankCode": "BCA",
      "accountNumber": "****7890",
      "accountName": "Wanderer of the Void"
    },
    "estimatedCompletion": "2026-08-07T08:00:00Z",
    "createdAt": "2026-08-06T20:00:00Z"
  }
}
```

### 2.5 P2P Tithing (Direct Transfer)
```http
POST /vault/tithing
Authorization: SacredSeal <access_seal>
Content-Type: application/json
Covenant-Key: <uuid>

Request:
{
  "recipientPhone": "+6289876543210",
  "amount": 100000.00,
  "note": "For the shared journey",
  "pin": "123456"
}

Response 200:
{
  "success": true,
  "data": {
    "titheId": "tithe-uuid-123",
    "senderId": "550e8400-e29b-41d4-a716-446655440000",
    "recipientId": "660e8400-e29b-41d4-a716-446655440001",
    "amount": 100000.00,
    "tithe": 0.00,
    "status": "COMPLETED",
    "note": "For the shared journey",
    "createdAt": "2026-08-06T20:00:00Z"
  }
}
```

### 2.6 Read Chronicles (Transaction History)
```http
GET /vault/chronicles?page=1&limit=20&filter=ALL&from=2026-08-01&to=2026-08-06
Authorization: SacredSeal <access_seal>

// filter: ALL | OFFERING | WITHDRAWAL | TITHING | SEAL_HOLD | SEAL_RELEASE | SEAL_REFUND | TITHE

Response 200:
{
  "success": true,
  "data": {
    "chronicles": [
      {
        "id": "chr-uuid-123",
        "type": "SEAL_HOLD",
        "amount": -500000.00,
        "status": "COMPLETED",
        "description": "Seal hold for Covenant CVN-8X2K9",
        "referenceId": "covenant-uuid-456",
        "treasuryAfter": 1000000.00,
        "createdAt": "2026-08-06T19:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 45,
      "totalPages": 3
    }
  }
}
```

### 2.7 Read Single Chronicle
```http
GET /vault/chronicles/:chronicleId
Authorization: SacredSeal <access_seal>

Response 200: { ...single chronicle object with full metadata }
```

### 2.8 List Banks
```http
GET /vault/banks
Authorization: SacredSeal <access_seal>

Response 200:
{
  "success": true,
  "data": {
    "banks": [
      { "code": "BCA", "name": "Bank Central Asia" },
      { "code": "BNI", "name": "Bank Negara Indonesia" },
      { "code": "BRI", "name": "Bank Rakyat Indonesia" },
      { "code": "MANDIRI", "name": "Bank Mandiri" }
    ]
  }
}
```

---

## 3. Covenant Sanctum (`/api/v1/covenant`)

### 3.1 Forge Covenant (Create Room)
```http
POST /covenant
Authorization: SacredSeal <access_seal>
Content-Type: application/json
Covenant-Key: <uuid>

Request:
{
  "counterpartPhone": "+6289876543210",
  "itemName": "Akun Mobile Legends S25",
  "itemDescription": "Akun tier Mythic, hero lengkap, skin 150+",
  "category": "GAME",                  // GAME | MARKETPLACE | SERVICE
  "amount": 750000.00,
  "deadlineHours": 72,
  "pin": "123456"
}

Response 201:
{
  "success": true,
  "data": {
    "covenantId": "covenant-uuid-456",
    "covenantCode": "CVN-8X2K9",
    "initiatorId": "550e8400-e29b-41d4-a716-446655440000",
    "counterpartId": "660e8400-e29b-41d4-a716-446655440001",
    "itemName": "Akun Mobile Legends S25",
    "itemDescription": "Akun tier Mythic, hero lengkap, skin 150+",
    "category": "GAME",
    "amount": 750000.00,
    "status": "FORGED",
    "deadlineAt": "2026-08-09T20:00:00Z",
    "createdAt": "2026-08-06T20:00:00Z",
    "escrow": {
      "sealId": "escrow-uuid-789",
      "amount": 750000.00,
      "status": "SEALED"
    }
  }
}
```

### 3.2 List Covenants
```http
GET /covenant?role=INITIATOR&status=ALL&page=1&limit=20
Authorization: SacredSeal <access_seal>

Response 200:
{
  "success": true,
  "data": {
    "covenants": [
      {
        "covenantId": "covenant-uuid-456",
        "covenantCode": "CVN-8X2K9",
        "role": "INITIATOR",
        "itemName": "Akun Mobile Legends S25",
        "amount": 750000.00,
        "status": "ACCEPTED",
        "otherParty": {
          "id": "660e8400-e29b-41d4-a716-446655440001",
          "fullName": "Keeper of Ruins",
          "phone": "+6289876543210",
          "covenantScore": 4.5
        },
        "deadlineAt": "2026-08-09T20:00:00Z",
        "updatedAt": "2026-08-06T20:05:00Z"
      }
    ],
    "pagination": { "page": 1, "limit": 20, "total": 5 }
  }
}
```

### 3.3 Gaze Upon Covenant (Get Detail)
```http
GET /covenant/:covenantId
Authorization: SacredSeal <access_seal>

Response 200:
{
  "success": true,
  "data": {
    "covenantId": "covenant-uuid-456",
    "covenantCode": "CVN-8X2K9",
    "initiator": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "fullName": "Wanderer of the Void",
      "phone": "+6281234567890",
      "covenantScore": 4.8
    },
    "counterpart": {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "fullName": "Keeper of Ruins",
      "phone": "+6289876543210",
      "covenantScore": 4.5
    },
    "itemName": "Akun Mobile Legends S25",
    "itemDescription": "Akun tier Mythic, hero lengkap, skin 150+",
    "category": "GAME",
    "amount": 750000.00,
    "status": "DELIVERED",
    "timeline": [
      {
        "status": "FORGED",
        "timestamp": "2026-08-06T20:00:00Z",
        "actor": "Wanderer of the Void",
        "note": "Covenant forged, escrow seal initiated"
      },
      {
        "status": "ACCEPTED",
        "timestamp": "2026-08-06T20:05:00Z",
        "actor": "Keeper of Ruins",
        "note": "Counterpart accepted the covenant"
      },
      {
        "status": "DELIVERED",
        "timestamp": "2026-08-06T20:30:00Z",
        "actor": "Keeper of Ruins",
        "note": "Counterpart delivered the item",
        "proofUrl": "https://cdn.eyesofpriestess.id/proofs/proof-123.jpg"
      }
    ],
    "deadlineAt": "2026-08-09T20:00:00Z",
    "canFulfill": true,
    "canBreak": true,
    "canSever": false,
    "escrow": {
      "sealId": "escrow-uuid-789",
      "amount": 750000.00,
      "status": "SEALED"
    },
    "createdAt": "2026-08-06T20:00:00Z",
    "updatedAt": "2026-08-06T20:30:00Z"
  }
}
```

### 3.4 Accept Covenant (Counterpart)
```http
POST /covenant/:covenantId/accept
Authorization: SacredSeal <access_seal>
Content-Type: application/json

Request:
{
  "pin": "123456"
}

Response 200:
{
  "success": true,
  "data": {
    "covenantId": "covenant-uuid-456",
    "status": "ACCEPTED",
    "message": "The covenant is sealed. Fulfill your oath before the deadline."
  }
}
```

### 3.5 Reject Covenant (Counterpart)
```http
POST /covenant/:covenantId/reject
Authorization: SacredSeal <access_seal>

Response 200:
{
  "success": true,
  "data": {
    "covenantId": "covenant-uuid-456",
    "status": "BROKEN",
    "message": "The covenant has been rejected. The sealed treasury returns to the initiator.",
    "refund": {
      "amount": 750000.00,
      "refundedAt": "2026-08-06T20:05:00Z"
    }
  }
}
```

### 3.6 Fulfill Oath (Counterpart Deliver)
```http
POST /covenant/:covenantId/fulfill
Authorization: SacredSeal <access_seal>
Content-Type: multipart/form-data

Request:
{
  "proof": <file>,
  "deliveryNotes": "Username: keeper_ruins, Password: sent via communion",
  "pin": "123456"
}

Response 200:
{
  "success": true,
  "data": {
    "covenantId": "covenant-uuid-456",
    "status": "DELIVERED",
    "proofUrl": "https://cdn.eyesofpriestess.id/proofs/proof-123.jpg",
    "deliveryNotes": "Username: keeper_ruins, Password: sent via communion",
    "deliveredAt": "2026-08-06T20:30:00Z",
    "message": "Oath fulfilled. Awaiting the initiator's confirmation."
  }
}
```

### 3.7 Confirm Fulfillment (Initiator)
```http
POST /covenant/:covenantId/confirm
Authorization: SacredSeal <access_seal>
Content-Type: application/json

Request:
{
  "pin": "123456",
  "rating": 5,
  "review": "Akun sesuai deskripsi, counterpart responsif"
}

Response 200:
{
  "success": true,
  "data": {
    "covenantId": "covenant-uuid-456",
    "status": "FULFILLED",
    "message": "The covenant is complete. The sealed treasury flows to the counterpart.",
    "release": {
      "amount": 750000.00,
      "tithe": 7500.00,
      "netAmount": 742500.00,
      "releasedAt": "2026-08-06T20:35:00Z"
    }
  }
}
```

### 3.8 Sever Covenant (Initiator, before accepted only)
```http
POST /covenant/:covenantId/sever
Authorization: SacredSeal <access_seal>

Response 200:
{
  "success": true,
  "data": {
    "covenantId": "covenant-uuid-456",
    "status": "BROKEN",
    "message": "The covenant has been severed. The sealed treasury returns to you.",
    "refund": {
      "amount": 750000.00,
      "refundedAt": "2026-08-06T20:01:00Z"
    }
  }
}
```

### 3.9 Invoke Judgment (Raise Dispute)
```http
POST /covenant/:covenantId/judgment
Authorization: SacredSeal <access_seal>
Content-Type: application/json

Request:
{
  "reason": "ITEM_NOT_AS_DESCRIBED",
  "description": "Akun tier hanya Epic, bukan Mythic. Skin cuma 20, bukan 150.",
  "evidenceUrls": [
    "https://cdn.eyesofpriestess.id/evidence/evidence-1.jpg"
  ],
  "pin": "123456"
}

Response 201:
{
  "success": true,
  "data": {
    "covenantId": "covenant-uuid-456",
    "status": "JUDGMENT",
    "judgmentId": "judgment-uuid-999",
    "message": "Judgment invoked. The sealed treasury is frozen pending oracle resolution.",
    "judgment": {
      "id": "judgment-uuid-999",
      "reason": "ITEM_NOT_AS_DESCRIBED",
      "status": "OPEN",
      "createdAt": "2026-08-06T20:40:00Z"
    }
  }
}
```

---

## 4. Communion Sanctum (`/api/v1/communion`)

### 4.1 Read Communion Messages
```http
GET /communion/covenant/:covenantId/messages?cursor=&limit=50
Authorization: SacredSeal <access_seal>

Response 200:
{
  "success": true,
  "data": {
    "messages": [
      {
        "id": "msg-uuid-111",
        "senderId": "660e8400-e29b-41d4-a716-446655440001",
        "senderName": "Keeper of Ruins",
        "message": "Halo, akun sudah saya siapkan. Mau dikirim sekarang?",
        "messageType": "TEXT",
        "createdAt": "2026-08-06T20:10:00Z",
        "isRead": true
      }
    ],
    "nextCursor": "base64cursor123",
    "hasMore": false
  }
}
```

### 4.2 Send Message (REST fallback)
```http
POST /communion/covenant/:covenantId/messages
Authorization: SacredSeal <access_seal>
Content-Type: application/json

Request:
{
  "message": "Terima kasih, akun sudah saya cek dan sesuai!",
  "messageType": "TEXT"
}

Response 201: { ...message object }
```

### 4.3 WebSocket Communion
```
Endpoint: ws://localhost:8084/ws/communion?seal=<jwt_access_seal>&covenantId=<covenantId>

// Client → Sanctum
{
  "type": "SEND_MESSAGE",
  "payload": {
    "message": "Halo, barang sudah dikirim",
    "messageType": "TEXT"
  }
}

// Sanctum → Client (broadcast)
{
  "type": "NEW_MESSAGE",
  "payload": {
    "id": "msg-uuid-113",
    "senderId": "660e8400-e29b-41d4-a716-446655440001",
    "senderName": "Keeper of Ruins",
    "message": "Halo, barang sudah dikirim",
    "messageType": "TEXT",
    "createdAt": "2026-08-06T20:30:00Z"
  }
}

// Sanctum → Client (system notification)
{
  "type": "SYSTEM_NOTIFICATION",
  "payload": {
    "covenantId": "covenant-uuid-456",
    "status": "DELIVERED",
    "message": "The counterpart has fulfilled their oath",
    "timestamp": "2026-08-06T20:30:00Z"
  }
}
```

---

## 5. Judgment Sanctum (`/api/v1/judgment`)

### 5.1 List My Judgments
```http
GET /judgment?status=ALL&page=1&limit=20
Authorization: SacredSeal <access_seal>

Response 200:
{
  "success": true,
  "data": {
    "judgments": [
      {
        "judgmentId": "judgment-uuid-999",
        "covenantId": "covenant-uuid-456",
        "covenantCode": "CVN-8X2K9",
        "itemName": "Akun Mobile Legends S25",
        "amount": 750000.00,
        "reason": "ITEM_NOT_AS_DESCRIBED",
        "status": "OPEN",
        "raisedByMe": true,
        "otherParty": {
          "id": "660e8400-e29b-41d4-a716-446655440001",
          "fullName": "Keeper of Ruins"
        },
        "createdAt": "2026-08-06T20:40:00Z"
      }
    ],
    "pagination": { "page": 1, "limit": 20, "total": 1 }
  }
}
```

### 5.2 Gaze Upon Judgment
```http
GET /judgment/:judgmentId
Authorization: SacredSeal <access_seal>

Response 200:
{
  "success": true,
  "data": {
    "judgmentId": "judgment-uuid-999",
    "covenantId": "covenant-uuid-456",
    "covenantCode": "CVN-8X2K9",
    "itemName": "Akun Mobile Legends S25",
    "amount": 750000.00,
    "reason": "ITEM_NOT_AS_DESCRIBED",
    "description": "Akun tier hanya Epic, bukan Mythic. Skin cuma 20, bukan 150.",
    "evidenceUrls": [
      "https://cdn.eyesofpriestess.id/evidence/evidence-1.jpg"
    ],
    "status": "OPEN",
    "raisedBy": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "fullName": "Wanderer of the Void"
    },
    "respondent": {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "fullName": "Keeper of Ruins"
    },
    "resolution": null,
    "oracleNotes": null,
    "createdAt": "2026-08-06T20:40:00Z",
    "updatedAt": "2026-08-06T20:40:00Z"
  }
}
```

### 5.3 Oracle: List All Judgments
```http
GET /judgment/oracle/all?status=OPEN&page=1&limit=20
Authorization: SacredSeal <oracle_access_seal>

Response 200: { ...array of judgments with full details }
```

### 5.4 Oracle: Render Judgment
```http
POST /judgment/:judgmentId/render
Authorization: SacredSeal <oracle_access_seal>
Content-Type: application/json

Request:
{
  "resolution": "REFUND_TO_INITIATOR",
  "splitAmount": null,
  "oracleNotes": "Bukti screenshot menunjukkan akun tier Epic, bukan Mythic. The initiator's claim is just.",
  "evidenceUrls": []
}

Response 200:
{
  "success": true,
  "data": {
    "judgmentId": "judgment-uuid-999",
    "status": "RESOLVED",
    "resolution": "REFUND_TO_INITIATOR",
    "amount": 750000.00,
    "resolvedAt": "2026-08-07T10:00:00Z",
    "message": "Judgment rendered. The sealed treasury returns to the initiator."
  }
}
```

---

## 6. Error Response Standard — The Omen

All errors follow this structure:

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Human readable message",
    "details": {},
    "timestamp": "2026-08-06T20:00:00Z",
    "path": "/api/v1/covenant/covenant-uuid-456/confirm",
    "requestId": "req-uuid-789"
  }
}
```

### Common Omen Codes

| Code | HTTP | Meaning |
|------|------|---------|
| `UNAUTHORIZED` | 401 | Invalid or expired seal |
| `SEAL_REVOKED` | 401 | Seal sanctioned by the Priestess |
| `FORBIDDEN` | 403 | Insufficient attunement |
| `NOT_FOUND` | 404 | Covenant not found in the archives |
| `VALIDATION_ERROR` | 400 | Invalid resonance |
| `INSUFFICIENT_TREASURY` | 400 | Not enough treasury to forge covenant |
| `INVALID_PIN` | 400 | Wrong sanctification seal |
| `PIN_LOCKED` | 403 | Too many failed PIN attempts |
| `COVENANT_INVALID_STATE` | 400 | Action not permitted in current covenant state |
| `RATE_LIMITED` | 429 | Too many invocations |
| `COVENANT_KEY_VIOLATION` | 409 | Duplicate invocation |
| `INTERNAL_ERROR` | 500 | Sanctum error |

---

## 7. The Veil Routing Table

| Path | Sanctum | Port |
|------|---------|------|
| `/api/v1/seal/**` | seal-service | 8081 |
| `/api/v1/vault/**` | vault-service | 8082 |
| `/api/v1/covenant/**` | covenant-service | 8083 |
| `/api/v1/communion/**` | communion-service | 8084 |
| `/api/v1/judgment/**` | judgment-service | 8085 |
| `/ws/communion` | communion-service | 8084 |
| `/vault/webhook/**` | vault-service | 8082 |

---

*Next: Read `03-database-schema.md` for the Archive's complete design.*
