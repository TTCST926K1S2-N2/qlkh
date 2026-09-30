package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.edu.ictu.qlkh.dao.UserDAO;
import vn.edu.ictu.qlkh.model.User;

import java.security.SecureRandom;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử nghiệp vụ quản lý tài khoản - HTQLKH-8.
 */
class UserServiceTest {

    private FakeUserDAO userDAO;
    private UserService userService;

    @BeforeEach
    void setUp() {

        userDAO =
                new FakeUserDAO();

        userService =
                new UserService(
                        userDAO,
                        new FixedSecureRandom()
                );
    }

    /**
     * Tạo tài khoản thành công.
     *
     * Kiểm tra:
     * - Có userId.
     * - Có mật khẩu tạm.
     * - Mật khẩu tạm dài 12 ký tự.
     * - DB chỉ nhận password hash.
     */
    @Test
    void createUserSuccess() throws Exception {

        userDAO.emailExists = false;
        userDAO.generatedUserId = 100L;

        UserService.CreateUserResult result =
                userService.createUser(
                        "Nguyen Van A",
                        "TEST@EXAMPLE.COM ",
                        "sales",
                        "active"
                );

        assertTrue(
                result.isSuccess()
        );

        assertEquals(
                100L,
                result.getUserId()
        );

        assertNotNull(
                result.getTemporaryPassword()
        );

        assertEquals(
                12,
                result.getTemporaryPassword()
                        .length()
        );

        assertNotNull(
                userDAO.createdUser
        );

        /*
         * Email phải được chuẩn hóa về chữ thường.
         */
        assertEquals(
                "test@example.com",
                userDAO.createdUser.getEmail()
        );

        assertEquals(
                "Nguyen Van A",
                userDAO.createdUser.getFullName()
        );

        assertEquals(
                "SALES",
                userDAO.createdUser.getRole()
        );

        assertEquals(
                "ACTIVE",
                userDAO.createdUser.getStatus()
        );

        /*
         * Không được lưu mật khẩu tạm dạng plain text.
         */
        assertNotNull(
                userDAO.createdUser.getPasswordHash()
        );

        assertFalse(
                userDAO.createdUser
                        .getPasswordHash()
                        .isBlank()
        );

        assertNotEquals(
                result.getTemporaryPassword(),
                userDAO.createdUser
                        .getPasswordHash()
        );

        assertEquals(
                1,
                userDAO.createUserCallCount
        );
    }

    /**
     * Email đã tồn tại phải bị từ chối.
     */
    @Test
    void createUserRejectsDuplicateEmail()
            throws Exception {

        userDAO.emailExists = true;

        UserService.CreateUserResult result =
                userService.createUser(
                        "Nguyen Van B",
                        "duplicate@example.com",
                        "SALES",
                        "ACTIVE"
                );

        assertFalse(
                result.isSuccess()
        );

        assertNotNull(
                result.getMessage()
        );

        assertNull(
                result.getUserId()
        );

        assertNull(
                result.getTemporaryPassword()
        );

        /*
         * Không được INSERT khi email bị trùng.
         */
        assertEquals(
                0,
                userDAO.createUserCallCount
        );
    }

    /**
     * Email sai định dạng phải bị từ chối
     * trước khi ghi database.
     */
    @Test
    void createUserRejectsInvalidEmail()
            throws Exception {

        UserService.CreateUserResult result =
                userService.createUser(
                        "Nguyen Van C",
                        "email-khong-hop-le",
                        "SALES",
                        "ACTIVE"
                );

        assertFalse(
                result.isSuccess()
        );

        assertNotNull(
                result.getMessage()
        );

        assertEquals(
                0,
                userDAO.createUserCallCount
        );
    }

    /**
     * Role không hợp lệ phải bị từ chối.
     */
    @Test
    void createUserRejectsInvalidRole()
            throws Exception {

        UserService.CreateUserResult result =
                userService.createUser(
                        "Nguyen Van D",
                        "d@example.com",
                        "SUPER_ADMIN",
                        "ACTIVE"
                );

        assertFalse(
                result.isSuccess()
        );

        assertNotNull(
                result.getMessage()
        );

        assertEquals(
                0,
                userDAO.createUserCallCount
        );
    }

