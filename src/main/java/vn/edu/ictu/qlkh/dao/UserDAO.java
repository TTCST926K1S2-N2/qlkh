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

    public User findByEmail(String email) throws SQLException {

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

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return null;
                }

                User user = new User();

                user.setId(resultSet.getLong("id"));
                user.setEmail(resultSet.getString("email"));
                user.setPasswordHash(resultSet.getString("password_hash"));
                user.setFullName(resultSet.getString("full_name"));
                user.setRole(resultSet.getString("role"));
                user.setStatus(resultSet.getString("status"));
                user.setFailedLoginAttempts(
                        resultSet.getInt("failed_login_attempts")
                );

                Timestamp lockedUntil =
                        resultSet.getTimestamp("locked_until");

                if (lockedUntil != null) {
                    user.setLockedUntil(
                            lockedUntil.toLocalDateTime()
                    );
                }

                return user;
            }
        }
    }

    public void updateLoginFailureState(
            long userId,
            int failedAttempts,
            LocalDateTime lockedUntil) throws SQLException {

        String sql = """
                UPDATE users
                SET failed_login_attempts = ?,
                    locked_until = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, failedAttempts);

            if (lockedUntil == null) {
                statement.setTimestamp(2, null);
            } else {
                statement.setTimestamp(
                        2,
                        Timestamp.valueOf(lockedUntil)
                );
            }

            statement.setLong(3, userId);
            statement.executeUpdate();
        }
    }

    public void resetLoginFailures(long userId) throws SQLException {

        String sql = """
                UPDATE users
                SET failed_login_attempts = 0,
                    locked_until = NULL
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);
            statement.executeUpdate();
        }
    }
}