package vn.edu.ictu.qlkh.service;

import java.util.Locale;

/**
 * HTQLKH-6 - Xác định quyền hiển thị menu theo vai trò.
 *
 * Backend chỉ cung cấp quyền.
 * Frontend/sidebar.jsp chịu trách nhiệm hiển thị hoặc ẩn menu.
 */
public class MenuService {

    public MenuPermissions getMenuPermissions(String role) {

        String normalizedRole = normalizeRole(role);

        if (normalizedRole == null) {
            return MenuPermissions.noPermission();
        }

        return switch (normalizedRole) {

            /*
             * Nhân viên kinh doanh:
             * - Trang chủ
             * - Quản lý khách hàng
             * - Hồ sơ cá nhân
             * - Đổi mật khẩu
             */
            case "SALES" -> new MenuPermissions(
                    true,
                    true,
                    false,
                    false,
                    false,
                    true,
                    true
            );

            /*
             * Quản lý kinh doanh:
             * - Trang chủ
             * - Quản lý khách hàng
             * - Hồ sơ cá nhân
             * - Đổi mật khẩu
             *
             * Các chức năng quản trị hệ thống không được hiển thị.
             */
            case "MANAGER" -> new MenuPermissions(
                    true,
                    true,
                    false,
                    false,
                    false,
                    true,
                    true
            );

            /*
             * ADMIN:
             * Có toàn bộ menu hiện có.
             */
            case "ADMIN" -> new MenuPermissions(
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true
            );

            default -> MenuPermissions.noPermission();
        };
    }

    /**
     * Kiểm tra role có phải role hợp lệ của hệ thống hay không.
     */
    public boolean isSupportedRole(String role) {

        String normalizedRole = normalizeRole(role);

        return "SALES".equals(normalizedRole)
                || "MANAGER".equals(normalizedRole)
                || "ADMIN".equals(normalizedRole);
    }

    /**
     * Chuẩn hóa role để tránh lỗi do chữ hoa/thường hoặc khoảng trắng.
     */
    private String normalizeRole(String role) {

        if (role == null || role.isBlank()) {
            return null;
        }

        return role.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * DTO chứa quyền hiển thị menu.
     *
     * JSP có thể sử dụng:
     *
     * ${menuPermissions.home}
     * ${menuPermissions.customerManagement}
     * ${menuPermissions.userManagement}
     * ${menuPermissions.roleGroupManagement}
     * ${menuPermissions.accountHandover}
     * ${menuPermissions.profile}
     * ${menuPermissions.changePassword}
     */
    public static class MenuPermissions {

        private final boolean home;
        private final boolean customerManagement;
        private final boolean userManagement;
        private final boolean roleGroupManagement;
        private final boolean accountHandover;
        private final boolean profile;
        private final boolean changePassword;

        public MenuPermissions(
                boolean home,
                boolean customerManagement,
                boolean userManagement,
                boolean roleGroupManagement,
                boolean accountHandover,
                boolean profile,
                boolean changePassword) {

            this.home = home;
            this.customerManagement = customerManagement;
            this.userManagement = userManagement;
            this.roleGroupManagement = roleGroupManagement;
            this.accountHandover = accountHandover;
            this.profile = profile;
            this.changePassword = changePassword;
        }

        public static MenuPermissions noPermission() {
            return new MenuPermissions(
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false
            );
        }

        public boolean isHome() {
            return home;
        }

        public boolean isCustomerManagement() {
            return customerManagement;
        }

        public boolean isUserManagement() {
            return userManagement;
        }

        public boolean isRoleGroupManagement() {
            return roleGroupManagement;
        }

        public boolean isAccountHandover() {
            return accountHandover;
        }

        public boolean isProfile() {
            return profile;
        }

        public boolean isChangePassword() {
            return changePassword;
        }
    }
}