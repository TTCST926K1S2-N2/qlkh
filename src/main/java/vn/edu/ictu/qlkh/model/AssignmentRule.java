package vn.edu.ictu.qlkh.model;

import java.time.LocalDateTime;

public class AssignmentRule {
    private Long id;
    private String name;
    private String conditionType;
    private String conditionValue;
    private Long assignToUserId;
    private Integer priority;
    private Boolean active;
    private LocalDateTime createdAt;

    public AssignmentRule() {}

    public AssignmentRule(Long id, String name, String conditionType, String conditionValue, Long assignToUserId, Integer priority, Boolean active) {
        this.id = id;
        this.name = name;
        this.conditionType = conditionType;
        this.conditionValue = conditionValue;
        this.assignToUserId = assignToUserId;
        this.priority = priority;
        this.active = active;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getConditionType() { return conditionType; }
    public void setConditionType(String conditionType) { this.conditionType = conditionType; }
    public String getConditionValue() { return conditionValue; }
    public void setConditionValue(String conditionValue) { this.conditionValue = conditionValue; }
    public Long getAssignToUserId() { return assignToUserId; }
    public void setAssignToUserId(Long assignToUserId) { this.assignToUserId = assignToUserId; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}