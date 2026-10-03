package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.CategoryDAO;
import vn.edu.ictu.qlkh.model.Category;

import java.sql.SQLException;
import java.util.List;

public class CategoryService {

    private final CategoryDAO categoryDAO;

    public CategoryService() {
        this(new CategoryDAO());
    }

    public CategoryService(CategoryDAO categoryDAO) {
        if (categoryDAO == null) {
            throw new IllegalArgumentException("CategoryDAO không được null.");
        }

        this.categoryDAO = categoryDAO;
    }

    public List<Category> getAllCategories() throws SQLException {
        return categoryDAO.findAll();
    }

    public Category getCategoryById(long id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException("ID danh mục không hợp lệ.");
        }

        return categoryDAO.findById(id);
    }

    public void addCategory(Category category)
            throws SQLException {

        validateCategory(category);

        if (categoryDAO.exists(
                category.getCategoryType(),
                category.getCategoryCode(),
                0)) {

            throw new IllegalArgumentException(
                    "Danh mục đã tồn tại."
            );
        }

        boolean inserted = categoryDAO.insert(category);

        if (!inserted) {
            throw new SQLException(
                    "Không thể thêm danh mục."
            );
        }
    }

    public void updateCategory(Category category)
            throws SQLException {

        validateCategory(category);

        if (category.getId() <= 0) {
            throw new IllegalArgumentException(
                    "ID danh mục không hợp lệ."
            );
        }

        Category existing =
                categoryDAO.findById(category.getId());

        if (existing == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy danh mục."
            );
        }

        if (categoryDAO.exists(
                category.getCategoryType(),
                category.getCategoryCode(),
                category.getId())) {

            throw new IllegalArgumentException(
                    "Danh mục đã tồn tại."
            );
        }

        boolean updated = categoryDAO.update(category);

        if (!updated) {
            throw new SQLException(
                    "Không thể cập nhật danh mục."
            );
        }
    }

    public void deleteCategory(long id)
            throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "ID danh mục không hợp lệ."
            );
        }

        Category existing =
                categoryDAO.findById(id);

        if (existing == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy danh mục."
            );
        }

        boolean deleted = categoryDAO.delete(id);

        if (!deleted) {
            throw new SQLException(
                    "Không thể xóa danh mục."
            );
        }
    }

    private void validateCategory(Category category) {

        if (category == null) {
            throw new IllegalArgumentException(
                    "Dữ liệu danh mục không được null."
            );
        }

        String categoryType =
                category.getCategoryType();

        String categoryCode =
                category.getCategoryCode();

        String categoryName =
                category.getCategoryName();

        if (categoryType == null
                || categoryType.isBlank()) {

            throw new IllegalArgumentException(
                    "Loại danh mục không được để trống."
            );
        }

        if (categoryCode == null
                || categoryCode.isBlank()) {

            throw new IllegalArgumentException(
                    "Mã danh mục không được để trống."
            );
        }

        if (categoryName == null
                || categoryName.isBlank()) {

            throw new IllegalArgumentException(
                    "Tên danh mục không được để trống."
            );
        }

        if (categoryType.length() > 50) {
            throw new IllegalArgumentException(
                    "Loại danh mục không được vượt quá 50 ký tự."
            );
        }

        if (categoryCode.length() > 50) {
            throw new IllegalArgumentException(
                    "Mã danh mục không được vượt quá 50 ký tự."
            );
        }

        if (categoryName.length() > 255) {
            throw new IllegalArgumentException(
                    "Tên danh mục không được vượt quá 255 ký tự."
            );
        }

        if (category.getDescription() != null
                && category.getDescription().length() > 500) {

            throw new IllegalArgumentException(
                    "Mô tả không được vượt quá 500 ký tự."
            );
        }

        category.setCategoryType(categoryType.trim());
        category.setCategoryCode(categoryCode.trim());
        category.setCategoryName(categoryName.trim());

        if (category.getDescription() != null) {
            category.setDescription(
                    category.getDescription().trim()
            );
        }
    }
}