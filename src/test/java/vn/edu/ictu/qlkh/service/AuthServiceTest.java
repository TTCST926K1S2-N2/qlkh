package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.dao.UserDAO;
import vn.edu.ictu.qlkh.model.User;
import vn.edu.ictu.qlkh.util.PasswordUtil;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private static final String EMAIL = "sales@company.vn";
    private static final String PASSWORD = "Password@123";

    private User user;
    private FakeUserDAO userDAO;
    private MutableClock clock;
    private AuthService authService;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);
        user.setEmail(EMAIL);
        user.setPasswordHash(
                PasswordUtil.hashPassword(PASSWORD)
        );
        user.setFullName("Nhan vien kinh doanh");
        user.setRole("SALES");
        user.setStatus("ACTIVE");
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);

        userDAO = new FakeUserDAO(user);

        clock = new MutableClock(
                Instant.parse("2026-09-30T00:00:00Z"),
                ZoneOffset.UTC
        );

        authService = new AuthService(
                userDAO,
                clock
        );
    }

    @Test
    void correctCredentialsShouldLoginSuccessfully()
            throws SQLException {

        AuthService.LoginResult result =
                authService.login(
                        EMAIL,
                        PASSWORD
                );

        assertTrue(result.isSuccess());
        assertNotNull(result.getUser());
        assertEquals(EMAIL, result.getUser().getEmail());
        assertEquals("SALES", result.getUser().getRole());
    }

    @Test
    void nonexistentEmailShouldFail()
            throws SQLException {

        AuthService.LoginResult result =
                authService.login(
                        "khongtontai@company.vn",
                        PASSWORD
                );

        assertFalse(result.isSuccess());
        assertNull(result.getUser());
    }

    @Test
    void wrongPasswordShouldFail()
            throws SQLException {

        AuthService.LoginResult result =
                authService.login(
                        EMAIL,
                        "SaiMatKhau"
                );

        assertFalse(result.isSuccess());
        assertNull(result.getUser());
        assertEquals(
                1,
                user.getFailedLoginAttempts()
        );
        assertNull(user.getLockedUntil());
    }

    @Test
    void firstFourFailuresShouldNotLockAccount()
            throws SQLException {

        for (int attempt = 1; attempt <= 4; attempt++) {

            AuthService.LoginResult result =
                    authService.login(
                            EMAIL,
                            "SaiMatKhau"
                    );

            assertFalse(result.isSuccess());

            assertEquals(
                    attempt,
                    user.getFailedLoginAttempts()
            );

            assertNull(
                    user.getLockedUntil()
            );
        }
    }

    @Test
    void fifthConsecutiveFailureShouldLockFor15Minutes()
            throws SQLException {

        LocalDateTime expectedLockedUntil =
                LocalDateTime.ofInstant(
                        clock.instant(),
                        clock.getZone()
                ).plusMinutes(15);

        for (int attempt = 1; attempt <= 5; attempt++) {

            AuthService.LoginResult result =
                    authService.login(
                            EMAIL,
                            "SaiMatKhau"
                    );

            assertFalse(result.isSuccess());
        }

        assertEquals(
                5,
                user.getFailedLoginAttempts()
        );

        assertEquals(
                expectedLockedUntil,
                user.getLockedUntil()
        );
    }

    @Test
    void correctPasswordDuringLockShouldStillBeRejected()
            throws SQLException {

        for (int attempt = 1; attempt <= 5; attempt++) {
            authService.login(
                    EMAIL,
                    "SaiMatKhau"
            );
        }

        assertNotNull(user.getLockedUntil());

        AuthService.LoginResult result =
                authService.login(
                        EMAIL,
                        PASSWORD
                );

        assertFalse(result.isSuccess());
        assertNull(result.getUser());
    }

    @Test
    void loginShouldSucceedAfter15MinuteLockExpires()
            throws SQLException {

        for (int attempt = 1; attempt <= 5; attempt++) {
            authService.login(
                    EMAIL,
                    "SaiMatKhau"
            );
        }

        assertNotNull(user.getLockedUntil());

        clock.advance(
                Duration.ofMinutes(15)
        );

        AuthService.LoginResult result =
                authService.login(
                        EMAIL,
                        PASSWORD
                );

        assertTrue(result.isSuccess());
        assertNotNull(result.getUser());

        assertEquals(
                0,
                user.getFailedLoginAttempts()
        );

        assertNull(
                user.getLockedUntil()
        );
    }

    @Test
    void successfulLoginShouldResetPreviousFailures()
            throws SQLException {

        authService.login(
                EMAIL,
                "SaiMatKhau"
        );

        authService.login(
                EMAIL,
                "SaiMatKhau"
        );

        assertEquals(
                2,
                user.getFailedLoginAttempts()
        );

        AuthService.LoginResult result =
                authService.login(
                        EMAIL,
                        PASSWORD
                );

        assertTrue(result.isSuccess());

        assertEquals(
                0,
                user.getFailedLoginAttempts()
        );

        assertNull(
                user.getLockedUntil()
        );
    }

    private static class FakeUserDAO extends UserDAO {

        private final User user;

        private FakeUserDAO(User user) {
            this.user = user;
        }

        @Override
        public User findByEmail(String email) {

            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }

            return null;
        }

        @Override
        public void updateLoginFailureState(
                long userId,
                int failedAttempts,
                LocalDateTime lockedUntil) {

            user.setFailedLoginAttempts(
                    failedAttempts
            );

            user.setLockedUntil(
                    lockedUntil
            );
        }

        @Override
        public void resetLoginFailures(long userId) {

            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
        }
    }

    private static class MutableClock extends Clock {

        private Instant instant;
        private final ZoneId zone;

        private MutableClock(
                Instant instant,
                ZoneId zone) {

            this.instant = instant;
            this.zone = zone;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(
                    instant,
                    zone
            );
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}