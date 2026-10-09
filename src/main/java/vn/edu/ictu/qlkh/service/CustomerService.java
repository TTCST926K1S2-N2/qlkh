package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.CustomerDAO;
import vn.edu.ictu.qlkh.model.Customer;
import vn.edu.ictu.qlkh.model.DataScope;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class CustomerService {

    private static final Set<String> STATUSES = Set.of(
            "POTENTIAL",
            "IN_PROGRESS",
            "CUSTOMER",
            "INACTIVE"
    );

    private final CustomerDAO dao;
    private final PermissionService permissionService;

    public CustomerService() {
        this(new CustomerDAO(), new PermissionService());
    }

    public CustomerService(
            CustomerDAO dao,
            PermissionService permissionService) {
        this.dao = dao;
        this.permissionService = permissionService;
    }

    private DataScope requireScope(String role) {
        DataScope scope =
                permissionService.resolveDataScope(role);

        if (scope == null) {
            throw new SecurityException(
                    "Khong co quyen quan ly khach hang"
            );
        }

        return scope;
    }

    public List<Customer> list(
            long userId,
            String role) throws SQLException {

        DataScope scope = requireScope(role);

        return dao.findVisible(userId, scope.name());
    }

    private String normalizeSearch(
            String value, int maxLength, String field) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        if (normalized.isEmpty()) {
            return null;
        }

        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(
                    field + " vuot qua " + maxLength + " ky tu");
        }

        return normalized;
    }

    public CustomerDAO.SearchPageData searchCustomers(
            long userId,
            String role,
            String keyword,
            String companyName,
            String taxCode,
            String phone,
            String industry,
            String status,
            Long ownerId,
            int page,
            int size) throws SQLException {

        DataScope scope = requireScope(role);

        if (userId <= 0) {
            throw new SecurityException("Nguoi dung khong hop le");
        }

        if (page < 1 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "page phai >= 1 va size phai tu 1 den 100");
        }

        keyword = normalizeSearch(keyword, 255, "Tu khoa");
        companyName = normalizeSearch(
                companyName, 255, "Ten doanh nghiep");
        taxCode = normalizeSearch(taxCode, 50, "Ma so thue");
        phone = normalizeSearch(phone, 30, "So dien thoai");
        industry = normalizeSearch(industry, 150, "Nganh nghe");
        status = normalizeSearch(status, 30, "Trang thai");

        if (status != null && !STATUSES.contains(status)) {
            throw new IllegalArgumentException(
                    "Trang thai khong hop le");
        }

        if (ownerId != null && ownerId <= 0) {
            throw new IllegalArgumentException(
                    "ownerId phai lon hon 0");
        }

        return dao.searchVisible(
                userId,
                scope.name(),
                keyword,
                companyName,
                taxCode,
                phone,
                industry,
                status,
                ownerId,
                page,
                size);
    }
    public Customer detail(
            long id,
            long userId,
            String role) throws SQLException {

        DataScope scope = requireScope(role);

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "ID khong hop le"
            );
        }

        return dao.findVisibleById(
                id, userId, scope.name()
        );
    }

    public long create(
            Customer customer,
            long userId,
            String role) throws SQLException {

        DataScope scope = requireScope(role);

        if (customer == null) {
            throw new IllegalArgumentException(
                    "Du lieu khong hop le"
            );
        }

        if (customer.getOwnerId() == null) {
            customer.setOwnerId(userId);
        }

        validate(customer);
        checkOwner(
                customer.getOwnerId(),
                userId,
                scope
        );

        if (dao.existsTaxCode(
                customer.getTaxCode(), null)) {
            throw new IllegalArgumentException(
                    "Ma so thue da ton tai"
            );
        }

        return dao.insert(customer);
    }

    public boolean update(
            long id,
            Customer customer,
            long userId,
            String role) throws SQLException {

        DataScope scope = requireScope(role);

        if (id <= 0 || customer == null) {
            throw new IllegalArgumentException(
                    "Du lieu khong hop le"
            );
        }

        Customer existing = dao.findVisibleById(
                id, userId, scope.name()
        );

        if (existing == null) {
            return false;
        }

        if (customer.getOwnerId() == null) {
            customer.setOwnerId(existing.getOwnerId());
        }

        customer.setId(id);

        validate(customer);

        checkOwner(
                customer.getOwnerId(),
                userId,
                scope
        );

        if (dao.existsTaxCode(
                customer.getTaxCode(), id)) {
            throw new IllegalArgumentException(
                    "Ma so thue da ton tai"
            );
        }

        return dao.updateVisible(
                customer, userId, scope.name()
        );
    }

    private void checkOwner(
            long ownerId,
            long userId,
            DataScope scope) throws SQLException {

        if (ownerId <= 0 ||
                !dao.ownerExists(ownerId)) {
            throw new IllegalArgumentException(
                    "Nguoi so huu khong ton tai"
            );
        }

        boolean allowed = switch (scope) {
            case MY -> ownerId == userId;
            case TEAM -> ownerId == userId ||
                    dao.isOwnerInTeam(ownerId, userId);
            case ALL -> true;
        };

        if (!allowed) {
            throw new SecurityException(
                    "Nguoi so huu ngoai pham vi quan ly"
            );
        }
    }

    private void validate(Customer c) {

        c.setCompanyName(
                trim(c.getCompanyName())
        );
        c.setTaxCode(
                trim(c.getTaxCode())
        );
        c.setIndustry(
                trim(c.getIndustry())
        );
        c.setCompanySize(
                trim(c.getCompanySize())
        );
        c.setWebsite(
                trim(c.getWebsite())
        );
        c.setAddress(
                trim(c.getAddress())
        );

        if (c.getCompanyName() == null ||
                c.getCompanyName().isBlank() ||
                c.getCompanyName().length() > 255) {
            throw new IllegalArgumentException(
                    "Ten doanh nghiep khong hop le"
            );
        }

        checkLength(c.getTaxCode(), 50, "Ma so thue");
        checkLength(c.getIndustry(), 150, "Nganh nghe");
        checkLength(c.getCompanySize(), 100, "Quy mo");
        checkLength(c.getWebsite(), 500, "Website");
        checkLength(c.getAddress(), 500, "Dia chi");

        if (c.getStatus() == null ||
                c.getStatus().isBlank()) {
            c.setStatus("POTENTIAL");
        }

        if (!STATUSES.contains(c.getStatus())) {
            throw new IllegalArgumentException(
                    "Trang thai khong hop le"
            );
        }
    }

    private void checkLength(
            String value,
            int max,
            String field) {

        if (value != null && value.length() > max) {
            throw new IllegalArgumentException(
                    field + " vuot qua " + max + " ky tu"
            );
        }
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }

        String result = value.trim();
        return result.isEmpty() ? null : result;
    }
}