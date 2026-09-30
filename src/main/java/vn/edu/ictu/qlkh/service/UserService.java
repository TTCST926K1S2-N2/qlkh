package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.UserDAO;
import vn.edu.ictu.qlkh.model.User;
import vn.edu.ictu.qlkh.util.PasswordUtil;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * Nghiệp vụ quản lý tài khoản người dùng - HTQLKH-8.
 */
public class UserService {

    public static final int DEFAULT_PAGE_SIZE = 20;

    private static final Set<String> VALID_ROLES =
            Set.of("ADMIN", "MANAGER", "SALES");

    private static final Set<String> VALID_STATUSES =
            Set.of("ACTIVE", "INACTIVE");

    private static final String TEMP_PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZ"
                    + "abcdefghijkmnopqrstuvwxyz"
                    + "23456789"
                    + "!@#$%";

    private static final int TEMP_PASSWORD_LENGTH = 12;

    private final UserDAO userDAO;
    private final SecureRandom secureRandom;

    public UserService() {
        this(
                new UserDAO(),
                new SecureRandom()
        );
    }

    UserService(
            UserDAO userDAO,
            SecureRandom secureRandom) {

        this.userDAO = userDAO;
        this.secureRandom = secureRandom;
    }

    /**
     * Lấy danh sách tài khoản có tìm kiếm, lọc và phân trang.
     */
    public UserPage getUsers(
            String keyword,
            String role,
            String status,
            int page)
            throws SQLException {

        int safePage = Math.max(page, 1);

        String normalizedRole =
                normalizeOptionalFilter(role);

        String normalizedStatus =
                normalizeOptionalFilter(status);

        if (normalizedRole != null
                && !VALID_ROLES.contains(normalizedRole)) {

            normalizedRole = null;
        }

        if (normalizedStatus != null
                && !VALID_STATUSES.contains(normalizedStatus)) {

            normalizedStatus = null;
        }

        int totalItems =
                userDAO.countUsers(
                        normalizeKeyword(keyword),
                        normalizedRole,
                        normalizedStatus
                );

        int totalPages =
                Math.max(
                        1,
                        (int) Math.ceil(
                                totalItems
                                        / (double) DEFAULT_PAGE_SIZE
                        )
                );

        if (safePage > totalPages) {
            safePage = totalPages;
        }

        List<User> users =
                userDAO.findUsers(
                        normalizeKeyword(keyword),
                        normalizedRole,
                        normalizedStatus,
                        safePage,
                        DEFAULT_PAGE_SIZE
                );

        return new UserPage(
                users,
                safePage,
                totalPages,
                totalItems,
                DEFAULT_PAGE_SIZE
        );
    }

    /**
     * Lấy tài khoản theo ID để hiển thị form sửa.
     */
    public User getUserById(long userId)
            throws SQLException {

        if (userId <= 0) {
            return null;
        }

        return userDAO.findById(userId);
    }

    /**
     * Tạo tài khoản mới.
     *
     * Hệ thống tự sinh mật khẩu tạm và chỉ lưu bản hash PBKDF2.
     * Mật khẩu tạm được trả về để bước gửi email sử dụng.
     */
    public CreateUserResult createUser(
            String fullName,
            String email,
            String role,
            String status)
            throws SQLException {

        String normalizedFullName =
                normalizeRequired(fullName);

        String normalizedEmail =
                normalizeEmail(email);

        String normalizedRole =
                normalizeRequired(role).toUpperCase();

        String normalizedStatus =
                normalizeRequired(status).toUpperCase();

        String validationMessage =
                validateUserInput(
                        normalizedFullName,
                        normalizedEmail,
                        normalizedRole,
                        normalizedStatus
                );

        if (validationMessage != null) {
            return CreateUserResult.failed(
                    validationMessage
            );
        }

        if (userDAO.existsByEmail(
                normalizedEmail,
                null)) {

            return CreateUserResult.failed(
                    "Email đã tồn tại trong hệ thống."
            );
        }

        String temporaryPassword =
                generateTemporaryPassword();

        User user =
                new User();

        user.setFullName(
                normalizedFullName
        );

        user.setEmail(
                normalizedEmail
        );

        user.setRole(
                normalizedRole
        );

        user.setStatus(
                normalizedStatus
        );

        user.setPasswordHash(
                PasswordUtil.hashPassword(
                        temporaryPassword
                )
        );

        long userId =
                userDAO.createUser(user);

        return CreateUserResult.success(
                userId,
                temporaryPassword
        );
    }

