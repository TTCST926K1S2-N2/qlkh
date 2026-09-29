package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.model.DataScope;

/**
 * Xử lý phân quyền theo phạm vi dữ liệu - HTQLKH-5.
 */
public class PermissionService {

    /**
     * Kiểm tra tài khoản hiện tại có được truy cập bản ghi hay không.
     */
    public boolean canAccess(
            DataScope scope,
            long currentUserId,
            Long currentGroupId,
            long recordOwnerId,
            Long recordGroupId) {

        if (scope == null) {
            return false;
        }

        return switch (scope) {
            case MY -> currentUserId == recordOwnerId;

            case TEAM -> currentGroupId != null
                    && recordGroupId != null
                    && currentGroupId.equals(recordGroupId);

            case ALL -> true;
        };
    }

    /**
     * Thông báo khi truy cập dữ liệu ngoài phạm vi.
     */
    public String getAccessDeniedMessage() {
        return "Bạn không có quyền truy cập dữ liệu ngoài phạm vi được phân công.";
    }
    /**
 * Xác định phạm vi dữ liệu theo vai trò hiện tại của hệ thống.
 *
 * SALES   -> MY
 * MANAGER -> TEAM
 * ADMIN   -> ALL
 */
public DataScope resolveDataScope(String role) {
    if (role == null || role.isBlank()) {
        return null;
    }

    return switch (role.trim().toUpperCase()) {
        case "SALES" -> DataScope.MY;
        case "MANAGER" -> DataScope.TEAM;
        case "ADMIN" -> DataScope.ALL;
        default -> null;
    };
}
}