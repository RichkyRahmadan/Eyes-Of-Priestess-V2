# EyesOfPriestess — Database Schema

> **Version:** 1.0  
> **DBMS:** PostgreSQL 15+  
> **Architecture:** 5 schemas terpisah per service domain  
> **Design System:** Warm Editorial

---

## 1. Schema Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    eyesofpriestess DB                       │
├─────────────┬─────────────┬─────────────┬─────────┬─────────┤
│   auth      │   wallet    │    room     │  chat   │ dispute │
├─────────────┼─────────────┼─────────────┼─────────┼─────────┤
│ users       │ wallets     │ rooms       │chat_rooms│disputes │
│ credentials │ transactions│room_participants│messages│dispute_evidence│
│ refresh_tokens│ topup_orders│delivery_proofs│message_reactions│dispute_logs│
│ pin_history │withdraw_reqs│escrow_snapshots│chat_participants│admin_decisions│
│ ban_logs    │bank_accounts│ room_reviews│         │         │
└─────────────┴─────────────┴─────────────┴─────────┴─────────┘
```

---

## 2. Auth Schema

### 2.1 `auth.users`
```sql
CREATE TABLE auth.users (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email               VARCHAR(255) NOT NULL UNIQUE,
    username            VARCHAR(50) NOT NULL UNIQUE,
    full_name           VARCHAR(100) NOT NULL,
    phone_number        VARCHAR(20) UNIQUE,
    role                VARCHAR(20) NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN', 'MODERATOR')),
    is_verified         BOOLEAN NOT NULL DEFAULT FALSE,
    is_pin_set          BOOLEAN NOT NULL DEFAULT FALSE,
    avatar_url          VARCHAR(500),
    ktp_number          VARCHAR(16),           -- encrypted at application layer
    ktp_image_url       VARCHAR(500),
    is_ktp_verified     BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ            -- soft delete
);

