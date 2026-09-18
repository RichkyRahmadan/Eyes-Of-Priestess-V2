-- ═══════════════════════════════════════════════════════════════════════════════
-- EyesOfPriestess — Schema Unification Patch
-- Bridges entity column expectations across all 5 microservices
-- ═══════════════════════════════════════════════════════════════════════════════

-- ─── 1. AUTH SCHEMA ──────────────────────────────────────────────────────────
ALTER TABLE auth.users ADD COLUMN IF NOT EXISTS phone VARCHAR(20);
ALTER TABLE auth.users ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);
ALTER TABLE auth.users ADD COLUMN IF NOT EXISTS pin_hash VARCHAR(255);
ALTER TABLE auth.users ADD COLUMN IF NOT EXISTS profile_photo_url VARCHAR(500);
ALTER TABLE auth.users ADD COLUMN IF NOT EXISTS attunement_status VARCHAR(20) DEFAULT 'UNATTUNED';
ALTER TABLE auth.users ADD COLUMN IF NOT EXISTS attunement_id_hash VARCHAR(255);
ALTER TABLE auth.users ADD COLUMN IF NOT EXISTS attunement_verified_at TIMESTAMPTZ;
ALTER TABLE auth.users ADD COLUMN IF NOT EXISTS is_oracle BOOLEAN DEFAULT FALSE;
ALTER TABLE auth.users ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'ATTUNED';
ALTER TABLE auth.users ADD COLUMN IF NOT EXISTS pin_failed_attempts INT DEFAULT 0;
ALTER TABLE auth.users ADD COLUMN IF NOT EXISTS pin_locked_until TIMESTAMPTZ;
ALTER TABLE auth.users ADD COLUMN IF NOT EXISTS login_failed_attempts INT DEFAULT 0;
ALTER TABLE auth.users ADD COLUMN IF NOT EXISTS login_locked_until TIMESTAMPTZ;
ALTER TABLE auth.users ADD COLUMN IF NOT EXISTS covenant_score NUMERIC(2,1) DEFAULT 5.0;
ALTER TABLE auth.users ADD COLUMN IF NOT EXISTS total_covenants INT DEFAULT 0;
ALTER TABLE auth.users ADD COLUMN IF NOT EXISTS fulfilled_covenants INT DEFAULT 0;
ALTER TABLE auth.users ADD COLUMN IF NOT EXISTS judgment_count INT DEFAULT 0;
ALTER TABLE auth.users ADD COLUMN IF NOT EXISTS attuned_at TIMESTAMPTZ DEFAULT NOW();
ALTER TABLE auth.users ADD COLUMN IF NOT EXISTS last_rite_at TIMESTAMPTZ;

-- Sync phone with phone_number for existing seeded records
UPDATE auth.users SET phone = phone_number WHERE phone IS NULL AND phone_number IS NOT NULL;
UPDATE auth.users SET phone_number = phone WHERE phone_number IS NULL AND phone IS NOT NULL;

-- Populate password_hash and pin_hash from auth.credentials for seeded users
UPDATE auth.users u
SET password_hash = c.password_hash,
    pin_hash = c.pin_hash
FROM auth.credentials c
WHERE c.user_id = u.id AND (u.password_hash IS NULL OR u.pin_hash IS NULL);

-- Unique index on phone
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_phone ON auth.users(phone);

-- Sync trigger between phone and phone_number
CREATE OR REPLACE FUNCTION auth.sync_user_phone()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.phone IS NOT NULL AND (NEW.phone_number IS NULL OR NEW.phone_number = '') THEN
        NEW.phone_number = NEW.phone;
    ELSIF NEW.phone_number IS NOT NULL AND (NEW.phone IS NULL OR NEW.phone = '') THEN
        NEW.phone = NEW.phone_number;
    END IF;
    IF NEW.username IS NULL OR NEW.username = '' THEN
        NEW.username = COALESCE(NEW.phone, 'user_' || SUBSTRING(REPLACE(NEW.id::text, '-', '') FROM 1 FOR 8));
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_sync_user_phone ON auth.users;
CREATE TRIGGER trg_sync_user_phone
    BEFORE INSERT OR UPDATE ON auth.users
    FOR EACH ROW EXECUTE FUNCTION auth.sync_user_phone();

