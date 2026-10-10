package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.Customer360DAO;
import vn.edu.ictu.qlkh.dao.CustomerDAO;
import vn.edu.ictu.qlkh.dto.Customer360DTO;
import vn.edu.ictu.qlkh.model.Customer;
import vn.edu.ictu.qlkh.model.DataScope;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/** S3-03: Authorize the customer BEFORE querying related records. */
public class Customer360Service {
    private final CustomerDAO customerDAO;
    private final Customer360DAO customer360DAO;
    private final PermissionService permissionService;

    public Customer360Service() {
        this(new CustomerDAO(), new Customer360DAO(), new PermissionService());
    }

    public Customer360Service(CustomerDAO customerDAO,
                              Customer360DAO customer360DAO,
                              PermissionService permissionService) {
        this.customerDAO = Objects.requireNonNull(customerDAO);
        this.customer360DAO = Objects.requireNonNull(customer360DAO);
        this.permissionService = Objects.requireNonNull(permissionService);
    }

    /** Returns null for missing OR inaccessible customers (no ID disclosure). */
    public Customer360DTO get(long customerId, long userId, String role)
            throws SQLException {
        if (customerId <= 0) {
            throw new IllegalArgumentException("ID khach hang khong hop le");
        }
        if (userId <= 0) {
            throw new SecurityException("Nguoi dung khong hop le");
        }
        DataScope scope = permissionService.resolveDataScope(role);
        if (scope == null) {
            throw new SecurityException("Khong co quyen truy cap");
        }

        // S3-01 already handles MY/TEAM/ALL (including multiple business groups).
        Customer customer = customerDAO.findVisibleById(
                customerId, userId, scope.name());
        if (customer == null) {
            return null;
        }

        Customer360DAO.ContactSection section =
                customer360DAO.findContacts(customerId);
        return new Customer360DTO(
                customer,
                section.items(),
                List.of(), // No opportunity source in the current Sprint 3 base.
                List.of(), // No activity source yet.
                List.of(), // No attachment source yet.
                new Customer360DTO.Availability(
                        section.available(), false, false, false));
    }
}