CREATE INDEX idx_users_email ON auth.users(email);
CREATE INDEX idx_users_username ON auth.users(username);
CREATE INDEX idx_users_role ON auth.users(role);
```

### 2.2 `auth.credentials`
```sql
CREATE TABLE auth.credentials (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    password_hash       VARCHAR(255) NOT NULL,  -- Argon2id
    pin_hash            VARCHAR(255),           -- Argon2id, nullable until set
    pin_set_at          TIMESTAMPTZ,
    pin_change_required BOOLEAN DEFAULT FALSE,
    failed_pin_attempts INT NOT NULL DEFAULT 0,
    pin_locked_until    TIMESTAMPTZ,
    last_login_at       TIMESTAMPTZ,
    last_login_ip       INET,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_credentials_user_id ON auth.credentials(user_id);
```

### 2.3 `auth.refresh_tokens`
```sql
CREATE TABLE auth.refresh_tokens (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    token_hash          VARCHAR(255) NOT NULL,  -- SHA-256 of token
    device_info         VARCHAR(255),
    ip_address          INET,
    expires_at          TIMESTAMPTZ NOT NULL,
    revoked_at          TIMESTAMPTZ,
    replaced_by         UUID REFERENCES auth.refresh_tokens(id),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_user_id ON auth.refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token_hash ON auth.refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_expires_at ON auth.refresh_tokens(expires_at) WHERE revoked_at IS NULL;
```

### 2.4 `auth.pin_history`
```sql
CREATE TABLE auth.pin_history (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    pin_hash            VARCHAR(255) NOT NULL,
    changed_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    changed_reason      VARCHAR(50) NOT NULL DEFAULT 'USER_INITIATED' CHECK (changed_reason IN ('USER_INITIATED', 'ADMIN_RESET', 'SECURITY_BREACH'))
);

CREATE INDEX idx_pin_history_user_id ON auth.pin_history(user_id);
```

### 2.5 `auth.ban_logs`
```sql
CREATE TABLE auth.ban_logs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    banned_by           UUID NOT NULL REFERENCES auth.users(id),
    ban_type            VARCHAR(20) NOT NULL CHECK (ban_type IN ('FULL', 'TRANSACTION_ONLY', 'ROOM_ONLY')),
    reason              TEXT NOT NULL,
    duration            VARCHAR(20) NOT NULL CHECK (duration IN ('PERMANENT', 'TEMPORARY')),
    duration_hours      INT,                    -- NULL if PERMANENT
    banned_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at          TIMESTAMPTZ,
    unbanned_by         UUID REFERENCES auth.users(id),
    unbanned_at         TIMESTAMPTZ,
    unban_reason        TEXT
);

CREATE INDEX idx_ban_logs_user_id ON auth.ban_logs(user_id);
CREATE INDEX idx_ban_logs_active ON auth.ban_logs(user_id) WHERE unbanned_at IS NULL;
```

---

## 3. Wallet Schema

### 3.1 `wallet.wallets`
```sql
CREATE TABLE wallet.wallets (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL UNIQUE REFERENCES auth.users(id),
    available_balance   BIGINT NOT NULL DEFAULT 0,  -- in smallest currency unit (IDR = rupiah)
    escrow_balance      BIGINT NOT NULL DEFAULT 0,
    currency            VARCHAR(3) NOT NULL DEFAULT 'IDR',
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    frozen_at           TIMESTAMPTZ,
    freeze_reason       TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_wallets_user_id ON wallet.wallets(user_id);
CREATE INDEX idx_wallets_active ON wallet.wallets(user_id, is_active);
```

### 3.2 `wallet.transactions`
```sql
CREATE TABLE wallet.transactions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id           UUID NOT NULL REFERENCES wallet.wallets(id),
    type                VARCHAR(30) NOT NULL CHECK (type IN ('TOPUP', 'WITHDRAW', 'P2P_TRANSFER', 'ESCROW_HOLD', 'ESCROW_RELEASE', 'ESCROW_REFUND', 'FEE', 'ADJUSTMENT')),
    direction           VARCHAR(10) NOT NULL CHECK (direction IN ('IN', 'OUT', 'NEUTRAL')),
    amount              BIGINT NOT NULL,
    fee                 BIGINT NOT NULL DEFAULT 0,
    net_amount          BIGINT NOT NULL,
    status              VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED')) DEFAULT 'PENDING',
    description         VARCHAR(255),
    counterparty_wallet_id UUID REFERENCES wallet.wallets(id),
    counterparty_name   VARCHAR(100),
    room_id             UUID,                     -- nullable, FK to room.rooms
    reference_id        VARCHAR(100),             -- external reference (payment gateway)
    metadata            JSONB,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    settled_at          TIMESTAMPTZ,
    failed_at           TIMESTAMPTZ,
    fail_reason         TEXT
);

CREATE INDEX idx_transactions_wallet_id ON wallet.transactions(wallet_id);
CREATE INDEX idx_transactions_type ON wallet.transactions(type);
CREATE INDEX idx_transactions_status ON wallet.transactions(status);
CREATE INDEX idx_transactions_created_at ON wallet.transactions(created_at DESC);
CREATE INDEX idx_transactions_room_id ON wallet.transactions(room_id);
CREATE INDEX idx_transactions_counterparty ON wallet.transactions(counterparty_wallet_id);
```

### 3.3 `wallet.topup_orders`
```sql
CREATE TABLE wallet.topup_orders (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id           UUID NOT NULL REFERENCES wallet.wallets(id),
    amount              BIGINT NOT NULL,
    fee                 BIGINT NOT NULL DEFAULT 0,
    method              VARCHAR(30) NOT NULL CHECK (method IN ('VIRTUAL_ACCOUNT', 'E_WALLET', 'RETAIL', 'BANK_TRANSFER')),
    bank_code           VARCHAR(20),
    ewallet_type        VARCHAR(20),
    virtual_account_number VARCHAR(50),
    qr_string           TEXT,
    status              VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'PAID', 'SUCCESS', 'FAILED', 'EXPIRED')) DEFAULT 'PENDING',
    payment_gateway_ref VARCHAR(100),
    paid_at             TIMESTAMPTZ,
    settled_at          TIMESTAMPTZ,
    expires_at          TIMESTAMPTZ NOT NULL,
    metadata            JSONB,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_topup_wallet_id ON wallet.topup_orders(wallet_id);
CREATE INDEX idx_topup_status ON wallet.topup_orders(status);
CREATE INDEX idx_topup_gateway_ref ON wallet.topup_orders(payment_gateway_ref);
```

### 3.4 `wallet.withdraw_requests`
```sql
CREATE TABLE wallet.withdraw_requests (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id           UUID NOT NULL REFERENCES wallet.wallets(id),
    bank_account_id     UUID NOT NULL REFERENCES wallet.bank_accounts(id),
    amount              BIGINT NOT NULL,
    fee                 BIGINT NOT NULL DEFAULT 0,
    net_amount          BIGINT NOT NULL,
    status              VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'PROCESSING', 'SUCCESS', 'FAILED')) DEFAULT 'PENDING',
    processed_at        TIMESTAMPTZ,
    processed_by        UUID,
    failure_reason      TEXT,
    estimated_arrival   TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_withdraw_wallet_id ON wallet.withdraw_requests(wallet_id);