-- Table auth.sessions
CREATE TABLE IF NOT EXISTS auth.sessions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pilgrim_id          UUID NOT NULL,
    refresh_seal_jti    VARCHAR(255) NOT NULL UNIQUE,
    device_id           VARCHAR(255),
    device_info         JSONB,
    expires_at          TIMESTAMPTZ NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    severed_at          TIMESTAMPTZ,
    severed_reason      VARCHAR(50)
);
CREATE INDEX IF NOT EXISTS idx_sessions_pilgrim_id ON auth.sessions(pilgrim_id);

-- Table auth.sanction_records
CREATE TABLE IF NOT EXISTS auth.sanction_records (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pilgrim_id          UUID NOT NULL,
    sanctioned_by       UUID,
    reason              VARCHAR(50) NOT NULL,
    description         TEXT,
    sanction_duration   VARCHAR(20) NOT NULL,
    sanctioned_until    TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    revoked_at          TIMESTAMPTZ,
    revoked_by          UUID
);

-- ─── 2. WALLET SCHEMA ────────────────────────────────────────────────────────
ALTER TABLE wallet.wallets ADD COLUMN IF NOT EXISTS pilgrim_id UUID;
ALTER TABLE wallet.wallets ADD COLUMN IF NOT EXISTS available_treasury NUMERIC(15,2) DEFAULT 0;
ALTER TABLE wallet.wallets ADD COLUMN IF NOT EXISTS sealed_treasury NUMERIC(15,2) DEFAULT 0;
ALTER TABLE wallet.wallets ADD COLUMN IF NOT EXISTS total_offered NUMERIC(15,2) DEFAULT 0;
ALTER TABLE wallet.wallets ADD COLUMN IF NOT EXISTS total_withdrawn NUMERIC(15,2) DEFAULT 0;
ALTER TABLE wallet.wallets ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'ACTIVE';
ALTER TABLE wallet.wallets ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

UPDATE wallet.wallets SET pilgrim_id = user_id WHERE pilgrim_id IS NULL AND user_id IS NOT NULL;
UPDATE wallet.wallets SET available_treasury = available_balance WHERE available_treasury = 0 AND available_balance > 0;

CREATE UNIQUE INDEX IF NOT EXISTS idx_wallets_pilgrim_id ON wallet.wallets(pilgrim_id);

CREATE OR REPLACE FUNCTION wallet.sync_wallet_columns()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.user_id IS NOT NULL AND NEW.pilgrim_id IS NULL THEN
        NEW.pilgrim_id = NEW.user_id;
    ELSIF NEW.pilgrim_id IS NOT NULL AND NEW.user_id IS NULL THEN
        NEW.user_id = NEW.pilgrim_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_sync_wallet_columns ON wallet.wallets;
CREATE TRIGGER trg_sync_wallet_columns
    BEFORE INSERT OR UPDATE ON wallet.wallets
    FOR EACH ROW EXECUTE FUNCTION wallet.sync_wallet_columns();

-- Transactions
ALTER TABLE wallet.transactions ADD COLUMN IF NOT EXISTS treasury_id UUID;
ALTER TABLE wallet.transactions ADD COLUMN IF NOT EXISTS tithe NUMERIC(15,2) DEFAULT 0;
ALTER TABLE wallet.transactions ADD COLUMN IF NOT EXISTS reference_type VARCHAR(30);
ALTER TABLE wallet.transactions ADD COLUMN IF NOT EXISTS counterparty_treasury_id UUID;
ALTER TABLE wallet.transactions ADD COLUMN IF NOT EXISTS counterparty_name VARCHAR(100);
ALTER TABLE wallet.transactions ADD COLUMN IF NOT EXISTS metadata JSONB;
ALTER TABLE wallet.transactions ADD COLUMN IF NOT EXISTS recorded_at TIMESTAMPTZ DEFAULT NOW();
ALTER TABLE wallet.transactions ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NOW();

UPDATE wallet.transactions SET treasury_id = wallet_id WHERE treasury_id IS NULL AND wallet_id IS NOT NULL;

-- Table wallet.topUpOrders
CREATE TABLE IF NOT EXISTS wallet."topUpOrders" (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    treasury_id             UUID NOT NULL,
    amount                  NUMERIC(15,2) NOT NULL,
    tithe                   NUMERIC(15,2) DEFAULT 0,
    gateway                 VARCHAR(20) NOT NULL DEFAULT 'XENDIT',
    gateway_transaction_id  VARCHAR(255),
    payment_method          VARCHAR(30) NOT NULL,
    payment_details         JSONB,
    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    expires_at              TIMESTAMPTZ,
    accepted_at             TIMESTAMPTZ,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    covenant_key            VARCHAR(100) UNIQUE
);

