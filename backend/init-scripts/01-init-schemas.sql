-- ═══════════════════════════════════════════════════════════════════════════════
-- EyesOfPriestess — The Archives: PostgreSQL Initialization Script
-- File: 01-init-schemas.sql
-- Description: Create all logical archives (schemas), extensions, and seed data
-- Run: Automatically via docker-entrypoint-initdb.d on first postgres startup
-- ═══════════════════════════════════════════════════════════════════════════════

-- ─── EXTENSIONS ─────────────────────────────────────────────────────────────
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ─── ARCHIVES (SCHEMAS) ─────────────────────────────────────────────────────
CREATE SCHEMA IF NOT EXISTS seal;
CREATE SCHEMA IF NOT EXISTS vault;
CREATE SCHEMA IF NOT EXISTS covenant;
CREATE SCHEMA IF NOT EXISTS communion;
CREATE SCHEMA IF NOT EXISTS judgment;

-- ═══════════════════════════════════════════════════════════════════════════════
-- SEAL ARCHIVE — Authentication & Identity
-- ═══════════════════════════════════════════════════════════════════════════════

-- ─── seal.pilgrims ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS seal.pilgrims (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone VARCHAR(15) NOT NULL UNIQUE,
    email VARCHAR(255) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    pin_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    profile_photo_url VARCHAR(500),

    -- Attunement (KYC)
    attunement_status VARCHAR(20) NOT NULL DEFAULT 'UNATTUNED'
        CHECK (attunement_status IN ('UNATTUNED', 'PENDING', 'ATTUNED', 'REJECTED')),
    attunement_id_hash VARCHAR(255),
    attunement_verified_at TIMESTAMPTZ,

    -- Roles
    is_oracle BOOLEAN NOT NULL DEFAULT FALSE,

    -- Status & Security
    status VARCHAR(20) NOT NULL DEFAULT 'ATTUNED'
        CHECK (status IN ('ATTUNED', 'SUSPENDED', 'SANCTIONED')),
    pin_failed_attempts INT NOT NULL DEFAULT 0,
    pin_locked_until TIMESTAMPTZ,
    login_failed_attempts INT NOT NULL DEFAULT 0,
    login_locked_until TIMESTAMPTZ,

    -- Covenant Metrics
    covenant_score DECIMAL(2,1) NOT NULL DEFAULT 5.0
        CHECK (covenant_score >= 0.0 AND covenant_score <= 5.0),
    total_covenants INT NOT NULL DEFAULT 0,
    fulfilled_covenants INT NOT NULL DEFAULT 0,
    judgment_count INT NOT NULL DEFAULT 0,

    -- Timestamps
    attuned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_rite_at TIMESTAMPTZ,

    CONSTRAINT valid_phone CHECK (phone ~ '^\+?[0-9]{10,15}$')
);

CREATE INDEX IF NOT EXISTS idx_pilgrims_phone ON seal.pilgrims(phone);
CREATE INDEX IF NOT EXISTS idx_pilgrims_status ON seal.pilgrims(status);
CREATE INDEX IF NOT EXISTS idx_pilgrims_attunement ON seal.pilgrims(attunement_status);

-- updated_at trigger for pilgrims
CREATE OR REPLACE FUNCTION seal.fn_update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_pilgrims_updated_at
    BEFORE UPDATE ON seal.pilgrims
    FOR EACH ROW EXECUTE FUNCTION seal.fn_update_updated_at();

-- ─── seal.sessions ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS seal.sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pilgrim_id UUID NOT NULL REFERENCES seal.pilgrims(id) ON DELETE CASCADE,
    refresh_seal_jti VARCHAR(255) NOT NULL UNIQUE,
    device_id VARCHAR(255),
    device_info JSONB,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    severed_at TIMESTAMPTZ,
    severed_reason VARCHAR(50)  -- SEVERED | SANCTIONED | PASSWORD_CHANGED
);

