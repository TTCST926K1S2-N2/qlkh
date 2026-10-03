package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.User;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class UserDAO {

    /**
     * Tìm người dùng theo email.
     *
     * HTQLKH-1:
     * Dùng cho chức năng đăng nhập.
     *
     * HTQLKH-3:
     * Dùng để xác định tài khoản khi người dùng
     * yêu cầu đặt lại mật khẩu.
     */
    public User findByEmail(String email)
            throws SQLException {

        String sql = """
                SELECT id,
                       email,
                       password_hash,
                       full_name,
                       role,
                       status,
                       failed_login_attempts,
                       locked_until
                FROM users
                WHERE LOWER(email) = LOWER(?)
                LIMIT 1
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    email
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (!resultSet.next()) {
                    return null;
                }

                return mapUser(resultSet);
            }
        }
    }

    /**
     * HTQLKH-4:
     * Tìm người dùng theo userId trong session.
     *
     * Khi người dùng đã đăng nhập,
     * LoginServlet lưu userId vào HttpSession.
     *
     * ChangePasswordService sử dụng userId này
     * để lấy đúng tài khoản đang đổi mật khẩu.
     */
    public User findById(long userId)
            throws SQLException {

        String sql = """
                SELECT id,
                       email,
                       password_hash,
                       full_name,
                       role,
                       status,
                       failed_login_attempts,
                       locked_until
                FROM users
                WHERE id = ?
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

                return mapUser(resultSet);
            }
        }
    }

    /**
     * HTQLKH-1:
     * Cập nhật số lần đăng nhập sai
     * và thời gian khóa.
     */
    public void updateLoginFailureState(
            long userId,
            int failedAttempts,
            LocalDateTime lockedUntil)
            throws SQLException {

        String sql = """
                UPDATE users
                SET failed_login_attempts = ?,
                    locked_until = ?
                WHERE id = ?
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    failedAttempts
            );

            if (lockedUntil == null) {

                statement.setTimestamp(
                        2,
                        null
                );

            } else {

                statement.setTimestamp(
                        2,
                        Timestamp.valueOf(
                                lockedUntil
                        )
                );
            }

            statement.setLong(
                    3,
                    userId
            );

            statement.executeUpdate();
        }
    }

    /**
     * HTQLKH-1:
     * Reset trạng thái đăng nhập sai
     * sau khi đăng nhập thành công.
     */
    public void resetLoginFailures(
            long userId)
            throws SQLException {

        String sql = """
                UPDATE users
                SET failed_login_attempts = 0,
                    locked_until = NULL
                WHERE id = ?
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
     * HTQLKH-3 + HTQLKH-4:
     * Cập nhật mật khẩu mới cho người dùng.
     *
     * Connection được truyền từ Service
     * để thao tác cập nhật có thể nằm
     * trong transaction.
     *
     * Khi đổi mật khẩu thành công:
     * - cập nhật password_hash
     * - reset failed_login_attempts
     * - bỏ trạng thái khóa tài khoản
     */
    public boolean updatePassword(
            Connection connection,
            long userId,
            String passwordHash)
            throws SQLException {

        String sql = """
                UPDATE users
                SET password_hash = ?,
                    failed_login_attempts = 0,
                    locked_until = NULL
                WHERE id = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    passwordHash
            );

            statement.setLong(
                    2,
                    userId
            );

            return statement.executeUpdate() == 1;
        }
    }

    /**
     * Chuyển dữ liệu ResultSet thành User.
     *
     * Dùng chung cho findByEmail()
     * và findById() để tránh lặp code.
     */
    private User mapUser(
            ResultSet resultSet)
            throws SQLException {

        User user =
                new User();

        user.setId(
                resultSet.getLong("id")
        );

        user.setEmail(
                resultSet.getString("email")
        );

        user.setPasswordHash(
                resultSet.getString(
                        "password_hash"
                )
        );

        user.setFullName(
                resultSet.getString(
                        "full_name"
                )
        );

        user.setRole(
                resultSet.getString("role")
        );

        user.setStatus(
                resultSet.getString("status")
        );

        user.setFailedLoginAttempts(
                resultSet.getInt(
                        "failed_login_attempts"
                )
        );

        Timestamp lockedUntil =
                resultSet.getTimestamp(
                        "locked_until"
                );

        if (lockedUntil != null) {

            user.setLockedUntil(
                    lockedUntil.toLocalDateTime()
            );
        }

        return user;
    }
}