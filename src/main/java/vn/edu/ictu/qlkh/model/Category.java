package vn.edu.ictu.qlkh.model;

public class Category {

    private long id;
    private String categoryType;
    private String categoryCode;
    private String categoryName;
    private String description;
    private boolean status;

    public Category() {
    }

    public Category(
            long id,
            String categoryType,
            String categoryCode,
            String categoryName,
            String description,
            boolean status) {

        this.id = id;
        this.categoryType = categoryType;
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
        this.description = description;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}