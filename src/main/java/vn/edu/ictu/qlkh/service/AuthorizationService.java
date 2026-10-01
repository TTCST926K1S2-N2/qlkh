package vn.edu.ictu.qlkh.service;

import java.util.Locale;

/**
 * HTQLKH-7
 *
 * Kiểm tra quyền truy cập URL theo vai trò người dùng.
 *
 * AuthenticationFilter:
 *     kiểm tra người dùng đã đăng nhập hay chưa.
 *
 * AuthorizationFilter:
 *     sử dụng service này để kiểm tra người dùng đã đăng nhập
 *     có quyền truy cập chức năng hay không.
 */
public class AuthorizationService {

    /**
     * Kiểm tra quyền truy cập.
     *
     * @param role vai trò hiện tại
     * @param path đường dẫn trong ứng dụng
     * @return true nếu được phép truy cập
     */
    public boolean canAccess(String role, String path) {

        String normalizedRole = normalizeRole(role);

        if (!isSupportedRole(normalizedRole)) {
            return false;
        }

        if (path == null || path.isBlank()) {
            return false;
        }

        /*
         * Các chức năng quản trị hệ thống chỉ ADMIN được phép truy cập.
         *
         * Đồng bộ với quyền menu của HTQLKH-6:
         * - Quản lý tài khoản
         * - Phân quyền & Nhóm nghiệp vụ
         * - Khóa tài khoản & Bàn giao dữ liệu
         */
        if (isAdminOnlyPath(path)) {
            return "ADMIN".equals(normalizedRole);
        }

        /*
         * Những URL không thuộc nhóm ADMIN ở trên không bị
         * AuthorizationService chặn.
         *
         * Quyền dữ liệu MY / TEAM / ALL được xử lý riêng
         * bởi PermissionService của HTQLKH-5.
         */
        return true;
    }

    /**
     * Kiểm tra các URL chỉ dành cho ADMIN.
     */
    public boolean isAdminOnlyPath(String path) {

        if (path == null) {
            return false;
        }

        return matchesPath(path, "/users")
                || matchesPath(path, "/roles")
                || matchesPath(path, "/lock-transfer");
    }

    /**
     * Các vai trò hiện được hệ thống hỗ trợ.
     */
    public boolean isSupportedRole(String role) {

        String normalizedRole = normalizeRole(role);

        return "SALES".equals(normalizedRole)
                || "MANAGER".equals(normalizedRole)
                || "ADMIN".equals(normalizedRole);
    }

    /**
     * Cho phép cả URL gốc và URL con.
     *
     * Ví dụ:
     * /users
     * /users/create
     * /users/123/edit
     */
    private boolean matchesPath(String path, String protectedPath) {

        return path.equals(protectedPath)
                || path.startsWith(protectedPath + "/");
    }

    private String normalizeRole(String role) {

        if (role == null || role.isBlank()) {
            return null;
        }

        return role.trim().toUpperCase(Locale.ROOT);
    }
}