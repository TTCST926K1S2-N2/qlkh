-- S4-04: Lead duplicate merge history
-- Review before executing against MySQL.

CREATE TABLE IF NOT EXISTS lead_merges (
    id BIGINT NOT NULL AUTO_INCREMENT,
    source_lead_id BIGINT NOT NULL,
    target_lead_id BIGINT NOT NULL,
    merged_by BIGINT NOT NULL,
    merged_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    UNIQUE KEY uk_lead_merges_source (source_lead_id),
    KEY idx_lead_merges_target (target_lead_id),
    KEY idx_lead_merges_actor (merged_by),

    CONSTRAINT fk_lead_merges_source
        FOREIGN KEY (source_lead_id) REFERENCES leads(id),

    CONSTRAINT fk_lead_merges_target
        FOREIGN KEY (target_lead_id) REFERENCES leads(id),

    CONSTRAINT fk_lead_merges_actor
        FOREIGN KEY (merged_by) REFERENCES users(id),

    CONSTRAINT chk_lead_merges_distinct
        CHECK (source_lead_id <> target_lead_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;