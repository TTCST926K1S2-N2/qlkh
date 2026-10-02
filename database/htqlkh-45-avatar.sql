-- HTQLKH-45 / S2-03: Avatar upload
-- Them duong dan avatar cho nguoi dung.

ALTER TABLE users
    ADD COLUMN avatar VARCHAR(500) NULL;
