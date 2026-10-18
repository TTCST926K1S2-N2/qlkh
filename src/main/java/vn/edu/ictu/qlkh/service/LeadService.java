package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.LeadDAO;
import vn.edu.ictu.qlkh.model.DataScope;

import java.sql.SQLException;
import java.util.Locale;
import java.util.Objects;

public class LeadService {

    private final LeadDAO leadDAO;
    private final PermissionService permissionService;

    public LeadService() {
        this(new LeadDAO(), new PermissionService());
    }

    public LeadService(
            LeadDAO leadDAO,
            PermissionService permissionService
    ) {
        this.leadDAO = Objects.requireNonNull(leadDAO);
        this.permissionService = Objects.requireNonNull(permissionService);
    }

    public LeadDAO.SearchPage search(
            long userId,
            String role,
            String keyword,
            String status,
            String source,
            Long assignedTo,
            int page,
            int size
    ) throws SQLException {
        return search(userId, role, keyword, status, source,
                assignedTo, page, size, null, null, "createdAt", "desc");
    }

    public LeadDAO.SearchPage search(
            long userId,
            String role,
            String keyword,
            String status,
            String source,
            Long assignedTo,
            int page,
            int size,
            java.time.LocalDate createdFrom,
            java.time.LocalDate createdTo,
            String sortBy,
            String sortDirection
    ) throws SQLException {
        return search(userId, role, keyword, status, source,
                assignedTo, page, size, createdFrom, createdTo,
                sortBy, sortDirection, null);
    }

    public LeadDAO.SearchPage search(
            long userId,
            String role,
            String keyword,
            String status,
            String source,
            Long assignedTo,
            int page,
            int size,
            java.time.LocalDate createdFrom,
            java.time.LocalDate createdTo,
            String sortBy,
            String sortDirection,
            String classification
    ) throws SQLException {
        if (createdFrom != null && createdTo != null
                && createdFrom.isAfter(createdTo)) {
            throw new IllegalArgumentException("Invalid date range");
        }

        if (userId <= 0) {
            throw new SecurityException("Nguoi dung khong hop le");
        }

        if (role == null || role.isBlank()) {
            throw new SecurityException("Khong co quyen truy cap");
        }

        String normalizedRole = role.trim().toUpperCase(Locale.ROOT);

        DataScope scope = permissionService.resolveDataScope(normalizedRole);

        if (scope == null) {
            throw new SecurityException("Khong co quyen truy cap");
        }

        // SALES khong duoc tu y xem Lead cua nguoi khac.
        if (scope == DataScope.MY
                && assignedTo != null
                && assignedTo != userId) {
            throw new SecurityException("Khong co quyen truy cap");
        }

        String normalizedClassification = normalize(classification);

        if (normalizedClassification != null
                && !"HOT".equals(normalizedClassification)
                && !"WARM".equals(normalizedClassification)
                && !"COLD".equals(normalizedClassification)) {
            throw new IllegalArgumentException("Invalid classification");
        }

        if (normalizedClassification == null
                && createdFrom == null && createdTo == null
                && (sortBy == null || sortBy.isBlank()
                    || "createdAt".equals(sortBy))
                && (sortDirection == null || sortDirection.isBlank()
                    || "desc".equalsIgnoreCase(sortDirection))) {
            return leadDAO.searchVisible(
                    userId,
                    scope.name(),
                    keyword == null || keyword.isBlank() ? null : keyword.trim(),
                    normalize(status),
                    normalize(source),
                    assignedTo,
                    page,
                    size
            );
        }

        if (normalizedClassification == null) {
        return leadDAO.searchVisible(
                userId,
                scope.name(),
                keyword == null || keyword.isBlank() ? null : keyword.trim(),
                normalize(status),
                normalize(source),
                assignedTo,
                page,
                size,
                createdFrom,
                createdTo,
                sortBy,
                sortDirection

        );
        }

        return leadDAO.searchVisible(
                userId,
                scope.name(),
                keyword == null || keyword.isBlank() ? null : keyword.trim(),
                normalize(status),
                normalize(source),
                assignedTo,
                page,
                size,
                createdFrom,
                createdTo,
                sortBy,
                sortDirection,
                normalizedClassification
        );
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}