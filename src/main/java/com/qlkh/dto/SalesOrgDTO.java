package com.qlkh.dto;

import java.util.ArrayList;
import java.util.List;

public class SalesOrgDTO {
    private Integer id;
    private String orgCode;
    private String orgName;
    private Integer parentId;
    private String status;
    private List<SalesOrgDTO> children = new ArrayList<>();

    public SalesOrgDTO() {}

    public SalesOrgDTO(Integer id, String orgCode, String orgName, Integer parentId, String status) {
        this.id = id;
        this.orgCode = orgCode;
        this.orgName = orgName;
        this.parentId = parentId;
        this.status = status;
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
    public List<SalesOrgDTO> getChildren() { return children; }
    public void setChildren(List<SalesOrgDTO> children) { this.children = children; }
}
