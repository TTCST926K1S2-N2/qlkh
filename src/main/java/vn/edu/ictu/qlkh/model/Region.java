package vn.edu.ictu.qlkh.model;

public class Region {

    private Long id;
    private String code;
    private String name;
    private String status;

    public Region() {
    }

    public Region(
            Long id,
            String code,
            String name,
            String status
    ) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}