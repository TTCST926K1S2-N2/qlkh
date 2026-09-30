CREATE TABLE IF NOT EXISTS account_handover_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_user_id BIGINT NOT NULL,
    target_user_id BIGINT NULL,
    performed_by BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL,
    previous_status VARCHAR(20) NOT NULL,
    new_status VARCHAR(20) NOT NULL,
    note VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_handover_source_user
        FOREIGN KEY (source_user_id) REFERENCES users(id),

    CONSTRAINT fk_handover_target_user
        FOREIGN KEY (target_user_id) REFERENCES users(id),

    CONSTRAINT fk_handover_performed_by
        FOREIGN KEY (performed_by) REFERENCES users(id)
);

CREATE INDEX idx_handover_source_user
    ON account_handover_logs(source_user_id);

CREATE INDEX idx_handover_target_user
    ON account_handover_logs(target_user_id);

CREATE INDEX idx_handover_created_at
    ON account_handover_logs(created_at);
