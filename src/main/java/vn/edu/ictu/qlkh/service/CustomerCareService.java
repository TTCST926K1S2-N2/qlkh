package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.CustomerCareDAO;
import vn.edu.ictu.qlkh.dto.CustomerCareDTO;

import java.sql.SQLException;
import java.util.List;

public class CustomerCareService {

    private final CustomerCareDAO dao;
    private final PermissionService permissionService;

    public CustomerCareService() {
        this(new CustomerCareDAO(), new PermissionService());
    }

    CustomerCareService(
            CustomerCareDAO dao,
            PermissionService permissionService) {
        this.dao = dao;
        this.permissionService = permissionService;
    }

    public List<CustomerCareDTO> list(long userId, String role)
            throws SQLException {

        String scope = requireScope(role);
        return dao.findCandidates(userId, scope);
    }

    private String requireScope(String role) {
        var scope = permissionService.resolveDataScope(role);

        if (scope == null) {
            throw new SecurityException(
                    "Khong co quyen xem danh sach khach hang can cham soc"
            );
        }

        return scope.name();
    }
}