CREATE INDEX IF NOT EXISTS idx_sessions_pilgrim ON seal.sessions(pilgrim_id);
CREATE INDEX IF NOT EXISTS idx_sessions_jti ON seal.sessions(refresh_seal_jti);
CREATE INDEX IF NOT EXISTS idx_sessions_expires ON seal.sessions(expires_at);
CREATE INDEX IF NOT EXISTS idx_sessions_severed ON seal.sessions(severed_at);

-- ─── seal.sanction_records ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS seal.sanction_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pilgrim_id UUID NOT NULL REFERENCES seal.pilgrims(id) ON DELETE CASCADE,
    sanctioned_by UUID REFERENCES seal.pilgrims(id),
    reason VARCHAR(50) NOT NULL
        CHECK (reason IN ('FRAUDULENT_ACTIVITY', 'SPAM', 'HARASSMENT', 'TERMS_VIOLATION', 'OTHER')),
    description TEXT,
    sanction_duration VARCHAR(20) NOT NULL
        CHECK (sanction_duration IN ('TEMPORARY', 'ETERNAL')),
    sanctioned_until TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    revoked_at TIMESTAMPTZ,
    revoked_by UUID REFERENCES seal.pilgrims(id)
);

CREATE INDEX IF NOT EXISTS idx_sanction_records_pilgrim ON seal.sanction_records(pilgrim_id);

-- ═══════════════════════════════════════════════════════════════════════════════
-- VAULT ARCHIVE — Treasury & Payments
-- ═══════════════════════════════════════════════════════════════════════════════

-- ─── vault.treasuries ───────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS vault.treasuries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pilgrim_id UUID NOT NULL UNIQUE REFERENCES seal.pilgrims(id) ON DELETE CASCADE,
    available_treasury DECIMAL(15,2) NOT NULL DEFAULT 0.00
        CHECK (available_treasury >= 0),
    sealed_treasury DECIMAL(15,2) NOT NULL DEFAULT 0.00
        CHECK (sealed_treasury >= 0),
    total_offered DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_withdrawn DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'FROZEN', 'SUSPENDED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0  -- optimistic lock
);

CREATE INDEX IF NOT EXISTS idx_treasuries_pilgrim ON vault.treasuries(pilgrim_id);
CREATE INDEX IF NOT EXISTS idx_treasuries_status ON vault.treasuries(status);

CREATE OR REPLACE FUNCTION vault.fn_update_updated_at()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_treasuries_updated_at
    BEFORE UPDATE ON vault.treasuries
    FOR EACH ROW EXECUTE FUNCTION vault.fn_update_updated_at();

-- ─── vault.chronicles ───────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS vault.chronicles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    treasury_id UUID NOT NULL REFERENCES vault.treasuries(id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL
        CHECK (type IN ('OFFERING', 'WITHDRAWAL', 'TITHING_IN', 'TITHING_OUT',
                        'SEAL_HOLD', 'SEAL_RELEASE', 'SEAL_REFUND', 'TITHE')),
    amount DECIMAL(15,2) NOT NULL,
    tithe DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    net_amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    reference_id VARCHAR(100),
    reference_type VARCHAR(30),
    counterparty_treasury_id UUID REFERENCES vault.treasuries(id),
    counterparty_name VARCHAR(100),
    description VARCHAR(255),
    metadata JSONB,
    treasury_before DECIMAL(15,2) NOT NULL,
    treasury_after DECIMAL(15,2) NOT NULL,
    covenant_key VARCHAR(100) UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    CONSTRAINT valid_amount CHECK (amount != 0)
);

