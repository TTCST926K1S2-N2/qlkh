package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.Test;

import vn.edu.ictu.qlkh.dao.ProductDAO;
import vn.edu.ictu.qlkh.model.Product;

import java.math.BigDecimal;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {

    @Test
    void deleteOrDeactivateProduct_shouldDeactivateInsteadOfDelete()
            throws SQLException {

        FakeProductDAO dao =
                new FakeProductDAO();

        Product product =
                createProduct();

        product.setId(1L);
        product.setStatus("ACTIVE");

        dao.product = product;

        ProductService service =
                new ProductService(dao);

        boolean result =
                service.deleteOrDeactivateProduct(1L);

        assertTrue(result);

        assertEquals(
                "INACTIVE",
                dao.product.getStatus()
        );

        assertTrue(
                dao.updateCalled
        );

        assertFalse(
                dao.deleteCalled
        );
    }


    @Test
    void deleteOrDeactivateProduct_shouldReturnFalseWhenNotFound()
            throws SQLException {

        FakeProductDAO dao =
                new FakeProductDAO();

        ProductService service =
                new ProductService(dao);

        assertFalse(
                service.deleteOrDeactivateProduct(99L)
        );

        assertFalse(
                dao.updateCalled
        );

        assertFalse(
                dao.deleteCalled
        );
    }


    @Test
    void deleteOrDeactivateProduct_shouldRejectInvalidId() {

        ProductService service =
                new ProductService(
                        new FakeProductDAO()
                );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.deleteOrDeactivateProduct(0)
        );
    }


    @Test
    void addProduct_shouldDefaultFloorPriceToBasePrice()
            throws SQLException {

        FakeProductDAO dao =
                new FakeProductDAO();

        ProductService service =
                new ProductService(dao);

        Product product =
                createProduct();

        product.setFloorPrice(null);

        Product created =
                service.addProduct(product);

        assertEquals(
                new BigDecimal("1000000"),
                created.getFloorPrice()
        );

        assertEquals(
                "ACTIVE",
                created.getStatus()
        );

        assertEquals(
                1L,
                created.getId()
        );
    }


    @Test
    void addProduct_shouldRejectFloorPriceGreaterThanBasePrice() {

        ProductService service =
                new ProductService(
                        new FakeProductDAO()
                );

        Product product =
                createProduct();

        product.setFloorPrice(
                new BigDecimal("1500000")
        );

        IllegalArgumentException ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.addProduct(product)
                );

        assertEquals(
                "Giá sàn không được lớn hơn giá niêm yết.",
                ex.getMessage()
        );
    }


    private Product createProduct() {

        Product product =
                new Product();

        product.setCode("SP001");
        product.setName("Sản phẩm test");
        product.setType("PRODUCT");
        product.setUnit("Gói");

        product.setBasePrice(
                new BigDecimal("1000000")
        );

        product.setFloorPrice(
                new BigDecimal("900000")
        );

        product.setCostPrice(
                new BigDecimal("700000")
        );

        product.setStatus("ACTIVE");
        product.setDescription("Test S2-05");

        return product;
    }


    private static class FakeProductDAO
            extends ProductDAO {

        private Product product;

        private boolean updateCalled;
        private boolean deleteCalled;


        @Override
        public Product findById(long id) {

            if (product != null
                    && product.getId() != null
                    && product.getId() == id) {

                return product;
            }

            return null;
        }


        @Override
        public Product findByCode(String code) {

            return null;
        }


        @Override
        public long insert(Product product) {

            product.setId(1L);

            this.product = product;

            return 1L;
        }


        @Override
        public boolean update(Product product) {

            updateCalled = true;

            this.product = product;

            return true;
        }


        @Override
        public boolean delete(long id) {

            deleteCalled = true;

            return true;
        }
    }
}
