package vn.edu.ictu.qlkh.service;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SessionServiceTest {

    @Test
    void configureSessionShouldSetTimeoutTo15Minutes() {
        FakeSession fake = new FakeSession();
        HttpSession session = fake.create();

        SessionService.configureSession(session);

        assertEquals(
                15 * 60,
                fake.maxInactiveInterval
        );
    }

    @Test
    void authenticatedSessionShouldBeAccepted() {
        FakeSession fake = new FakeSession();

        fake.attributes.put("userId", 1L);
        fake.attributes.put("userRole", "ADMIN");

        HttpSession session = fake.create();

        assertTrue(
                SessionService.isAuthenticated(session)
        );
    }

    @Test
    void sessionWithoutUserShouldNotBeAuthenticated() {
        FakeSession fake = new FakeSession();
        HttpSession session = fake.create();

        assertFalse(
                SessionService.isAuthenticated(session)
        );
    }

    @Test
    void extendSessionShouldKeepAuthenticatedSessionAliveFor15Minutes() {
        FakeSession fake = new FakeSession();

        fake.attributes.put("userId", 1L);
        fake.attributes.put("userRole", "ADMIN");

        HttpSession session = fake.create();

        boolean result =
                SessionService.extendSession(session);

        assertTrue(result);

        assertEquals(
                SessionService.SESSION_TIMEOUT_SECONDS,
                fake.maxInactiveInterval
        );
    }

    @Test
    void extendSessionShouldRejectUnauthenticatedSession() {
        FakeSession fake = new FakeSession();
        HttpSession session = fake.create();

        boolean result =
                SessionService.extendSession(session);

        assertFalse(result);
    }

    @Test
    void logoutShouldInvalidateSessionImmediately() {
        FakeSession fake = new FakeSession();

        fake.attributes.put("userId", 1L);
        fake.attributes.put("userRole", "ADMIN");

        HttpSession session = fake.create();

        SessionService.invalidateSession(session);

        assertTrue(fake.invalidated);
        assertFalse(
                SessionService.isAuthenticated(session)
        );
    }

    @Test
    void invalidatedSessionShouldNotBeAuthenticated() {
        FakeSession fake = new FakeSession();

        fake.attributes.put("userId", 1L);
        fake.attributes.put("userRole", "ADMIN");

        HttpSession session = fake.create();

        session.invalidate();

        assertFalse(
                SessionService.isAuthenticated(session)
        );
    }

    /*
     * HttpSession giả phục vụ unit test.
     * Không cần thêm Mockito vào project.
     */
    private static class FakeSession {

        private final Map<String, Object> attributes =
                new HashMap<>();

        private int maxInactiveInterval;

        private boolean invalidated;

        HttpSession create() {

            return (HttpSession) Proxy.newProxyInstance(
                    HttpSession.class.getClassLoader(),
                    new Class<?>[]{HttpSession.class},
                    (proxy, method, args) -> {

                        String methodName =
                                method.getName();

                        switch (methodName) {

                            case "getAttribute":

                                ensureValid();

                                return attributes.get(
                                        (String) args[0]
                                );

                            case "setAttribute":

                                ensureValid();

                                attributes.put(
                                        (String) args[0],
                                        args[1]
                                );

                                return null;

                            case "setMaxInactiveInterval":

                                ensureValid();

                                maxInactiveInterval =
                                        (Integer) args[0];

                                return null;

                            case "getMaxInactiveInterval":

                                ensureValid();

                                return maxInactiveInterval;

                            case "invalidate":

                                ensureValid();

                                invalidated = true;
                                attributes.clear();

                                return null;

                            case "isNew":

                                ensureValid();
                                return false;

                            case "getId":

                                ensureValid();
                                return "test-session";

                            case "toString":

                                return "FakeHttpSession";

                            case "hashCode":

                                return System.identityHashCode(
                                        proxy
                                );

                            case "equals":

                                return proxy == args[0];

                            default:

                                if (method.getReturnType()
                                        .equals(boolean.class)) {
                                    return false;
                                }

                                if (method.getReturnType()
                                        .equals(int.class)) {
                                    return 0;
                                }

                                if (method.getReturnType()
                                        .equals(long.class)) {
                                    return 0L;
                                }

                                return null;
                        }
                    }
            );
        }

        private void ensureValid() {
            if (invalidated) {
                throw new IllegalStateException(
                        "Session đã bị invalidate."
                );
            }
        }
    }
}