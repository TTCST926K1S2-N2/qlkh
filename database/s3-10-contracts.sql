-- Apply to MySQL 8 after S3-01. Back up database first.
CREATE TABLE IF NOT EXISTS contracts (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 contract_code VARCHAR(80) NOT NULL UNIQUE,
 customer_id BIGINT NOT NULL,
 contract_name VARCHAR(255) NOT NULL,
 contract_value DECIMAL(18,2) NOT NULL DEFAULT 0,
 status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
 signed_date DATE NULL,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 CONSTRAINT fk_contracts_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
 CONSTRAINT chk_contracts_value CHECK (contract_value >= 0),
 CONSTRAINT chk_contracts_status CHECK (status IN ('DRAFT','PENDING','SIGNED','CANCELLED')),
 CONSTRAINT chk_contracts_signed CHECK (status <> 'SIGNED' OR signed_date IS NOT NULL),
 KEY idx_contracts_customer_status (customer_id,status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
