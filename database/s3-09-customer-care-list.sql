-- S3-09: Customer periodic care tracking
-- Keep care information separate from customer profile updates.

CREATE TABLE IF NOT EXISTS customer_care_tracking (
    customer_id BIGINT NOT NULL,
    contract_value DECIMAL(18,2) NOT NULL DEFAULT 0,
    last_contact_at DATETIME NULL,
    updated_at TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (customer_id),

    CONSTRAINT fk_care_tracking_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id),

    CONSTRAINT chk_care_contract_value
        CHECK (contract_value >= 0)
);

CREATE TABLE IF NOT EXISTS customer_care_contact_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    contacted_by BIGINT NOT NULL,
    contacted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    KEY idx_care_history_customer_date (
        customer_id,
        contacted_at
    ),

    CONSTRAINT fk_care_history_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id),

    CONSTRAINT fk_care_history_user
        FOREIGN KEY (contacted_by)
        REFERENCES users(id)
);