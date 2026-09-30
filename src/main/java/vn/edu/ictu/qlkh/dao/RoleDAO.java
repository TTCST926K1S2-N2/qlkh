package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.Role;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Truy xuất thông tin vai trò phục vụ menu điều hướng - HTQLKH-6.
 */
public class RoleDAO {

    /**
     * Lấy vai trò hiện tại của người dùng.
     *
     * Project hiện lưu role trực tiếp trong bảng users.
     */
    public Role findByUserId(long userId) throws SQLException {

        String sql = """
                SELECT role
                FROM users
                WHERE id = ?
                  AND status = 'ACTIVE'
                LIMIT 1
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (!resultSet.next()) {
                    return null;
                }

                String roleCode =
                        resultSet.getString("role");

                return new Role(
                        roleCode,
                        getRoleDisplayName(roleCode)
                );
            }
        }
    }

    /**
     * Chuyển mã role thành tên tiếng Việt để FE hiển thị.
     */
    public String getRoleDisplayName(String roleCode) {

        if (roleCode == null) {
            return "Không xác định";
        }

        return switch (roleCode.trim().toUpperCase()) {

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