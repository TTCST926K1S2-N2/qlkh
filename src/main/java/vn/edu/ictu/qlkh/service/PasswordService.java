package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.PasswordResetTokenDAO;
import vn.edu.ictu.qlkh.dao.UserDAO;
import vn.edu.ictu.qlkh.model.PasswordResetToken;
import vn.edu.ictu.qlkh.model.User;
import vn.edu.ictu.qlkh.util.DBConnection;
import vn.edu.ictu.qlkh.util.PasswordUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

public class PasswordService {

    /*
     * Jira HTQLKH-3:
     * Link đặt lại mật khẩu có hiệu lực 30 phút.
     */
    public static final int TOKEN_EXPIRATION_MINUTES = 30;

    private static final int TOKEN_BYTES = 32;

    private final UserDAO userDAO;
    private final PasswordResetTokenDAO tokenDAO;
    private final Clock clock;
    private final SecureRandom secureRandom;

    public PasswordService() {
        this(
                new UserDAO(),
                new PasswordResetTokenDAO(),
                Clock.systemDefaultZone(),
                new SecureRandom()
        );
    }

    /*
     * Constructor phục vụ kiểm thử.
     */
    public PasswordService(
            UserDAO userDAO,
            PasswordResetTokenDAO tokenDAO,
            Clock clock,
            SecureRandom secureRandom) {

        this.userDAO = userDAO;
        this.tokenDAO = tokenDAO;
        this.clock = clock;
        this.secureRandom = secureRandom;
    }

    /**
     * Tạo yêu cầu đặt lại mật khẩu.
     *
     * Nếu email tồn tại:
     * - tạo token ngẫu nhiên
     * - token hết hạn sau 30 phút
     * - chỉ lưu SHA-256 của token vào DB
     * - vô hiệu hóa các token cũ chưa sử dụng
     *
     * Nếu email không tồn tại:
     * - trả về null
     *
     * Servlet KHÔNG được dùng kết quả này để hiển thị
     * thông báo khác nhau cho người dùng.
     */
    public String createResetToken(String email)
            throws SQLException {

        String normalizedEmail = normalizeEmail(email);

        if (normalizedEmail == null) {
            return null;
        }

        User user =
                userDAO.findByEmail(normalizedEmail);

        /*
         * Không tạo token nếu email không tồn tại.
         * ForgotPasswordServlet sau này vẫn phải trả về
         * cùng một thông báo chung.
         */
        if (user == null) {
            return null;
        }

        String rawToken = generateSecureToken();
        String tokenHash = hashToken(rawToken);

        LocalDateTime now =
                LocalDateTime.now(clock);

        LocalDateTime expiresAt =
                now.plusMinutes(
                        TOKEN_EXPIRATION_MINUTES
                );

        /*
         * Token cũ chưa sử dụng của tài khoản này
         * sẽ bị vô hiệu hóa trước khi tạo token mới.
         */
        tokenDAO.invalidateUnusedTokensForUser(
                user.getId(),
                now
        );

        tokenDAO.create(
                user.getId(),
                tokenHash,
                expiresAt
        );

        /*
         * Chỉ raw token này được dùng để tạo link gửi email.
         * Database chỉ lưu tokenHash.
         */
        return rawToken;
    }

    /**
     * Kiểm tra token có còn hợp lệ hay không.
     *
     * Dùng khi người dùng mở link reset-password.
     */
    public boolean isTokenValid(String rawToken)
            throws SQLException {

        if (rawToken == null
                || rawToken.isBlank()) {

            return false;
        }

        String tokenHash =
                hashToken(rawToken);

        Optional<PasswordResetToken> optionalToken =
                tokenDAO.findByTokenHash(tokenHash);

        if (optionalToken.isEmpty()) {
            return false;
        }

        PasswordResetToken token =
                optionalToken.get();

        LocalDateTime now =
                LocalDateTime.now(clock);

        return !token.isUsed()
                && !token.isExpired(now);
    }

