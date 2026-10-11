-- S3-05: Quan he cong ty me - cong ty con
-- MySQL 8.x
-- Chay sau migration S3-01 (customers).
-- Chi chay mot lan tren database da duoc sao luu.

ALTER TABLE customers
    ADD COLUMN parent_id BIGINT NULL;

CREATE INDEX idx_customers_parent_id
    ON customers(parent_id);

ALTER TABLE customers
    ADD CONSTRAINT fk_customers_parent
    FOREIGN KEY (parent_id)
    REFERENCES customers(id)
    ON DELETE SET NULL;

-- Ngan cong ty tu chon chinh minh lam cong ty me.
ALTER TABLE customers
    ADD CONSTRAINT chk_customers_not_self_parent
    CHECK (parent_id IS NULL OR parent_id <> id);

-- Kiem tra cau truc sau khi migration.
SHOW CREATE TABLE customers;