CREATE INDEX idx_withdraw_status ON wallet.withdraw_requests(status);
```

### 3.5 `wallet.bank_accounts`
```sql
CREATE TABLE wallet.bank_accounts (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    bank_code           VARCHAR(20) NOT NULL,
    bank_name           VARCHAR(100) NOT NULL,
    account_number      VARCHAR(50) NOT NULL,
    account_holder_name VARCHAR(100) NOT NULL,
    is_primary          BOOLEAN NOT NULL DEFAULT FALSE,
    is_verified         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ,
    UNIQUE(user_id, account_number, bank_code)
);

CREATE INDEX idx_bank_accounts_user_id ON wallet.bank_accounts(user_id);
```

---

## 4. Room Schema

### 4.1 `room.rooms`
```sql
CREATE TABLE room.rooms (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_code           VARCHAR(20) NOT NULL UNIQUE,  -- EOP-ABC123 format
    title               VARCHAR(200) NOT NULL,
    description         TEXT,
    item_category       VARCHAR(50) NOT NULL CHECK (item_category IN ('GAME_ACCOUNT', 'GAME_ITEM', 'DIGITAL_PRODUCT', 'PHYSICAL_PRODUCT', 'SERVICE', 'OTHER')),
    item_price          BIGINT NOT NULL,
    fee                 BIGINT NOT NULL DEFAULT 0,
    total_amount        BIGINT NOT NULL,
    status              VARCHAR(20) NOT NULL CHECK (status IN ('WAITING_PAYMENT', 'FUNDED', 'DELIVERED', 'COMPLETED', 'DISPUTED', 'CANCELLED', 'REFUNDED')) DEFAULT 'WAITING_PAYMENT',
    buyer_id            UUID NOT NULL REFERENCES auth.users(id),
    seller_id           UUID NOT NULL REFERENCES auth.users(id),
    funded_at           TIMESTAMPTZ,
    delivered_at        TIMESTAMPTZ,
    completed_at        TIMESTAMPTZ,
    cancelled_at        TIMESTAMPTZ,
    disputed_at         TIMESTAMPTZ,
    refunded_at         TIMESTAMPTZ,
    auto_release_hours  INT NOT NULL DEFAULT 24,
    auto_release_at     TIMESTAMPTZ,
    chat_room_id        UUID,
    dispute_id          UUID,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT different_buyer_seller CHECK (buyer_id != seller_id)
);

CREATE INDEX idx_rooms_buyer_id ON room.rooms(buyer_id);
CREATE INDEX idx_rooms_seller_id ON room.rooms(seller_id);
CREATE INDEX idx_rooms_status ON room.rooms(status);
CREATE INDEX idx_rooms_room_code ON room.rooms(room_code);
CREATE INDEX idx_rooms_auto_release ON room.rooms(auto_release_at) WHERE status IN ('FUNDED', 'DELIVERED');
```

### 4.2 `room.room_participants`
```sql
CREATE TABLE room.room_participants (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id             UUID NOT NULL REFERENCES room.rooms(id) ON DELETE CASCADE,
    user_id             UUID NOT NULL REFERENCES auth.users(id),
    role                VARCHAR(20) NOT NULL CHECK (role IN ('BUYER', 'SELLER')),
    joined_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    left_at             TIMESTAMPTZ,
    UNIQUE(room_id, user_id)
);

CREATE INDEX idx_room_participants_room_id ON room.room_participants(room_id);
CREATE INDEX idx_room_participants_user_id ON room.room_participants(user_id);
```

### 4.3 `room.delivery_proofs`
```sql
CREATE TABLE room.delivery_proofs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id             UUID NOT NULL REFERENCES room.rooms(id) ON DELETE CASCADE,
    uploaded_by         UUID NOT NULL REFERENCES auth.users(id),
    proof_type          VARCHAR(20) NOT NULL CHECK (proof_type IN ('IMAGE', 'VIDEO', 'DOCUMENT', 'LINK')),
    proof_url           VARCHAR(500) NOT NULL,
    thumbnail_url       VARCHAR(500),
    description         TEXT,
    uploaded_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_delivery_proofs_room_id ON room.delivery_proofs(room_id);
