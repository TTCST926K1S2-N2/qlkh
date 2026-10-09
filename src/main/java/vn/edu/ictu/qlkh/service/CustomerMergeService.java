package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.CustomerMergeDAO;
import vn.edu.ictu.qlkh.model.Customer;
import java.sql.SQLException;

public class CustomerMergeService {
    private final CustomerDuplicateService duplicateService = new CustomerDuplicateService();
    private final CustomerMergeDAO mergeDAO = new CustomerMergeDAO();

    public void merge(long sourceId, long targetId, long userId, String role, String username)
            throws SQLException {
        if (role == null || !"MANAGER".equalsIgnoreCase(role.trim()))
            throw new SecurityException("Chi MANAGER duoc phep gop khach hang");
        if (sourceId <= 0 || targetId <= 0)
            throw new IllegalArgumentException("ID khach hang phai lon hon 0");
        if (sourceId == targetId)
            throw new IllegalArgumentException("Khong the gop khach hang voi chinh no");

        Customer[] pair = duplicateService.compare(sourceId, targetId, userId, role);
        if (!duplicateService.areDuplicates(pair[0], pair[1]))
            throw new IllegalStateException(
                    "Hai khach hang khong trung ma so thue, ten cong ty hoac website");
        mergeDAO.merge(sourceId, targetId, userId, username);
    }
}