CREATE INDEX IF NOT EXISTS idx_chronicles_treasury ON vault.chronicles(treasury_id);
CREATE INDEX IF NOT EXISTS idx_chronicles_type ON vault.chronicles(type);
CREATE INDEX IF NOT EXISTS idx_chronicles_status ON vault.chronicles(status);
CREATE INDEX IF NOT EXISTS idx_chronicles_created ON vault.chronicles(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_chronicles_reference ON vault.chronicles(reference_id);
CREATE INDEX IF NOT EXISTS idx_chronicles_covenant_key ON vault.chronicles(covenant_key);

-- ─── vault.offerings ────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS vault.offerings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    treasury_id UUID NOT NULL REFERENCES vault.treasuries(id) ON DELETE CASCADE,
    amount DECIMAL(15,2) NOT NULL CHECK (amount > 0),
    tithe DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    gateway VARCHAR(20) NOT NULL
        CHECK (gateway IN ('MIDTRANS', 'XENDIT', 'MANUAL')),
    gateway_transaction_id VARCHAR(255),
    payment_method VARCHAR(30) NOT NULL
        CHECK (payment_method IN ('VIRTUAL_ACCOUNT', 'E_WALLET', 'BANK_TRANSFER', 'QRIS')),
    payment_details JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'ACCEPTED', 'EXPIRED', 'CANCELLED', 'FAILED')),
    expires_at TIMESTAMPTZ,
    accepted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    covenant_key VARCHAR(100) UNIQUE
);

CREATE INDEX IF NOT EXISTS idx_offerings_treasury ON vault.offerings(treasury_id);
CREATE INDEX IF NOT EXISTS idx_offerings_status ON vault.offerings(status);
CREATE INDEX IF NOT EXISTS idx_offerings_gateway ON vault.offerings(gateway_transaction_id);

-- ─── vault.withdrawals ──────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS vault.withdrawals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    treasury_id UUID NOT NULL REFERENCES vault.treasuries(id) ON DELETE CASCADE,
    amount DECIMAL(15,2) NOT NULL CHECK (amount > 0),
    tithe DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    net_amount DECIMAL(15,2) NOT NULL,
    bank_code VARCHAR(20) NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    account_number_hash VARCHAR(255) NOT NULL,
    account_number_masked VARCHAR(20) NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
    gateway VARCHAR(20),
    gateway_transaction_id VARCHAR(255),
    processed_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    covenant_key VARCHAR(100) UNIQUE
);

CREATE INDEX IF NOT EXISTS idx_withdrawals_treasury ON vault.withdrawals(treasury_id);
CREATE INDEX IF NOT EXISTS idx_withdrawals_status ON vault.withdrawals(status);

-- ─── vault.bank_accounts ────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS vault.bank_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    treasury_id UUID NOT NULL REFERENCES vault.treasuries(id) ON DELETE CASCADE,
    bank_code VARCHAR(20) NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    account_number_hash VARCHAR(255) NOT NULL,
    account_number_masked VARCHAR(20) NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_bank_account UNIQUE (treasury_id, bank_code, account_number_hash)
);

CREATE INDEX IF NOT EXISTS idx_bank_accounts_treasury ON vault.bank_accounts(treasury_id);

-- ═══════════════════════════════════════════════════════════════════════════════
-- COVENANT ARCHIVE — Escrow Room Engine
-- ═══════════════════════════════════════════════════════════════════════════════

-- ─── covenant.covenants ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS covenant.covenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    covenant_code VARCHAR(10) NOT NULL UNIQUE,   -- e.g., CVN-8X2K9

    initiator_id UUID NOT NULL REFERENCES seal.pilgrims(id),
    counterpart_id UUID NOT NULL REFERENCES seal.pilgrims(id),

    item_name VARCHAR(200) NOT NULL,
    item_description TEXT,
    category VARCHAR(20) NOT NULL
        CHECK (category IN ('GAME', 'MARKETPLACE', 'SERVICE')),

    amount DECIMAL(12,2) NOT NULL CHECK (amount > 0),
    tithe DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    tithe_percentage DECIMAL(5,2) NOT NULL DEFAULT 1.00,

    status VARCHAR(20) NOT NULL DEFAULT 'FORGED'
        CHECK (status IN ('FORGED', 'ACCEPTED', 'DELIVERED', 'FULFILLED',
                          'JUDGMENT', 'BROKEN', 'EXPIRED')),

    deadline_hours INT NOT NULL DEFAULT 72 CHECK (deadline_hours > 0 AND deadline_hours <= 168),
    deadline_at TIMESTAMPTZ NOT NULL,

    accepted_at TIMESTAMPTZ,
    delivered_at TIMESTAMPTZ,
    fulfilled_at TIMESTAMPTZ,
    broken_at TIMESTAMPTZ,
    expired_at TIMESTAMPTZ,
    judgment_at TIMESTAMPTZ,

    initiator_rating INT CHECK (initiator_rating >= 1 AND initiator_rating <= 5),
    initiator_review TEXT,
    counterpart_rating INT CHECK (counterpart_rating >= 1 AND counterpart_rating <= 5),
    counterpart_review TEXT,

    broken_by UUID REFERENCES seal.pilgrims(id),
    break_reason VARCHAR(50),

    metadata JSONB,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_covenants_initiator ON covenant.covenants(initiator_id);
