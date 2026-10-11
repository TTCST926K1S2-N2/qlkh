-- S3-08: Customer care and after-sales support
-- MySQL 8.x

CREATE TABLE IF NOT EXISTS customer_support_requests (
    id BIGINT NOT NULL AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    assigned_to BIGINT NULL,
    created_by BIGINT NOT NULL,
    resolution_note TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,

    PRIMARY KEY (id),

    KEY idx_support_customer (customer_id),
    KEY idx_support_status (status),
    KEY idx_support_assignee (assigned_to),
    KEY idx_support_priority (priority),

    CONSTRAINT fk_support_customer
        FOREIGN KEY (customer_id) REFERENCES customers(id),

    CONSTRAINT fk_support_assignee
        FOREIGN KEY (assigned_to) REFERENCES users(id),

    CONSTRAINT fk_support_creator
        FOREIGN KEY (created_by) REFERENCES users(id),

    CONSTRAINT chk_support_priority
        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),

    CONSTRAINT chk_support_status
        CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'))

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;