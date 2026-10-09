package vn.edu.ictu.qlkh.model;

/**
 * Thông tin nhóm kinh doanh.
 *
 * HTQLKH-6 chỉ sử dụng model này để cung cấp thông tin
 * cho giao diện điều hướng.
 *
 * Việc gán/lưu nhóm kinh doanh thuộc chức năng HTQLKH-9.
 */
public class BusinessGroup {

    private Long id;
    private String name;

    public BusinessGroup() {
    }

    public BusinessGroup(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    private Long parentId;

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}