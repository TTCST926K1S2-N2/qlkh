CREATE TABLE IF NOT EXISTS custom_fields (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity_type VARCHAR(30) NOT NULL,
    field_key VARCHAR(100) NOT NULL,
    field_name VARCHAR(255) NOT NULL,
    field_type VARCHAR(30) NOT NULL,
    required BOOLEAN NOT NULL DEFAULT FALSE,
    status BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT NOT NULL DEFAULT 0,
    config VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_custom_fields_entity_key
        UNIQUE (entity_type, field_key)
);

INSERT INTO custom_fields
    (entity_type, field_key, field_name, field_type,
     required, status, display_order, config)
VALUES
    ('CUSTOMER', 'customer_source', 'Nguồn khách hàng',
     'SELECT', FALSE, TRUE, 1,
     'Website,Facebook,Zalo,Gioi thieu'),

    ('CUSTOMER', 'customer_note', 'Ghi chú khách hàng',
     'TEXT', FALSE, TRUE, 2, NULL),

    ('OPPORTUNITY', 'expected_value', 'Giá trị dự kiến',
     'NUMBER', FALSE, TRUE, 1, NULL)
ON DUPLICATE KEY UPDATE
    field_name = VALUES(field_name),
    field_type = VALUES(field_type),
    required = VALUES(required),
    status = VALUES(status),
    display_order = VALUES(display_order),
    config = VALUES(config);
