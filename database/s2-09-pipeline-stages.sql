-- =========================================================
-- S2-09 - Pipeline stages
-- Ho tro ca DB moi va DB da co bang stages cu
-- =========================================================

CREATE TABLE IF NOT EXISTS stages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) NOT NULL,
    stage_order INT NULL,
    win_probability DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    exit_condition VARCHAR(1000) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    description VARCHAR(1000) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_stages_code UNIQUE (code)
);

-- ---------------------------------------------------------
-- Them stage_order neu DB cu chua co
-- ---------------------------------------------------------

SET @has_stage_order = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'stages'
      AND column_name = 'stage_order'
);

SET @sql = IF(
    @has_stage_order = 0,
    'ALTER TABLE stages ADD COLUMN stage_order INT NULL AFTER code',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- ---------------------------------------------------------
-- Them win_probability neu DB cu chua co
-- ---------------------------------------------------------

SET @has_probability = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'stages'
      AND column_name = 'win_probability'
);

SET @sql = IF(
    @has_probability = 0,
    'ALTER TABLE stages ADD COLUMN win_probability DECIMAL(5,2) NOT NULL DEFAULT 0.00 AFTER stage_order',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- ---------------------------------------------------------
-- Them exit_condition neu DB cu chua co
-- ---------------------------------------------------------

SET @has_exit_condition = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'stages'
      AND column_name = 'exit_condition'
);

SET @sql = IF(
    @has_exit_condition = 0,
    'ALTER TABLE stages ADD COLUMN exit_condition VARCHAR(1000) NULL AFTER win_probability',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- ---------------------------------------------------------
-- Gan thu tu cho du lieu cu
-- ---------------------------------------------------------

SET @next_order = (
    SELECT COALESCE(MAX(stage_order), 0)
    FROM stages
    WHERE stage_order IS NOT NULL
      AND stage_order > 0
);

UPDATE stages
SET stage_order = (@next_order := @next_order + 1)
WHERE stage_order IS NULL
   OR stage_order <= 0
ORDER BY id;


-- stage_order bat buoc sau khi migrate du lieu cu
ALTER TABLE stages
MODIFY COLUMN stage_order INT NOT NULL;


-- ---------------------------------------------------------
-- Unique index cho thu tu neu chua co
-- ---------------------------------------------------------

SET @has_order_index = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'stages'
      AND index_name = 'uk_stages_order'
);

SET @sql = IF(
    @has_order_index = 0,
    'ALTER TABLE stages ADD CONSTRAINT uk_stages_order UNIQUE (stage_order)',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
