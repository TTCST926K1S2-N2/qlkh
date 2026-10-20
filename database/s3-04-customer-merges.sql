-- S3-04: Customer duplicate merge history
-- MySQL 8.x
-- Review before executing against the database.

CREATE TABLE IF NOT EXISTS customer_merges (
    id BIGINT NOT NULL AUTO_INCREMENT,
    source_customer_id BIGINT NOT NULL,
    target_customer_id BIGINT NOT NULL,
    merged_by BIGINT NOT NULL,
    merged_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    UNIQUE KEY uk_customer_merges_source (source_customer_id),

    KEY idx_customer_merges_target (target_customer_id),
    KEY idx_customer_merges_actor (merged_by),

    CONSTRAINT fk_customer_merges_source
        FOREIGN KEY (source_customer_id)
        REFERENCES customers(id),

    CONSTRAINT fk_customer_merges_target
        FOREIGN KEY (target_customer_id)
        REFERENCES customers(id),

    CONSTRAINT fk_customer_merges_actor
        FOREIGN KEY (merged_by)
        REFERENCES users(id),

    CONSTRAINT chk_customer_merges_distinct
        CHECK (source_customer_id <> target_customer_id)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;