```

### 4.4 `room.escrow_snapshots`
```sql
CREATE TABLE room.escrow_snapshots (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id             UUID NOT NULL REFERENCES room.rooms(id) ON DELETE CASCADE,
    transaction_id      UUID NOT NULL REFERENCES wallet.transactions(id),
    amount_held         BIGINT NOT NULL,
    fee_deducted        BIGINT NOT NULL DEFAULT 0,
    status_at_snapshot  VARCHAR(20) NOT NULL,
    snapshot_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_escrow_snapshots_room_id ON room.escrow_snapshots(room_id);
```

### 4.5 `room.room_reviews`
```sql
CREATE TABLE room.room_reviews (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id             UUID NOT NULL UNIQUE REFERENCES room.rooms(id),
    reviewer_id         UUID NOT NULL REFERENCES auth.users(id),
    reviewee_id         UUID NOT NULL REFERENCES auth.users(id),
    rating              INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment             TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_room_reviews_reviewee ON room.room_reviews(reviewee_id);
```

### 4.6 `room.room_timeline`
```sql
CREATE TABLE room.room_timeline (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id             UUID NOT NULL REFERENCES room.rooms(id) ON DELETE CASCADE,
    status              VARCHAR(20) NOT NULL,
    actor_id            UUID REFERENCES auth.users(id),
    actor_name          VARCHAR(100),
    notes               TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_room_timeline_room_id ON room.room_timeline(room_id);
CREATE INDEX idx_room_timeline_created_at ON room.room_timeline(created_at DESC);
```

---

## 5. Chat Schema

### 5.1 `chat.chat_rooms`
```sql
CREATE TABLE chat.chat_rooms (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    escrow_room_id      UUID UNIQUE REFERENCES room.rooms(id),
    room_name           VARCHAR(200) NOT NULL,
    room_code           VARCHAR(20) NOT NULL,
    created_by          UUID NOT NULL REFERENCES auth.users(id),
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_chat_rooms_escrow_id ON chat.chat_rooms(escrow_room_id);
```

### 5.2 `chat.chat_participants`
```sql
CREATE TABLE chat.chat_participants (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chat_room_id        UUID NOT NULL REFERENCES chat.chat_rooms(id) ON DELETE CASCADE,
    user_id             UUID NOT NULL REFERENCES auth.users(id),
    joined_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_read_at        TIMESTAMPTZ,
    is_typing           BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(chat_room_id, user_id)
);

CREATE INDEX idx_chat_participants_room_id ON chat.chat_participants(chat_room_id);
CREATE INDEX idx_chat_participants_user_id ON chat.chat_participants(user_id);
```

### 5.3 `chat.messages`
```sql
CREATE TABLE chat.messages (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chat_room_id        UUID NOT NULL REFERENCES chat.chat_rooms(id) ON DELETE CASCADE,
    sender_id           UUID NOT NULL REFERENCES auth.users(id),
    content             TEXT NOT NULL,
    message_type        VARCHAR(20) NOT NULL CHECK (message_type IN ('TEXT', 'IMAGE', 'FILE', 'SYSTEM', 'ESCROW_STATUS')) DEFAULT 'TEXT',
    file_url            VARCHAR(500),
    file_name           VARCHAR(255),
    file_size           BIGINT,
    reply_to_id         UUID REFERENCES chat.messages(id),
    is_edited           BOOLEAN NOT NULL DEFAULT FALSE,
    edited_at           TIMESTAMPTZ,
    deleted_at          TIMESTAMPTZ,            -- soft delete
    sent_at             TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_messages_chat_room_id ON chat.messages(chat_room_id);
CREATE INDEX idx_messages_sent_at ON chat.messages(sent_at DESC);
CREATE INDEX idx_messages_sender_id ON chat.messages(sender_id);
```

### 5.4 `chat.message_reactions`
```sql
CREATE TABLE chat.message_reactions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id          UUID NOT NULL REFERENCES chat.messages(id) ON DELETE CASCADE,
    user_id             UUID NOT NULL REFERENCES auth.users(id),
    reaction            VARCHAR(20) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(message_id, user_id, reaction)
);

CREATE INDEX idx_message_reactions_message_id ON chat.message_reactions(message_id);
```

---

## 6. Dispute Schema

### 6.1 `dispute.disputes`
```sql
CREATE TABLE dispute.disputes (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id             UUID NOT NULL REFERENCES room.rooms(id),
    initiated_by        UUID NOT NULL REFERENCES auth.users(id),
    initiator_role      VARCHAR(10) NOT NULL CHECK (initiator_role IN ('BUYER', 'SELLER')),
    title               VARCHAR(200) NOT NULL,
    description         TEXT NOT NULL,
    status              VARCHAR(20) NOT NULL CHECK (status IN ('OPEN', 'UNDER_REVIEW', 'RESOLVED', 'CLOSED')) DEFAULT 'OPEN',
    decision            VARCHAR(30) CHECK (decision IN ('RELEASE_TO_SELLER', 'REFUND_BUYER', 'PARTIAL_REFUND', 'CANCEL')),
    decision_reason     TEXT,
    refund_amount       BIGINT,
    resolved_by         UUID REFERENCES auth.users(id),
    resolved_at         TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_disputes_room_id ON dispute.disputes(room_id);
CREATE INDEX idx_disputes_status ON dispute.disputes(status);
CREATE INDEX idx_disputes_initiated_by ON dispute.disputes(initiated_by);
```

### 6.2 `dispute.dispute_evidence`
```sql
CREATE TABLE dispute.dispute_evidence (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dispute_id          UUID NOT NULL REFERENCES dispute.disputes(id) ON DELETE CASCADE,
    uploaded_by         UUID NOT NULL REFERENCES auth.users(id),
    evidence_type       VARCHAR(20) NOT NULL CHECK (evidence_type IN ('IMAGE', 'VIDEO', 'DOCUMENT', 'CHAT_LOG')),
    file_url            VARCHAR(500) NOT NULL,
    file_name           VARCHAR(255),
    description         TEXT,
    uploaded_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_dispute_evidence_dispute_id ON dispute.dispute_evidence(dispute_id);
```

### 6.3 `dispute.dispute_logs`
```sql
CREATE TABLE dispute.dispute_logs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dispute_id          UUID NOT NULL REFERENCES dispute.disputes(id) ON DELETE CASCADE,
    actor_id            UUID REFERENCES auth.users(id),
    actor_role          VARCHAR(20) NOT NULL CHECK (actor_role IN ('BUYER', 'SELLER', 'ADMIN', 'SYSTEM')),
    action              VARCHAR(50) NOT NULL,
    details             JSONB,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_dispute_logs_dispute_id ON dispute.dispute_logs(dispute_id);
CREATE INDEX idx_dispute_logs_created_at ON dispute.dispute_logs(created_at DESC);
```

### 6.4 `dispute.admin_decisions`
```sql
CREATE TABLE dispute.admin_decisions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dispute_id          UUID NOT NULL UNIQUE REFERENCES dispute.disputes(id),
    admin_id            UUID NOT NULL REFERENCES auth.users(id),
    decision            VARCHAR(30) NOT NULL CHECK (decision IN ('RELEASE_TO_SELLER', 'REFUND_BUYER', 'PARTIAL_REFUND', 'CANCEL')),
    reason              TEXT NOT NULL,
    refund_amount       BIGINT,
    transaction_id      UUID REFERENCES wallet.transactions(id),
    decided_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

---

## 7. Views

### 7.1 `wallet.vw_wallet_summary`
```sql
CREATE VIEW wallet.vw_wallet_summary AS
SELECT 
    w.id AS wallet_id,
    w.user_id,
    u.full_name,
    u.username,
    w.available_balance,
    w.escrow_balance,
    w.available_balance + w.escrow_balance AS total_balance,
    w.currency,
    w.is_active,
    w.created_at
FROM wallet.wallets w
JOIN auth.users u ON w.user_id = u.id
WHERE u.deleted_at IS NULL;
```

### 7.2 `room.vw_room_summary`
```sql
CREATE VIEW room.vw_room_summary AS
SELECT 
    r.id,
    r.room_code,
    r.title,
    r.item_category,
    r.item_price,
    r.status,
    r.buyer_id,
    bu.full_name AS buyer_name,
    r.seller_id,
    su.full_name AS seller_name,
    r.created_at,
    r.auto_release_at
FROM room.rooms r
JOIN auth.users bu ON r.buyer_id = bu.id
JOIN auth.users su ON r.seller_id = su.id;
```

### 7.3 `dispute.vw_dispute_summary`
```sql
CREATE VIEW dispute.vw_dispute_summary AS
SELECT 
    d.id,
    d.room_id,
    r.room_code,
    d.title,
    d.status,
    d.initiated_by,
    i.full_name AS initiator_name,
    d.initiator_role,
    d.decision,
    d.resolved_by,
    a.full_name AS resolver_name,
    d.created_at,
    d.resolved_at
FROM dispute.disputes d
JOIN room.rooms r ON d.room_id = r.id
JOIN auth.users i ON d.initiated_by = i.id
LEFT JOIN auth.users a ON d.resolved_by = a.id;
```

---

## 8. Functions & Triggers

### 8.1 Auto-update `updated_at`
```sql
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply to all tables with updated_at
CREATE TRIGGER trg_users_updated_at BEFORE UPDATE ON auth.users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trg_wallets_updated_at BEFORE UPDATE ON wallet.wallets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trg_rooms_updated_at BEFORE UPDATE ON room.rooms
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trg_chat_rooms_updated_at BEFORE UPDATE ON chat.chat_rooms
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trg_disputes_updated_at BEFORE UPDATE ON dispute.disputes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trg_bank_accounts_updated_at BEFORE UPDATE ON wallet.bank_accounts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

### 8.2 Auto-create wallet on user registration
```sql
CREATE OR REPLACE FUNCTION create_wallet_for_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO wallet.wallets (user_id, currency)
    VALUES (NEW.id, 'IDR');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_create_wallet_after_user_insert
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION create_wallet_for_new_user();
```

### 8.3 Auto-create chat room on escrow room creation
```sql
CREATE OR REPLACE FUNCTION create_chat_room_for_escrow()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO chat.chat_rooms (escrow_room_id, room_name, room_code, created_by)
    VALUES (NEW.id, NEW.title, NEW.room_code, NEW.buyer_id);

    UPDATE room.rooms 
    SET chat_room_id = (SELECT id FROM chat.chat_rooms WHERE escrow_room_id = NEW.id)
    WHERE id = NEW.id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_create_chat_room_after_room_insert
    AFTER INSERT ON room.rooms
    FOR EACH ROW EXECUTE FUNCTION create_chat_room_for_escrow();
```

### 8.4 Log room status changes
```sql
CREATE OR REPLACE FUNCTION log_room_timeline()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.status IS DISTINCT FROM NEW.status THEN
        INSERT INTO room.room_timeline (room_id, status, actor_id, notes)
        VALUES (NEW.id, NEW.status, NULL, 'Status changed from ' || COALESCE(OLD.status, 'NULL') || ' to ' || NEW.status);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_log_room_timeline
    AFTER UPDATE ON room.rooms
    FOR EACH ROW EXECUTE FUNCTION log_room_timeline();
```

---

## 9. Migration Order

```sql
-- 001_create_auth_schema.sql
-- 002_create_wallet_schema.sql
-- 003_create_room_schema.sql
-- 004_create_chat_schema.sql
-- 005_create_dispute_schema.sql
-- 006_create_functions.sql
-- 007_create_views.sql
-- 008_create_indexes.sql
-- 009_seed_admin_user.sql
```

---

## 10. ER Diagram (Text)

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│ auth.users  │◄──────┤wallet.wallets│       │room.rooms   │
│─────────────│  1:1  │─────────────│       │─────────────│
│ id (PK)     │       │ id (PK)     │       │ id (PK)     │
│ email       │       │ user_id(FK) │       │ buyer_id(FK)│
│ full_name   │       │ available   │       │ seller_id(FK)│
│ role        │       │ escrow      │       │ status      │
└──────┬──────┘       └──────┬──────┘       │ chat_room_id│
       │                     │              └──────┬──────┘
       │                     │                     │
       │              ┌──────┴──────┐              │
       │              │wallet.trans │              │
       │              │actions      │              │
       │              │─────────────│              │
       │              │ wallet_id   │              │
       │              │ room_id     │◄─────────────┘
       │              └─────────────┘
       │
       │       ┌─────────────┐       ┌─────────────┐
       └──────►│chat.chat_rooms│◄────┤dispute.disputes│
               │─────────────│ 1:1   │─────────────│
               │ escrow_room_id(FK)   │ room_id(FK) │
               │ id (PK)     │       │ id (PK)     │
               └──────┬──────┘       └─────────────┘
                      │
               ┌──────┴──────┐
               │chat.messages│
               │─────────────│
               │ chat_room_id│
               │ sender_id   │
               └─────────────┘
```

---

*End of Database Schema*