    /**
     * Status không hợp lệ phải bị từ chối.
     */
    @Test
    void createUserRejectsInvalidStatus()
            throws Exception {

        UserService.CreateUserResult result =
                userService.createUser(
                        "Nguyen Van E",
                        "e@example.com",
                        "SALES",
                        "DELETED"
                );

        assertFalse(
                result.isSuccess()
        );

        assertNotNull(
                result.getMessage()
        );

        assertEquals(
                0,
                userDAO.createUserCallCount
        );
    }

    /**
     * Cập nhật tài khoản thành công.
     */
    @Test
    void updateUserSuccess()
            throws Exception {

        User existingUser =
                new User();

        existingUser.setFullName(
                "Old Name"
        );

        existingUser.setEmail(
                "old@example.com"
        );

        existingUser.setRole(
                "SALES"
        );

        existingUser.setStatus(
                "ACTIVE"
        );

        userDAO.userById =
                existingUser;

        userDAO.emailExists =
                false;

        userDAO.updateResult =
                true;

        UserService.UpdateUserResult result =
                userService.updateUser(
                        10L,
                        "  New Name  ",
                        " NEW@EXAMPLE.COM ",
                        "manager",
                        "inactive"
                );

        assertTrue(
                result.isSuccess()
        );

        assertNotNull(
                userDAO.updatedUser
        );

        assertEquals(
                "New Name",
                userDAO.updatedUser
                        .getFullName()
        );

        assertEquals(
                "new@example.com",
                userDAO.updatedUser
                        .getEmail()
        );

        assertEquals(
                "MANAGER",
                userDAO.updatedUser
                        .getRole()
        );

        assertEquals(
                "INACTIVE",
                userDAO.updatedUser
                        .getStatus()
        );

        assertEquals(
                1,
                userDAO.updateUserCallCount
        );
    }

    /**
     * Không cho cập nhật sang email
     * đang thuộc tài khoản khác.
     */
    @Test
    void updateUserRejectsDuplicateEmail()
            throws Exception {

        User existingUser =
                new User();

        existingUser.setFullName(
                "Existing User"
        );

        existingUser.setEmail(
                "existing@example.com"
        );

        existingUser.setRole(
                "SALES"
        );

        existingUser.setStatus(
                "ACTIVE"
        );

        userDAO.userById =
                existingUser;

        userDAO.emailExists =
                true;

        UserService.UpdateUserResult result =
                userService.updateUser(
                        10L,
                        "Existing User",
                        "used@example.com",
                        "SALES",
                        "ACTIVE"
                );

        assertFalse(
                result.isSuccess()
        );

        assertNotNull(
                result.getMessage()
        );

        assertEquals(
                0,
                userDAO.updateUserCallCount
        );
    }

    /**
     * ID không hợp lệ phải bị từ chối.
     */
    @Test
    void updateUserRejectsInvalidId()
            throws Exception {

        UserService.UpdateUserResult result =
                userService.updateUser(
                        0L,
                        "Test User",
                        "test@example.com",
                        "SALES",
                        "ACTIVE"
                );

        assertFalse(
                result.isSuccess()
        );

        assertNotNull(
                result.getMessage()
        );

        assertEquals(
                0,
                userDAO.findByIdCallCount
        );

        assertEquals(
                0,
                userDAO.updateUserCallCount
        );
    }

    /**
     * DAO giả để test UserService
     * mà không truy cập MySQL thật.
     */
    private static class FakeUserDAO
            extends UserDAO {

        private boolean emailExists;
        private long generatedUserId = 1L;
        private boolean updateResult = true;

        private User userById;
        private User createdUser;
        private User updatedUser;

        private int createUserCallCount;
        private int updateUserCallCount;
        private int findByIdCallCount;

        @Override
        public boolean existsByEmail(
                String email,
                Long excludeUserId)
                throws SQLException {

            return emailExists;
        }

        @Override
        public long createUser(
                User user)
                throws SQLException {

            createUserCallCount++;

            createdUser =
                    user;

            return generatedUserId;
        }

        @Override
        public User findById(
                long userId)
                throws SQLException {

            findByIdCallCount++;

            return userById;
        }

        @Override
        public boolean updateUser(
                User user)
                throws SQLException {

            updateUserCallCount++;

            updatedUser =
                    user;

            return updateResult;
        }
    }

    /**
     * SecureRandom cố định để test
     * không phụ thuộc dữ liệu ngẫu nhiên.
     */
    private static class FixedSecureRandom
            extends SecureRandom {

        @Override
        public int nextInt(
                int bound) {

            return 0;
        }
    }
}