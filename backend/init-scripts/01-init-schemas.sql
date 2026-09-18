-- ═══════════════════════════════════════════════════════════════════════════════
-- EyesOfPriestess — PostgreSQL Initialization Script
-- File: 01-init-schemas.sql
-- Description: Create all 5 service domain schemas and tables
-- Run: Automatically via docker-entrypoint-initdb.d on first postgres startup
-- ═══════════════════════════════════════════════════════════════════════════════

-- ─── EXTENSIONS ─────────────────────────────────────────────────────────────
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ─── DOMAIN SCHEMAS ─────────────────────────────────────────────────────────
CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS wallet;
CREATE SCHEMA IF NOT EXISTS room;
CREATE SCHEMA IF NOT EXISTS chat;
CREATE SCHEMA IF NOT EXISTS dispute;

-- ═══════════════════════════════════════════════════════════════════════════════
-- AUTH SCHEMA — Authentication & User Identity
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS auth.users (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email               VARCHAR(255) NOT NULL UNIQUE,
    username            VARCHAR(50) NOT NULL UNIQUE,
    full_name           VARCHAR(100) NOT NULL,
    phone_number        VARCHAR(20) UNIQUE,
    role                VARCHAR(20) NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN', 'MODERATOR')),
    is_verified         BOOLEAN NOT NULL DEFAULT FALSE,
    is_pin_set          BOOLEAN NOT NULL DEFAULT FALSE,
    avatar_url          VARCHAR(500),
    ktp_number          VARCHAR(16),
    ktp_image_url       VARCHAR(500),
    is_ktp_verified     BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_users_email ON auth.users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON auth.users(username);
CREATE INDEX IF NOT EXISTS idx_users_role ON auth.users(role);

CREATE TABLE IF NOT EXISTS auth.credentials (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    password_hash       VARCHAR(255) NOT NULL,
    pin_hash            VARCHAR(255),
    pin_set_at          TIMESTAMPTZ,
    pin_change_required BOOLEAN DEFAULT FALSE,
    failed_pin_attempts INT NOT NULL DEFAULT 0,
    pin_locked_until    TIMESTAMPTZ,
    last_login_at       TIMESTAMPTZ,
    last_login_ip       INET,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_credentials_user_id ON auth.credentials(user_id);

CREATE TABLE IF NOT EXISTS auth.refresh_tokens (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    token_hash          VARCHAR(255) NOT NULL,
    device_info         VARCHAR(255),
    ip_address          INET,
    expires_at          TIMESTAMPTZ NOT NULL,
    revoked_at          TIMESTAMPTZ,
    replaced_by         UUID REFERENCES auth.refresh_tokens(id),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON auth.refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token_hash ON auth.refresh_tokens(token_hash);

CREATE TABLE IF NOT EXISTS auth.pin_history (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    pin_hash            VARCHAR(255) NOT NULL,
    changed_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    changed_reason      VARCHAR(50) NOT NULL DEFAULT 'USER_INITIATED' CHECK (changed_reason IN ('USER_INITIATED', 'ADMIN_RESET', 'SECURITY_BREACH'))
);

CREATE INDEX IF NOT EXISTS idx_pin_history_user_id ON auth.pin_history(user_id);

CREATE TABLE IF NOT EXISTS auth.ban_logs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    banned_by           UUID NOT NULL REFERENCES auth.users(id),
    ban_type            VARCHAR(20) NOT NULL CHECK (ban_type IN ('FULL', 'TRANSACTION_ONLY', 'ROOM_ONLY')),
    reason              TEXT NOT NULL,
    duration            VARCHAR(20) NOT NULL CHECK (duration IN ('PERMANENT', 'TEMPORARY')),
    duration_hours      INT,
    banned_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at          TIMESTAMPTZ,
    unbanned_by         UUID REFERENCES auth.users(id),
    unbanned_at         TIMESTAMPTZ,
    unban_reason        TEXT
);

CREATE INDEX IF NOT EXISTS idx_ban_logs_user_id ON auth.ban_logs(user_id);

-- ═══════════════════════════════════════════════════════════════════════════════
-- WALLET SCHEMA — E-Wallet & Transactions
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS wallet.wallets (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL UNIQUE REFERENCES auth.users(id),
    available_balance   BIGINT NOT NULL DEFAULT 0,
    escrow_balance      BIGINT NOT NULL DEFAULT 0,
    currency            VARCHAR(3) NOT NULL DEFAULT 'IDR',
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    frozen_at           TIMESTAMPTZ,
    freeze_reason       TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_wallets_user_id ON wallet.wallets(user_id);

CREATE TABLE IF NOT EXISTS wallet.bank_accounts (
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

CREATE INDEX IF NOT EXISTS idx_bank_accounts_user_id ON wallet.bank_accounts(user_id);

CREATE TABLE IF NOT EXISTS wallet.transactions (
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
    room_id             UUID,
    reference_id        VARCHAR(100),
    metadata            JSONB,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    settled_at          TIMESTAMPTZ,
    failed_at           TIMESTAMPTZ,
    fail_reason         TEXT
);

CREATE INDEX IF NOT EXISTS idx_transactions_wallet_id ON wallet.transactions(wallet_id);
CREATE INDEX IF NOT EXISTS idx_transactions_type ON wallet.transactions(type);
CREATE INDEX IF NOT EXISTS idx_transactions_status ON wallet.transactions(status);
CREATE INDEX IF NOT EXISTS idx_transactions_created_at ON wallet.transactions(created_at DESC);

CREATE TABLE IF NOT EXISTS wallet.topup_orders (
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

CREATE INDEX IF NOT EXISTS idx_topup_wallet_id ON wallet.topup_orders(wallet_id);

CREATE TABLE IF NOT EXISTS wallet.withdraw_requests (
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

CREATE INDEX IF NOT EXISTS idx_withdraw_wallet_id ON wallet.withdraw_requests(wallet_id);

-- ═══════════════════════════════════════════════════════════════════════════════
-- ROOM SCHEMA — Escrow Rooms Engine
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS room.rooms (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_code           VARCHAR(20) NOT NULL UNIQUE,
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

CREATE INDEX IF NOT EXISTS idx_rooms_buyer_id ON room.rooms(buyer_id);
CREATE INDEX IF NOT EXISTS idx_rooms_seller_id ON room.rooms(seller_id);
CREATE INDEX IF NOT EXISTS idx_rooms_status ON room.rooms(status);
CREATE INDEX IF NOT EXISTS idx_rooms_room_code ON room.rooms(room_code);

CREATE TABLE IF NOT EXISTS room.room_participants (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id             UUID NOT NULL REFERENCES room.rooms(id) ON DELETE CASCADE,
    user_id             UUID NOT NULL REFERENCES auth.users(id),
    role                VARCHAR(20) NOT NULL CHECK (role IN ('BUYER', 'SELLER')),
    joined_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    left_at             TIMESTAMPTZ,
    UNIQUE(room_id, user_id)
);

CREATE TABLE IF NOT EXISTS room.delivery_proofs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id             UUID NOT NULL REFERENCES room.rooms(id) ON DELETE CASCADE,
    uploaded_by         UUID NOT NULL REFERENCES auth.users(id),
    proof_type          VARCHAR(20) NOT NULL CHECK (proof_type IN ('IMAGE', 'VIDEO', 'DOCUMENT', 'LINK')),
    proof_url           VARCHAR(500) NOT NULL,
    thumbnail_url       VARCHAR(500),
    description         TEXT,
    uploaded_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS room.escrow_snapshots (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id             UUID NOT NULL REFERENCES room.rooms(id) ON DELETE CASCADE,
    transaction_id      UUID NOT NULL REFERENCES wallet.transactions(id),
    amount_held         BIGINT NOT NULL,
    fee_deducted        BIGINT NOT NULL DEFAULT 0,
    status_at_snapshot  VARCHAR(20) NOT NULL,
    snapshot_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS room.room_reviews (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id             UUID NOT NULL UNIQUE REFERENCES room.rooms(id),
    reviewer_id         UUID NOT NULL REFERENCES auth.users(id),
    reviewee_id         UUID NOT NULL REFERENCES auth.users(id),
    rating              INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment             TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS room.room_timeline (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id             UUID NOT NULL REFERENCES room.rooms(id) ON DELETE CASCADE,
    status              VARCHAR(20) NOT NULL,
    actor_id            UUID REFERENCES auth.users(id),
    actor_name          VARCHAR(100),
    notes               TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- CHAT SCHEMA — Real-time Messaging
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS chat.chat_rooms (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    escrow_room_id      UUID UNIQUE REFERENCES room.rooms(id),
    room_name           VARCHAR(200) NOT NULL,
    room_code           VARCHAR(20) NOT NULL,
    created_by          UUID NOT NULL REFERENCES auth.users(id),
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS chat.chat_participants (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chat_room_id        UUID NOT NULL REFERENCES chat.chat_rooms(id) ON DELETE CASCADE,
    user_id             UUID NOT NULL REFERENCES auth.users(id),
    joined_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_read_at        TIMESTAMPTZ,
    is_typing           BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(chat_room_id, user_id)
);

CREATE TABLE IF NOT EXISTS chat.messages (
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
    deleted_at          TIMESTAMPTZ,
    sent_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_messages_chat_room_id ON chat.messages(chat_room_id);

CREATE TABLE IF NOT EXISTS chat.message_reactions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id          UUID NOT NULL REFERENCES chat.messages(id) ON DELETE CASCADE,
    user_id             UUID NOT NULL REFERENCES auth.users(id),
    reaction            VARCHAR(20) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(message_id, user_id, reaction)
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- DISPUTE SCHEMA — Arbitration & Evidence
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS dispute.disputes (
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

CREATE INDEX IF NOT EXISTS idx_disputes_room_id ON dispute.disputes(room_id);

CREATE TABLE IF NOT EXISTS dispute.dispute_evidence (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dispute_id          UUID NOT NULL REFERENCES dispute.disputes(id) ON DELETE CASCADE,
    uploaded_by         UUID NOT NULL REFERENCES auth.users(id),
    evidence_type       VARCHAR(20) NOT NULL CHECK (evidence_type IN ('IMAGE', 'VIDEO', 'DOCUMENT', 'CHAT_LOG')),
    file_url            VARCHAR(500) NOT NULL,
    file_name           VARCHAR(255),
    description         TEXT,
    uploaded_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS dispute.dispute_logs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dispute_id          UUID NOT NULL REFERENCES dispute.disputes(id) ON DELETE CASCADE,
    actor_id            UUID REFERENCES auth.users(id),
    actor_role          VARCHAR(20) NOT NULL CHECK (actor_role IN ('BUYER', 'SELLER', 'ADMIN', 'SYSTEM')),
    action              VARCHAR(50) NOT NULL,
    details             JSONB,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS dispute.admin_decisions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dispute_id          UUID NOT NULL UNIQUE REFERENCES dispute.disputes(id),
    admin_id            UUID NOT NULL REFERENCES auth.users(id),
    decision            VARCHAR(30) NOT NULL CHECK (decision IN ('RELEASE_TO_SELLER', 'REFUND_BUYER', 'PARTIAL_REFUND', 'CANCEL')),
    reason              TEXT NOT NULL,
    refund_amount       BIGINT,
    transaction_id      UUID REFERENCES wallet.transactions(id),
    decided_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- SEED ADMIN USER
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO auth.users (
    id, email, username, full_name, role, is_verified, is_pin_set
) VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'admin@eyesofpriestess.com',
    'admin',
    'System Admin',
    'ADMIN',
    TRUE,
    TRUE
) ON CONFLICT (email) DO NOTHING;

INSERT INTO wallet.wallets (
    user_id, available_balance, escrow_balance, currency
) VALUES (
    'a0000000-0000-0000-0000-000000000001',
    1000000000,
    0,
    'IDR'
) ON CONFLICT (user_id) DO NOTHING;