CREATE INDEX IF NOT EXISTS idx_covenants_counterpart ON covenant.covenants(counterpart_id);
CREATE INDEX IF NOT EXISTS idx_covenants_status ON covenant.covenants(status);
CREATE INDEX IF NOT EXISTS idx_covenants_code ON covenant.covenants(covenant_code);
CREATE INDEX IF NOT EXISTS idx_covenants_deadline ON covenant.covenants(deadline_at);
CREATE INDEX IF NOT EXISTS idx_covenants_created ON covenant.covenants(created_at DESC);

CREATE OR REPLACE FUNCTION covenant.fn_update_updated_at()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_covenants_updated_at
    BEFORE UPDATE ON covenant.covenants
    FOR EACH ROW EXECUTE FUNCTION covenant.fn_update_updated_at();

-- ─── covenant.escrow_seals ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS covenant.escrow_seals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    covenant_id UUID NOT NULL UNIQUE REFERENCES covenant.covenants(id) ON DELETE CASCADE,
    treasury_id UUID NOT NULL REFERENCES vault.treasuries(id),
    amount DECIMAL(12,2) NOT NULL CHECK (amount > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'SEALED'
        CHECK (status IN ('SEALED', 'RELEASED', 'REFUNDED', 'FROZEN')),
    sealed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    released_at TIMESTAMPTZ,
    refunded_at TIMESTAMPTZ,
    released_to_treasury_id UUID REFERENCES vault.treasuries(id),
    release_chronicle_id UUID REFERENCES vault.chronicles(id),
    refund_chronicle_id UUID REFERENCES vault.chronicles(id)
);

CREATE INDEX IF NOT EXISTS idx_escrow_seals_covenant ON covenant.escrow_seals(covenant_id);
CREATE INDEX IF NOT EXISTS idx_escrow_seals_treasury ON covenant.escrow_seals(treasury_id);
CREATE INDEX IF NOT EXISTS idx_escrow_seals_status ON covenant.escrow_seals(status);

-- ─── covenant.covenant_events ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS covenant.covenant_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    covenant_id UUID NOT NULL REFERENCES covenant.covenants(id) ON DELETE CASCADE,
    event_type VARCHAR(30) NOT NULL
        CHECK (event_type IN ('FORGED', 'ACCEPTED', 'REJECTED', 'DELIVERED',
                              'CONFIRMED', 'BROKEN', 'EXPIRED', 'JUDGMENT',
                              'AUTO_RELEASED', 'TREASURY_RELEASED', 'TREASURY_REFUNDED')),
    actor_id UUID NOT NULL REFERENCES seal.pilgrims(id),
    actor_role VARCHAR(10) NOT NULL
        CHECK (actor_role IN ('INITIATOR', 'COUNTERPART', 'SYSTEM', 'ORACLE')),
    previous_status VARCHAR(20),
    new_status VARCHAR(20),
    notes TEXT,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_covenant_events_covenant ON covenant.covenant_events(covenant_id);
CREATE INDEX IF NOT EXISTS idx_covenant_events_type ON covenant.covenant_events(event_type);
CREATE INDEX IF NOT EXISTS idx_covenant_events_created ON covenant.covenant_events(created_at);

