-- S4-06: Lead Assignment Rules
-- Run this script against the intended CRM database.
-- The users table must already exist.

CREATE TABLE IF NOT EXISTS lead_assignment_rules (
    id BIGINT NOT NULL AUTO_INCREMENT,
    rule_name VARCHAR(255) NOT NULL,
    condition_type VARCHAR(30) NOT NULL,
    condition_value VARCHAR(255) NOT NULL,
    assign_to_user_id BIGINT NOT NULL,
    priority INT NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    assignment_method VARCHAR(30) NOT NULL DEFAULT 'DIRECT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    KEY idx_assignment_rules_priority (priority),
    KEY idx_assignment_rules_active (is_active),

    CONSTRAINT fk_assignment_rules_user
        FOREIGN KEY (assign_to_user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_assignment_rules_condition
        CHECK (condition_type IN ('SOURCE', 'INDUSTRY', 'REGION')),

    CONSTRAINT chk_assignment_rules_priority
        CHECK (priority >= 1),

    CONSTRAINT chk_assignment_rules_method
        CHECK (assignment_method IN ('DIRECT', 'ROUND_ROBIN'))
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;