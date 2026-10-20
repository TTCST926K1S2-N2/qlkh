package vn.edu.ictu.qlkh.model;

public class CustomField {

    private long id;
    private String entityType;
    private String fieldKey;
    private String fieldName;
    private String fieldType;
    private boolean required;
    private boolean status;
    private int displayOrder;
    private String config;

    public CustomField() {
    }

    public CustomField(
            long id,
            String entityType,
            String fieldKey,
            String fieldName,
            String fieldType,
            boolean required,
            boolean status,
            int displayOrder,
            String config) {
        this.id = id;
        this.entityType = entityType;
        this.fieldKey = fieldKey;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.required = required;
        this.status = status;
        this.displayOrder = displayOrder;
        this.config = config;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public void setFieldKey(String fieldKey) {
        this.fieldKey = fieldKey;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }
}
