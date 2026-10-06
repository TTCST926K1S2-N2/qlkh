-- S3-01: Customer management
-- Run against the application database after review.

CREATE TABLE IF NOT EXISTS customers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    company_name VARCHAR(255) NOT NULL,
    tax_code VARCHAR(50) NULL,
    industry VARCHAR(150) NULL,
    company_size VARCHAR(100) NULL,
    website VARCHAR(500) NULL,
    address VARCHAR(500) NULL,
    owner_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'POTENTIAL',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_customers_tax_code (tax_code),
    KEY idx_customers_owner (owner_id),
    KEY idx_customers_status (status),

    CONSTRAINT fk_customers_owner
        FOREIGN KEY (owner_id)
        REFERENCES users(id),

    CONSTRAINT chk_customers_status
        CHECK (
            status IN (
                'POTENTIAL',
                'IN_PROGRESS',
                'CUSTOMER',
                'INACTIVE'
            )
        ),

    CONSTRAINT chk_customers_company_name
        CHECK (CHAR_LENGTH(TRIM(company_name)) > 0),

    CONSTRAINT chk_customers_tax_code
        CHECK (
            tax_code IS NULL
            OR CHAR_LENGTH(TRIM(tax_code)) > 0
        )
);