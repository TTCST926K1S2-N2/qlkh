CREATE TABLE IF NOT EXISTS common_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_type VARCHAR(50) NOT NULL,
    category_code VARCHAR(50) NOT NULL,
    category_name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    status BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_common_categories_type_code
        UNIQUE (category_type, category_code)
);

INSERT INTO common_categories
    (category_type, category_code, category_name, description, status)
VALUES
    ('CUSTOMER_TYPE', 'PERSON', 'Cá nhân', 'Khách hàng cá nhân', TRUE),
    ('CUSTOMER_TYPE', 'COMPANY', 'Doanh nghiệp', 'Khách hàng doanh nghiệp', TRUE),
    ('CUSTOMER_STATUS', 'ACTIVE', 'Đang hoạt động', 'Khách hàng đang hoạt động', TRUE),
    ('CUSTOMER_STATUS', 'INACTIVE', 'Ngừng hoạt động', 'Khách hàng ngừng hoạt động', TRUE)
ON DUPLICATE KEY UPDATE
    category_name = VALUES(category_name),
    description = VALUES(description),
    status = VALUES(status);