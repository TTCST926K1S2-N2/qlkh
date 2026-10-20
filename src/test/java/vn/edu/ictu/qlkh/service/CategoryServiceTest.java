package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.dao.CategoryDAO;
import vn.edu.ictu.qlkh.model.Category;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CategoryServiceTest {

    @Test
    void getAllCategories_shouldReturnCategories() throws SQLException {
        CategoryDAO dao = new FakeCategoryDAO();
        CategoryService service = new CategoryService(dao);

        List<Category> categories = service.getAllCategories();

        assertNotNull(categories);
        assertEquals(1, categories.size());
        assertEquals("CUSTOMER_TYPE", categories.get(0).getCategoryType());
    }

    @Test
    void getCategoryById_shouldReturnCategory() throws SQLException {
        CategoryDAO dao = new FakeCategoryDAO();
        CategoryService service = new CategoryService(dao);

        Category category = service.getCategoryById(1);

        assertNotNull(category);
        assertEquals(1, category.getId());
        assertEquals("PERSON", category.getCategoryCode());
    }

    @Test
    void getCategoryById_shouldRejectInvalidId() {
        CategoryService service = new CategoryService(new FakeCategoryDAO());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.getCategoryById(0)
        );

        assertEquals("ID danh mục không hợp lệ.", exception.getMessage());
    }

    @Test
    void addCategory_shouldRejectNullCategory() {
        CategoryService service = new CategoryService(new FakeCategoryDAO());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.addCategory(null)
        );

        assertEquals(
                "Dữ liệu danh mục không được null.",
                exception.getMessage()
        );
    }

    @Test
    void addCategory_shouldRejectEmptyCategoryType() {
        CategoryService service = new CategoryService(new FakeCategoryDAO());

        Category category = createCategory();
        category.setCategoryType("");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.addCategory(category)
        );

        assertEquals(
                "Loại danh mục không được để trống.",
                exception.getMessage()
        );
    }

    @Test
    void addCategory_shouldRejectEmptyCategoryCode() {
        CategoryService service = new CategoryService(new FakeCategoryDAO());

        Category category = createCategory();
        category.setCategoryCode("");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.addCategory(category)
        );

        assertEquals(
                "Mã danh mục không được để trống.",
                exception.getMessage()
        );
    }

    @Test
    void addCategory_shouldRejectEmptyCategoryName() {
        CategoryService service = new CategoryService(new FakeCategoryDAO());

        Category category = createCategory();
        category.setCategoryName("");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.addCategory(category)
        );

        assertEquals(
                "Tên danh mục không được để trống.",
                exception.getMessage()
        );
    }

    @Test
    void addCategory_shouldRejectDuplicateCategory() {
        FakeCategoryDAO dao = new FakeCategoryDAO();
        dao.duplicate = true;

        CategoryService service = new CategoryService(dao);

        Category category = createCategory();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.addCategory(category)
        );

        assertEquals(
                "Danh mục đã tồn tại.",
                exception.getMessage()
        );
    }

    @Test
    void addCategory_shouldInsertValidCategory() throws SQLException {
        FakeCategoryDAO dao = new FakeCategoryDAO();
        CategoryService service = new CategoryService(dao);

        Category category = createCategory();

        service.addCategory(category);

        assertTrue(dao.insertCalled);
    }

    @Test
    void updateCategory_shouldRejectInvalidId() {
        CategoryService service = new CategoryService(new FakeCategoryDAO());

        Category category = createCategory();
        category.setId(0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.updateCategory(category)
        );

        assertEquals(
                "ID danh mục không hợp lệ.",
                exception.getMessage()
        );
    }

    @Test
    void updateCategory_shouldRejectNotFound() {
        FakeCategoryDAO dao = new FakeCategoryDAO();
        dao.existingCategory = null;

        CategoryService service = new CategoryService(dao);

        Category category = createCategory();
        category.setId(99);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.updateCategory(category)
        );

        assertEquals(
                "Không tìm thấy danh mục.",
                exception.getMessage()
        );
    }

    @Test
    void updateCategory_shouldRejectDuplicateCategory() {
        FakeCategoryDAO dao = new FakeCategoryDAO();
        dao.duplicate = true;

        CategoryService service = new CategoryService(dao);

        Category category = createCategory();
        category.setId(1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.updateCategory(category)
        );

        assertEquals(
                "Danh mục đã tồn tại.",
                exception.getMessage()
        );
    }

    @Test
    void updateCategory_shouldUpdateValidCategory() throws SQLException {
        FakeCategoryDAO dao = new FakeCategoryDAO();
        CategoryService service = new CategoryService(dao);

        Category category = createCategory();
        category.setId(1);

        service.updateCategory(category);

        assertTrue(dao.updateCalled);
    }

    @Test
    void deleteCategory_shouldRejectInvalidId() {
        CategoryService service = new CategoryService(new FakeCategoryDAO());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.deleteCategory(0)
        );

        assertEquals(
                "ID danh mục không hợp lệ.",
                exception.getMessage()
        );
    }

    @Test
    void deleteCategory_shouldRejectNotFound() {
        FakeCategoryDAO dao = new FakeCategoryDAO();
        dao.existingCategory = null;

        CategoryService service = new CategoryService(dao);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.deleteCategory(99)
        );

        assertEquals(
                "Không tìm thấy danh mục.",
                exception.getMessage()
        );
    }

    @Test
    void deleteCategory_shouldDeleteValidCategory() throws SQLException {
        FakeCategoryDAO dao = new FakeCategoryDAO();
        CategoryService service = new CategoryService(dao);

        service.deleteCategory(1);

        assertTrue(dao.deleteCalled);
    }

    private Category createCategory() {
        Category category = new Category();

        category.setId(1);
        category.setCategoryType("CUSTOMER_TYPE");
        category.setCategoryCode("PERSON_NEW");
        category.setCategoryName("Cá nhân mới");
        category.setDescription("Khách hàng cá nhân");
        category.setStatus(true);

        return category;
    }

    /**
     * DAO giả để test CategoryService mà không cần kết nối MySQL.
     */
    private static class FakeCategoryDAO extends CategoryDAO {

        private boolean duplicate = false;
        private boolean insertCalled = false;
        private boolean updateCalled = false;
        private boolean deleteCalled = false;

        private Category existingCategory = createDefaultCategory();

        @Override
        public List<Category> findAll() {
            return List.of(createDefaultCategory());
        }

        @Override
        public Category findById(long id) {
            return existingCategory;
        }

        @Override
        public boolean exists(
                String categoryType,
                String categoryCode,
                long excludeId) {
            return duplicate;
        }

        @Override
        public boolean insert(Category category) {
            insertCalled = true;
            return true;
        }

        @Override
        public boolean update(Category category) {
            updateCalled = true;
            return true;
        }

        @Override
        public boolean delete(long id) {
            deleteCalled = true;
            return true;
        }

        private static Category createDefaultCategory() {
            Category category = new Category();

            category.setId(1);
            category.setCategoryType("CUSTOMER_TYPE");
            category.setCategoryCode("PERSON");
            category.setCategoryName("Cá nhân");
            category.setDescription("Khách hàng cá nhân");
            category.setStatus(true);

            return category;
        }
    }
}