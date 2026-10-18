-- S4-02: Bo sung thong tin Lead import tu Excel.
-- Yeu cau: bang leads cua S4-01 da ton tai.
-- Chi chay mot lan tren moi database.
-- Khong chay lai tren database da co 6 cot nay.

ALTER TABLE leads
    ADD COLUMN job_title VARCHAR(150) NULL,
    ADD COLUMN tax_code VARCHAR(20) NULL,
    ADD COLUMN industry VARCHAR(150) NULL,
    ADD COLUMN company_size VARCHAR(100) NULL,
    ADD COLUMN website VARCHAR(500) NULL,
    ADD COLUMN address VARCHAR(500) NULL;