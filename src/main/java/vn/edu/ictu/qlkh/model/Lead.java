package vn.edu.ictu.qlkh.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model đại diện cho thực thể Lead trong bảng leads (Sprint 4).
 */
public class Lead {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String company;
    private String interest;
    private String status;
    private String source;
    private String sourceForm;
    private Long campaignId;
    private Long assignedTo;
    private BigDecimal score;
    private Long convertedCustomerId;
    private Long convertedContactId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Lead() {
    }

    public Lead(Long id, String fullName, String email, String phone, String company, String status) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.company = company;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceForm() {
        return sourceForm;
    }

    public void setSourceForm(String sourceForm) {
        this.sourceForm = sourceForm;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public Long getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Long assignedTo) {
        this.assignedTo = assignedTo;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public Long getConvertedCustomerId() {
        return convertedCustomerId;
    }

    public void setConvertedCustomerId(Long convertedCustomerId) {
        this.convertedCustomerId = convertedCustomerId;
    }

    public Long getConvertedContactId() {
        return convertedContactId;
    }

    public void setConvertedContactId(Long convertedContactId) {
        this.convertedContactId = convertedContactId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
