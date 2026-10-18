-- S3-02: Contact management
-- Review before applying to the application database.

CREATE TABLE IF NOT EXISTS contacts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    job_title VARCHAR(150) NULL,
    email VARCHAR(255) NULL,
    phone VARCHAR(30) NULL,
    decision_role VARCHAR(30) NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    KEY idx_contacts_customer (customer_id),
    KEY idx_contacts_email (email),

    CONSTRAINT fk_contacts_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id),

    CONSTRAINT chk_contacts_name
        CHECK (CHAR_LENGTH(TRIM(full_name)) > 0),

    CONSTRAINT chk_contacts_decision_role
        CHECK (
            decision_role IS NULL
            OR decision_role IN (
                'DECISION_MAKER',
                'INFLUENCER',
                'END_USER',
                'BLOCKER'
            )
        )
);


-- History of contact transfers between customers.
-- Retain history independently from the contact record.
CREATE TABLE IF NOT EXISTS contact_customer_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    contact_id BIGINT NOT NULL,
    old_customer_id BIGINT NOT NULL,
    new_customer_id BIGINT NOT NULL,
    transferred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    KEY idx_contact_history_contact (contact_id),
    KEY idx_contact_history_old_customer (old_customer_id),
    KEY idx_contact_history_new_customer (new_customer_id),

    CONSTRAINT chk_contact_history_customers
        CHECK (old_customer_id <> new_customer_id)
);