-- Table wallet.withdrawals
CREATE TABLE IF NOT EXISTS wallet.withdrawals (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    treasury_id             UUID NOT NULL,
    amount                  NUMERIC(15,2) NOT NULL,
    tithe                   NUMERIC(15,2) DEFAULT 0,
    net_amount              NUMERIC(15,2) NOT NULL,
    bank_code               VARCHAR(20) NOT NULL,
    bank_name               VARCHAR(100) NOT NULL,
    account_number_hash     VARCHAR(255) NOT NULL,
    account_number_masked   VARCHAR(20) NOT NULL,
    account_name            VARCHAR(100) NOT NULL,
    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    gateway                 VARCHAR(20) DEFAULT 'XENDIT',
    gateway_transaction_id  VARCHAR(255),
    processed_at            TIMESTAMPTZ,
    completed_at            TIMESTAMPTZ,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    covenant_key            VARCHAR(100) UNIQUE
);

-- ─── 3. ROOM SCHEMA ──────────────────────────────────────────────────────────
ALTER TABLE room.rooms ADD COLUMN IF NOT EXISTS initiator_id UUID;
ALTER TABLE room.rooms ADD COLUMN IF NOT EXISTS initiator_role VARCHAR(20) DEFAULT 'BUYER';
ALTER TABLE room.rooms ADD COLUMN IF NOT EXISTS counterparty_id UUID;
ALTER TABLE room.rooms ADD COLUMN IF NOT EXISTS amount NUMERIC(19,2) DEFAULT 0;
ALTER TABLE room.rooms ADD COLUMN IF NOT EXISTS auto_release_hours INT DEFAULT 48;
ALTER TABLE room.rooms ADD COLUMN IF NOT EXISTS expires_at TIMESTAMPTZ;
ALTER TABLE room.rooms ADD COLUMN IF NOT EXISTS sealed_at TIMESTAMPTZ;
ALTER TABLE room.rooms ADD COLUMN IF NOT EXISTS fulfilled_at TIMESTAMPTZ;

-- Table room.room_invitations
CREATE TABLE IF NOT EXISTS room.room_invitations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id         UUID NOT NULL,
    invitee_phone   VARCHAR(20) NOT NULL,
    role            VARCHAR(20) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at      TIMESTAMPTZ NOT NULL
);

-- ─── 4. CHAT SCHEMA ──────────────────────────────────────────────────────────
ALTER TABLE chat.messages ADD COLUMN IF NOT EXISTS covenant_id UUID;
ALTER TABLE chat.messages ADD COLUMN IF NOT EXISTS sender_display VARCHAR(100);
ALTER TABLE chat.messages ADD COLUMN IF NOT EXISTS is_read BOOLEAN DEFAULT FALSE;
ALTER TABLE chat.messages ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ DEFAULT NOW();
ALTER TABLE chat.messages ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NOW();

