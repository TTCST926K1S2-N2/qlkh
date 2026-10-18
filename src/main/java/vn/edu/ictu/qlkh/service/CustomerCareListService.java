package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.CustomerCareListDAO;
import vn.edu.ictu.qlkh.model.Customer;
import vn.edu.ictu.qlkh.model.DataScope;
import java.sql.SQLException;
import java.util.List;

public class CustomerCareListService {
    private final CustomerCareListDAO dao = new CustomerCareListDAO();
    private final CustomerService customers = new CustomerService();
    private final PermissionService permissions = new PermissionService();

    private String scope(long userId, String role) {
        if (userId <= 0) throw new SecurityException("Phien dang nhap khong hop le");
        DataScope scope = permissions.resolveDataScope(role);
        if (scope == null) throw new SecurityException("Khong co quyen truy cap");
        return scope.name();
    }
    public List<CustomerCareListDAO.Item> list(int days, long userId, String role) throws SQLException {
        if (days < 1 || days > 3650) throw new IllegalArgumentException("So ngay phai tu 1 den 3650");
        return dao.find(userId, scope(userId, role), days);
    }
    public void contacted(long customerId, long userId, String role) throws SQLException {
        scope(userId, role);
        if (customerId <= 0) throw new IllegalArgumentException("ID khong hop le");
        Customer c = customers.detail(customerId, userId, role);
        if (c == null) throw new SecurityException("Khong co quyen truy cap khach hang");
        dao.markContacted(customerId, userId);
    }
}
