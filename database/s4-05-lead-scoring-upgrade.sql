-- S4-05: Upgrade existing Lead Scoring configuration.
-- Run only after checking the target database.
-- Intended for tables that still have the legacy columns.
-- Do not rerun after the upgrade has been applied.

ALTER TABLE lead_scoring_config
    ADD COLUMN industry_weight DECIMAL(5,2) NOT NULL DEFAULT 30,
    ADD COLUMN company_size_weight DECIMAL(5,2) NOT NULL DEFAULT 25,
    ADD COLUMN source_weight DECIMAL(5,2) NOT NULL DEFAULT 20,
    ADD COLUMN interest_weight DECIMAL(5,2) NOT NULL DEFAULT 25,
    ADD COLUMN hot_threshold DECIMAL(5,2) NOT NULL DEFAULT 70,
    ADD COLUMN warm_threshold DECIMAL(5,2) NOT NULL DEFAULT 40,
    ADD COLUMN target_industries JSON NULL;