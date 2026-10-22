-- ============================================================
-- HE THONG QUAN LY KHACH HANG - NHOM 2
-- SCRIPT TAO DU LIEU MAU PHUC VU DEMO
-- Database kiem thu: qlkh_main_test
-- ============================================================

-- Du lieu du kien:
-- 10 customers
-- 15 contacts
-- 3 campaigns
-- 20 leads
-- 5 opportunities
-- 5 contracts

-- Yeu cau:
-- 1. Khong xoa du lieu hien co.
-- 2. Khong su dung thong tin ca nhan that.
-- 3. Khong tao trung khi chay lai.
-- 4. Dam bao khoa ngoai hop le.
-- 5. Chi kiem thu tren qlkh_main_test.

-- NOI DUNG INSERT SE DUOC BO SUNG SAU KHI RA SOAT.
-- DEMO CUSTOMERS BEGIN
-- Tao 10 khach hang mau, khong ghi de du lieu cu.

SET @demo_owner_id = (
    SELECT id
    FROM users
    WHERE role = 'SALES' AND status = 'ACTIVE'
    ORDER BY id
    LIMIT 1
);

-- Khong chay INSERT neu khong tim duoc owner hop le.
-- Dieu kien owner_id IS NOT NULL duoc kiem tra trong moi INSERT.

INSERT INTO customers
    (company_name, tax_code, industry, company_size, owner_id, status)
SELECT
    d.company_name,
    d.tax_code,
    d.industry,
    d.company_size,
    @demo_owner_id,
    'POTENTIAL'
FROM (
    SELECT 'DEMO - Cong ty Sao Mai' AS company_name, '9900000001' AS tax_code, 'Cong nghe' AS industry, 'SME' AS company_size
    UNION ALL SELECT 'DEMO - Cong ty Binh Minh', '9900000002', 'Thuong mai', 'SME'
    UNION ALL SELECT 'DEMO - Cong ty Hoa Sen', '9900000003', 'Dich vu', 'SME'
    UNION ALL SELECT 'DEMO - Cong ty Dai Duong', '9900000004', 'Logistics', 'LARGE'
    UNION ALL SELECT 'DEMO - Cong ty Thanh Cong', '9900000005', 'San xuat', 'SME'
    UNION ALL SELECT 'DEMO - Cong ty Phuong Nam', '9900000006', 'Ban le', 'SME'
    UNION ALL SELECT 'DEMO - Cong ty An Phat', '9900000007', 'Xay dung', 'LARGE'
    UNION ALL SELECT 'DEMO - Cong ty Minh Chau', '9900000008', 'Giao duc', 'SME'
    UNION ALL SELECT 'DEMO - Cong ty Hoang Gia', '9900000009', 'Tai chinh', 'LARGE'
    UNION ALL SELECT 'DEMO - Cong ty Tan Viet', '9900000010', 'Dich vu', 'SME'
) AS d
WHERE @demo_owner_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM customers c
      WHERE c.tax_code = d.tax_code
  );

-- DEMO CUSTOMERS END
-- DEMO CONTACTS BEGIN
-- Tao 15 nguoi lien he gia lap cho 10 khach hang DEMO.
-- Khong su dung email hoac so dien thoai that.

INSERT INTO contacts (customer_id, full_name, email, phone)
SELECT
    c.id,
    d.full_name,
    d.email,
    d.phone
