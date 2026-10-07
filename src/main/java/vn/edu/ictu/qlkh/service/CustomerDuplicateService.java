package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.CustomerDAO;
import vn.edu.ictu.qlkh.dao.CustomerDuplicateDAO;
import vn.edu.ictu.qlkh.model.Customer;
import vn.edu.ictu.qlkh.model.CustomerDuplicate;
import vn.edu.ictu.qlkh.model.DataScope;

import java.sql.SQLException;
import java.util.List;

public class CustomerDuplicateService {

    private final CustomerDAO customerDAO;
    private final CustomerDuplicateDAO dao;
    private final PermissionService permissionService;

    public CustomerDuplicateService() {
        this(
                new CustomerDAO(),
                new CustomerDuplicateDAO(),
                new PermissionService()
        );
    }

    public CustomerDuplicateService(
            CustomerDAO customerDAO,
            CustomerDuplicateDAO dao,
            PermissionService permissionService) {

        this.customerDAO = customerDAO;
        this.dao = dao;
        this.permissionService = permissionService;
    }

    public List<CustomerDuplicate> findDuplicates(
            long customerId,
            long userId,
            String role) throws SQLException {

        if (customerId <= 0) {
            throw new IllegalArgumentException(
                    "ID khach hang khong hop le.");
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
                        customerId,
                        userId,
                        scope.name());

        if (source == null) {
            throw new SecurityException(
                    "Khong co quyen truy cap khach hang.");
        }

        return dao.findDuplicates(
                customerId,
                userId,
                scope.name());
    }
}
