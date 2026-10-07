package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.CustomerDAO;
import vn.edu.ictu.qlkh.dao.CustomerDuplicateDAO;
import vn.edu.ictu.qlkh.dao.CustomerMergeDAO;
import vn.edu.ictu.qlkh.model.Customer;
import vn.edu.ictu.qlkh.model.CustomerDuplicate;
import vn.edu.ictu.qlkh.model.DataScope;

import java.sql.SQLException;
import java.util.List;

public class CustomerMergeService {

    private final CustomerDAO customerDAO;
    private final CustomerDuplicateDAO duplicateDAO;
    private final CustomerMergeDAO mergeDAO;
    private final PermissionService permissionService;

    public CustomerMergeService() {
        this(
                new CustomerDAO(),
                new CustomerDuplicateDAO(),
                new CustomerMergeDAO(),
                new PermissionService()
        );
    }

    public CustomerMergeService(
            CustomerDAO customerDAO,
            CustomerDuplicateDAO duplicateDAO,
            CustomerMergeDAO mergeDAO,
            PermissionService permissionService) {

        this.customerDAO = customerDAO;
        this.duplicateDAO = duplicateDAO;
        this.mergeDAO = mergeDAO;
        this.permissionService = permissionService;
    }

    public void merge(
            long sourceId,
            long targetId,
            long userId,
            String role)
            throws SQLException {

        if (sourceId <= 0 || targetId <= 0) {
            throw new IllegalArgumentException(
                    "ID khach hang khong hop le.");
        }

        if (sourceId == targetId) {
            throw new IllegalArgumentException(
                    "Khong the gop mot khach hang vao chinh no.");
        }

        if (userId <= 0) {
            throw new SecurityException(
                    "Nguoi dung khong hop le.");
        }

        DataScope scope =
                permissionService.resolveDataScope(role);

        if (scope == null) {
            throw new SecurityException(
                    "Khong co quyen quan ly khach hang.");
        }

        Customer source =
                customerDAO.findVisibleById(
                        sourceId,
                        userId,
                        scope.name());

        if (source == null) {
            throw new SecurityException(
                    "Khong co quyen truy cap khach hang nguon.");
        }

        Customer target =
                customerDAO.findVisibleById(
                        targetId,
                        userId,
                        scope.name());

        if (target == null) {
            throw new SecurityException(
                    "Khong co quyen truy cap khach hang dich.");
        }

        List<CustomerDuplicate> duplicates =
                duplicateDAO.findDuplicates(
                        sourceId,
                        userId,
                        scope.name());

        boolean isDuplicate = duplicates.stream()
                .anyMatch(item ->
                        item.getCustomer() != null
                                && item.getCustomer().getId() == targetId);

        if (!isDuplicate) {
            throw new IllegalArgumentException(
                    "Hai khach hang khong du dieu kien de gop.");
        }

        String oldValue =
                customerJson(source);

        String newValue =
                customerJson(target);

        String details =
                "Merge customer sourceId=" + sourceId
                + " vao targetId=" + targetId;

        String auditUsername =
                "userId:" + userId;

        mergeDAO.merge(
                sourceId,
                targetId,
                auditUsername,
                oldValue,
                newValue,
                details
        );
    }

    private String customerJson(Customer c) {
        return "{"
                + "\"id\":" + c.getId()
                + ",\"companyName\":\""
                + escape(c.getCompanyName())
                + "\""
                + ",\"taxCode\":\""
                + escape(c.getTaxCode())
                + "\""
                + ",\"website\":\""
                + escape(c.getWebsite())
                + "\""
                + ",\"ownerId\":" + c.getOwnerId()
                + ",\"status\":\""
                + escape(c.getStatus())
                + "\""
                + "}";
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
