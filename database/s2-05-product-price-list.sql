-- =========================================================
-- S2-05 - Quan ly san pham, dich vu va bang gia niem yet
-- =========================================================

CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,

    product_type VARCHAR(20) NOT NULL,
    unit VARCHAR(50) NOT NULL,

    base_price DECIMAL(18,2) NOT NULL,
    floor_price DECIMAL(18,2) NOT NULL,
    cost_price DECIMAL(18,2) NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    description VARCHAR(1000) NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_products_code
        UNIQUE (code)
);


CREATE TABLE IF NOT EXISTS price_lists (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,

    start_date DATE NOT NULL,
    end_date DATE NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    description VARCHAR(1000) NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_price_lists_code
        UNIQUE (code)
);


CREATE TABLE IF NOT EXISTS price_list_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    price_list_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,

    list_price DECIMAL(18,2) NOT NULL,
    floor_price DECIMAL(18,2) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_price_list_product
        UNIQUE (price_list_id, product_id),

    CONSTRAINT fk_price_list_items_price_list
        FOREIGN KEY (price_list_id)
        REFERENCES price_lists(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_price_list_items_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE
);


CREATE INDEX idx_products_name
    ON products(name);

CREATE INDEX idx_products_type
    ON products(product_type);

CREATE INDEX idx_products_status
    ON products(status);

CREATE INDEX idx_price_lists_status
    ON price_lists(status);
