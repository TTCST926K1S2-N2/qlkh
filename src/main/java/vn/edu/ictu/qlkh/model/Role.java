package vn.edu.ictu.qlkh.model;

/**
 * Thông tin vai trò phục vụ điều hướng theo quyền - HTQLKH-6.
 */
public class Role {

    private String code;
    private String name;

    public Role() {
    }

    public Role(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}