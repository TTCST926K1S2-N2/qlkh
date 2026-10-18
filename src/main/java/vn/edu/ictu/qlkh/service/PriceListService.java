package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.PriceListDAO;
import vn.edu.ictu.qlkh.dao.ProductDAO;
import vn.edu.ictu.qlkh.model.PriceList;
import vn.edu.ictu.qlkh.model.PriceListItem;
import vn.edu.ictu.qlkh.model.Product;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class PriceListService {

    private final PriceListDAO priceListDAO;
    private final ProductDAO productDAO;

    public PriceListService() {
        this(
                new PriceListDAO(),
                new ProductDAO()
        );
    }

    PriceListService(
            PriceListDAO priceListDAO,
            ProductDAO productDAO) {

        this.priceListDAO =
                priceListDAO;

        this.productDAO =
                productDAO;
    }


    public List<PriceList> getAllPriceLists()
            throws SQLException {

        return priceListDAO.findAll();
    }


    public PriceList getPriceListById(long id)
            throws SQLException {

        validateId(id);

        return priceListDAO.findById(id);
    }


    public List<PriceListItem> getItems(
            long priceListId)
            throws SQLException {

        validateId(priceListId);

        PriceList priceList =
                priceListDAO.findById(
                        priceListId
                );

        if (priceList == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy bảng giá."
            );
        }

        return priceListDAO.findItems(
                priceListId
        );
    }


    public PriceList addPriceList(
            PriceList priceList)
            throws SQLException {

        if (priceList == null) {
            throw new IllegalArgumentException(
                    "Dữ liệu bảng giá không hợp lệ."
            );
        }

        normalize(priceList);

        if (isBlank(priceList.getStatus())) {
            priceList.setStatus("ACTIVE");
        }

        validate(priceList);

        PriceList duplicate =
                priceListDAO.findByCode(
                        priceList.getCode()
                );

        if (duplicate != null) {
            throw new IllegalArgumentException(
                    "Mã bảng giá đã tồn tại."
            );
        }

        priceListDAO.insert(
                priceList
        );

        return priceList;
    }


    public PriceList updatePriceList(
            long id,
            PriceList priceList)
            throws SQLException {

        validateId(id);

        if (priceList == null) {
            throw new IllegalArgumentException(
                    "Dữ liệu bảng giá không hợp lệ."
            );
        }

        PriceList existing =
                priceListDAO.findById(id);

        if (existing == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy bảng giá."
            );
        }

        priceList.setId(id);

        normalize(priceList);
        validate(priceList);

        PriceList duplicate =
                priceListDAO.findByCode(
                        priceList.getCode()
                );

        if (duplicate != null
                && !duplicate.getId().equals(id)) {

            throw new IllegalArgumentException(
                    "Mã bảng giá đã tồn tại."
            );
        }

        if (!priceListDAO.update(priceList)) {
            throw new SQLException(
                    "Không thể cập nhật bảng giá."
            );
        }

        return priceList;
    }


    public boolean deleteOrDeactivatePriceList(
            long id)
            throws SQLException {

        validateId(id);

        PriceList existing =
                priceListDAO.findById(id);

        if (existing == null) {
            return false;
        }

        if (priceListDAO.hasItems(id)) {

            return priceListDAO.deactivate(id);
        }

        return priceListDAO.delete(id);
    }


    public PriceListItem addItem(
            long priceListId,
            PriceListItem item)
            throws SQLException {

        validateId(priceListId);

        if (item == null) {
            throw new IllegalArgumentException(
                    "Dữ liệu chi tiết bảng giá không hợp lệ."
            );
        }

        PriceList priceList =
                priceListDAO.findById(
                        priceListId
                );

        if (priceList == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy bảng giá."
            );
        }

        if (item.getProductId() == null
                || item.getProductId() <= 0) {

            throw new IllegalArgumentException(
                    "Sản phẩm/dịch vụ không hợp lệ."
            );
        }

        Product product =
                productDAO.findById(
                        item.getProductId()
                );

        if (product == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy sản phẩm/dịch vụ."
            );
        }

        if (!"ACTIVE".equalsIgnoreCase(
                product.getStatus())) {

            throw new IllegalArgumentException(
                    "Sản phẩm/dịch vụ đã ngừng kinh doanh."
            );
        }

        PriceListItem duplicate =
                priceListDAO.findItemByProduct(
                        priceListId,
                        item.getProductId()
                );

        if (duplicate != null) {
            throw new IllegalArgumentException(
                    "Sản phẩm/dịch vụ đã có trong bảng giá."
            );
        }

        item.setPriceListId(
                priceListId
        );

        if (item.getListPrice() == null) {

            item.setListPrice(
                    product.getBasePrice()
            );
        }

        if (item.getFloorPrice() == null) {

            item.setFloorPrice(
                    product.getFloorPrice()
            );
        }

        validateItem(item);

        priceListDAO.insertItem(
                item
        );

        return priceListDAO.findItemById(
                priceListId,
                item.getId()
        );
    }


    public PriceListItem updateItem(
            long priceListId,
            long itemId,
            PriceListItem item)
            throws SQLException {

        validateId(priceListId);
        validateId(itemId);

        if (item == null) {
            throw new IllegalArgumentException(
                    "Dữ liệu chi tiết bảng giá không hợp lệ."
            );
        }

        PriceListItem existing =
                priceListDAO.findItemById(
                        priceListId,
                        itemId
                );

        if (existing == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy chi tiết bảng giá."
            );
        }

        if (item.getProductId() == null) {
            item.setProductId(
                    existing.getProductId()
            );
        }

        Product product =
                productDAO.findById(
                        item.getProductId()
                );

        if (product == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy sản phẩm/dịch vụ."
            );
        }

        PriceListItem duplicate =
                priceListDAO.findItemByProduct(
                        priceListId,
                        item.getProductId()
                );

        if (duplicate != null
                && !duplicate.getId().equals(itemId)) {

            throw new IllegalArgumentException(
                    "Sản phẩm/dịch vụ đã có trong bảng giá."
            );
        }

        if (item.getListPrice() == null) {
            item.setListPrice(
                    existing.getListPrice()
            );
        }

        if (item.getFloorPrice() == null) {
            item.setFloorPrice(
                    existing.getFloorPrice()
            );
        }

        item.setId(itemId);
        item.setPriceListId(priceListId);

        validateItem(item);

        if (!priceListDAO.updateItem(item)) {
            throw new SQLException(
                    "Không thể cập nhật chi tiết bảng giá."
            );
        }

        return priceListDAO.findItemById(
                priceListId,
                itemId
        );
    }


    public boolean deleteItem(
            long priceListId,
            long itemId)
            throws SQLException {

        validateId(priceListId);
        validateId(itemId);

        return priceListDAO.deleteItem(
                priceListId,
                itemId
        );
    }


    private void normalize(
            PriceList priceList) {

        if (priceList.getCode() != null) {

            priceList.setCode(
                    priceList.getCode()
                            .trim()
                            .toUpperCase(Locale.ROOT)
            );
        }

        if (priceList.getName() != null) {
            priceList.setName(
                    priceList.getName()
                            .trim()
            );
        }

        if (priceList.getStatus() != null) {

            priceList.setStatus(
                    priceList.getStatus()
                            .trim()
                            .toUpperCase(Locale.ROOT)
            );
        }

        if (priceList.getDescription() != null) {

            String value =
                    priceList.getDescription()
                            .trim();

            priceList.setDescription(
                    value.isEmpty()
                            ? null
                            : value
            );
        }
    }


    private void validate(
            PriceList priceList) {

        if (isBlank(priceList.getCode())) {
            throw new IllegalArgumentException(
                    "Mã bảng giá không được để trống."
            );
        }

        if (priceList.getCode().length() > 50) {
            throw new IllegalArgumentException(
                    "Mã bảng giá tối đa 50 ký tự."
            );
        }

        if (isBlank(priceList.getName())) {
            throw new IllegalArgumentException(
                    "Tên bảng giá không được để trống."
            );
        }

        if (priceList.getName().length() > 255) {
            throw new IllegalArgumentException(
                    "Tên bảng giá tối đa 255 ký tự."
            );
        }

        if (priceList.getStartDate() == null) {
            throw new IllegalArgumentException(
                    "Ngày bắt đầu không được để trống."
            );
        }

        if (priceList.getEndDate() != null
                && priceList.getEndDate()
                .isBefore(
                        priceList.getStartDate()
                )) {

            throw new IllegalArgumentException(
                    "Ngày kết thúc không được trước ngày bắt đầu."
            );
        }

        if (!"ACTIVE".equals(priceList.getStatus())
                && !"INACTIVE".equals(priceList.getStatus())) {

            throw new IllegalArgumentException(
                    "Trạng thái phải là ACTIVE hoặc INACTIVE."
            );
        }

        if (priceList.getDescription() != null
                && priceList.getDescription().length() > 1000) {

            throw new IllegalArgumentException(
                    "Mô tả tối đa 1000 ký tự."
            );
        }
    }


    private void validateItem(
            PriceListItem item) {

        validateMoney(
                item.getListPrice(),
                "Giá niêm yết"
        );

        validateMoney(
                item.getFloorPrice(),
                "Giá sàn"
        );

        if (item.getFloorPrice()
                .compareTo(
                        item.getListPrice()
                ) > 0) {

            throw new IllegalArgumentException(
                    "Giá sàn không được lớn hơn giá niêm yết."
            );
        }
    }


    private void validateMoney(
            BigDecimal value,
            String fieldName) {

        if (value == null) {
            throw new IllegalArgumentException(
                    fieldName
                            + " không được để trống."
            );
        }

        if (value.compareTo(
                BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    fieldName
                            + " không được âm."
            );
        }
    }


    private void validateId(long id) {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "ID không hợp lệ."
            );
        }
    }


    private boolean isBlank(
            String value) {

        return value == null
                || value.isBlank();
    }
}