FROM (
    SELECT '9900000001' AS tax_code, 'Nguyen Demo An' AS full_name, 'contact01@example.com' AS email, '0900000001' AS phone
    UNION ALL SELECT '9900000001', 'Tran Demo Binh', 'contact02@example.com', '0900000002'
    UNION ALL SELECT '9900000002', 'Le Demo Chi', 'contact03@example.com', '0900000003'
    UNION ALL SELECT '9900000002', 'Pham Demo Dung', 'contact04@example.com', '0900000004'
    UNION ALL SELECT '9900000003', 'Hoang Demo Ha', 'contact05@example.com', '0900000005'
    UNION ALL SELECT '9900000003', 'Vu Demo Huy', 'contact06@example.com', '0900000006'
    UNION ALL SELECT '9900000004', 'Do Demo Khanh', 'contact07@example.com', '0900000007'
    UNION ALL SELECT '9900000004', 'Bui Demo Lan', 'contact08@example.com', '0900000008'
    UNION ALL SELECT '9900000005', 'Ngo Demo Minh', 'contact09@example.com', '0900000009'
    UNION ALL SELECT '9900000005', 'Dang Demo Nam', 'contact10@example.com', '0900000010'
    UNION ALL SELECT '9900000006', 'Phan Demo Oanh', 'contact11@example.com', '0900000011'
    UNION ALL SELECT '9900000007', 'Trinh Demo Phuc', 'contact12@example.com', '0900000012'
    UNION ALL SELECT '9900000008', 'Mai Demo Quynh', 'contact13@example.com', '0900000013'
    UNION ALL SELECT '9900000009', 'Cao Demo Son', 'contact14@example.com', '0900000014'
    UNION ALL SELECT '9900000010', 'Duong Demo Trang', 'contact15@example.com', '0900000015'
) AS d
JOIN customers c ON c.tax_code = d.tax_code
WHERE NOT EXISTS (
    SELECT 1
    FROM contacts existing
    WHERE existing.customer_id = c.id
      AND existing.email = d.email
);

-- DEMO CONTACTS END
-- DEMO CAMPAIGNS BEGIN
-- Tao 3 chien dich marketing gia lap.

INSERT INTO campaigns
    (name, budget, start_date, end_date, channel)
SELECT
    d.name,
    d.budget,
    d.start_date,
    d.end_date,
    d.channel
FROM (
    SELECT
        'DEMO - Chien dich Website' AS name,
        10000000.00 AS budget,
        '2026-10-01' AS start_date,
        '2026-12-31' AS end_date,
        'WEBSITE' AS channel
    UNION ALL SELECT
        'DEMO - Chien dich Facebook',
        15000000.00,
        '2026-10-01',
        '2026-12-31',
        'FACEBOOK'
    UNION ALL SELECT
        'DEMO - Chien dich Email',
        5000000.00,
        '2026-10-01',
        '2026-12-31',
        'EMAIL'
) AS d
WHERE NOT EXISTS (
    SELECT 1
    FROM campaigns c
    WHERE c.name = d.name
);

-- DEMO CAMPAIGNS END
-- DEMO LEADS BEGIN
-- Tao 20 Lead gia lap, lien ket voi chien dich va SALES.

SET @demo_sales_id = (
    SELECT id
    FROM users
    WHERE role = 'SALES' AND status = 'ACTIVE'
    ORDER BY id
    LIMIT 1
);

INSERT INTO leads
    (full_name, email, phone, company, interest, status, source, campaign_id, assigned_to)
SELECT
    names.full_name,
    d.email,
    d.phone,
    d.company,
    d.interest,
    d.status,
    'WEBSITE',
    c.id,
    @demo_sales_id
