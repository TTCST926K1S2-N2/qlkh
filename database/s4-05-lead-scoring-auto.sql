-- S4-05: Automatic Lead Scoring
-- MySQL 8.0+
-- Apply only after backing up the database.
-- This migration is not automatically executed.

DELIMITER $$

CREATE FUNCTION s405_calculate_lead_score(
    p_industry VARCHAR(150),
    p_company_size VARCHAR(100),
    p_source VARCHAR(100),
    p_interest TEXT
)
RETURNS DECIMAL(5,2)
READS SQL DATA
BEGIN
    DECLARE v_industry_score DECIMAL(5,2) DEFAULT 0;
    DECLARE v_size_score DECIMAL(5,2) DEFAULT 0;
    DECLARE v_source_score DECIMAL(5,2) DEFAULT 0;
    DECLARE v_interest_score DECIMAL(5,2) DEFAULT 0;

    DECLARE v_industry_weight DECIMAL(5,2);
    DECLARE v_size_weight DECIMAL(5,2);
    DECLARE v_source_weight DECIMAL(5,2);
    DECLARE v_interest_weight DECIMAL(5,2);

    DECLARE v_targets JSON;
    DECLARE v_size VARCHAR(100);
    DECLARE v_first VARCHAR(100);
    DECLARE v_employees BIGINT DEFAULT 0;
    DECLARE v_i INT DEFAULT 0;
    DECLARE v_target_count INT DEFAULT 0;
    DECLARE v_target VARCHAR(150);

    SELECT
        industry_weight,
        company_size_weight,
        source_weight,
        interest_weight,
        target_industries
    INTO
        v_industry_weight,
        v_size_weight,
        v_source_weight,
        v_interest_weight,
        v_targets
    FROM lead_scoring_config
    WHERE id = 1;

    IF p_industry IS NOT NULL
       AND TRIM(p_industry) <> ''
       AND v_targets IS NOT NULL
       AND JSON_TYPE(v_targets) = 'ARRAY'
       AND JSON_LENGTH(v_targets) > 0 THEN

        SET v_industry_score = 50;
        SET v_target_count = JSON_LENGTH(v_targets);

        WHILE v_i < v_target_count DO
            SET v_target = JSON_UNQUOTE(
                JSON_EXTRACT(v_targets, CONCAT('$[', v_i, ']'))
            );

            IF BINARY UPPER(TRIM(p_industry))
               = BINARY UPPER(TRIM(v_target)) THEN
                SET v_industry_score = 100;
                SET v_i = v_target_count;
            ELSE
                SET v_i = v_i + 1;
            END IF;
        END WHILE;
    END IF;

    SET v_size = TRIM(
        REPLACE(
            REPLACE(COALESCE(p_company_size, ''), '–', '-'),
            '—', '-'
        )
    );

    IF REGEXP_LIKE(v_size, '^[0-9]+([[:space:]]*-[[:space:]]*[0-9]+)?$') THEN
        SET v_first = TRIM(SUBSTRING_INDEX(v_size, '-', 1));

        IF CHAR_LENGTH(v_first) < 19
   OR (
       CHAR_LENGTH(v_first) = 19
       AND BINARY v_first <= BINARY '9223372036854775807'
   ) THEN
            SET v_employees = CAST(v_first AS UNSIGNED);

            IF v_employees >= 50 THEN
                SET v_size_score = 100;
            ELSEIF v_employees >= 10 THEN
                SET v_size_score = 70;
            ELSE
                SET v_size_score = 40;
            END IF;
        END IF;
    END IF;

    CASE UPPER(TRIM(COALESCE(p_source, '')))
        WHEN 'WEBSITE' THEN SET v_source_score = 80;
        WHEN 'MANUAL' THEN SET v_source_score = 70;
        WHEN 'EXCEL' THEN SET v_source_score = 50;
        ELSE SET v_source_score = 0;
    END CASE;

    CASE UPPER(TRIM(COALESCE(p_interest, '')))
        WHEN 'HIGH' THEN SET v_interest_score = 100;
        WHEN 'CAO' THEN SET v_interest_score = 100;
        WHEN 'MEDIUM' THEN SET v_interest_score = 60;
        WHEN 'TRUNG BINH' THEN SET v_interest_score = 60;
        WHEN 'LOW' THEN SET v_interest_score = 20;
        WHEN 'THAP' THEN SET v_interest_score = 20;
        ELSE SET v_interest_score = 0;
    END CASE;

    RETURN ROUND(
        (
            v_industry_score * v_industry_weight +
            v_size_score * v_size_weight +
            v_source_score * v_source_weight +
            v_interest_score * v_interest_weight
        ) / 100,
        2
    );
END$$

CREATE TRIGGER s405_leads_before_insert
BEFORE INSERT ON leads
FOR EACH ROW
BEGIN
    IF NEW.status NOT IN ('MERGED', 'CONVERTED') THEN
        SET NEW.score = s405_calculate_lead_score(
            NEW.industry,
            NEW.company_size,
            NEW.source,
            NEW.interest
        );
    END IF;
END$$

CREATE TRIGGER s405_leads_before_update
BEFORE UPDATE ON leads
FOR EACH ROW
BEGIN
    IF NEW.status NOT IN ('MERGED', 'CONVERTED')
       AND (
           NOT (NEW.industry <=> OLD.industry)
           OR NOT (NEW.company_size <=> OLD.company_size)
           OR NOT (NEW.source <=> OLD.source)
           OR NOT (NEW.interest <=> OLD.interest)
           OR NOT (NEW.status <=> OLD.status)
       ) THEN

        SET NEW.score = s405_calculate_lead_score(
            NEW.industry,
            NEW.company_size,
            NEW.source,
            NEW.interest
        );
    END IF;
END$$

DELIMITER ;