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
     * TÃ¬m ngÆ°á»i dÃ¹ng theo email.
     *
     * HTQLKH-1:
     * DÃ¹ng cho chá»©c nÄƒng Ä‘Äƒng nháº­p.
     *
     * HTQLKH-3:
     * DÃ¹ng Ä‘á»ƒ xÃ¡c Ä‘á»‹nh tÃ i khoáº£n khi ngÆ°á»i dÃ¹ng
     * yÃªu cáº§u Ä‘áº·t láº¡i máº­t kháº©u.
     */
    public User findByEmail(String email)
            throws SQLException {

        String sql = """
                SELECT id,
                       email,
                       password_hash,
                       full_name,
                       phone,
                       email_signature,
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
     * TÃ¬m ngÆ°á»i dÃ¹ng theo userId trong session.
     *
     * Khi ngÆ°á»i dÃ¹ng Ä‘Ã£ Ä‘Äƒng nháº­p,
     * LoginServlet lÆ°u userId vÃ o HttpSession.
     *
     * ChangePasswordService sá»­ dá»¥ng userId nÃ y
     * Ä‘á»ƒ láº¥y Ä‘Ãºng tÃ i khoáº£n Ä‘ang Ä‘á»•i máº­t kháº©u.
     */
    public User findById(long userId)
            throws SQLException {

        String sql = """
                SELECT id,
                       email,
                       password_hash,
                       full_name,
                       phone,
                       email_signature,
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
     * Cáº­p nháº­t sá»‘ láº§n Ä‘Äƒng nháº­p sai
     * vÃ  thá»i gian khÃ³a.
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
     * Reset tráº¡ng thÃ¡i Ä‘Äƒng nháº­p sai
     * sau khi Ä‘Äƒng nháº­p thÃ nh cÃ´ng.
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
     * Cáº­p nháº­t máº­t kháº©u má»›i cho ngÆ°á»i dÃ¹ng.
     *
     * Connection Ä‘Æ°á»£c truyá»n tá»« Service
     * Ä‘á»ƒ thao tÃ¡c cáº­p nháº­t cÃ³ thá»ƒ náº±m
     * trong transaction.
     *
     * Khi Ä‘á»•i máº­t kháº©u thÃ nh cÃ´ng:
     * - cáº­p nháº­t password_hash
     * - reset failed_login_attempts
     * - bá» tráº¡ng thÃ¡i khÃ³a tÃ i khoáº£n
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
     * Láº¥y danh sÃ¡ch tÃ i khoáº£n cÃ³ tÃ¬m kiáº¿m, lá»c vÃ  phÃ¢n trang.
     *
     * Schema hiá»‡n táº¡i chÆ°a cÃ³ quan há»‡ nhÃ³m kinh doanh.
     * Viá»‡c gÃ¡n/lÆ°u nhÃ³m thuá»™c HTQLKH-9 nÃªn khÃ´ng xá»­ lÃ½ group táº¡i Ä‘Ã¢y.
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
                       phone,
                       email_signature,
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
     * Äáº¿m sá»‘ tÃ i khoáº£n theo Ä‘iá»u kiá»‡n tÃ¬m kiáº¿m/lá»c.
     * DÃ¹ng Ä‘á»ƒ tÃ­nh tá»•ng sá»‘ trang.
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
     * Táº¡o tÃ i khoáº£n má»›i.
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
                        "KhÃ´ng thá»ƒ táº¡o tÃ i khoáº£n."
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
                "KhÃ´ng láº¥y Ä‘Æ°á»£c ID tÃ i khoáº£n vá»«a táº¡o."
        );
    }

    /**
     * HTQLKH-8:
     * Cáº­p nháº­t thÃ´ng tin cÆ¡ báº£n cá»§a tÃ i khoáº£n.
     *
     * KhÃ´ng cáº­p nháº­t password táº¡i Ä‘Ã¢y.
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
     * HTQLKH-44 / S2-02:
     * Cap nhat ho so ca nhan.
     *
     * Chi cho phep cap nhat:
     * - ho ten
     * - so dien thoai
     * - chu ky email
     *
     * Khong cap nhat email, role, status hoac nhom.
     */
    public boolean updateProfile(
            long userId,
            String fullName,
            String phone,
            String emailSignature)
            throws SQLException {

        String sql = """
                UPDATE users
                SET full_name = ?,
                    phone = ?,
                    email_signature = ?
                WHERE id = ?
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, fullName);
            statement.setString(2, phone);
            statement.setString(3, emailSignature);
            statement.setLong(4, userId);

            return statement.executeUpdate() == 1;
        }
    }
    /**
     * HTQLKH-8:
     * Kiá»ƒm tra email Ä‘Ã£ Ä‘Æ°á»£c tÃ i khoáº£n khÃ¡c sá»­ dá»¥ng hay chÆ°a.
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
     * GÃ¡n tham sá»‘ cho PreparedStatement.
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
     * Chuyá»ƒn dá»¯ liá»‡u ResultSet thÃ nh User.
     *
     * DÃ¹ng chung cho findByEmail()
     * vÃ  findById() Ä‘á»ƒ trÃ¡nh láº·p code.
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

        user.setPhone(
                resultSet.getString("phone")
        );

        user.setEmailSignature(
                resultSet.getString("email_signature")
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
