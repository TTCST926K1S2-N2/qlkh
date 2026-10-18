-- S4-05: Lead Scoring configuration
CREATE TABLE IF NOT EXISTS lead_scoring_config (
    id INT PRIMARY KEY,
    expected_value_weight DECIMAL(5,2) NOT NULL,
    completeness_weight DECIMAL(5,2) NOT NULL,
    open_opportunity_weight DECIMAL(5,2) NOT NULL,
    industry_weight DECIMAL(5,2) NOT NULL DEFAULT 30,
    company_size_weight DECIMAL(5,2) NOT NULL DEFAULT 25,
    source_weight DECIMAL(5,2) NOT NULL DEFAULT 20,
    interest_weight DECIMAL(5,2) NOT NULL DEFAULT 25,
    hot_threshold DECIMAL(5,2) NOT NULL DEFAULT 70,
    warm_threshold DECIMAL(5,2) NOT NULL DEFAULT 40,
    updated_by BIGINT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_lead_score_weights CHECK (
        expected_value_weight BETWEEN 0 AND 100
        AND completeness_weight BETWEEN 0 AND 100
        AND open_opportunity_weight BETWEEN 0 AND 100
        AND expected_value_weight + completeness_weight
            + open_opportunity_weight = 100
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO lead_scoring_config
    (id, expected_value_weight, completeness_weight, open_opportunity_weight)
VALUES (1, 50, 30, 20);
