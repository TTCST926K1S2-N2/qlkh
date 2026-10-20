package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.UserDAO;
import vn.edu.ictu.qlkh.model.User;
import vn.edu.ictu.qlkh.util.PasswordUtil;

import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;

public class AuthService {

    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final int LOCK_MINUTES = 15;

    private static final String DUMMY_PASSWORD_HASH =
            PasswordUtil.hashPassword("dummy-password-for-timing-protection");

    private final UserDAO userDAO;
    private final Clock clock;

    public AuthService() {
        this(new UserDAO(), Clock.systemDefaultZone());
    }

    public AuthService(UserDAO userDAO, Clock clock) {
        this.userDAO = userDAO;
        this.clock = clock;
    }

    public LoginResult login(String email, String password)
            throws SQLException {

        String normalizedEmail = normalizeEmail(email);

        if (normalizedEmail == null
                || password == null
                || password.isBlank()) {

            return LoginResult.invalid();
        }

        User user = userDAO.findByEmail(normalizedEmail);

        /*
         * Vẫn thực hiện phép kiểm tra mật khẩu khi email không tồn tại
         * để giảm khác biệt thời gian phản hồi giữa:
         * - email không tồn tại
         * - mật khẩu không đúng
         */
        if (user == null) {
            PasswordUtil.verifyPassword(
                    password,
                    DUMMY_PASSWORD_HASH
            );

            return LoginResult.invalid();
        }

        LocalDateTime now = LocalDateTime.now(clock);

        /*
         * Tài khoản vẫn đang trong 15 phút khóa.
         * Không kiểm tra mật khẩu và không tiết lộ trạng thái tài khoản.
         */
        if (user.getLockedUntil() != null
                && now.isBefore(user.getLockedUntil())) {

            return LoginResult.invalid();
        }

        /*
         * Thời gian khóa đã hết:
         * bắt đầu lại chuỗi đăng nhập sai từ 0.
         */
        if (user.getLockedUntil() != null
                && !now.isBefore(user.getLockedUntil())) {

            userDAO.resetLoginFailures(user.getId());
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
        }

        /*
         * Tài khoản không ACTIVE cũng không được đăng nhập.
         * Vẫn trả cùng kết quả chung để không tiết lộ thông tin.
         */
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            PasswordUtil.verifyPassword(
                    password,
                    user.getPasswordHash()
            );

            return LoginResult.invalid();
        }

        boolean passwordCorrect =
                PasswordUtil.verifyPassword(
                        password,
                        user.getPasswordHash()
                );

        if (!passwordCorrect) {

            int failedAttempts =
                    user.getFailedLoginAttempts() + 1;

            LocalDateTime lockedUntil = null;

            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                lockedUntil =
                        now.plusMinutes(LOCK_MINUTES);
            }

            userDAO.updateLoginFailureState(
                    user.getId(),
                    failedAttempts,
                    lockedUntil
            );

            return LoginResult.invalid();
        }

        /*
         * Đăng nhập thành công:
         * xóa toàn bộ trạng thái đăng nhập sai trước đó.
         */
        userDAO.resetLoginFailures(user.getId());

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);

        return LoginResult.success(user);
    }

    private String normalizeEmail(String email) {

        if (email == null || email.isBlank()) {
            return null;
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    public static class LoginResult {

        private final boolean success;
        private final User user;

        private LoginResult(boolean success, User user) {
            this.success = success;
            this.user = user;
        }

        public static LoginResult success(User user) {
            return new LoginResult(true, user);
        }

        public static LoginResult invalid() {
            return new LoginResult(false, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public User getUser() {
            return user;
        }
    }
}