package vn.edu.ictu.qlkh.model;

import java.sql.Timestamp;

public class SupportRequest {
    private Long id;
    private Long customerId;
    private String description;
    private String priority;
    private String status;
    private Timestamp createdAt;

    public SupportRequest() {
    }

    public SupportRequest(Long id, Long customerId, String description, String priority, String status, Timestamp createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}