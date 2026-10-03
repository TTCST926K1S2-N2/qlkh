-- =========================================================
-- S2-06 - Co cau to chuc kinh doanh
-- =========================================================

CREATE TABLE IF NOT EXISTS regions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_regions_code
        UNIQUE (code),

    CONSTRAINT uk_regions_name
        UNIQUE (name)
);


-- ---------------------------------------------------------
-- business_groups: bổ sung thông tin cây tổ chức
-- ---------------------------------------------------------

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE business_groups ADD COLUMN code VARCHAR(50) NULL AFTER id',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'business_groups'
      AND column_name = 'code'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE business_groups ADD COLUMN parent_id BIGINT NULL AFTER name',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'business_groups'
      AND column_name = 'parent_id'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE business_groups ADD COLUMN leader_id BIGINT NULL AFTER parent_id',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'business_groups'
      AND column_name = 'leader_id'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE business_groups ADD COLUMN region_id BIGINT NULL AFTER leader_id',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'business_groups'
      AND column_name = 'region_id'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE business_groups ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT ''ACTIVE'' AFTER region_id',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'business_groups'
      AND column_name = 'status'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- ---------------------------------------------------------
-- Sinh code cho dữ liệu nhóm cũ
-- ---------------------------------------------------------

UPDATE business_groups
SET code = CONCAT('BG_', id)
WHERE code IS NULL
   OR TRIM(code) = '';


ALTER TABLE business_groups
MODIFY COLUMN code VARCHAR(50) NOT NULL;


-- ---------------------------------------------------------
-- Unique code
-- ---------------------------------------------------------

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE business_groups ADD CONSTRAINT uk_business_groups_code UNIQUE (code)',
        'SELECT 1'
    )
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'business_groups'
      AND index_name = 'uk_business_groups_code'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- ---------------------------------------------------------
-- Foreign key parent -> business_groups
-- ---------------------------------------------------------

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE business_groups ADD CONSTRAINT fk_business_groups_parent FOREIGN KEY (parent_id) REFERENCES business_groups(id) ON DELETE RESTRICT',
        'SELECT 1'
    )
    FROM information_schema.table_constraints
    WHERE constraint_schema = DATABASE()
      AND table_name = 'business_groups'
      AND constraint_name = 'fk_business_groups_parent'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- ---------------------------------------------------------
-- Foreign key leader -> users
-- ---------------------------------------------------------

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE business_groups ADD CONSTRAINT fk_business_groups_leader FOREIGN KEY (leader_id) REFERENCES users(id) ON DELETE RESTRICT',
        'SELECT 1'
    )
    FROM information_schema.table_constraints
    WHERE constraint_schema = DATABASE()
      AND table_name = 'business_groups'
      AND constraint_name = 'fk_business_groups_leader'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- ---------------------------------------------------------
-- Foreign key region -> regions
-- ---------------------------------------------------------

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE business_groups ADD CONSTRAINT fk_business_groups_region FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE RESTRICT',
        'SELECT 1'
    )
    FROM information_schema.table_constraints
    WHERE constraint_schema = DATABASE()
      AND table_name = 'business_groups'
      AND constraint_name = 'fk_business_groups_region'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'CREATE INDEX idx_regions_status ON regions(status)',
        'SELECT 1'
    )
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'regions'
      AND index_name = 'idx_regions_status'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;