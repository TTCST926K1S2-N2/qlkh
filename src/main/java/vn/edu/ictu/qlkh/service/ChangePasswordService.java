package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.UserDAO;
import vn.edu.ictu.qlkh.model.User;
import vn.edu.ictu.qlkh.util.DBConnection;
import vn.edu.ictu.qlkh.util.PasswordUtil;

import java.sql.Connection;
import java.sql.SQLException;

public class ChangePasswordService {

    private final UserDAO userDAO;

    public ChangePasswordService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Constructor phục vụ kiểm thử.
     */
    public ChangePasswordService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * HTQLKH-4:
     * Đổi mật khẩu cho người dùng đang đăng nhập.
     *
     * Yêu cầu Jira:
     * 1. Bắt buộc nhập mật khẩu hiện tại.
     * 2. Mật khẩu mới tối thiểu 8 ký tự,
     *    có chữ và số.
     *
     * Ngoài ra FE đã thống nhất:
     * - mật khẩu mới phải khác mật khẩu hiện tại;
     * - xác nhận mật khẩu phải trùng mật khẩu mới.
     */
    public ChangePasswordResult changePassword(
            long userId,
            String currentPassword,
            String newPassword,
            String confirmPassword)
            throws SQLException {

        /*
         * ==========================================
         * 1. KIỂM TRA MẬT KHẨU HIỆN TẠI
         * ==========================================
         */
        if (currentPassword == null
                || currentPassword.isBlank()) {

            return ChangePasswordResult.failure(
                    "Vui lòng nhập mật khẩu hiện tại."
            );
        }

        /*
         * ==========================================
         * 2. KIỂM TRA MẬT KHẨU MỚI
         * ==========================================
         */
        if (newPassword == null
                || newPassword.isBlank()) {

            return ChangePasswordResult.failure(
                    "Vui lòng nhập mật khẩu mới."
            );
        }

        /*
         * Jira HTQLKH-4:
         * tối thiểu 8 ký tự.
         */
        if (newPassword.length() < 8) {

            return ChangePasswordResult.failure(
                    "Mật khẩu mới phải có ít nhất 8 ký tự."
            );
        }

        /*
         * Jira HTQLKH-4:
         * phải có ít nhất một chữ cái.
         *
         * FE hiện tại cũng kiểm tra a-z, A-Z.
         */
        boolean hasLetter =
                newPassword.matches(
                        ".*[a-zA-Z].*"
                );

        if (!hasLetter) {

            return ChangePasswordResult.failure(
                    "Mật khẩu mới phải chứa ít nhất 1 chữ cái."
            );
        }

        /*
         * Jira HTQLKH-4:
         * phải có ít nhất một chữ số.
         */
        boolean hasNumber =
                newPassword.matches(
                        ".*[0-9].*"
                );

        if (!hasNumber) {

            return ChangePasswordResult.failure(
                    "Mật khẩu mới phải chứa ít nhất 1 chữ số."
            );
        }

        /*
         * ==========================================
         * 3. KIỂM TRA XÁC NHẬN MẬT KHẨU
         * ==========================================
         */
        if (confirmPassword == null
                || !newPassword.equals(confirmPassword)) {

            return ChangePasswordResult.failure(
                    "Mật khẩu xác nhận không trùng khớp."
            );
        }

        /*
         * Không cho mật khẩu mới giống
         * mật khẩu hiện tại.
         */
        if (newPassword.equals(currentPassword)) {

            return ChangePasswordResult.failure(
                    "Mật khẩu mới không được trùng với mật khẩu hiện tại."
            );
        }

        /*
         * ==========================================
         * 4. LẤY TÀI KHOẢN TỪ DATABASE
         * ==========================================
         */
        User user =
                userDAO.findById(userId);

        if (user == null) {

            return ChangePasswordResult.failure(
                    "Không tìm thấy tài khoản."
            );
        }

        /*
         * ==========================================
         * 5. XÁC THỰC MẬT KHẨU HIỆN TẠI
         * ==========================================
         */
        boolean currentPasswordCorrect =
                PasswordUtil.verifyPassword(
                        currentPassword,
                        user.getPasswordHash()
                );

        if (!currentPasswordCorrect) {

            return ChangePasswordResult.failure(
                    "Mật khẩu hiện tại không chính xác."
            );
        }

        /*
         * Kiểm tra thêm bằng hash hiện tại.
         *
         * Trường hợp hiếm:
         * mật khẩu mới có giá trị giống mật khẩu
         * đang lưu nhưng currentPassword được xử lý
         * khác ở phía client.
         */
        if (PasswordUtil.verifyPassword(
                newPassword,
                user.getPasswordHash())) {

            return ChangePasswordResult.failure(
                    "Mật khẩu mới không được trùng với mật khẩu hiện tại."
            );
        }

        /*
         * ==========================================
         * 6. HASH MẬT KHẨU MỚI
         * ==========================================
         */
        String newPasswordHash =
                PasswordUtil.hashPassword(
                        newPassword
                );

        /*
         * ==========================================
         * 7. CẬP NHẬT DATABASE
         * ==========================================
         */
        try (Connection connection =
                     DBConnection.getConnection()) {

            boolean originalAutoCommit =
                    connection.getAutoCommit();

            try {

                connection.setAutoCommit(false);

                boolean updated =
                        userDAO.updatePassword(
                                connection,
                                userId,
                                newPasswordHash
                        );

                if (!updated) {

                    connection.rollback();

                    return ChangePasswordResult.failure(
                            "Không thể cập nhật mật khẩu."
                    );
                }

                connection.commit();

                return ChangePasswordResult.success(
                        "Đổi mật khẩu thành công."
                );

            } catch (SQLException
                     | RuntimeException e) {

                try {
                    connection.rollback();
                } catch (SQLException ignored) {
                    // Giữ nguyên lỗi gốc.
                }

                throw e;

            } finally {

                try {
                    connection.setAutoCommit(
                            originalAutoCommit
                    );
                } catch (SQLException ignored) {
                    // Connection sẽ được đóng bởi try-with-resources.
                }
            }
        }
    }

    /**
     * Kết quả xử lý đổi mật khẩu.
     */
    public static class ChangePasswordResult {

        private final boolean success;
        private final String message;

        private ChangePasswordResult(
                boolean success,
                String message) {

            this.success = success;
            this.message = message;
        }

        public static ChangePasswordResult success(
                String message) {

            return new ChangePasswordResult(
                    true,
                    message
            );
        }

        public static ChangePasswordResult failure(
                String message) {

            return new ChangePasswordResult(
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