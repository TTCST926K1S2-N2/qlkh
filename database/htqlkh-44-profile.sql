-- HTQLKH-44 / S2-02
-- Bo sung thong tin ho so ca nhan cho nguoi dung.
-- Email, vai tro va nhom khong duoc cap nhat boi API ho so ca nhan.

ALTER TABLE users
    ADD COLUMN phone VARCHAR(20) NULL AFTER full_name,
    ADD COLUMN email_signature TEXT NULL AFTER phone;
