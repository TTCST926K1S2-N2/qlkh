package vn.edu.ictu.qlkh.dto;

import java.util.ArrayList;
import java.util.List;

public class SalesOrgDTO {

    private Long id;
    private String orgCode;
    private String orgName;

    private Long parentId;

    private Long leaderId;
    private String leaderName;

    private Long regionId;
    private String regionName;

    private String status;

    private List<SalesOrgDTO> children =
            new ArrayList<>();

    public SalesOrgDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(Long leaderId) {
        this.leaderId = leaderId;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

    public Long getRegionId() {
        return regionId;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<SalesOrgDTO> getChildren() {
        return children;
    }

    public void setChildren(
            List<SalesOrgDTO> children
    ) {
        this.children = children;
    }
}