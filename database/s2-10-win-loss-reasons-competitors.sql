-- =========================================================
-- S2-10 - DANH MUC LY DO THANG / THUA VA DOI THU CANH TRANH
-- =========================================================

CREATE TABLE IF NOT EXISTS win_loss_reasons (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    description VARCHAR(1000) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_win_loss_reasons_code (code),

    CONSTRAINT chk_win_loss_reason_type
        CHECK (type IN ('WON', 'LOST')),

    CONSTRAINT chk_win_loss_reason_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS competitors (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    description VARCHAR(1000) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_competitors_code (code),

    CONSTRAINT chk_competitor_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;


-- Du lieu khoi tao toi thieu de kiem tra
INSERT INTO win_loss_reasons
    (name, code, type, status, description)
SELECT
    'Giá phù hợp',
    'WON_PRICE',
    'WON',
    'ACTIVE',
    'Khách hàng đánh giá mức giá phù hợp'
WHERE NOT EXISTS (
    SELECT 1
    FROM win_loss_reasons
    WHERE code = 'WON_PRICE'
);


INSERT INTO win_loss_reasons
    (name, code, type, status, description)
SELECT
    'Đáp ứng yêu cầu',
    'WON_REQUIREMENT',
    'WON',
    'ACTIVE',
    'Giải pháp đáp ứng tốt yêu cầu khách hàng'
WHERE NOT EXISTS (
    SELECT 1
    FROM win_loss_reasons
    WHERE code = 'WON_REQUIREMENT'
);


INSERT INTO win_loss_reasons
    (name, code, type, status, description)
SELECT
    'Giá cao',
    'LOST_PRICE',
    'LOST',
    'ACTIVE',
    'Khách hàng đánh giá mức giá chưa phù hợp'
WHERE NOT EXISTS (
    SELECT 1
    FROM win_loss_reasons
    WHERE code = 'LOST_PRICE'
);


INSERT INTO win_loss_reasons
    (name, code, type, status, description)
SELECT
    'Chọn đối thủ',
    'LOST_COMPETITOR',
    'LOST',
    'ACTIVE',
    'Khách hàng lựa chọn giải pháp của đối thủ'
WHERE NOT EXISTS (
    SELECT 1
    FROM win_loss_reasons
    WHERE code = 'LOST_COMPETITOR'
);