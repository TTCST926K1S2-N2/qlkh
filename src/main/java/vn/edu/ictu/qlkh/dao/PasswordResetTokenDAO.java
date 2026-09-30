package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.PasswordResetToken;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

public class PasswordResetTokenDAO {

    public void create(
            long userId,
            String tokenHash,
            LocalDateTime expiresAt)
            throws SQLException {

        try (Connection connection =
                     DBConnection.getConnection()) {

            create(
                    connection,
                    userId,
                    tokenHash,
                    expiresAt
            );
        }
    }

    public void create(
            Connection connection,
            long userId,
            String tokenHash,
            LocalDateTime expiresAt)
            throws SQLException {

        String sql = """
                INSERT INTO password_reset_tokens
                    (user_id, token_hash, expires_at)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, userId);
            statement.setString(2, tokenHash);
            statement.setTimestamp(
                    3,
                    Timestamp.valueOf(expiresAt)
            );

            statement.executeUpdate();
        }
    }

    public Optional<PasswordResetToken> findByTokenHash(
            String tokenHash)
            throws SQLException {

        try (Connection connection =
                     DBConnection.getConnection()) {

            return findByTokenHash(
                    connection,
                    tokenHash
            );
        }
    }

    public Optional<PasswordResetToken> findByTokenHash(
            Connection connection,
            String tokenHash)
            throws SQLException {

        String sql = """
                SELECT
                    id,
                    user_id,
                    token_hash,
                    expires_at,
                    used_at,
                    created_at
                FROM password_reset_tokens
                WHERE token_hash = ?
                LIMIT 1
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, tokenHash);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(
                        mapToken(resultSet)
                );
            }
        }
    }

    public boolean markAsUsed(
            long tokenId,
            LocalDateTime usedAt)
            throws SQLException {

        try (Connection connection =
                     DBConnection.getConnection()) {

            return markAsUsed(
                    connection,
                    tokenId,
                    usedAt
            );
        }
    }

    public boolean markAsUsed(
            Connection connection,
            long tokenId,
            LocalDateTime usedAt)
            throws SQLException {

        String sql = """
                UPDATE password_reset_tokens
                SET used_at = ?
                WHERE id = ?
                  AND used_at IS NULL
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setTimestamp(
                    1,
                    Timestamp.valueOf(usedAt)
            );

            statement.setLong(
                    2,
                    tokenId
            );

            return statement.executeUpdate() == 1;
        }
    }

    public void invalidateUnusedTokensForUser(
            long userId,
            LocalDateTime usedAt)
            throws SQLException {

        try (Connection connection =
                     DBConnection.getConnection()) {

            invalidateUnusedTokensForUser(
                    connection,
                    userId,
                    usedAt
            );
        }
    }

    public void invalidateUnusedTokensForUser(
            Connection connection,
            long userId,
            LocalDateTime usedAt)
            throws SQLException {

        String sql = """
                UPDATE password_reset_tokens
                SET used_at = ?
                WHERE user_id = ?
                  AND used_at IS NULL
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setTimestamp(
                    1,
                    Timestamp.valueOf(usedAt)
            );

            statement.setLong(
                    2,
                    userId
            );

            statement.executeUpdate();
        }
    }

    private PasswordResetToken mapToken(
            ResultSet resultSet)
            throws SQLException {

        PasswordResetToken token =
                new PasswordResetToken();

        token.setId(
                resultSet.getLong("id")
        );

        token.setUserId(
                resultSet.getLong("user_id")
        );

        token.setTokenHash(
                resultSet.getString("token_hash")
        );

        Timestamp expiresAt =
                resultSet.getTimestamp("expires_at");

        if (expiresAt != null) {
            token.setExpiresAt(
                    expiresAt.toLocalDateTime()
            );
        }

        Timestamp usedAt =
                resultSet.getTimestamp("used_at");

        if (usedAt != null) {
            token.setUsedAt(
                    usedAt.toLocalDateTime()
            );
        }

        Timestamp createdAt =
                resultSet.getTimestamp("created_at");

        if (createdAt != null) {
            token.setCreatedAt(
                    createdAt.toLocalDateTime()
            );
        }

        return token;
    }
}