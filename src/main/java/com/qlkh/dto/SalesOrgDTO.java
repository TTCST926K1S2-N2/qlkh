package com.qlkh.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SalesOrgDTO implements Serializable {
    private String orgCode;
    private String orgName;
    private String parentOrgCode;
    private String description;
    private String status;
    private List<SalesOrgDTO> children = new ArrayList<>();

    public SalesOrgDTO() {}

    public SalesOrgDTO(String orgCode, String orgName, String parentOrgCode, String description, String status) {
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

    public List<SalesOrgDTO> getChildren() { return children; }
    public void setChildren(List<SalesOrgDTO> children) { this.children = children; }

    public void addChild(SalesOrgDTO child) { this.children.add(child); }
}
