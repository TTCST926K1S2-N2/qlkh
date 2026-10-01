package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.Role;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Quản lý vai trò người dùng.
 *
 * HTQLKH-6:
 * - Lấy vai trò chính phục vụ menu và phân quyền hiện tại.
 *
 * HTQLKH-9:
 * - Một người dùng có thể có nhiều vai trò.
 * - Các vai trò được lưu trong bảng user_roles.
 *
 * Cột users.role vẫn được giữ và đồng bộ vai trò chính
 * để các chức năng cũ của hệ thống tiếp tục hoạt động.
 */
public class RoleDAO {

    /**
     * Lấy vai trò chính của người dùng.
     *
     * Nếu có nhiều vai trò thì ưu tiên:
     * ADMIN -> MANAGER -> SALES.
     *
     * Giữ method này để tương thích với HTQLKH-6.
     */
    public Role findByUserId(long userId) throws SQLException {

        String sql = """
                SELECT r.code, r.name
                FROM user_roles ur
                JOIN roles r
                    ON r.code = ur.role_code
                JOIN users u
                    ON u.id = ur.user_id
                WHERE ur.user_id = ?
                  AND u.status = 'ACTIVE'
                ORDER BY CASE r.code
                    WHEN 'ADMIN' THEN 1
                    WHEN 'MANAGER' THEN 2
                    WHEN 'SALES' THEN 3
                    ELSE 99
                END
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRole(resultSet);
                }
            }
        }

        // Fallback để tương thích dữ liệu cũ.
        return findLegacyRoleByUserId(userId);
    }

    /**
     * Lấy toàn bộ vai trò trong hệ thống.
     */
    public List<Role> findAll() throws SQLException {

        String sql = """
                SELECT code, name
                FROM roles
                ORDER BY CASE code
                    WHEN 'ADMIN' THEN 1
                    WHEN 'MANAGER' THEN 2
                    WHEN 'SALES' THEN 3
                    ELSE 99
                END, code
                """;

        List<Role> roles = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                roles.add(mapRole(resultSet));
            }
        }

        return roles;
    }

    /**
     * Lấy tất cả vai trò của một người dùng.
     */
    public List<Role> findAllByUserId(long userId) throws SQLException {

        String sql = """
                SELECT r.code, r.name
                FROM user_roles ur
                JOIN roles r
                    ON r.code = ur.role_code
                WHERE ur.user_id = ?
                ORDER BY CASE r.code
                    WHEN 'ADMIN' THEN 1
                    WHEN 'MANAGER' THEN 2
                    WHEN 'SALES' THEN 3
                    ELSE 99
                END, r.code
                """;

        List<Role> roles = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    roles.add(mapRole(resultSet));
                }
            }
        }

        return roles;
    }

    /**
     * Kiểm tra người dùng có vai trò cụ thể hay không.
     */
    public boolean userHasRole(
            long userId,
            String roleCode
    ) throws SQLException {

        if (roleCode == null || roleCode.isBlank()) {
            return false;
        }

        String sql = """
                SELECT 1
                FROM user_roles
                WHERE user_id = ?
                  AND role_code = ?
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);
            statement.setString(
                    2,
                    normalizeRoleCode(roleCode)
            );

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /**
     * Kiểm tra mã vai trò có tồn tại hay không.
     */
    public boolean existsByCode(String roleCode)
            throws SQLException {

        if (roleCode == null || roleCode.isBlank()) {
            return false;
        }

        String sql = """
                SELECT 1
                FROM roles
                WHERE code = ?
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    normalizeRoleCode(roleCode)
            );

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /**
     * Thay toàn bộ vai trò của một người dùng.
     *
     * Dùng transaction để tránh dữ liệu bị cập nhật một nửa.
     *
     * Sau khi cập nhật user_roles, users.role cũng được cập nhật
     * bằng vai trò chính để Login/Menu/Authorization cũ vẫn hoạt động.
     */
    public void replaceUserRoles(
            long userId,
            Collection<String> roleCodes
    ) throws SQLException {

        if (roleCodes == null || roleCodes.isEmpty()) {
            throw new IllegalArgumentException(
                    "Người dùng phải có ít nhất một vai trò."
            );
        }

        List<String> normalizedRoles = roleCodes.stream()
                .filter(role -> role != null && !role.isBlank())
                .map(this::normalizeRoleCode)
                .distinct()
                .toList();

        if (normalizedRoles.isEmpty()) {
            throw new IllegalArgumentException(
                    "Người dùng phải có ít nhất một vai trò."
            );
        }

        String deleteSql = """
                DELETE FROM user_roles
                WHERE user_id = ?
                """;

        String insertSql = """
                INSERT INTO user_roles (
                    user_id,
                    role_code
                )
                VALUES (?, ?)
                """;

        String updateUserSql = """
                UPDATE users
                SET role = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection()) {

            boolean oldAutoCommit = connection.getAutoCommit();

            try {
                connection.setAutoCommit(false);

                validateRoles(
                        connection,
                        normalizedRoles
                );

                /*
                 * Xóa danh sách role cũ.
                 */
                try (PreparedStatement statement =
                             connection.prepareStatement(deleteSql)) {

                    statement.setLong(1, userId);
                    statement.executeUpdate();
                }

                /*
                 * Thêm danh sách role mới.
                 */
                try (PreparedStatement statement =
                             connection.prepareStatement(insertSql)) {

                    for (String roleCode : normalizedRoles) {

                        statement.setLong(1, userId);
                        statement.setString(2, roleCode);
                        statement.addBatch();
                    }

                    statement.executeBatch();
                }

                /*
                 * Xác định role chính để giữ tương thích
                 * với users.role của hệ thống cũ.
                 */
                String primaryRole =
                        determinePrimaryRole(normalizedRoles);

                try (PreparedStatement statement =
                             connection.prepareStatement(updateUserSql)) {

                    statement.setString(
                            1,
                            primaryRole
                    );

                    statement.setLong(
                            2,
                            userId
                    );

                    int updated =
                            statement.executeUpdate();

                    if (updated != 1) {
                        throw new SQLException(
                                "Không tìm thấy tài khoản cần cập nhật."
                        );
                    }
                }

                connection.commit();

            } catch (SQLException | RuntimeException e) {

                connection.rollback();
                throw e;

            } finally {

                connection.setAutoCommit(oldAutoCommit);
            }
        }
    }

    /**
     * Lấy role theo users.role cũ.
     *
     * Dùng làm fallback để tránh ảnh hưởng dữ liệu
     * hoặc chức năng cũ.
     */
    private Role findLegacyRoleByUserId(long userId)
            throws SQLException {

        String sql = """
                SELECT role
                FROM users
                WHERE id = ?
                  AND status = 'ACTIVE'
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return null;
                }

                String roleCode =
                        resultSet.getString("role");

                if (roleCode == null || roleCode.isBlank()) {
                    return null;
                }

                String normalized =
                        normalizeRoleCode(roleCode);

                return new Role(
                        normalized,
                        getRoleDisplayName(normalized)
                );
            }
        }
    }

    /**
     * Kiểm tra tất cả role gửi lên đều tồn tại
     * trong bảng roles.
     */
    private void validateRoles(
            Connection connection,
            List<String> roleCodes
    ) throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM roles
                WHERE code = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            for (String roleCode : roleCodes) {

                statement.setString(
                        1,
                        roleCode
                );

                try (ResultSet resultSet =
                             statement.executeQuery()) {

                    resultSet.next();

                    if (resultSet.getInt(1) != 1) {
                        throw new IllegalArgumentException(
                                "Vai trò không hợp lệ: "
                                        + roleCode
                        );
                    }
                }
            }
        }
    }

    /**
     * Xác định vai trò chính.
     */
    private String determinePrimaryRole(
            Collection<String> roles
    ) {

        if (roles.contains("ADMIN")) {
            return "ADMIN";
        }

        if (roles.contains("MANAGER")) {
            return "MANAGER";
        }

        if (roles.contains("SALES")) {
            return "SALES";
        }

        return roles.iterator().next();
    }

    /**
     * Chuẩn hóa mã role.
     */
    private String normalizeRoleCode(
            String roleCode
    ) {

        return roleCode
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    /**
     * Map ResultSet thành Role.
     */
    private Role mapRole(ResultSet resultSet)
            throws SQLException {

        return new Role(
                resultSet.getString("code"),
                resultSet.getString("name")
        );
    }

    /**
     * Tên hiển thị role.
     *
     * Giữ method để code HTQLKH-6 cũ vẫn sử dụng được.
     */
    public String getRoleDisplayName(
            String roleCode
    ) {

        if (roleCode == null) {
            return "Không xác định";
        }

        return switch (
                normalizeRoleCode(roleCode)
        ) {

            case "ADMIN" ->
                    "Quản trị hệ thống";

            case "MANAGER" ->
                    "Quản lý kinh doanh";

            case "SALES" ->
                    "Nhân viên kinh doanh";

            default ->
                    "Không xác định";
        };
    }
}