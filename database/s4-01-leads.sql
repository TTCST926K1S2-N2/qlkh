-- S4-01: Shared Lead schema for Sprint 4
CREATE TABLE IF NOT EXISTS leads (
    id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) DEFAULT NULL,
    phone VARCHAR(30) DEFAULT NULL,
    company VARCHAR(255) DEFAULT NULL,
    interest TEXT DEFAULT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'NEW',
    source VARCHAR(100) NOT NULL DEFAULT 'WEBSITE',
    source_form VARCHAR(100) DEFAULT NULL,
    campaign_id BIGINT DEFAULT NULL,

    assigned_to BIGINT DEFAULT NULL,
    score DECIMAL(5,2) DEFAULT NULL,

    converted_customer_id BIGINT DEFAULT NULL,
    converted_contact_id BIGINT DEFAULT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    KEY idx_leads_status (status),
    KEY idx_leads_email (email),
    KEY idx_leads_phone (phone),
    KEY idx_leads_source (source),
    KEY idx_leads_assigned (assigned_to),

    CONSTRAINT fk_leads_assigned
        FOREIGN KEY (assigned_to) REFERENCES users(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_leads_customer
        FOREIGN KEY (converted_customer_id) REFERENCES customers(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_leads_contact
        FOREIGN KEY (converted_contact_id) REFERENCES contacts(id)
        ON DELETE SET NULL,

    CONSTRAINT chk_leads_name
        CHECK (CHAR_LENGTH(TRIM(full_name)) > 0)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
