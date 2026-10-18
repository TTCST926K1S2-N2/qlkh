package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.CustomerMergeDAO;

import java.sql.SQLException;
import java.util.Locale;
import java.util.Set;

public class CustomerMergeService {

    private final CustomerMergeDAO dao;

    public CustomerMergeService() {
        this(new CustomerMergeDAO());
    }

    public CustomerMergeService(CustomerMergeDAO dao) {
        this.dao = java.util.Objects.requireNonNull(dao);
    }

    public CustomerMergeDAO.MergeResult merge(
            long sourceId,
            long targetId,
            long actorId,
            String role) throws SQLException {

        if (actorId <= 0) {
            throw new SecurityException("Chua dang nhap");
        }

        if (role == null) {
            throw new SecurityException("Khong co quyen");
        }

        String normalizedRole =
                role.trim().toUpperCase(Locale.ROOT);

        if (!Set.of("MANAGER", "ADMIN")
                .contains(normalizedRole)) {
            throw new SecurityException(
                    "Chi quan ly va quan tri vien duoc gop");
        }

        if (sourceId <= 0 || targetId <= 0) {
            throw new IllegalArgumentException(
                    "ID khach hang phai lon hon 0");
        }

        if (sourceId == targetId) {
            throw new IllegalArgumentException(
                    "Khong the gop khach hang voi chinh minh");
        }

        return dao.merge(
                sourceId,
                targetId,
                actorId,
                normalizedRole);
    }
}