package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.BusinessGroup;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý nhóm kinh doanh - HTQLKH-9.
 *
 * Một người dùng được gán tối đa một nhóm kinh doanh.
 */
public class BusinessGroupDAO {

    /**
     * Lấy toàn bộ nhóm kinh doanh.
     */
    public List<BusinessGroup> findAll()
            throws SQLException {

        String sql = """
                SELECT id, name
                FROM business_groups
                ORDER BY name ASC
                """;

        List<BusinessGroup> groups =
                new ArrayList<>();

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql);

             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {

                groups.add(
                        mapBusinessGroup(resultSet)
                );
            }
        }

        return groups;
    }

    /**
     * Tìm nhóm kinh doanh theo ID.
     */
    public BusinessGroup findById(long groupId)
            throws SQLException {

        String sql = """
                SELECT id, name
                FROM business_groups
                WHERE id = ?
                LIMIT 1
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    groupId
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (!resultSet.next()) {
                    return null;
                }

                return mapBusinessGroup(
                        resultSet
                );
            }
        }
    }

    /**
     * Lấy nhóm kinh doanh hiện tại của một user.
     */
    public BusinessGroup findByUserId(long userId)
            throws SQLException {

        String sql = """
                SELECT bg.id, bg.name
                FROM user_business_groups ubg
                JOIN business_groups bg
                    ON bg.id = ubg.group_id
                WHERE ubg.user_id = ?
                LIMIT 1
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    userId
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (!resultSet.next()) {
                    return null;
                }

                return mapBusinessGroup(
                        resultSet
                );
            }
        }
    }

    /**
     * Kiểm tra nhóm kinh doanh có tồn tại.
     */
    public boolean existsById(long groupId)
            throws SQLException {

        String sql = """
                SELECT 1
                FROM business_groups
                WHERE id = ?
                LIMIT 1
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    groupId
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                return resultSet.next();
            }
        }
    }

    /**
     * Gán user vào nhóm kinh doanh.
     *
     * user_id là PRIMARY KEY trong user_business_groups,
     * vì vậy một user chỉ có tối đa một nhóm.
     *
     * Nếu user đã có nhóm thì chuyển sang nhóm mới.
     */
    public void assignUserToGroup(
            long userId,
            long groupId
    ) throws SQLException {

        String sql = """
                INSERT INTO user_business_groups (
                    user_id,
                    group_id
                )
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE
                    group_id = ?
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    userId
            );

            statement.setLong(
                    2,
                    groupId
            );

            statement.setLong(
                    3,
                    groupId
            );

            statement.executeUpdate();
        }
    }

    /**
     * Gỡ user khỏi nhóm kinh doanh.
     */
    public void removeUserFromGroup(long userId)
            throws SQLException {

        String sql = """
                DELETE FROM user_business_groups
                WHERE user_id = ?
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    userId
            );

            statement.executeUpdate();
        }
    }

    /**
     * Map ResultSet thành BusinessGroup.
     */
    private BusinessGroup mapBusinessGroup(
            ResultSet resultSet
    ) throws SQLException {

        return new BusinessGroup(
                resultSet.getLong("id"),
                resultSet.getString("name")
        );
    }
}