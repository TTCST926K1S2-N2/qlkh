-- S4-03: Campaign management and tracking
-- MySQL 8.x

CREATE TABLE IF NOT EXISTS campaigns (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    budget DECIMAL(18,2) NOT NULL DEFAULT 0,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    channel VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    KEY idx_campaigns_dates (start_date, end_date),

    CONSTRAINT chk_campaigns_budget
        CHECK (budget >= 0),

    CONSTRAINT chk_campaigns_dates
        CHECK (end_date >= start_date),

    CONSTRAINT chk_campaigns_name
        CHECK (CHAR_LENGTH(TRIM(name)) > 0)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- Link leads to campaigns.
-- leads.campaign_id already exists in S4-01.
ALTER TABLE leads
    ADD CONSTRAINT fk_leads_campaign
    FOREIGN KEY (campaign_id)
    REFERENCES campaigns(id)
    ON DELETE SET NULL;

-- Link opportunities to campaigns.
ALTER TABLE opportunities
    ADD COLUMN campaign_id BIGINT NULL,
    ADD KEY idx_opportunities_campaign (campaign_id),
    ADD CONSTRAINT fk_opportunities_campaign
        FOREIGN KEY (campaign_id)
        REFERENCES campaigns(id)
        ON DELETE SET NULL;