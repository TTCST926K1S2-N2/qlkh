package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.ProductDAO;
import vn.edu.ictu.qlkh.model.Product;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class ProductService {

    private final ProductDAO productDAO;

    public ProductService() {
        this(new ProductDAO());
    }

    ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }


    public List<Product> getAllProducts()
            throws SQLException {

        return productDAO.findAll();
    }


    public Product getProductById(long id)
            throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "ID sản phẩm/dịch vụ không hợp lệ."
            );
        }

        return productDAO.findById(id);
    }


    public Product addProduct(Product product)
            throws SQLException {

        if (product == null) {
            throw new IllegalArgumentException(
                    "Dữ liệu sản phẩm/dịch vụ không hợp lệ."
            );
        }

        prepareNewProduct(product);
        validate(product);

        Product duplicate =
                productDAO.findByCode(
                        product.getCode()
                );

        if (duplicate != null) {
            throw new IllegalArgumentException(
                    "Mã sản phẩm/dịch vụ đã tồn tại."
            );
        }

        productDAO.insert(product);

        return product;
    }


    public Product updateProduct(
            long id,
            Product product)
            throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "ID sản phẩm/dịch vụ không hợp lệ."
            );
        }

        if (product == null) {
            throw new IllegalArgumentException(
                    "Dữ liệu sản phẩm/dịch vụ không hợp lệ."
            );
        }

        Product existing =
                productDAO.findById(id);

        if (existing == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy sản phẩm/dịch vụ."
            );
        }

        /*
         * FE hiện tại chưa gửi floorPrice và costPrice.
         * Khi thiếu thì giữ giá trị cũ thay vì ghi đè.
         */
        if (product.getFloorPrice() == null) {
            product.setFloorPrice(
                    existing.getFloorPrice()
            );
        }

        if (product.getCostPrice() == null) {
            product.setCostPrice(
                    existing.getCostPrice()
            );
        }

        product.setId(id);

        normalize(product);
        validate(product);

        Product duplicate =
                productDAO.findByCode(
                        product.getCode()
                );

        if (duplicate != null
                && !duplicate.getId().equals(id)) {

            throw new IllegalArgumentException(
                    "Mã sản phẩm/dịch vụ đã tồn tại."
            );
        }

        if (!productDAO.update(product)) {
            throw new SQLException(
                    "Không thể cập nhật sản phẩm/dịch vụ."
            );
        }

        return product;
    }


    public boolean deleteOrDeactivateProduct(long id)
            throws SQLException {

        Product product =
                productDAO.findById(id);

        if (product == null) {
            return false;
        }

        /*
         * Nếu sản phẩm đã nằm trong bảng giá,
         * không xóa vật lý để tránh mất lịch sử.
         */
        if (productDAO.existsInPriceList(id)) {

            product.setStatus("INACTIVE");

            return productDAO.update(product);
        }

        return productDAO.delete(id);
    }


    private void prepareNewProduct(Product product) {

        normalize(product);

        if (product.getStatus() == null
                || product.getStatus().isBlank()) {

            product.setStatus("ACTIVE");
        }

        /*
         * FE hiện tại chỉ có giá niêm yết.
         * Khi chưa gửi giá sàn, mặc định bằng giá niêm yết.
         */
        if (product.getFloorPrice() == null) {

            product.setFloorPrice(
                    product.getBasePrice()
            );
        }
    }


    private void normalize(Product product) {

        if (product.getCode() != null) {

            product.setCode(
                    product.getCode()
                            .trim()
                            .toUpperCase(Locale.ROOT)
            );
        }

        if (product.getName() != null) {
            product.setName(
                    product.getName().trim()
            );
        }

        if (product.getType() != null) {

            product.setType(
                    product.getType()
                            .trim()
                            .toUpperCase(Locale.ROOT)
            );
        }

        if (product.getUnit() != null) {
            product.setUnit(
                    product.getUnit().trim()
            );
        }

        if (product.getStatus() != null) {

            product.setStatus(
                    product.getStatus()
                            .trim()
                            .toUpperCase(Locale.ROOT)
            );
        }

        if (product.getDescription() != null) {

            String description =
                    product.getDescription().trim();

            product.setDescription(
                    description.isEmpty()
                            ? null
                            : description
            );
        }
    }


    private void validate(Product product) {

        if (isBlank(product.getCode())) {
            throw new IllegalArgumentException(
                    "Mã sản phẩm/dịch vụ không được để trống."
            );
        }

        if (product.getCode().length() > 50) {
            throw new IllegalArgumentException(
                    "Mã sản phẩm/dịch vụ tối đa 50 ký tự."
            );
        }

        if (isBlank(product.getName())) {
            throw new IllegalArgumentException(
                    "Tên sản phẩm/dịch vụ không được để trống."
            );
        }

        if (product.getName().length() > 255) {
            throw new IllegalArgumentException(
                    "Tên sản phẩm/dịch vụ tối đa 255 ký tự."
            );
        }

        if (!"PRODUCT".equals(product.getType())
                && !"SERVICE".equals(product.getType())) {

            throw new IllegalArgumentException(
                    "Loại phải là PRODUCT hoặc SERVICE."
            );
        }

        if (isBlank(product.getUnit())) {
            throw new IllegalArgumentException(
                    "Đơn vị tính không được để trống."
            );
        }

        validateNonNegative(
                product.getBasePrice(),
                "Giá niêm yết"
        );

        validateNonNegative(
                product.getFloorPrice(),
                "Giá sàn"
        );

        if (product.getCostPrice() != null) {

            validateNonNegative(
                    product.getCostPrice(),
                    "Giá vốn"
            );
        }

        if (product.getFloorPrice()
                .compareTo(product.getBasePrice()) > 0) {

            throw new IllegalArgumentException(
                    "Giá sàn không được lớn hơn giá niêm yết."
            );
        }

        if (!"ACTIVE".equals(product.getStatus())
                && !"INACTIVE".equals(product.getStatus())) {

            throw new IllegalArgumentException(
                    "Trạng thái phải là ACTIVE hoặc INACTIVE."
            );
        }

        if (product.getDescription() != null
                && product.getDescription().length() > 1000) {

            throw new IllegalArgumentException(
                    "Mô tả tối đa 1000 ký tự."
            );
        }
    }


    private void validateNonNegative(
            BigDecimal value,
            String fieldName) {

        if (value == null) {
            throw new IllegalArgumentException(
                    fieldName + " không được để trống."
            );
        }

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    fieldName + " không được âm."
            );
        }
    }


    private boolean isBlank(String value) {

        return value == null
                || value.isBlank();
    }
}