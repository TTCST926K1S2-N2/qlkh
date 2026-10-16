-- S4-07: Lead acceptance, rejection and response SLA.
-- Run after database/s4-01-leads.sql.
-- Apply only after the team agrees on the SLA policy.

ALTER TABLE leads
    ADD COLUMN assigned_at DATETIME NULL,
    ADD COLUMN sla_deadline DATETIME NULL,
    ADD COLUMN accepted_at DATETIME NULL,
    ADD COLUMN rejected_at DATETIME NULL,
    ADD COLUMN rejection_reason TEXT NULL,
    ADD COLUMN first_contact_at DATETIME NULL,
    ADD COLUMN sla_notified_at DATETIME NULL;

CREATE INDEX idx_leads_sla_deadline
    ON leads (sla_deadline);