-- ─── 5. DISPUTE SCHEMA ───────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS dispute.judgment_cases (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    covenant_id                 UUID NOT NULL UNIQUE,
    initiator_id                UUID NOT NULL,
    counterparty_id             UUID NOT NULL,
    reason                      VARCHAR(255) NOT NULL,
    description                 TEXT,
    initiator_evidence_url      VARCHAR(500),
    counterparty_evidence_url   VARCHAR(500),
    status                      VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    resolution_type             VARCHAR(30),
    oracle_id                   UUID,
    oracle_notes                TEXT,
    split_percentage            INT,
    opened_at                   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deliberating_at             TIMESTAMPTZ,
    rendered_at                 TIMESTAMPTZ,
    updated_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ─── 6. ROOM VERSION & CHAT/WALLET HARMONIZATION ────────────────────────────
ALTER TABLE room.rooms ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE chat.messages ADD COLUMN IF NOT EXISTS type VARCHAR(20) DEFAULT 'USER';
UPDATE chat.messages SET type = COALESCE(message_type, 'USER') WHERE type IS NULL;

CREATE TABLE IF NOT EXISTS wallet.topuporders (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    treasury_id             UUID NOT NULL,
    amount                  NUMERIC(15,2) NOT NULL,
    tithe                   NUMERIC(15,2) NOT NULL DEFAULT 0,
    gateway                 VARCHAR(20) NOT NULL DEFAULT 'XENDIT',
    gateway_transaction_id  VARCHAR(255),
    payment_method          VARCHAR(30) NOT NULL,
    payment_details         JSONB,
    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    expires_at              TIMESTAMPTZ,
    accepted_at             TIMESTAMPTZ,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    covenant_key            VARCHAR(100) UNIQUE
);

UPDATE wallet.transactions SET treasury_id = wallet_id WHERE treasury_id IS NULL;
UPDATE wallet.transactions SET recorded_at = created_at WHERE recorded_at IS NULL;
ALTER TABLE wallet.transactions ADD COLUMN IF NOT EXISTS completed_at TIMESTAMPTZ;
UPDATE wallet.transactions SET completed_at = COALESCE(settled_at, created_at) WHERE completed_at IS NULL;
ALTER TABLE wallet.transactions ADD COLUMN IF NOT EXISTS covenant_key VARCHAR(100);
ALTER TABLE wallet.transactions ADD COLUMN IF NOT EXISTS treasury_before NUMERIC(15,2) DEFAULT 0;
ALTER TABLE wallet.transactions DROP CONSTRAINT IF EXISTS transactions_status_check;
ALTER TABLE wallet.transactions ADD CONSTRAINT transactions_status_check CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'SUCCESS', 'FAILED', 'CANCELLED'));
ALTER TABLE wallet.transactions DROP CONSTRAINT IF EXISTS transactions_type_check;
ALTER TABLE wallet.transactions DROP CONSTRAINT IF EXISTS transactions_direction_check;
ALTER TABLE wallet.transactions ALTER COLUMN direction DROP NOT NULL;
ALTER TABLE wallet.transactions ALTER COLUMN wallet_id DROP NOT NULL;
ALTER TABLE wallet.transactions ALTER COLUMN fee DROP NOT NULL;
ALTER TABLE wallet.transactions ALTER COLUMN amount DROP NOT NULL;
UPDATE wallet.transactions SET status = 'COMPLETED' WHERE status = 'SUCCESS';

CREATE OR REPLACE FUNCTION wallet.sync_transaction_cols()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.wallet_id IS NULL THEN
        NEW.wallet_id = NEW.treasury_id;
    END IF;
    IF NEW.treasury_id IS NULL THEN
        NEW.treasury_id = NEW.wallet_id;
    END IF;
    IF NEW.direction IS NULL THEN
        NEW.direction = 'IN';
    END IF;
    IF NEW.amount IS NULL THEN
        NEW.amount = COALESCE(NEW.net_amount, 0);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_sync_transaction_cols ON wallet.transactions;
CREATE TRIGGER trg_sync_transaction_cols
    BEFORE INSERT OR UPDATE ON wallet.transactions
    FOR EACH ROW EXECUTE FUNCTION wallet.sync_transaction_cols();

UPDATE chat.messages SET type = 'USER' WHERE type NOT IN ('USER', 'SYSTEM');

-- ─── 7. ROOM SCHEMA HARMONIZATION & INVITATIONS ─────────────────────────────
ALTER TABLE room.rooms ALTER COLUMN item_category DROP NOT NULL;
ALTER TABLE room.rooms ALTER COLUMN item_price DROP NOT NULL;
ALTER TABLE room.rooms ALTER COLUMN total_amount DROP NOT NULL;
ALTER TABLE room.rooms ALTER COLUMN buyer_id DROP NOT NULL;
ALTER TABLE room.rooms ALTER COLUMN seller_id DROP NOT NULL;
ALTER TABLE room.rooms DROP CONSTRAINT IF EXISTS different_buyer_seller;
ALTER TABLE room.rooms ADD CONSTRAINT different_buyer_seller CHECK (buyer_id IS NULL OR seller_id IS NULL OR buyer_id <> seller_id);

CREATE TABLE IF NOT EXISTS room.covenant_invitations (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    covenant_id         UUID NOT NULL REFERENCES room.rooms(id) ON DELETE CASCADE,
    invitation_code     VARCHAR(64) NOT NULL UNIQUE,
    claimed_by          UUID REFERENCES auth.users(id),
    is_claimed          BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at          TIMESTAMPTZ NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