FROM (
    SELECT 1 AS n, 'Nguyen Demo 01' AS full_name
    UNION ALL SELECT 2, 'Tran Demo 02'
    UNION ALL SELECT 3, 'Le Demo 03'
    UNION ALL SELECT 4, 'Pham Demo 04'
    UNION ALL SELECT 5, 'Hoang Demo 05'
    UNION ALL SELECT 6, 'Vu Demo 06'
    UNION ALL SELECT 7, 'Do Demo 07'
    UNION ALL SELECT 8, 'Bui Demo 08'
    UNION ALL SELECT 9, 'Ngo Demo 09'
    UNION ALL SELECT 10, 'Dang Demo 10'
    UNION ALL SELECT 11, 'Phan Demo 11'
    UNION ALL SELECT 12, 'Trinh Demo 12'
    UNION ALL SELECT 13, 'Mai Demo 13'
    UNION ALL SELECT 14, 'Cao Demo 14'
    UNION ALL SELECT 15, 'Duong Demo 15'
    UNION ALL SELECT 16, 'Nguyen Demo 16'
    UNION ALL SELECT 17, 'Tran Demo 17'
    UNION ALL SELECT 18, 'Le Demo 18'
    UNION ALL SELECT 19, 'Pham Demo 19'
    UNION ALL SELECT 20, 'Hoang Demo 20'
) AS names
JOIN (
    SELECT
        n,
        CONCAT('demo.lead', LPAD(n, 2, '0'), '@example.com') AS email,
        CONCAT('090000', LPAD(n + 100, 4, '0')) AS phone,
        CONCAT('DEMO Company ', LPAD(n, 2, '0')) AS company,
        'Tu van CRM' AS interest,
        'NEW' AS status,
        CASE MOD(n, 3)
            WHEN 0 THEN 'DEMO - Chien dich Website'
            WHEN 1 THEN 'DEMO - Chien dich Facebook'
            ELSE 'DEMO - Chien dich Email'
        END AS campaign_name
    FROM (
        SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3
        UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6
        UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
        UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
        UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15
        UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18
        UNION ALL SELECT 19 UNION ALL SELECT 20
    ) nums
) d ON d.n = names.n
JOIN campaigns c ON c.name = d.campaign_name
WHERE @demo_sales_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM leads existing
      WHERE existing.email = d.email
  );

-- DEMO LEADS END
-- DEMO OPPORTUNITIES BEGIN
-- Tao 5 co hoi kinh doanh gia lap.

INSERT INTO opportunities
    (customer_id, name, status, stage_id)
SELECT
    c.id,
    d.name,
    'OPEN',
    s.id
FROM (
    SELECT '9900000001' AS tax_code,
           'DEMO - Tu van he thong CRM' AS name,
           'CONTACT' AS stage_code
    UNION ALL SELECT
           '9900000002',
           'DEMO - Trien khai CRM doanh nghiep',
           'NEEDS'
    UNION ALL SELECT
           '9900000003',
           'DEMO - Nang cap quan ly khach hang',
           'PROPOSAL'
    UNION ALL SELECT
           '9900000004',
           'DEMO - Tu dong hoa quy trinh ban hang',
           'QUOTATION'
    UNION ALL SELECT
           '9900000005',
           'DEMO - Hop tac phat trien CRM',
           'NEGOTIATION'
) d
JOIN customers c ON c.tax_code = d.tax_code
JOIN stages s ON s.code = d.stage_code
WHERE NOT EXISTS (
    SELECT 1
    FROM opportunities existing
    WHERE existing.customer_id = c.id
      AND existing.name = d.name
);

-- DEMO OPPORTUNITIES END
-- DEMO CONTRACTS BEGIN
-- Tao 5 hop dong mau, khong ghi de hop dong hien co.

INSERT INTO contracts
    (contract_code, customer_id, contract_name, contract_value, status)
SELECT
    d.contract_code,
    c.id,
    d.contract_name,
    d.contract_value,
    'DRAFT'
FROM (
    SELECT 'DEMO-HD-001' AS contract_code,
           '9900000001' AS tax_code,
           'DEMO - Hop dong tu van CRM' AS contract_name,
           15000000.00 AS contract_value
    UNION ALL SELECT
           'DEMO-HD-002',
           '9900000002',
           'DEMO - Hop dong trien khai CRM',
           30000000.00
    UNION ALL SELECT
           'DEMO-HD-003',
           '9900000003',
           'DEMO - Hop dong nang cap he thong',
           25000000.00
    UNION ALL SELECT
           'DEMO-HD-004',
           '9900000004',
           'DEMO - Hop dong tu dong hoa ban hang',
           40000000.00
    UNION ALL SELECT
           'DEMO-HD-005',
           '9900000005',
           'DEMO - Hop dong hop tac CRM',
           20000000.00
) d
JOIN customers c ON c.tax_code = d.tax_code
WHERE NOT EXISTS (
    SELECT 1
    FROM contracts existing
    WHERE existing.contract_code = d.contract_code
);

-- DEMO CONTRACTS END