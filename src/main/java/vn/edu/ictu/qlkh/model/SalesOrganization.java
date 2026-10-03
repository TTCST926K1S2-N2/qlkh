package com.qlkh.model;

import java.sql.Timestamp;

public class SalesOrganization {
    private Integer id;
    private String orgCode;
    private String orgName;
    private Integer parentId;
    private String status;
    private Timestamp createdAt;

    public SalesOrganization() {}

    public SalesOrganization(Integer id, String orgCode, String orgName, Integer parentId, String status, Timestamp createdAt) {
        this.id = id;
        this.orgCode = orgCode;
        this.orgName = orgName;
        this.parentId = parentId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getOrgCode() { return orgCode; }
    public void setOrgCode(String orgCode) { this.orgCode = orgCode; }
    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }
    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
