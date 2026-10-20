package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.edu.ictu.qlkh.dao.UserDAO;
import vn.edu.ictu.qlkh.model.User;

import java.security.SecureRandom;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class ProfileUserServiceTest {

    private FakeUserDAO userDAO;
    private UserService userService;
    private User existingUser;

    @BeforeEach
    void setUp() {

        userDAO = new FakeUserDAO();

        userService = new UserService(
                userDAO,
                new SecureRandom()
        );

        existingUser = new User();
        existingUser.setId(10L);
        existingUser.setEmail("user@example.com");
        existingUser.setFullName("Nguyen Van A");
        existingUser.setRole("SALES");
        existingUser.setStatus("ACTIVE");

        userDAO.userById = existingUser;
    }

    @Test
    void getProfileSuccess() throws Exception {

        User result =
                userService.getProfile(10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(
                "user@example.com",
                result.getEmail()
        );
        assertEquals(1, userDAO.findByIdCallCount);
    }

    @Test
    void updateProfileSuccess() throws Exception {

        UserService.ProfileUpdateResult result =
                userService.updateProfile(
                        10L,
                        "  Nguyen Van B  ",
                        "0912 345 678",
                        "  Tran trong  "
                );

        assertTrue(result.isSuccess());
        assertNotNull(result.getUser());

        assertEquals(
                "Nguyen Van B",
                userDAO.updatedFullName
        );

        assertEquals(
                "0912345678",
                userDAO.updatedPhone
        );

        assertEquals(
                "Tran trong",
                userDAO.updatedEmailSignature
        );

        /*
         * Email, role va status khong duoc
         * thay doi boi cap nhat profile.
         */
        assertEquals(
                "user@example.com",
                result.getUser().getEmail()
        );

        assertEquals(
                "SALES",
                result.getUser().getRole()
        );

        assertEquals(
                "ACTIVE",
                result.getUser().getStatus()
        );

        assertEquals(1, userDAO.updateProfileCallCount);
    }

    @Test
    void updateProfileAcceptsInternationalVietnamesePhone()
            throws Exception {

        UserService.ProfileUpdateResult result =
                userService.updateProfile(
                        10L,
                        "Nguyen Van B",
                        "+84912345678",
                        "Signature"
                );

        assertTrue(result.isSuccess());

        assertEquals(
                "+84912345678",
                userDAO.updatedPhone
        );

        assertEquals(1, userDAO.updateProfileCallCount);
    }

    @Test
    void updateProfileRejectsInvalidVietnamesePhone()
            throws Exception {

        UserService.ProfileUpdateResult result =
                userService.updateProfile(
                        10L,
                        "Nguyen Van B",
                        "0123456789",
                        "Signature"
                );

        assertFalse(result.isSuccess());

        assertEquals(
                0,
                userDAO.updateProfileCallCount
        );
    }

    @Test
    void updateProfileRejectsBlankFullName()
            throws Exception {

        UserService.ProfileUpdateResult result =
                userService.updateProfile(
                        10L,
                        "   ",
                        "0912345678",
                        "Signature"
                );

        assertFalse(result.isSuccess());

        assertEquals(
                0,
                userDAO.updateProfileCallCount
        );
    }

    @Test
    void updateProfileAllowsBlankPhone()
            throws Exception {

        UserService.ProfileUpdateResult result =
                userService.updateProfile(
                        10L,
                        "Nguyen Van B",
                        "   ",
                        "Signature"
                );

        assertTrue(result.isSuccess());
        assertNull(userDAO.updatedPhone);
    }

    @Test
    void updateProfileFailsWhenUserDoesNotExist()
            throws Exception {

        userDAO.userById = null;

        UserService.ProfileUpdateResult result =
                userService.updateProfile(
                        999L,
                        "Nguyen Van B",
                        "0912345678",
                        "Signature"
                );

        assertFalse(result.isSuccess());

        assertEquals(
                0,
                userDAO.updateProfileCallCount
        );
    }

    private static class FakeUserDAO
            extends UserDAO {

        private User userById;

        private int findByIdCallCount;
        private int updateProfileCallCount;

        private String updatedFullName;
        private String updatedPhone;
        private String updatedEmailSignature;

        @Override
        public User findById(long userId)
                throws SQLException {

            findByIdCallCount++;
            return userById;
        }

        @Override
        public boolean updateProfile(
                long userId,
                String fullName,
                String phone,
                String emailSignature)
                throws SQLException {

            updateProfileCallCount++;

            updatedFullName = fullName;
            updatedPhone = phone;
            updatedEmailSignature = emailSignature;

            if (userById == null
                    || userById.getId() == null
                    || userById.getId() != userId) {

                return false;
            }

            /*
             * Chi cap nhat cac truong profile duoc phep.
             * Email, role, status khong bi dong den.
             */
            userById.setFullName(fullName);
            userById.setPhone(phone);
            userById.setEmailSignature(
                    emailSignature
            );

            return true;
        }
    }
}