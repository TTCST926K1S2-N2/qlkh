package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.CustomerDAO;
import vn.edu.ictu.qlkh.dao.CustomerDuplicateDAO;
import vn.edu.ictu.qlkh.model.Customer;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class CustomerDuplicateService {
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final CustomerDuplicateDAO duplicateDAO = new CustomerDuplicateDAO();

    public List<Customer> findDuplicates(long customerId, long userId, String role)
            throws SQLException {
        String scope = scope(role);
        Customer base = customerDAO.findVisibleById(customerId, userId, scope);
        if (base == null)
            throw new IllegalArgumentException("Khong tim thay khach hang trong pham vi truy cap");
        return duplicateDAO.findPotentialDuplicates(base, userId, scope);
    }

    public Customer[] compare(long sourceId, long targetId, long userId, String role)
            throws SQLException {
        if (sourceId <= 0 || targetId <= 0)
            throw new IllegalArgumentException("ID khach hang khong hop le");
        if (sourceId == targetId) throw new IllegalArgumentException("Hai ID phai khac nhau");
        String scope = scope(role);
        Customer source = customerDAO.findVisibleById(sourceId, userId, scope);
        Customer target = customerDAO.findVisibleById(targetId, userId, scope);
        if (source == null || target == null)
            throw new IllegalArgumentException(
                    "Khong tim thay mot trong hai khach hang trong pham vi truy cap");
        return new Customer[]{source, target};
    }

    public boolean areDuplicates(Customer a, Customer b) {
        return duplicateDAO.isDuplicate(a, b);
    }

    private String scope(String role) {
        return switch (role == null ? "" : role.trim().toUpperCase(Locale.ROOT)) {
            case "SALES" -> "MY";
            case "MANAGER" -> "TEAM";
            case "ADMIN" -> "ALL";
            default -> throw new SecurityException("Khong co quyen truy cap");
        };
    }
}
