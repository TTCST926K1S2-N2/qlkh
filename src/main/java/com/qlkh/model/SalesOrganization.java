package com.qlkh.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class SalesOrganization implements Serializable {
    private String orgCode;
    private String orgName;
    private String parentOrgCode;
    private String description;
    private String status; // ACTIVE, INACTIVE
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public SalesOrganization() {}

    public SalesOrganization(String orgCode, String orgName, String parentOrgCode, String description, String status) {
        this.orgCode = orgCode;
        this.orgName = orgName;
        this.parentOrgCode = parentOrgCode;
        this.description = description;
        this.status = status;
    }

    public String getOrgCode() { return orgCode; }
    public void setOrgCode(String orgCode) { this.orgCode = orgCode; }

    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }

    public String getParentOrgCode() { return parentOrgCode; }
    public void setParentOrgCode(String parentOrgCode) { this.parentOrgCode = parentOrgCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
