package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.CustomFieldDAO;
import vn.edu.ictu.qlkh.model.CustomField;

import java.sql.SQLException;
import java.util.List;

public class CustomFieldService {

    private static final String CUSTOMER = "CUSTOMER";
    private static final String OPPORTUNITY = "OPPORTUNITY";

    private static final String TEXT = "TEXT";
    private static final String NUMBER = "NUMBER";
    private static final String DATE = "DATE";
    private static final String BOOLEAN = "BOOLEAN";
    private static final String SELECT = "SELECT";

    private final CustomFieldDAO customFieldDAO;

    public CustomFieldService() {
        this(new CustomFieldDAO());
    }

    public CustomFieldService(CustomFieldDAO customFieldDAO) {
        if (customFieldDAO == null) {
            throw new IllegalArgumentException(
                    "CustomFieldDAO không được null.");
        }

        this.customFieldDAO = customFieldDAO;
    }

    public List<CustomField> getAllFields() throws SQLException {
        return customFieldDAO.findAll();
    }

    public List<CustomField> getFieldsByEntityType(String entityType)
            throws SQLException {

        String normalizedEntityType = normalizeEntityType(entityType);

        return customFieldDAO.findByEntityType(normalizedEntityType);
    }

    public CustomField getFieldById(long id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "ID trường tùy chỉnh không hợp lệ.");
        }

        return customFieldDAO.findById(id);
    }

    public void addField(CustomField field) throws SQLException {
        validateField(field);

        if (customFieldDAO.exists(
                field.getEntityType(),
                field.getFieldKey(),
                0)) {

            throw new IllegalArgumentException(
                    "Trường tùy chỉnh đã tồn tại.");
        }

        boolean inserted = customFieldDAO.insert(field);

        if (!inserted) {
            throw new SQLException(
                    "Không thể thêm trường tùy chỉnh.");
        }
    }

    public void updateField(CustomField field) throws SQLException {
        validateField(field);

        if (field.getId() <= 0) {
            throw new IllegalArgumentException(
                    "ID trường tùy chỉnh không hợp lệ.");
        }

        CustomField existing =
                customFieldDAO.findById(field.getId());

        if (existing == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy trường tùy chỉnh.");
        }

        if (customFieldDAO.exists(
                field.getEntityType(),
                field.getFieldKey(),
                field.getId())) {

            throw new IllegalArgumentException(
                    "Trường tùy chỉnh đã tồn tại.");
        }

        boolean updated = customFieldDAO.update(field);

        if (!updated) {
            throw new SQLException(
                    "Không thể cập nhật trường tùy chỉnh.");
        }
    }

    public void deleteField(long id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "ID trường tùy chỉnh không hợp lệ.");
        }

        CustomField existing =
                customFieldDAO.findById(id);

        if (existing == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy trường tùy chỉnh.");
        }

        boolean deleted = customFieldDAO.delete(id);

        if (!deleted) {
            throw new SQLException(
                    "Không thể xóa trường tùy chỉnh.");
        }
    }

    private void validateField(CustomField field) {

        if (field == null) {
            throw new IllegalArgumentException(
                    "Dữ liệu trường tùy chỉnh không được null.");
        }

        field.setEntityType(
                normalizeEntityType(field.getEntityType()));

        if (field.getFieldKey() == null
                || field.getFieldKey().isBlank()) {

            throw new IllegalArgumentException(
                    "Mã trường không được để trống.");
        }

        if (field.getFieldName() == null
                || field.getFieldName().isBlank()) {

            throw new IllegalArgumentException(
                    "Tên trường không được để trống.");
        }

        if (field.getFieldType() == null
                || field.getFieldType().isBlank()) {

            throw new IllegalArgumentException(
                    "Kiểu dữ liệu không được để trống.");
        }

        field.setFieldKey(field.getFieldKey().trim());
        field.setFieldName(field.getFieldName().trim());
        field.setFieldType(
                field.getFieldType().trim().toUpperCase());

        if (field.getFieldKey().length() > 100) {
            throw new IllegalArgumentException(
                    "Mã trường không được vượt quá 100 ký tự.");
        }

        if (!field.getFieldKey().matches(
                "[a-zA-Z][a-zA-Z0-9_]*")) {

            throw new IllegalArgumentException(
                    "Mã trường chỉ được chứa chữ, số và dấu gạch dưới; "
                    + "ký tự đầu tiên phải là chữ.");
        }

        if (field.getFieldName().length() > 255) {
            throw new IllegalArgumentException(
                    "Tên trường không được vượt quá 255 ký tự.");
        }

        if (!isSupportedFieldType(field.getFieldType())) {
            throw new IllegalArgumentException(
                    "Kiểu dữ liệu không được hỗ trợ.");
        }

        if (field.getDisplayOrder() < 0) {
            throw new IllegalArgumentException(
                    "Thứ tự hiển thị không được âm.");
        }

        if (field.getConfig() != null
                && field.getConfig().length() > 1000) {

            throw new IllegalArgumentException(
                    "Cấu hình trường không được vượt quá 1000 ký tự.");
        }

        if (SELECT.equals(field.getFieldType())
                && (field.getConfig() == null
                || field.getConfig().isBlank())) {

            throw new IllegalArgumentException(
                    "Trường SELECT phải có cấu hình lựa chọn.");
        }

        if (field.getConfig() != null) {
            field.setConfig(field.getConfig().trim());
        }
    }

    private String normalizeEntityType(String entityType) {

        if (entityType == null || entityType.isBlank()) {
            throw new IllegalArgumentException(
                    "Đối tượng áp dụng không được để trống.");
        }

        String normalized =
                entityType.trim().toUpperCase();

        if (!CUSTOMER.equals(normalized)
                && !OPPORTUNITY.equals(normalized)) {

            throw new IllegalArgumentException(
                    "Đối tượng áp dụng chỉ được là CUSTOMER hoặc OPPORTUNITY.");
        }

        return normalized;
    }

    private boolean isSupportedFieldType(String fieldType) {

        return TEXT.equals(fieldType)
                || NUMBER.equals(fieldType)
                || DATE.equals(fieldType)
                || BOOLEAN.equals(fieldType)
                || SELECT.equals(fieldType);
    }
}
