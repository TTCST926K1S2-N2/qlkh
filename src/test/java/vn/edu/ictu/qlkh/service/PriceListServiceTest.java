package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.Test;

import vn.edu.ictu.qlkh.dao.PriceListDAO;
import vn.edu.ictu.qlkh.dao.ProductDAO;
import vn.edu.ictu.qlkh.model.PriceList;
import vn.edu.ictu.qlkh.model.PriceListItem;
import vn.edu.ictu.qlkh.model.Product;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PriceListServiceTest {

    @Test
    void addPriceList_shouldNormalizeAndInsert()
            throws SQLException {

        FakePriceListDAO priceListDAO =
                new FakePriceListDAO();

        FakeProductDAO productDAO =
                new FakeProductDAO();

        PriceListService service =
                new PriceListService(
                        priceListDAO,
                        productDAO
                );

        PriceList priceList =
                createPriceList();

        priceList.setCode(" bg_standard ");
        priceList.setStatus(null);

        PriceList result =
                service.addPriceList(priceList);

        assertEquals(
                "BG_STANDARD",
                result.getCode()
        );

        assertEquals(
                "ACTIVE",
                result.getStatus()
        );

        assertEquals(
                1L,
                result.getId()
        );

        assertTrue(
                priceListDAO.insertCalled
        );
    }


    @Test
    void addPriceList_shouldRejectDuplicateCode() {

        FakePriceListDAO priceListDAO =
                new FakePriceListDAO();

        priceListDAO.duplicatePriceList =
                createPriceList();

        priceListDAO.duplicatePriceList.setId(99L);

        PriceListService service =
                new PriceListService(
                        priceListDAO,
                        new FakeProductDAO()
                );

        IllegalArgumentException ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.addPriceList(
                                        createPriceList()
                                )
                );

        assertEquals(
                "Mã bảng giá đã tồn tại.",
                ex.getMessage()
        );
    }


    @Test
    void addPriceList_shouldRejectEndDateBeforeStartDate() {

        PriceListService service =
                new PriceListService(
                        new FakePriceListDAO(),
                        new FakeProductDAO()
                );

        PriceList priceList =
                createPriceList();

        priceList.setStartDate(
                LocalDate.of(2026, 10, 10)
        );

        priceList.setEndDate(
                LocalDate.of(2026, 10, 1)
        );

        IllegalArgumentException ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.addPriceList(
                                        priceList
                                )
                );

        assertEquals(
                "Ngày kết thúc không được trước ngày bắt đầu.",
                ex.getMessage()
        );
    }


    @Test
    void deleteOrDeactivatePriceList_shouldDeactivateWhenHasItems()
            throws SQLException {

        FakePriceListDAO priceListDAO =
                new FakePriceListDAO();

        PriceList existing =
                createPriceList();

        existing.setId(1L);

        priceListDAO.priceList =
                existing;

        priceListDAO.hasItems = true;

        PriceListService service =
                new PriceListService(
                        priceListDAO,
                        new FakeProductDAO()
                );

        boolean result =
                service.deleteOrDeactivatePriceList(1L);

        assertTrue(result);

        assertTrue(
                priceListDAO.deactivateCalled
        );

        assertFalse(
                priceListDAO.deleteCalled
        );
    }


    @Test
    void addItem_shouldUseProductDefaultPrices()
            throws SQLException {

        FakePriceListDAO priceListDAO =
                new FakePriceListDAO();

        PriceList existing =
                createPriceList();

        existing.setId(1L);

        priceListDAO.priceList =
                existing;

        FakeProductDAO productDAO =
                new FakeProductDAO();

        productDAO.product =
                createProduct(
                        10L,
                        "ACTIVE"
                );

        PriceListService service =
                new PriceListService(
                        priceListDAO,
                        productDAO
                );

        PriceListItem item =
                new PriceListItem();

        item.setProductId(10L);

        PriceListItem result =
                service.addItem(
                        1L,
                        item
                );

        assertNotNull(result);

        assertEquals(
                new BigDecimal("1000000.00"),
                result.getListPrice()
        );

        assertEquals(
                new BigDecimal("800000.00"),
                result.getFloorPrice()
        );

        assertTrue(
                priceListDAO.insertItemCalled
        );
    }


    @Test
    void addItem_shouldRejectInactiveProduct() {

        FakePriceListDAO priceListDAO =
                new FakePriceListDAO();

        PriceList existing =
                createPriceList();

        existing.setId(1L);

        priceListDAO.priceList =
                existing;

        FakeProductDAO productDAO =
                new FakeProductDAO();

        productDAO.product =
                createProduct(
                        10L,
                        "INACTIVE"
                );

        PriceListService service =
                new PriceListService(
                        priceListDAO,
                        productDAO
                );

        PriceListItem item =
                new PriceListItem();

        item.setProductId(10L);

        IllegalArgumentException ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.addItem(
                                        1L,
                                        item
                                )
                );

        assertEquals(
                "Sản phẩm/dịch vụ đã ngừng kinh doanh.",
                ex.getMessage()
        );
    }


    @Test
    void addItem_shouldRejectFloorPriceGreaterThanListPrice() {

        FakePriceListDAO priceListDAO =
                new FakePriceListDAO();

        PriceList existing =
                createPriceList();

        existing.setId(1L);

        priceListDAO.priceList =
                existing;

        FakeProductDAO productDAO =
                new FakeProductDAO();

        productDAO.product =
                createProduct(
                        10L,
                        "ACTIVE"
                );

        PriceListService service =
                new PriceListService(
                        priceListDAO,
                        productDAO
                );

        PriceListItem item =
                new PriceListItem();

        item.setProductId(10L);

        item.setListPrice(
                new BigDecimal("900000.00")
        );

        item.setFloorPrice(
                new BigDecimal("950000.00")
        );

        IllegalArgumentException ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.addItem(
                                        1L,
                                        item
                                )
                );

        assertEquals(
                "Giá sàn không được lớn hơn giá niêm yết.",
                ex.getMessage()
        );
    }


    @Test
    void addItem_shouldRejectDuplicateProduct() {

        FakePriceListDAO priceListDAO =
                new FakePriceListDAO();

        PriceList existing =
                createPriceList();

        existing.setId(1L);

        priceListDAO.priceList =
                existing;

        PriceListItem duplicate =
                new PriceListItem();

        duplicate.setId(5L);
        duplicate.setPriceListId(1L);
        duplicate.setProductId(10L);

        priceListDAO.duplicateItem =
                duplicate;

        FakeProductDAO productDAO =
                new FakeProductDAO();

        productDAO.product =
                createProduct(
                        10L,
                        "ACTIVE"
                );

        PriceListService service =
                new PriceListService(
                        priceListDAO,
                        productDAO
                );

        PriceListItem item =
                new PriceListItem();

        item.setProductId(10L);

        IllegalArgumentException ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.addItem(
                                        1L,
                                        item
                                )
                );

        assertEquals(
                "Sản phẩm/dịch vụ đã có trong bảng giá.",
                ex.getMessage()
        );
    }


    @Test
    void updateItem_shouldUpdatePrices()
            throws SQLException {

        FakePriceListDAO priceListDAO =
                new FakePriceListDAO();

        PriceListItem existing =
                new PriceListItem();

        existing.setId(5L);
        existing.setPriceListId(1L);
        existing.setProductId(10L);

        existing.setListPrice(
                new BigDecimal("1000000.00")
        );

        existing.setFloorPrice(
                new BigDecimal("800000.00")
        );

        priceListDAO.storedItem =
                existing;

        FakeProductDAO productDAO =
                new FakeProductDAO();

        productDAO.product =
                createProduct(
                        10L,
                        "ACTIVE"
                );

        PriceListService service =
                new PriceListService(
                        priceListDAO,
                        productDAO
                );

        PriceListItem update =
                new PriceListItem();

        update.setProductId(10L);

        update.setListPrice(
                new BigDecimal("1200000.00")
        );

        update.setFloorPrice(
                new BigDecimal("900000.00")
        );

        PriceListItem result =
                service.updateItem(
                        1L,
                        5L,
                        update
                );

        assertNotNull(result);

        assertEquals(
                new BigDecimal("1200000.00"),
                result.getListPrice()
        );

        assertEquals(
                new BigDecimal("900000.00"),
                result.getFloorPrice()
        );

        assertTrue(
                priceListDAO.updateItemCalled
        );
    }


    @Test
    void deleteItem_shouldCallDao()
            throws SQLException {

        FakePriceListDAO priceListDAO =
                new FakePriceListDAO();

        PriceListService service =
                new PriceListService(
                        priceListDAO,
                        new FakeProductDAO()
                );

        boolean result =
                service.deleteItem(
                        1L,
                        5L
                );

        assertTrue(result);

        assertTrue(
                priceListDAO.deleteItemCalled
        );
    }


    private PriceList createPriceList() {

        PriceList priceList =
                new PriceList();

        priceList.setCode("BG_STANDARD");

        priceList.setName(
                "Bảng giá chuẩn"
        );

        priceList.setStartDate(
                LocalDate.of(
                        2026,
                        10,
                        1
                )
        );

        priceList.setEndDate(
                LocalDate.of(
                        2026,
                        12,
                        31
                )
        );

        priceList.setStatus("ACTIVE");

        priceList.setDescription(
                "Bảng giá test S2-05"
        );

        return priceList;
    }


    private Product createProduct(
            long id,
            String status) {

        Product product =
                new Product();

        product.setId(id);
        product.setCode("SP001");
        product.setName("Sản phẩm test");
        product.setType("PRODUCT");
        product.setUnit("Gói");

        product.setBasePrice(
                new BigDecimal("1000000.00")
        );

        product.setFloorPrice(
                new BigDecimal("800000.00")
        );

        product.setCostPrice(
                new BigDecimal("600000.00")
        );

        product.setStatus(status);

        return product;
    }


    private static class FakeProductDAO
            extends ProductDAO {

        private Product product;


        @Override
        public Product findById(long id) {

            if (product != null
                    && product.getId() != null
                    && product.getId() == id) {

                return product;
            }

            return null;
        }
    }


    private static class FakePriceListDAO
            extends PriceListDAO {

        private PriceList priceList;

        private PriceList duplicatePriceList;

        private PriceListItem duplicateItem;

        private PriceListItem storedItem;

        private boolean hasItems;

        private boolean insertCalled;

        private boolean deleteCalled;

        private boolean deactivateCalled;

        private boolean insertItemCalled;

        private boolean updateItemCalled;

        private boolean deleteItemCalled;


        @Override
        public PriceList findById(long id) {

            if (priceList != null
                    && priceList.getId() != null
                    && priceList.getId() == id) {

                return priceList;
            }

            return null;
        }


        @Override
        public PriceList findByCode(
                String code) {

            return duplicatePriceList;
        }


        @Override
        public long insert(
                PriceList priceList) {

            insertCalled = true;

            priceList.setId(1L);

            this.priceList =
                    priceList;

            return 1L;
        }


        @Override
        public boolean update(
                PriceList priceList) {

            this.priceList =
                    priceList;

            return true;
        }


        @Override
        public boolean hasItems(
                long priceListId) {

            return hasItems;
        }


        @Override
        public boolean deactivate(
                long id) {

            deactivateCalled = true;

            if (priceList != null) {
                priceList.setStatus(
                        "INACTIVE"
                );
            }

            return true;
        }


        @Override
        public boolean delete(
                long id) {

            deleteCalled = true;

            return true;
        }


        @Override
        public List<PriceListItem> findItems(
                long priceListId) {

            List<PriceListItem> result =
                    new ArrayList<>();

            if (storedItem != null) {
                result.add(storedItem);
            }

            return result;
        }


        @Override
        public PriceListItem findItemById(
                long priceListId,
                long itemId) {

            if (storedItem != null
                    && storedItem.getId() != null
                    && storedItem.getId() == itemId
                    && storedItem.getPriceListId() != null
                    && storedItem.getPriceListId() == priceListId) {

                return storedItem;
            }

            return null;
        }


        @Override
        public PriceListItem findItemByProduct(
                long priceListId,
                long productId) {

            if (duplicateItem != null) {
                return duplicateItem;
            }

            if (storedItem != null
                    && storedItem.getProductId() != null
                    && storedItem.getProductId() == productId
                    && storedItem.getPriceListId() != null
                    && storedItem.getPriceListId() == priceListId) {

                return storedItem;
            }

            return null;
        }


        @Override
        public long insertItem(
                PriceListItem item) {

            insertItemCalled = true;

            item.setId(10L);

            storedItem = item;

            return 10L;
        }


        @Override
        public boolean updateItem(
                PriceListItem item) {

            updateItemCalled = true;

            storedItem = item;

            return true;
        }


        @Override
        public boolean deleteItem(
                long priceListId,
                long itemId) {

            deleteItemCalled = true;

            storedItem = null;

            return true;
        }
    }
}
