-- S4-02: Extend shared Lead schema for Excel import
--
-- S4-01 already creates the shared `leads` table.
-- S4-02 adds the business fields required by the Lead Excel template.
-- Lead imported from Excel will use:
--   status = NEW
--   source = EXCEL

ALTER TABLE leads
    ADD COLUMN job_title VARCHAR(150) DEFAULT NULL AFTER company,
    ADD COLUMN tax_code VARCHAR(50) DEFAULT NULL AFTER job_title,
    ADD COLUMN industry VARCHAR(150) DEFAULT NULL AFTER tax_code,
    ADD COLUMN company_size VARCHAR(100) DEFAULT NULL AFTER industry,
    ADD COLUMN website VARCHAR(500) DEFAULT NULL AFTER company_size,
    ADD COLUMN address VARCHAR(500) DEFAULT NULL AFTER website;