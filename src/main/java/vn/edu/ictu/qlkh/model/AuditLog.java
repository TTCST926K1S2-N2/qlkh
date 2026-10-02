package vn.edu.ictu.qlkh.model;

import java.sql.Timestamp;

public class AuditLog {

    private long id;
    private String username;
    private String actionType;
    private String targetObject;
    private String oldValue;
    private String newValue;
    private String details;
    private Timestamp actionTime;

    public AuditLog() {
    }

    public AuditLog(long id, String username, String actionType,
                    String targetObject, String oldValue, String newValue,
                    String details, Timestamp actionTime) {
        this.id = id;
        this.username = username;
        this.actionType = actionType;
        this.targetObject = targetObject;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.details = details;
        this.actionTime = actionTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getTargetObject() {
        return targetObject;
    }

    public void setTargetObject(String targetObject) {
        this.targetObject = targetObject;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Timestamp getActionTime() {
        return actionTime;
    }

    public void setActionTime(Timestamp actionTime) {
        this.actionTime = actionTime;
    }
}