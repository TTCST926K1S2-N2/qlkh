package vn.edu.ictu.qlkh.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * HTQLKH-10:
 * Xử lý dữ liệu phục vụ khóa/mở khóa tài khoản
 * và ghi nhận lịch sử bàn giao.
 */
public class AccountHandoverDAO {

    /**
     * Lấy trạng thái hiện tại của tài khoản và khóa bản ghi
     * trong transaction để tránh cập nhật đồng thời.
     */
    public String findUserStatusForUpdate(
            Connection connection,
            long userId)
            throws SQLException {

        String sql = """
                SELECT status
                FROM users
                WHERE id = ?
                FOR UPDATE
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (!resultSet.next()) {
                    return null;
                }

                return resultSet.getString("status");
            }
        }
    }

    /**
     * Kiểm tra tài khoản tiếp nhận có tồn tại
     * và đang hoạt động hay không.
     */
    public boolean isActiveUser(
            Connection connection,
            long userId)
            throws SQLException {

        String sql = """
                SELECT 1
                FROM users
                WHERE id = ?
                  AND status = 'ACTIVE'
                LIMIT 1
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                return resultSet.next();
            }
        }
    }

    /**
     * Cập nhật trạng thái tài khoản.
     */
    public boolean updateUserStatus(
            Connection connection,
            long userId,
            String status)
            throws SQLException {

        String sql = """
                UPDATE users
                SET status = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setLong(2, userId);

            return statement.executeUpdate() == 1;
        }
    }

    /**
     * Ghi nhật ký khóa/mở khóa và người tiếp nhận.
     */
    public void insertHandoverLog(
            Connection connection,
            long sourceUserId,
            Long targetUserId,
            long performedBy,
            String action,
            String previousStatus,
            String newStatus,
            String note)
            throws SQLException {

        String sql = """
                INSERT INTO account_handover_logs (
                    source_user_id,
                    target_user_id,
                    performed_by,
                    action,
                    previous_status,
                    new_status,
                    note
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    sourceUserId
            );

            if (targetUserId == null) {
                statement.setNull(
                        2,
                        java.sql.Types.BIGINT
                );
            } else {
                statement.setLong(
                        2,
                        targetUserId
                );
            }

            statement.setLong(
                    3,
                    performedBy
            );

            statement.setString(
                    4,
                    action
            );

            statement.setString(
                    5,
                    previousStatus
            );

            statement.setString(
                    6,
                    newStatus
            );

            statement.setString(
                    7,
                    note
            );

            statement.executeUpdate();
        }
    }
}