-- ─── covenant.covenant_proofs ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS covenant.covenant_proofs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    covenant_id UUID NOT NULL REFERENCES covenant.covenants(id) ON DELETE CASCADE,
    uploaded_by UUID NOT NULL REFERENCES seal.pilgrims(id),
    proof_type VARCHAR(20) NOT NULL
        CHECK (proof_type IN ('DELIVERY', 'EVIDENCE', 'JUDGMENT')),
    file_url VARCHAR(500) NOT NULL,
    file_name VARCHAR(255),
    file_size INT,
    mime_type VARCHAR(50),
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_covenant_proofs_covenant ON covenant.covenant_proofs(covenant_id);
CREATE INDEX IF NOT EXISTS idx_covenant_proofs_type ON covenant.covenant_proofs(proof_type);

-- ═══════════════════════════════════════════════════════════════════════════════
-- COMMUNION ARCHIVE — Real-time Chat
-- ═══════════════════════════════════════════════════════════════════════════════

-- ─── communion.messages ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS communion.messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    covenant_id UUID NOT NULL REFERENCES covenant.covenants(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES seal.pilgrims(id),
    message TEXT NOT NULL,
    message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT'
        CHECK (message_type IN ('TEXT', 'IMAGE', 'FILE', 'SYSTEM')),
    file_url VARCHAR(500),
    file_name VARCHAR(255),
    file_size INT,
    system_event_type VARCHAR(30),
    system_metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_messages_covenant ON communion.messages(covenant_id);
CREATE INDEX IF NOT EXISTS idx_messages_sender ON communion.messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_messages_created ON communion.messages(created_at DESC);

-- ─── communion.message_reads ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS communion.message_reads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID NOT NULL REFERENCES communion.messages(id) ON DELETE CASCADE,
    pilgrim_id UUID NOT NULL REFERENCES seal.pilgrims(id),
    read_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_message_read UNIQUE (message_id, pilgrim_id)
);

CREATE INDEX IF NOT EXISTS idx_message_reads_message ON communion.message_reads(message_id);
CREATE INDEX IF NOT EXISTS idx_message_reads_pilgrim ON communion.message_reads(pilgrim_id);

-- ═══════════════════════════════════════════════════════════════════════════════
-- JUDGMENT ARCHIVE — Dispute Resolution
-- ═══════════════════════════════════════════════════════════════════════════════

-- ─── judgment.judgments ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS judgment.judgments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    covenant_id UUID NOT NULL UNIQUE REFERENCES covenant.covenants(id) ON DELETE CASCADE,
    raised_by UUID NOT NULL REFERENCES seal.pilgrims(id),
    respondent_id UUID NOT NULL REFERENCES seal.pilgrims(id),
    reason VARCHAR(30) NOT NULL
        CHECK (reason IN ('ITEM_NOT_AS_DESCRIBED', 'NOT_DELIVERED', 'OTHER')),
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN'
        CHECK (status IN ('OPEN', 'UNDER_REVIEW', 'RESOLVED', 'REJECTED')),
    resolution VARCHAR(30)
        CHECK (resolution IN ('RELEASE_TO_COUNTERPART', 'REFUND_TO_INITIATOR', 'SPLIT')),
    split_amount DECIMAL(12,2),
    assigned_oracle_id UUID REFERENCES seal.pilgrims(id),
    oracle_notes TEXT,
    reviewed_at TIMESTAMPTZ,
    resolved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_judgments_covenant ON judgment.judgments(covenant_id);
CREATE INDEX IF NOT EXISTS idx_judgments_status ON judgment.judgments(status);
CREATE INDEX IF NOT EXISTS idx_judgments_raised_by ON judgment.judgments(raised_by);
CREATE INDEX IF NOT EXISTS idx_judgments_oracle ON judgment.judgments(assigned_oracle_id);

