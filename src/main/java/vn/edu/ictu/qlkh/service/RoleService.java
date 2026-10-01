package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.BusinessGroupDAO;
import vn.edu.ictu.qlkh.dao.RoleDAO;
import vn.edu.ictu.qlkh.model.Role;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Xử lý nghiệp vụ vai trò và nhóm kinh doanh - HTQLKH-9.
 *
 * Yêu cầu:
 * - Một người dùng có thể giữ nhiều vai trò.
 * - Người giữ vai trò MANAGER phải được gán một nhóm kinh doanh.
 * - Quản trị viên không được tự thu hồi vai trò ADMIN của chính mình.
 */
public class RoleService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MANAGER = "MANAGER";
    private static final String ROLE_SALES = "SALES";

    private final RoleDAO roleDAO;
    private final BusinessGroupDAO businessGroupDAO;

    public RoleService() {
        this(
                new RoleDAO(),
                new BusinessGroupDAO()
        );
    }

    public RoleService(
            RoleDAO roleDAO,
            BusinessGroupDAO businessGroupDAO
    ) {
        if (roleDAO == null) {
            throw new IllegalArgumentException(
                    "RoleDAO không được null."
            );
        }

        if (businessGroupDAO == null) {
            throw new IllegalArgumentException(
                    "BusinessGroupDAO không được null."
            );
        }

        this.roleDAO = roleDAO;
        this.businessGroupDAO = businessGroupDAO;
    }

    /**
     * Lấy danh sách tất cả vai trò.
     */
    public List<Role> getAllRoles()
            throws SQLException {

        return roleDAO.findAll();
    }

    /**
     * Lấy toàn bộ vai trò của một người dùng.
     */
    public List<Role> getRolesByUserId(long userId)
            throws SQLException {

        if (userId <= 0) {
            return List.of();
        }

        return roleDAO.findAllByUserId(userId);
    }

    /**
     * Kiểm tra người dùng có role cụ thể.
     */
    public boolean userHasRole(
            long userId,
            String roleCode
    ) throws SQLException {

        if (userId <= 0
                || roleCode == null
                || roleCode.isBlank()) {

            return false;
        }

        return roleDAO.userHasRole(
                userId,
                normalizeRole(roleCode)
        );
    }

    /**
     * Cập nhật vai trò và nhóm kinh doanh của một tài khoản.
     *
     * @param currentAdminUserId ID admin đang thực hiện thao tác
     * @param targetUserId       ID tài khoản được phân quyền
     * @param roleCodes          danh sách role mới
     * @param groupId            nhóm kinh doanh được chọn; có thể null
     */
    public void updateRoleAndGroup(
            long currentAdminUserId,
            long targetUserId,
            Collection<String> roleCodes,
            Long groupId
    ) throws SQLException {

        validateUserIds(
                currentAdminUserId,
                targetUserId
        );

        List<String> normalizedRoles =
                normalizeRoles(roleCodes);

        validateRoleCodes(normalizedRoles);

        preventSelfAdminRemoval(
                currentAdminUserId,
                targetUserId,
                normalizedRoles
        );

        validateManagerGroup(
                normalizedRoles,
                groupId
        );

        /*
         * Kiểm tra group trước khi thay đổi role để tránh
         * cập nhật role xong mới phát hiện group không tồn tại.
         */
        if (groupId != null) {

            if (groupId <= 0) {
                throw new IllegalArgumentException(
                        "Nhóm kinh doanh không hợp lệ."
                );
            }

            if (!businessGroupDAO.existsById(groupId)) {
                throw new IllegalArgumentException(
                        "Nhóm kinh doanh không tồn tại."
                );
            }
        }

        /*
         * Lưu nhiều vai trò.
         * RoleDAO đồng thời cập nhật users.role bằng role chính
         * để tương thích với Login/Menu/Authorization hiện tại.
         */
        roleDAO.replaceUserRoles(
                targetUserId,
                normalizedRoles
        );

        /*
         * Nếu có nhóm được chọn thì gán/cập nhật nhóm.
         *
         * Nếu không chọn nhóm và user không còn là MANAGER,
         * gỡ quan hệ nhóm hiện tại.
         */
        if (groupId != null) {

            businessGroupDAO.assignUserToGroup(
                    targetUserId,
                    groupId
            );

        } else if (!normalizedRoles.contains(ROLE_MANAGER)) {

            businessGroupDAO.removeUserFromGroup(
                    targetUserId
            );
        }
    }

    /**
     * Không cho admin đang đăng nhập tự bỏ quyền ADMIN.
     */
    private void preventSelfAdminRemoval(
            long currentAdminUserId,
            long targetUserId,
            List<String> newRoles
    ) throws SQLException {

        if (currentAdminUserId != targetUserId) {
            return;
        }

        boolean currentlyAdmin =
                roleDAO.userHasRole(
                        targetUserId,
                        ROLE_ADMIN
                );

        if (currentlyAdmin
                && !newRoles.contains(ROLE_ADMIN)) {

            throw new IllegalArgumentException(
                    "Bạn không thể tự thu hồi vai trò quản trị của chính mình."
            );
        }
    }

    /**
     * MANAGER bắt buộc phải thuộc một nhóm kinh doanh.
     */
    private void validateManagerGroup(
            List<String> roles,
            Long groupId
    ) {

        if (roles.contains(ROLE_MANAGER)
                && (groupId == null || groupId <= 0)) {

            throw new IllegalArgumentException(
                    "Người giữ vai trò Quản lý kinh doanh phải được gán một nhóm kinh doanh."
            );
        }
    }

    /**
     * Kiểm tra từng role có nằm trong danh mục hợp lệ.
     */
    private void validateRoleCodes(
            List<String> roles
    ) throws SQLException {

        for (String roleCode : roles) {

            if (!roleDAO.existsByCode(roleCode)) {

                throw new IllegalArgumentException(
                        "Vai trò không hợp lệ: "
                                + roleCode
                );
            }
        }
    }

    /**
     * Chuẩn hóa danh sách role:
     * - loại null/rỗng
     * - uppercase
     * - loại trùng
     */
    private List<String> normalizeRoles(
            Collection<String> roleCodes
    ) {

        if (roleCodes == null
                || roleCodes.isEmpty()) {

            throw new IllegalArgumentException(
                    "Người dùng phải có ít nhất một vai trò."
            );
        }

        Set<String> roles =
                new LinkedHashSet<>();

        for (String roleCode : roleCodes) {

            if (roleCode == null
                    || roleCode.isBlank()) {
                continue;
            }

            roles.add(
                    normalizeRole(roleCode)
            );
        }

        if (roles.isEmpty()) {

            throw new IllegalArgumentException(
                    "Người dùng phải có ít nhất một vai trò."
            );
        }

        return new ArrayList<>(roles);
    }

    private String normalizeRole(
            String roleCode
    ) {

        return roleCode
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private void validateUserIds(
            long currentAdminUserId,
            long targetUserId
    ) {

        if (currentAdminUserId <= 0) {
            throw new IllegalArgumentException(
                    "Tài khoản quản trị không hợp lệ."
            );
        }

        if (targetUserId <= 0) {
            throw new IllegalArgumentException(
                    "Tài khoản cần phân quyền không hợp lệ."
            );
        }
    }
}