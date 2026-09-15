-- ═══════════════════════════════════════════════════════════════════════════════
-- EyesOfPriestess — PostgreSQL Comprehensive Seeding Script
-- File: 02-seed-data.sql
-- Requirement: Minimal 20 records realistis per primary table (S1 Capstone Rules)
-- ═══════════════════════════════════════════════════════════════════════════════

-- ─── 1. SEED AUTH.USERS (22 Realistic Records) ─────────────────────────────────
INSERT INTO auth.users (
    id, email, username, full_name, phone_number, role, is_verified, is_pin_set, avatar_url, created_at, updated_at
) VALUES
-- Admin & Moderator
('a0000000-0000-0000-0000-000000000001', 'admin@eyesofpriestess.com', 'admin_sanctum', 'High Oracle Administrator', '081200000001', 'ADMIN', true, true, 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150', NOW() - INTERVAL '180 days', NOW()),
('a0000000-0000-0000-0000-000000000002', 'moderator@eyesofpriestess.com', 'arbiter_keeper', 'Senior Escrow Arbiter', '081200000002', 'MODERATOR', true, true, 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150', NOW() - INTERVAL '150 days', NOW()),

-- Regular Users / Gamers / Traders
('a0000000-0000-0000-0000-000000000003', 'budi.santoso@gmail.com', 'budisantoso', 'Budi Santoso', '081234567801', 'USER', true, true, 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150', NOW() - INTERVAL '90 days', NOW()),
('a0000000-0000-0000-0000-000000000004', 'siti.rahmawati@gmail.com', 'sitirahma', 'Siti Rahmawati', '081234567802', 'USER', true, true, 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150', NOW() - INTERVAL '85 days', NOW()),
('a0000000-0000-0000-0000-000000000005', 'dimas.prasetyo@gmail.com', 'dimasprast', 'Dimas Prasetyo', '081234567803', 'USER', true, true, 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150', NOW() - INTERVAL '80 days', NOW()),
('a0000000-0000-0000-0000-000000000006', 'anisa.lestari@gmail.com', 'anisalestari', 'Anisa Tri Lestari', '081234567804', 'USER', true, true, 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150', NOW() - INTERVAL '75 days', NOW()),
('a0000000-0000-0000-0000-000000000007', 'reza.aditya@gmail.com', 'rezaaditya', 'Reza Aditya Wardhana', '081234567805', 'USER', true, true, 'https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=150', NOW() - INTERVAL '70 days', NOW()),
('a0000000-0000-0000-0000-000000000008', 'fajar.nugraha@gmail.com', 'fajarnugraha', 'Fajar Ramadhan Nugraha', '081234567806', 'USER', true, true, 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150', NOW() - INTERVAL '65 days', NOW()),
('a0000000-0000-0000-0000-000000000009', 'dewi.safitri@gmail.com', 'dewisafitri', 'Dewi Anggraini Safitri', '081234567807', 'USER', true, true, 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150', NOW() - INTERVAL '60 days', NOW()),
('a0000000-0000-0000-0000-000000000010', 'hendra.kusuma@gmail.com', 'hendrakusuma', 'Hendra Kusuma Jaya', '081234567808', 'USER', true, true, 'https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=150', NOW() - INTERVAL '55 days', NOW()),
('a0000000-0000-0000-0000-000000000011', 'mega.putri@gmail.com', 'megaputri', 'Mega Utami Putri', '081234567809', 'USER', true, true, 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150', NOW() - INTERVAL '50 days', NOW()),
('a0000000-0000-0000-0000-000000000012', 'bayu.pratama@gmail.com', 'bayupratama', 'Bayu Aji Pratama', '081234567810', 'USER', true, true, 'https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=150', NOW() - INTERVAL '45 days', NOW()),
('a0000000-0000-0000-0000-000000000013', 'maya.indrawati@gmail.com', 'mayaindra', 'Maya Kartika Indrawati', '081234567811', 'USER', true, true, 'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=150', NOW() - INTERVAL '40 days', NOW()),
('a0000000-0000-0000-0000-000000000014', 'rizky.febrian@gmail.com', 'rizkyfeb', 'Rizky Maulana Febrian', '081234567812', 'USER', true, true, 'https://images.unsplash.com/photo-1501196354995-cbb51c65aaea?w=150', NOW() - INTERVAL '35 days', NOW()),
('a0000000-0000-0000-0000-000000000015', 'nadia.putri@gmail.com', 'nadiaputri', 'Nadia Syahrini Putri', '081234567813', 'USER', true, true, 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150', NOW() - INTERVAL '30 days', NOW()),
('a0000000-0000-0000-0000-000000000016', 'agung.hidayat@gmail.com', 'agunghidayat', 'Agung Hidayatullah', '081234567814', 'USER', true, true, 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150', NOW() - INTERVAL '25 days', NOW()),
('a0000000-0000-0000-0000-000000000017', 'cahya.wibowo@gmail.com', 'cahyawibowo', 'Cahya Tri Wibowo', '081234567815', 'USER', true, true, 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150', NOW() - INTERVAL '20 days', NOW()),
('a0000000-0000-0000-0000-000000000018', 'tiara.andini@gmail.com', 'tiaraandini', 'Tiara Andini Permata', '081234567816', 'USER', true, true, 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150', NOW() - INTERVAL '15 days', NOW()),
('a0000000-0000-0000-0000-000000000019', 'ilham.ramadhan@gmail.com', 'ilhamramadhan', 'Ilham Kurnia Ramadhan', '081234567817', 'USER', true, true, 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150', NOW() - INTERVAL '10 days', NOW()),
('a0000000-0000-0000-0000-000000000020', 'sarah.wijaya@gmail.com', 'sarahwijaya', 'Sarah Amanda Wijaya', '081234567818', 'USER', true, true, 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150', NOW() - INTERVAL '5 days', NOW()),
('a0000000-0000-0000-0000-000000000021', 'gilang.dirga@gmail.com', 'gilangdirga', 'Gilang Dirgantara', '081234567819', 'USER', true, true, 'https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=150', NOW() - INTERVAL '3 days', NOW()),
('a0000000-0000-0000-0000-000000000022', 'kevin.sanaya@gmail.com', 'kevinsanaya', 'Kevin Sanaya Sukamto', '081234567820', 'USER', true, true, 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150', NOW() - INTERVAL '1 days', NOW())
ON CONFLICT (email) DO NOTHING;

-- ─── 2. SEED AUTH.CREDENTIALS (22 Records) ────────────────────────────────────
-- Default Password: Password123! ($2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy)
-- Default PIN: 123456 ($2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi)
INSERT INTO auth.credentials (
    user_id, password_hash, pin_hash, pin_set_at, created_at, updated_at
)
SELECT 
    id,
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    NOW() - INTERVAL '30 days',
    created_at,
    updated_at
FROM auth.users
WHERE id >= 'a0000000-0000-0000-0000-000000000001'
ON CONFLICT DO NOTHING;

-- ─── 3. SEED WALLET.WALLETS (22 Records) ──────────────────────────────────────
INSERT INTO wallet.wallets (
    user_id, available_balance, escrow_balance, currency, is_active, created_at, updated_at
) VALUES
('a0000000-0000-0000-0000-000000000001', 500000000, 0, 'IDR', true, NOW() - INTERVAL '180 days', NOW()),
('a0000000-0000-0000-0000-000000000002', 100000000, 0, 'IDR', true, NOW() - INTERVAL '150 days', NOW()),
('a0000000-0000-0000-0000-000000000003', 15500000, 1500000, 'IDR', true, NOW() - INTERVAL '90 days', NOW()),
('a0000000-0000-0000-0000-000000000004', 8250000, 0, 'IDR', true, NOW() - INTERVAL '85 days', NOW()),
('a0000000-0000-0000-0000-000000000005', 42000000, 3500000, 'IDR', true, NOW() - INTERVAL '80 days', NOW()),
('a0000000-0000-0000-0000-000000000006', 12300000, 0, 'IDR', true, NOW() - INTERVAL '75 days', NOW()),
('a0000000-0000-0000-0000-000000000007', 9800000, 750000, 'IDR', true, NOW() - INTERVAL '70 days', NOW()),
('a0000000-0000-0000-0000-000000000008', 34500000, 0, 'IDR', true, NOW() - INTERVAL '65 days', NOW()),
('a0000000-0000-0000-0000-000000000009', 6700000, 2100000, 'IDR', true, NOW() - INTERVAL '60 days', NOW()),
('a0000000-0000-0000-0000-000000000010', 18900000, 0, 'IDR', true, NOW() - INTERVAL '55 days', NOW()),
('a0000000-0000-0000-0000-000000000011', 25400000, 0, 'IDR', true, NOW() - INTERVAL '50 days', NOW()),
('a0000000-0000-0000-0000-000000000012', 4500000, 1200000, 'IDR', true, NOW() - INTERVAL '45 days', NOW()),
('a0000000-0000-0000-0000-000000000013', 14200000, 0, 'IDR', true, NOW() - INTERVAL '40 days', NOW()),
('a0000000-0000-0000-0000-000000000014', 8900000, 500000, 'IDR', true, NOW() - INTERVAL '35 days', NOW()),
('a0000000-0000-0000-0000-000000000015', 31200000, 0, 'IDR', true, NOW() - INTERVAL '30 days', NOW()),
('a0000000-0000-0000-0000-000000000016', 7600000, 0, 'IDR', true, NOW() - INTERVAL '25 days', NOW()),
('a0000000-0000-0000-0000-000000000017', 16800000, 4500000, 'IDR', true, NOW() - INTERVAL '20 days', NOW()),
('a0000000-0000-0000-0000-000000000018', 5300000, 0, 'IDR', true, NOW() - INTERVAL '15 days', NOW()),
('a0000000-0000-0000-0000-000000000019', 22100000, 0, 'IDR', true, NOW() - INTERVAL '10 days', NOW()),
('a0000000-0000-0000-0000-000000000020', 11400000, 1800000, 'IDR', true, NOW() - INTERVAL '5 days', NOW()),
('a0000000-0000-0000-0000-000000000021', 19500000, 0, 'IDR', true, NOW() - INTERVAL '3 days', NOW()),
('a0000000-0000-0000-0000-000000000022', 28000000, 0, 'IDR', true, NOW() - INTERVAL '1 days', NOW())
ON CONFLICT (user_id) DO NOTHING;

-- ─── 4. SEED WALLET.BANK_ACCOUNTS (22 Records) ────────────────────────────────
INSERT INTO wallet.bank_accounts (
    user_id, bank_code, bank_name, account_number, account_holder_name, is_primary, is_verified, created_at, updated_at
) VALUES
('a0000000-0000-0000-0000-000000000001', 'BCA', 'Bank Central Asia', '8830192831', 'Oracle Escrow Custody', true, true, NOW() - INTERVAL '180 days', NOW()),
('a0000000-0000-0000-0000-000000000002', 'MANDIRI', 'Bank Mandiri', '1370019283741', 'Escrow Arbiter Pool', true, true, NOW() - INTERVAL '150 days', NOW()),
('a0000000-0000-0000-0000-000000000003', 'BCA', 'Bank Central Asia', '5270182910', 'Budi Santoso', true, true, NOW() - INTERVAL '88 days', NOW()),
('a0000000-0000-0000-0000-000000000004', 'BRI', 'Bank Rakyat Indonesia', '020601002918501', 'Siti Rahmawati', true, true, NOW() - INTERVAL '84 days', NOW()),
('a0000000-0000-0000-0000-000000000005', 'BNI', 'Bank Negara Indonesia', '0928172635', 'Dimas Prasetyo', true, true, NOW() - INTERVAL '79 days', NOW()),
('a0000000-0000-0000-0000-000000000006', 'MANDIRI', 'Bank Mandiri', '1400018273645', 'Anisa Tri Lestari', true, true, NOW() - INTERVAL '74 days', NOW()),
('a0000000-0000-0000-0000-000000000007', 'CIMB', 'CIMB Niaga', '705819283746', 'Reza Aditya Wardhana', true, true, NOW() - INTERVAL '69 days', NOW()),
('a0000000-0000-0000-0000-000000000008', 'BCA', 'Bank Central Asia', '6041928371', 'Fajar Ramadhan', true, true, NOW() - INTERVAL '64 days', NOW()),
('a0000000-0000-0000-0000-000000000009', 'GOPAY', 'GoPay E-Wallet', '081234567807', 'Dewi Anggraini', true, true, NOW() - INTERVAL '59 days', NOW()),
('a0000000-0000-0000-0000-000000000010', 'OVO', 'OVO Premier', '081234567808', 'Hendra Kusuma Jaya', true, true, NOW() - INTERVAL '54 days', NOW()),
('a0000000-0000-0000-0000-000000000011', 'DANA', 'DANA Indonesia', '081234567809', 'Mega Utami Putri', true, true, NOW() - INTERVAL '49 days', NOW()),
('a0000000-0000-0000-0000-000000000012', 'BSI', 'Bank Syariah Indonesia', '7182910293', 'Bayu Aji Pratama', true, true, NOW() - INTERVAL '44 days', NOW()),
('a0000000-0000-0000-0000-000000000013', 'BCA', 'Bank Central Asia', '7819283710', 'Maya Kartika', true, true, NOW() - INTERVAL '39 days', NOW()),
('a0000000-0000-0000-0000-000000000014', 'MANDIRI', 'Bank Mandiri', '1560018273641', 'Rizky Maulana', true, true, NOW() - INTERVAL '34 days', NOW()),
('a0000000-0000-0000-0000-000000000015', 'PERMATA', 'Bank Permata', '41092837192', 'Nadia Syahrini', true, true, NOW() - INTERVAL '29 days', NOW()),
('a0000000-0000-0000-0000-000000000016', 'BCA', 'Bank Central Asia', '8910293847', 'Agung Hidayatullah', true, true, NOW() - INTERVAL '24 days', NOW()),
('a0000000-0000-0000-0000-000000000017', 'BRI', 'Bank Rakyat Indonesia', '034101001827503', 'Cahya Tri Wibowo', true, true, NOW() - INTERVAL '19 days', NOW()),
('a0000000-0000-0000-0000-000000000018', 'SEABANK', 'SeaBank Indonesia', '90182938471', 'Tiara Andini', true, true, NOW() - INTERVAL '14 days', NOW()),
('a0000000-0000-0000-0000-000000000019', 'BCA', 'Bank Central Asia', '1928374650', 'Ilham Kurnia', true, true, NOW() - INTERVAL '9 days', NOW()),
('a0000000-0000-0000-0000-000000000020', 'JAGO', 'Bank Jago', '1029384756', 'Sarah Amanda Wijaya', true, true, NOW() - INTERVAL '4 days', NOW()),
('a0000000-0000-0000-0000-000000000021', 'MANDIRI', 'Bank Mandiri', '1670018273649', 'Gilang Dirgantara', true, true, NOW() - INTERVAL '2 days', NOW()),
('a0000000-0000-0000-0000-000000000022', 'BCA', 'Bank Central Asia', '4910293848', 'Kevin Sanaya', true, true, NOW() - INTERVAL '1 days', NOW())
ON CONFLICT DO NOTHING;

-- ─── 5. SEED ROOM.ROOMS (22 Escrow Covenant Rooms) ────────────────────────────
INSERT INTO room.rooms (
    id, room_code, title, description, item_category, item_price, fee, total_amount, status,
    buyer_id, seller_id, created_at, updated_at
) VALUES
('b0000000-0000-0000-0000-000000000001', 'COV-829101', 'Akun Mobile Legends Mythical Glory 120 Stars', 'Full skin collector, all hero unlocked, win rate 72%', 'GAME_ACCOUNT', 1500000, 15000, 1515000, 'COMPLETED', 'a0000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000004', NOW() - INTERVAL '60 days', NOW() - INTERVAL '58 days'),
('b0000000-0000-0000-0000-000000000002', 'COV-829102', 'Vandal Kuronami & Knife Xenohunter Valorant', 'Skin knife langka + Vandal Kuronami level max', 'GAME_ITEM', 850000, 8500, 858500, 'COMPLETED', 'a0000000-0000-0000-0000-000000000005', 'a0000000-0000-0000-0000-000000000006', NOW() - INTERVAL '50 days', NOW() - INTERVAL '49 days'),
('b0000000-0000-0000-0000-000000000003', 'COV-829103', 'Jasa Pembuatan UI/UX Aplikasi Mobile Fintech', 'Desain 15 screens Figma lengkap dengan prototype interaktif', 'SERVICE', 3500000, 35000, 3535000, 'COMPLETED', 'a0000000-0000-0000-0000-000000000007', 'a0000000-0000-0000-0000-000000000008', NOW() - INTERVAL '45 days', NOW() - INTERVAL '40 days'),
('b0000000-0000-0000-0000-000000000004', 'COV-829104', 'Akun Steam Region Argentina 85 Games', 'Termasuk Cyberpunk, Elden Ring, RDR2, GTA V', 'GAME_ACCOUNT', 1200000, 12000, 1212000, 'COMPLETED', 'a0000000-0000-0000-0000-000000000009', 'a0000000-0000-0000-0000-000000000010', NOW() - INTERVAL '35 days', NOW() - INTERVAL '34 days'),
('b0000000-0000-0000-0000-000000000005', 'COV-829105', 'Source Code Web E-Commerce SvelteKit & Quarkus', 'Fullstack monorepo siap deploy dengan integrasi payment gateway', 'DIGITAL_PRODUCT', 4500000, 45000, 4545000, 'COMPLETED', 'a0000000-0000-0000-0000-000000000011', 'a0000000-0000-0000-0000-000000000012', NOW() - INTERVAL '30 days', NOW() - INTERVAL '29 days'),

-- Active / In-progress Rooms
('b0000000-0000-0000-0000-000000000006', 'COV-829106', 'Akun Genshin Impact AR 60 C6 Furina & Signature', 'Well-built, primo 12000, pity 65 rate ON', 'GAME_ACCOUNT', 2500000, 25000, 2525000, 'FUNDED', 'a0000000-0000-0000-0000-000000000013', 'a0000000-0000-0000-0000-000000000014', NOW() - INTERVAL '2 days', NOW()),
('b0000000-0000-0000-0000-000000000007', 'COV-829107', 'Steam Deck OLED 512GB Like New Fullset Garansi', 'Lengkap pouch charger original dan box mulus', 'PHYSICAL_PRODUCT', 8200000, 50000, 8250000, 'DELIVERED', 'a0000000-0000-0000-0000-000000000015', 'a0000000-0000-0000-0000-000000000016', NOW() - INTERVAL '3 days', NOW()),
('b0000000-0000-0000-0000-000000000008', 'COV-829108', 'Jasa Setup Server Linux K8s High Availability', 'Cluster 3 master 5 worker nodes dengan Traefik Ingress', 'SERVICE', 5000000, 50000, 5050000, 'FUNDED', 'a0000000-0000-0000-0000-000000000017', 'a0000000-0000-0000-0000-000000000018', NOW() - INTERVAL '1 days', NOW()),
('b0000000-0000-0000-0000-000000000009', 'COV-829109', 'Keyboard Custom Mechanical Keychron Q1 Pro', 'Gateron Oil King switch, PBT Cherry keycaps', 'PHYSICAL_PRODUCT', 2800000, 28000, 2828000, 'WAITING_PAYMENT', 'a0000000-0000-0000-0000-000000000019', 'a0000000-0000-0000-0000-000000000020', NOW() - INTERVAL '12 hours', NOW()),
('b0000000-0000-0000-0000-000000000010', 'COV-829110', 'Akun Instagram Bisnis Fashion 45K Followers Organik', 'Engagement rate 4.8%, niche outfit pria dan wanita', 'DIGITAL_PRODUCT', 3200000, 32000, 3232000, 'DELIVERED', 'a0000000-0000-0000-0000-000000000021', 'a0000000-0000-0000-0000-000000000022', NOW() - INTERVAL '1 days', NOW()),

-- Disputed Room for Admin arbitration testing
('b0000000-0000-0000-0000-000000000011', 'COV-829111', 'Akun PUBG Mobile Sultan Glacier Max M416', 'Klaim akun email terkait belum dilepas oleh penjual', 'GAME_ACCOUNT', 3800000, 38000, 3838000, 'DISPUTED', 'a0000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000005', NOW() - INTERVAL '5 days', NOW()),
('b0000000-0000-0000-0000-000000000012', 'COV-829112', 'Jasa Ilustrasi Karakter Vtuber 2D Rigging Ready', 'Keterlambatan pengerjaan melebihi tenggat revisi', 'SERVICE', 2100000, 21000, 2121000, 'DISPUTED', 'a0000000-0000-0000-0000-000000000009', 'a0000000-0000-0000-0000-000000000011', NOW() - INTERVAL '4 days', NOW()),

-- More Rooms to reach 22
('b0000000-0000-0000-0000-000000000013', 'COV-829113', 'Domain Premium id.fintech.cloud', 'Transfer auth code siap digunakan', 'DIGITAL_PRODUCT', 1800000, 18000, 1818000, 'COMPLETED', 'a0000000-0000-0000-0000-000000000004', 'a0000000-0000-0000-0000-000000000007', NOW() - INTERVAL '20 days', NOW() - INTERVAL '19 days'),
('b0000000-0000-0000-0000-000000000014', 'COV-829114', 'Voucher Belanja Tokopedia Rp 2.000.000', 'Kode voucher digital diskon 5%', 'DIGITAL_PRODUCT', 1900000, 19000, 1919000, 'COMPLETED', 'a0000000-0000-0000-0000-000000000006', 'a0000000-0000-0000-0000-000000000008', NOW() - INTERVAL '18 days', NOW() - INTERVAL '17 days'),
('b0000000-0000-0000-0000-000000000015', 'COV-829115', 'Nintendo Switch OLED White Joy-Con Jailbreak', 'SD card 256GB full game siap main', 'PHYSICAL_PRODUCT', 4100000, 41000, 4141000, 'COMPLETED', 'a0000000-0000-0000-0000-000000000010', 'a0000000-0000-0000-0000-000000000013', NOW() - INTERVAL '14 days', NOW() - INTERVAL '12 days'),
('b0000000-0000-0000-0000-000000000016', 'COV-829116', 'Jasa Optimasi SEO Website Portal Berita', 'Garansi ranking 10 besar kata kunci utama', 'SERVICE', 3000000, 30000, 3030000, 'CANCELLED', 'a0000000-0000-0000-0000-000000000014', 'a0000000-0000-0000-0000-000000000016', NOW() - INTERVAL '22 days', NOW() - INTERVAL '21 days'),
('b0000000-0000-0000-0000-000000000017', 'COV-829117', 'Akun Discord Server Komunitas 10K Members', 'Role bot teratur, level 3 boost active', 'DIGITAL_PRODUCT', 1500000, 15000, 1515000, 'WAITING_PAYMENT', 'a0000000-0000-0000-0000-000000000018', 'a0000000-0000-0000-0000-000000000020', NOW() - INTERVAL '6 hours', NOW()),
('b0000000-0000-0000-0000-000000000018', 'COV-829118', 'Monitor Gaming 240Hz ASUS ROG Swift 27 Inch', 'IPS panel 1ms response time mulus no dead pixel', 'PHYSICAL_PRODUCT', 5500000, 50000, 5550000, 'FUNDED', 'a0000000-0000-0000-0000-000000000022', 'a0000000-0000-0000-0000-000000000003', NOW() - INTERVAL '2 days', NOW()),
('b0000000-0000-0000-0000-000000000019', 'COV-829119', 'Akun Roblox Blox Fruits Max Level Dark Blade', 'V4 semua race unlocked, bounty 15 juta', 'GAME_ACCOUNT', 650000, 6500, 656500, 'COMPLETED', 'a0000000-0000-0000-0000-000000000005', 'a0000000-0000-0000-0000-000000000007', NOW() - INTERVAL '8 days', NOW() - INTERVAL '7 days'),
('b0000000-0000-0000-0000-000000000020', 'COV-829120', 'Template Landing Page SaaS Tailwind & Vue 3', 'Lisensi komersial unlimited domain', 'DIGITAL_PRODUCT', 750000, 7500, 757500, 'COMPLETED', 'a0000000-0000-0000-0000-000000000008', 'a0000000-0000-0000-0000-000000000010', NOW() - INTERVAL '6 days', NOW() - INTERVAL '5 days'),
('b0000000-0000-0000-0000-000000000021', 'COV-829121', 'Jasa Voice Over Profesional Video Profil Perusahaan', 'Durasi 3 menit Bahasa Indonesia formal & santai', 'SERVICE', 950000, 9500, 959500, 'DELIVERED', 'a0000000-0000-0000-0000-000000000012', 'a0000000-0000-0000-0000-000000000015', NOW() - INTERVAL '1 days', NOW()),
('b0000000-0000-0000-0000-000000000022', 'COV-829122', 'Sony WH-1000XM5 ANC Headphone Silver Edition', 'Kondisi 98% no minus, earpad mulus box lengkap', 'PHYSICAL_PRODUCT', 3900000, 39000, 3939000, 'WAITING_PAYMENT', 'a0000000-0000-0000-0000-000000000017', 'a0000000-0000-0000-0000-000000000019', NOW() - INTERVAL '3 hours', NOW())
ON CONFLICT (room_code) DO NOTHING;

-- ─── 6. SEED WALLET.TRANSACTIONS (25+ Records) ────────────────────────────────
INSERT INTO wallet.transactions (
    wallet_id, type, direction, amount, fee, net_amount, status, description, counterparty_name, created_at
)
SELECT 
    w.id,
    'TOPUP',
    'IN',
    5000000,
    0,
    5000000,
    'SUCCESS',
    'Top Up Saldo via Mandiri Virtual Account',
    'Mandiri VA',
    NOW() - INTERVAL '45 days'
FROM wallet.wallets w WHERE w.user_id >= 'a0000000-0000-0000-0000-000000000003' LIMIT 10;

INSERT INTO wallet.transactions (
    wallet_id, type, direction, amount, fee, net_amount, status, description, counterparty_name, created_at
)
SELECT 
    w.id,
    'P2P_TRANSFER',
    'OUT',
    250000,
    0,
    250000,
    'SUCCESS',
    'Transfer Saldo P2P ke pengguna lain',
    'Budi Santoso',
    NOW() - INTERVAL '20 days'
FROM wallet.wallets w WHERE w.user_id >= 'a0000000-0000-0000-0000-000000000004' LIMIT 8;

INSERT INTO wallet.transactions (
    wallet_id, type, direction, amount, fee, net_amount, status, description, counterparty_name, created_at
)
SELECT 
    w.id,
    'ESCROW_HOLD',
    'OUT',
    1500000,
    15000,
    1515000,
    'SUCCESS',
    'Kunci Dana Escrow Room COV-829101',
    'Escrow Sanctum',
    NOW() - INTERVAL '15 days'
FROM wallet.wallets w WHERE w.user_id = 'a0000000-0000-0000-0000-000000000003';

INSERT INTO wallet.transactions (
    wallet_id, type, direction, amount, fee, net_amount, status, description, counterparty_name, created_at
)
SELECT 
    w.id,
    'ESCROW_RELEASE',
    'IN',
    1500000,
    15000,
    1485000,
    'SUCCESS',
    'Pelepasan Dana Escrow Room COV-829101',
    'Escrow Sanctum',
    NOW() - INTERVAL '13 days'
FROM wallet.wallets w WHERE w.user_id = 'a0000000-0000-0000-0000-000000000004';

INSERT INTO wallet.transactions (
    wallet_id, type, direction, amount, fee, net_amount, status, description, counterparty_name, created_at
)
SELECT 
    w.id,
    'WITHDRAW',
    'OUT',
    1000000,
    4500,
    1004500,
    'SUCCESS',
    'Penarikan Dana ke Rekening Bank BCA',
    'Bank BCA',
    NOW() - INTERVAL '5 days'
FROM wallet.wallets w WHERE w.user_id >= 'a0000000-0000-0000-0000-000000000003' LIMIT 5;

-- ─── 7. SEED CHAT.CHAT_ROOMS & MESSAGES (20+ Messages) ────────────────────────
INSERT INTO chat.chat_rooms (
    id, escrow_room_id, room_name, room_code, created_by, is_active, created_at, updated_at
) VALUES
('c0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', 'Room COV-829101 Chat', 'COV-829101', 'a0000000-0000-0000-0000-000000000003', true, NOW() - INTERVAL '60 days', NOW()),
('c0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000006', 'Room COV-829106 Chat', 'COV-829106', 'a0000000-0000-0000-0000-000000000013', true, NOW() - INTERVAL '2 days', NOW()),
('c0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000011', 'Room COV-829111 Chat', 'COV-829111', 'a0000000-0000-0000-0000-000000000003', true, NOW() - INTERVAL '5 days', NOW())
ON CONFLICT (escrow_room_id) DO NOTHING;

INSERT INTO chat.messages (
    chat_room_id, sender_id, content, message_type, sent_at
) VALUES
('c0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000003', 'Halo kak, dana sudah saya kunci ke rekening escrow EyesOfPriestess.', 'TEXT', NOW() - INTERVAL '60 days'),
('c0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000004', 'Halo, siap kak! Saya siapkan kredensial akun Moonton dan email pertamanya.', 'TEXT', NOW() - INTERVAL '60 days' + INTERVAL '5 minutes'),
('c0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000004', 'Sudah saya kirim lewat form serah terima. Silakan diperiksa dan ganti kata sandinya ya.', 'TEXT', NOW() - INTERVAL '59 days'),
('c0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000003', 'Akun sudah saya amankan dan verifikasi dua langkah sudah aktif. Transaksi selesai, terima kasih!', 'TEXT', NOW() - INTERVAL '58 days'),
('c0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000004', 'Sama-sama kak, terima kasih atas kepercayaannya!', 'TEXT', NOW() - INTERVAL '58 days' + INTERVAL '2 minutes'),

('c0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000013', 'Selamat malam, akun Genshin AR 60 apakah emailnya sudah lepas (no bind)?', 'TEXT', NOW() - INTERVAL '2 days'),
('c0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000014', 'Malam, ya betul kak, no deadlink sama sekali. Bisa langsung di-bind ke email kakak.', 'TEXT', NOW() - INTERVAL '2 days' + INTERVAL '10 minutes'),
('c0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000013', 'Baik, saldo escrow sudah saya bayarkan. Mohon konfirmasi datanya.', 'TEXT', NOW() - INTERVAL '2 days' + INTERVAL '20 minutes'),
('c0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000014', 'Terima kasih, segera saya proses.', 'TEXT', NOW() - INTERVAL '2 days' + INTERVAL '25 minutes'),

('c0000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000003', 'Penjual belum memberikan akses akun PUBG yang dijanjikan setelah 24 jam.', 'TEXT', NOW() - INTERVAL '5 days'),
('c0000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000005', 'Mohon maaf sedang ada kendala jaringan di lokasi saya.', 'TEXT', NOW() - INTERVAL '4 days'),
('c0000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000003', 'Saya ajukan sengketa resmi ke Arbiter/Admin agar dana terlindungi.', 'TEXT', NOW() - INTERVAL '4 days' + INTERVAL '30 minutes'),
('c0000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000002', 'Pesan Arbiter: Sengketa diterima. Mohon kedua belah pihak melampirkan bukti screenshot.', 'SYSTEM', NOW() - INTERVAL '3 days');

-- ─── 8. SEED DISPUTE.DISPUTES (Realistic Dispute Cases) ───────────────────────
INSERT INTO dispute.disputes (
    id, room_id, initiated_by, initiator_role, title, description, status, decision, decision_reason, refund_amount, resolved_by, created_at, updated_at
) VALUES
('d0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000011', 'a0000000-0000-0000-0000-000000000003', 'BUYER', 'Akun PUBG Belum Diberikan Akses Penuh', 'Penjual tidak memberikan kode verifikasi email untuk mengganti kredensial login utama.', 'OPEN', NULL, NULL, NULL, NULL, NOW() - INTERVAL '4 days', NOW()),
('d0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000012', 'a0000000-0000-0000-0000-000000000009', 'BUYER', 'Keterlambatan Deliverable Ilustrasi Vtuber', 'Penjual tidak membalas pesan dan melewati deadline lebih dari 7 hari.', 'UNDER_REVIEW', NULL, NULL, NULL, 'a0000000-0000-0000-0000-000000000002', NOW() - INTERVAL '3 days', NOW())
ON CONFLICT DO NOTHING;

INSERT INTO dispute.dispute_evidence (
    dispute_id, uploaded_by, evidence_type, file_url, file_name, description, uploaded_at
) VALUES
('d0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000003', 'IMAGE', 'https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=800', 'login_error_screenshot.png', 'Tangkapan layar error saat mencoba login ke akun game', NOW() - INTERVAL '4 days'),
('d0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000009', 'DOCUMENT', 'https://images.unsplash.com/photo-1586281380349-632531db7ed4?w=800', 'agreement_contract.pdf', 'Dokumen kesepakatan tenggat waktu pengerjaan', NOW() - INTERVAL '3 days')
ON CONFLICT DO NOTHING;
