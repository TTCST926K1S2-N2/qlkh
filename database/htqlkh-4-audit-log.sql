CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    target_object VARCHAR(100) NOT NULL,
    old_value TEXT NULL,
    new_value TEXT NULL,
    details TEXT NULL,
    action_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_audit_username (username),
    INDEX idx_audit_action_type (action_type),
    INDEX idx_audit_target_object (target_object),
    INDEX idx_audit_action_time (action_time)
);