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
     * HTQLKH-8:
     * Lấy danh sách tài khoản có tìm kiếm, lọc và phân trang.
     *
     * Schema hiện tại chưa có quan hệ nhóm kinh doanh.
     * Việc gán/lưu nhóm thuộc HTQLKH-9 nên không xử lý group tại đây.
     */
    public java.util.List<User> findUsers(
            String keyword,
            String role,
            String status,
            int page,
            int pageSize)
            throws SQLException {

        java.util.List<User> users =
                new java.util.ArrayList<>();

        int safePage = Math.max(page, 1);
        int safePageSize = pageSize > 0 ? pageSize : 20;
        int offset = (safePage - 1) * safePageSize;

        StringBuilder sql = new StringBuilder("""
                SELECT id,
                       email,
                       password_hash,
                       full_name,
                       role,
                       status,
                       failed_login_attempts,
                       locked_until
                FROM users
                WHERE 1 = 1
                """);

        java.util.List<Object> parameters =
                new java.util.ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append("""

                    AND (
                        LOWER(full_name) LIKE LOWER(?)
                        OR LOWER(email) LIKE LOWER(?)
                    )
                    """);

            String searchValue =
                    "%" + keyword.trim() + "%";

            parameters.add(searchValue);
            parameters.add(searchValue);
        }

        if (role != null && !role.isBlank()) {
            sql.append("""

                    AND UPPER(role) = UPPER(?)
                    """);

            parameters.add(role.trim());
        }

        if (status != null && !status.isBlank()) {
            sql.append("""

                    AND UPPER(status) = UPPER(?)
                    """);

            parameters.add(status.trim());
        }

        sql.append("""

                ORDER BY id DESC
                LIMIT ? OFFSET ?
                """);

        parameters.add(safePageSize);
        parameters.add(offset);

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(
                             sql.toString())) {

            setParameters(statement, parameters);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    users.add(mapUser(resultSet));
                }
            }
        }

        return users;
    }

    /**
     * HTQLKH-8:
     * Đếm số tài khoản theo điều kiện tìm kiếm/lọc.
     * Dùng để tính tổng số trang.
     */
    public int countUsers(
            String keyword,
            String role,
            String status)
            throws SQLException {

        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM users
                WHERE 1 = 1
                """);

        java.util.List<Object> parameters =
                new java.util.ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append("""

                    AND (
                        LOWER(full_name) LIKE LOWER(?)
                        OR LOWER(email) LIKE LOWER(?)
                    )
                    """);

            String searchValue =
                    "%" + keyword.trim() + "%";

            parameters.add(searchValue);
            parameters.add(searchValue);
        }

        if (role != null && !role.isBlank()) {
            sql.append("""

                    AND UPPER(role) = UPPER(?)
                    """);

            parameters.add(role.trim());
        }

        if (status != null && !status.isBlank()) {
            sql.append("""

                    AND UPPER(status) = UPPER(?)
                    """);

            parameters.add(status.trim());
        }

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(
                             sql.toString())) {

            setParameters(statement, parameters);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }

        return 0;
    }

    /**
     * HTQLKH-8:
     * Tạo tài khoản mới.
     */
    public long createUser(User user)
            throws SQLException {

        String sql = """
                INSERT INTO users (
                    email,
                    password_hash,
                    full_name,
                    role,
                    status,
                    failed_login_attempts,
                    locked_until
                )
                VALUES (?, ?, ?, ?, ?, 0, NULL)
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             java.sql.Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(
                    1,
                    user.getEmail()
            );

            statement.setString(
                    2,
                    user.getPasswordHash()
            );

            statement.setString(
                    3,
                    user.getFullName()
            );

            statement.setString(
                    4,
                    user.getRole()
            );

            statement.setString(
                    5,
                    user.getStatus()
            );

            int affectedRows =
                    statement.executeUpdate();

            if (affectedRows != 1) {
                throw new SQLException(
                        "Không thể tạo tài khoản."
                );
            }

            try (ResultSet generatedKeys =
                         statement.getGeneratedKeys()) {

                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
        }

        throw new SQLException(
                "Không lấy được ID tài khoản vừa tạo."
        );
    }

    /**
     * HTQLKH-8:
     * Cập nhật thông tin cơ bản của tài khoản.
     *
     * Không cập nhật password tại đây.
     */
    public boolean updateUser(User user)
            throws SQLException {

        String sql = """
                UPDATE users
                SET email = ?,
                    full_name = ?,
                    role = ?,
                    status = ?
                WHERE id = ?
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    user.getEmail()
            );

            statement.setString(
                    2,
                    user.getFullName()
            );

            statement.setString(
                    3,
                    user.getRole()
            );

            statement.setString(
                    4,
                    user.getStatus()
            );

            statement.setLong(
                    5,
                    user.getId()
            );

            return statement.executeUpdate() == 1;
        }
    }

    /**
     * HTQLKH-8:
     * Kiểm tra email đã được tài khoản khác sử dụng hay chưa.
     */
    public boolean existsByEmail(
            String email,
            Long excludeUserId)
            throws SQLException {

        String sql;

        if (excludeUserId == null) {

            sql = """
                    SELECT 1
                    FROM users
                    WHERE LOWER(email) = LOWER(?)
                    LIMIT 1
                    """;

        } else {

            sql = """
                    SELECT 1
                    FROM users
                    WHERE LOWER(email) = LOWER(?)
                      AND id <> ?
                    LIMIT 1
                    """;
        }

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    email
            );

            if (excludeUserId != null) {
                statement.setLong(
                        2,
                        excludeUserId
                );
            }

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                return resultSet.next();
            }
        }
    }

    /**
     * Gán tham số cho PreparedStatement.
     */
    private void setParameters(
            PreparedStatement statement,
            java.util.List<Object> parameters)
            throws SQLException {

        for (int i = 0;
             i < parameters.size();
             i++) {

            Object value =
                    parameters.get(i);

            if (value instanceof Integer integerValue) {

                statement.setInt(
                        i + 1,
                        integerValue
                );

            } else {

                statement.setString(
                        i + 1,
                        String.valueOf(value)
                );
            }
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