CREATE OR REPLACE FUNCTION judgment.fn_update_updated_at()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_judgments_updated_at
    BEFORE UPDATE ON judgment.judgments
    FOR EACH ROW EXECUTE FUNCTION judgment.fn_update_updated_at();

-- ─── judgment.judgment_evidence ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS judgment.judgment_evidence (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    judgment_id UUID NOT NULL REFERENCES judgment.judgments(id) ON DELETE CASCADE,
    uploaded_by UUID NOT NULL REFERENCES seal.pilgrims(id),
    evidence_type VARCHAR(20) NOT NULL
        CHECK (evidence_type IN ('PHOTO', 'SCREENSHOT', 'COMMUNION_LOG', 'VIDEO', 'DOCUMENT', 'OTHER')),
    file_url VARCHAR(500) NOT NULL,
    file_name VARCHAR(255),
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_judgment_evidence_judgment ON judgment.judgment_evidence(judgment_id);

-- ═══════════════════════════════════════════════════════════════════════════════
-- VIEWS — The Oracle's Gaze
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE OR REPLACE VIEW seal.pilgrim_observatory AS
SELECT
    p.id,
    p.phone,
    p.full_name,
    p.profile_photo_url,
    p.attunement_status,
    p.covenant_score,
    p.is_oracle,
    t.available_treasury,
    t.sealed_treasury,
    (t.available_treasury + t.sealed_treasury) AS total_treasury,
    p.total_covenants,
    p.fulfilled_covenants,
    p.judgment_count,
    p.status AS pilgrim_status,
    p.attuned_at
FROM seal.pilgrims p
LEFT JOIN vault.treasuries t ON t.pilgrim_id = p.id;

CREATE OR REPLACE VIEW covenant.covenant_summary AS
SELECT
    c.id,
    c.covenant_code,
    c.item_name,
    c.amount,
    c.status,
    c.deadline_at,
    c.created_at,
    i.full_name AS initiator_name,
    i.phone AS initiator_phone,
    cp.full_name AS counterpart_name,
    cp.phone AS counterpart_phone,
    e.status AS escrow_status,
    e.amount AS escrow_amount
FROM covenant.covenants c
JOIN seal.pilgrims i ON i.id = c.initiator_id
JOIN seal.pilgrims cp ON cp.id = c.counterpart_id
LEFT JOIN covenant.escrow_seals e ON e.covenant_id = c.id;

-- Auto-release helper function (called by cron in covenant-service)
CREATE OR REPLACE FUNCTION covenant.get_expired_covenants()
RETURNS TABLE (covenant_id UUID, initiator_id UUID, counterpart_id UUID, amount DECIMAL) AS $$
BEGIN
    RETURN QUERY
    SELECT c.id, c.initiator_id, c.counterpart_id, c.amount
    FROM covenant.covenants c
    WHERE c.status = 'DELIVERED'
      AND c.deadline_at <= NOW()
      AND c.fulfilled_at IS NULL
      AND c.judgment_at IS NULL;
END;
$$ LANGUAGE plpgsql;

-- ═══════════════════════════════════════════════════════════════════════════════
-- SEED DATA
-- ═══════════════════════════════════════════════════════════════════════════════

-- Insert a default Oracle (admin) pilgrim
-- Password: OraclePass123! | PIN: 000000 (change in production!)
INSERT INTO seal.pilgrims (
    id, phone, email, password_hash, pin_hash,
    full_name, is_oracle, status, attunement_status
) VALUES (
    '00000000-0000-0000-0000-000000000001',
    '+621234567890',
    'oracle@eyesofpriestess.id',
    crypt('OraclePass123!', gen_salt('bf', 12)),
    crypt('000000', gen_salt('bf', 12)),
    'The High Oracle',
    TRUE,
    'ATTUNED',
    'ATTUNED'
) ON CONFLICT (phone) DO NOTHING;

-- Create treasury for oracle pilgrim
INSERT INTO vault.treasuries (pilgrim_id)
VALUES ('00000000-0000-0000-0000-000000000001')
ON CONFLICT (pilgrim_id) DO NOTHING;
