-- S3-03: Customer 360 - Opportunities and Attachments
-- MySQL 8.x

CREATE TABLE IF NOT EXISTS opportunities (
    id BIGINT NOT NULL AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    stage_id BIGINT NULL,
    name VARCHAR(255) NOT NULL,
    expected_value DECIMAL(18,2) NULL,
    expected_close_date DATE NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    KEY idx_opportunities_customer (customer_id),
    KEY idx_opportunities_stage (stage_id),

    CONSTRAINT fk_opportunities_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id),

    CONSTRAINT fk_opportunities_stage
        FOREIGN KEY (stage_id)
        REFERENCES stages(id),

    CONSTRAINT chk_opportunities_value
        CHECK (expected_value IS NULL OR expected_value >= 0)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS attachments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    storage_path VARCHAR(1000) NOT NULL,
    content_type VARCHAR(150) NULL,
    file_size BIGINT NULL,
    uploaded_by BIGINT NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    KEY idx_attachments_customer (customer_id),
    KEY idx_attachments_uploader (uploaded_by),

    CONSTRAINT fk_attachments_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id),

    CONSTRAINT fk_attachments_uploader
        FOREIGN KEY (uploaded_by)
        REFERENCES users(id),

    CONSTRAINT chk_attachments_size
        CHECK (file_size IS NULL OR file_size >= 0)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;