    /**
     * Cập nhật thông tin tài khoản.
     */
    public UpdateUserResult updateUser(
            long userId,
            String fullName,
            String email,
            String role,
            String status)
            throws SQLException {

        if (userId <= 0) {
            return UpdateUserResult.failed(
                    "Tài khoản không hợp lệ."
            );
        }

        User existingUser =
                userDAO.findById(userId);

        if (existingUser == null) {
            return UpdateUserResult.failed(
                    "Không tìm thấy tài khoản."
            );
        }

        String normalizedFullName =
                normalizeRequired(fullName);

        String normalizedEmail =
                normalizeEmail(email);

        String normalizedRole =
                normalizeRequired(role).toUpperCase();

        String normalizedStatus =
                normalizeRequired(status).toUpperCase();

        String validationMessage =
                validateUserInput(
                        normalizedFullName,
                        normalizedEmail,
                        normalizedRole,
                        normalizedStatus
                );

        if (validationMessage != null) {
            return UpdateUserResult.failed(
                    validationMessage
            );
        }

        if (userDAO.existsByEmail(
                normalizedEmail,
                userId)) {

            return UpdateUserResult.failed(
                    "Email đã được sử dụng bởi tài khoản khác."
            );
        }

        existingUser.setFullName(
                normalizedFullName
        );

        existingUser.setEmail(
                normalizedEmail
        );

        existingUser.setRole(
                normalizedRole
        );

        existingUser.setStatus(
                normalizedStatus
        );

        boolean updated =
                userDAO.updateUser(
                        existingUser
                );

        if (!updated) {
            return UpdateUserResult.failed(
                    "Không thể cập nhật tài khoản."
            );
        }

        return UpdateUserResult.success();
    }

    /**
     * Validate dữ liệu tạo/sửa tài khoản.
     */
    private String validateUserInput(
            String fullName,
            String email,
            String role,
            String status) {

        if (fullName.isBlank()) {
            return "Họ tên không được để trống.";
        }

        if (fullName.length() > 255) {
            return "Họ tên không được vượt quá 255 ký tự.";
        }

        if (email.isBlank()) {
            return "Email không được để trống.";
        }

        if (email.length() > 255) {
            return "Email không được vượt quá 255 ký tự.";
        }

        if (!isValidEmail(email)) {
            return "Email không đúng định dạng.";
        }

        if (!VALID_ROLES.contains(role)) {
            return "Vai trò không hợp lệ.";
        }

        if (!VALID_STATUSES.contains(status)) {
            return "Trạng thái tài khoản không hợp lệ.";
        }

        return null;
    }

    private boolean isValidEmail(
            String email) {

        int atIndex =
                email.indexOf('@');

        int lastAtIndex =
                email.lastIndexOf('@');

        if (atIndex <= 0
                || atIndex != lastAtIndex) {

            return false;
        }

        int dotIndex =
                email.lastIndexOf('.');

        return dotIndex > atIndex + 1
                && dotIndex < email.length() - 1;
    }

    /**
     * Sinh mật khẩu tạm bằng SecureRandom.
     */
    private String generateTemporaryPassword() {

        StringBuilder password =
                new StringBuilder(
                        TEMP_PASSWORD_LENGTH
                );

        for (int i = 0;
             i < TEMP_PASSWORD_LENGTH;
             i++) {

            int index =
                    secureRandom.nextInt(
                            TEMP_PASSWORD_CHARS.length()
                    );

            password.append(
                    TEMP_PASSWORD_CHARS.charAt(index)
            );
        }

        return password.toString();
    }

    private String normalizeRequired(
            String value) {

        return value == null
                ? ""
                : value.trim();
    }

    private String normalizeEmail(
            String email) {

        return normalizeRequired(email)
                .toLowerCase();
    }

    private String normalizeKeyword(
            String keyword) {

        if (keyword == null
                || keyword.isBlank()) {

            return null;
        }

        return keyword.trim();
    }

    private String normalizeOptionalFilter(
            String value) {

        if (value == null
                || value.isBlank()) {

            return null;
        }

        return value.trim()
                .toUpperCase();
    }

    /**
     * Kết quả phân trang.
     */
    public static final class UserPage {

        private final List<User> users;
        private final int currentPage;
        private final int totalPages;
        private final int totalItems;
        private final int pageSize;

        public UserPage(
                List<User> users,
                int currentPage,
                int totalPages,
                int totalItems,
                int pageSize) {

            this.users = users;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.totalItems = totalItems;
            this.pageSize = pageSize;
        }

        public List<User> getUsers() {
            return users;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public int getTotalItems() {
            return totalItems;
        }

        public int getPageSize() {
            return pageSize;
        }
    }

    /**
     * Kết quả tạo tài khoản.
     */
    public static final class CreateUserResult {

        private final boolean success;
        private final String message;
        private final Long userId;
        private final String temporaryPassword;

        private CreateUserResult(
                boolean success,
                String message,
                Long userId,
                String temporaryPassword) {

            this.success = success;
            this.message = message;
            this.userId = userId;
            this.temporaryPassword =
                    temporaryPassword;
        }

        public static CreateUserResult success(
                long userId,
                String temporaryPassword) {

            return new CreateUserResult(
                    true,
                    null,
                    userId,
                    temporaryPassword
            );
        }

        public static CreateUserResult failed(
                String message) {

            return new CreateUserResult(
                    false,
                    message,
                    null,
                    null
            );
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Long getUserId() {
            return userId;
        }

        public String getTemporaryPassword() {
            return temporaryPassword;
        }
    }

    /**
     * Kết quả cập nhật tài khoản.
     */
    public static final class UpdateUserResult {

        private final boolean success;
        private final String message;

        private UpdateUserResult(
                boolean success,
                String message) {

            this.success = success;
            this.message = message;
        }

        public static UpdateUserResult success() {
            return new UpdateUserResult(
                    true,
                    null
            );
        }

        public static UpdateUserResult failed(
                String message) {

            return new UpdateUserResult(
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