    /**
     * Đặt mật khẩu mới.
     *
     * Toàn bộ quá trình:
     *
     * 1. Kiểm tra token
     * 2. Kiểm tra hết hạn
     * 3. Kiểm tra token đã dùng chưa
     * 4. Băm mật khẩu mới
     * 5. UPDATE mật khẩu
     * 6. Đánh dấu token đã sử dụng
     *
     * được xử lý an toàn trong transaction.
     */
    public ResetPasswordResult resetPassword(
            String rawToken,
            String newPassword,
            String confirmPassword)
            throws SQLException {

        if (rawToken == null
                || rawToken.isBlank()) {

            return ResetPasswordResult.invalidToken();
        }

        if (newPassword == null
                || newPassword.isBlank()) {

            return ResetPasswordResult.invalidPassword(
                    "Mật khẩu mới không được để trống."
            );
        }

        if (!newPassword.equals(confirmPassword)) {

            return ResetPasswordResult.invalidPassword(
                    "Mật khẩu xác nhận không khớp."
            );
        }

        String tokenHash =
                hashToken(rawToken);

        LocalDateTime now =
                LocalDateTime.now(clock);

        try (Connection connection =
                     DBConnection.getConnection()) {

            boolean originalAutoCommit =
                    connection.getAutoCommit();

            connection.setAutoCommit(false);

            try {

                Optional<PasswordResetToken> optionalToken =
                        tokenDAO.findByTokenHash(
                                connection,
                                tokenHash
                        );

                if (optionalToken.isEmpty()) {
                    connection.rollback();

                    return ResetPasswordResult.invalidToken();
                }

                PasswordResetToken token =
                        optionalToken.get();

                /*
                 * Token chỉ dùng được một lần.
                 */
                if (token.isUsed()) {
                    connection.rollback();

                    return ResetPasswordResult.invalidToken();
                }

                /*
                 * Token chỉ có hiệu lực trong 30 phút.
                 */
                if (token.isExpired(now)) {
                    connection.rollback();

                    return ResetPasswordResult.invalidToken();
                }

                String newPasswordHash =
                        PasswordUtil.hashPassword(
                                newPassword
                        );

                boolean passwordUpdated =
                        userDAO.updatePassword(
                                connection,
                                token.getUserId(),
                                newPasswordHash
                        );

                if (!passwordUpdated) {
                    connection.rollback();

                    return ResetPasswordResult.failed(
                            "Không thể cập nhật mật khẩu."
                    );
                }

                /*
                 * WHERE used_at IS NULL trong DAO giúp
                 * ngăn token được sử dụng lại.
                 */
                boolean tokenMarkedAsUsed =
                        tokenDAO.markAsUsed(
                                connection,
                                token.getId(),
                                now
                        );

                if (!tokenMarkedAsUsed) {
                    connection.rollback();

                    return ResetPasswordResult.invalidToken();
                }

                connection.commit();

                return ResetPasswordResult.success();

            } catch (SQLException
                     | RuntimeException e) {

                connection.rollback();
                throw e;

            } finally {

                try {
                    connection.setAutoCommit(
                            originalAutoCommit
                    );
                } catch (SQLException ignored) {
                    // Connection sắp được đóng.
                }
            }
        }
    }

    /**
     * Sinh token bằng SecureRandom.
     */
    private String generateSecureToken() {

        byte[] bytes =
                new byte[TOKEN_BYTES];

        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    /**
     * Database không lưu raw token.
     * Chỉ lưu SHA-256 của token.
     */
    public String hashToken(String rawToken) {

        if (rawToken == null) {
            throw new IllegalArgumentException(
                    "Token không được null."
            );
        }

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            rawToken.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat.of()
                    .formatHex(hash);

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "Không thể băm reset token.",
                    e
            );
        }
    }

    private String normalizeEmail(String email) {

        if (email == null) {
            return null;
        }

        String normalized =
                email.trim()
                        .toLowerCase();

        if (normalized.isBlank()) {
            return null;
        }

        return normalized;
    }

    /**
     * Kết quả reset mật khẩu.
     */
    public static final class ResetPasswordResult {

        private final boolean success;
        private final String message;

        private ResetPasswordResult(
                boolean success,
                String message) {

            this.success = success;
            this.message = message;
        }

        public static ResetPasswordResult success() {

            return new ResetPasswordResult(
                    true,
                    "Đặt lại mật khẩu thành công."
            );
        }

        public static ResetPasswordResult invalidToken() {

            return new ResetPasswordResult(
                    false,
                    "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn."
            );
        }

        public static ResetPasswordResult invalidPassword(
                String message) {

            return new ResetPasswordResult(
                    false,
                    message
            );
        }

        public static ResetPasswordResult failed(
                String message) {

            return new ResetPasswordResult(
                    false,
                    message
            );
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}