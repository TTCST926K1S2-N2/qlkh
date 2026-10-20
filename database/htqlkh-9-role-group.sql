-- =========================================================
-- HTQLKH-9
-- Gan vai tro va nhom kinh doanh
-- =========================================================

-- ---------------------------------------------------------
-- 1. Danh muc vai tro
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

INSERT INTO roles (code, name)
VALUES
    ('ADMIN', 'Quản trị hệ thống'),
    ('MANAGER', 'Quản lý kinh doanh'),
    ('SALES', 'Nhân viên kinh doanh')
ON DUPLICATE KEY UPDATE
    name = VALUES(name);


-- ---------------------------------------------------------
-- 2. Một người dùng có thể có nhiều vai trò
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_code VARCHAR(20) NOT NULL,

    PRIMARY KEY (user_id, role_code),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_code)
        REFERENCES roles(code)
        ON DELETE RESTRICT
);


-- ---------------------------------------------------------
-- 3. Chuyển role hiện tại trong users sang user_roles
--    để không làm mất quyền của tài khoản cũ
-- ---------------------------------------------------------
INSERT IGNORE INTO user_roles (user_id, role_code)
SELECT id, UPPER(TRIM(role))
FROM users
WHERE role IS NOT NULL
  AND TRIM(role) <> '';


-- ---------------------------------------------------------
-- 4. Nhóm kinh doanh
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS business_groups (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- ---------------------------------------------------------
-- 5. Mỗi người dùng thuộc tối đa một nhóm kinh doanh
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_business_groups (
    user_id BIGINT PRIMARY KEY,
    group_id BIGINT NOT NULL,

    CONSTRAINT fk_user_business_groups_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_business_groups_group
        FOREIGN KEY (group_id)
        REFERENCES business_groups(id)
        ON DELETE RESTRICT
);