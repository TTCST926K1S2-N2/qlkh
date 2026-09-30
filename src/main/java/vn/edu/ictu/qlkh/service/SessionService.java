package vn.edu.ictu.qlkh.service;

import jakarta.servlet.http.HttpSession;

public final class SessionService {

    public static final int SESSION_TIMEOUT_SECONDS = 15 * 60;

    private SessionService() {
    }

    public static void configureSession(HttpSession session) {
        if (session == null) {
            return;
        }

        session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);
    }

    public static boolean isAuthenticated(HttpSession session) {
        if (session == null) {
            return false;
        }

        try {
            return session.getAttribute("userId") != null
                    && session.getAttribute("userRole") != null;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    public static boolean extendSession(HttpSession session) {
        if (!isAuthenticated(session)) {
            return false;
        }

        /*
         * Request /extend-session hiện tại chính là một lần truy cập
         * vào HttpSession, vì vậy Tomcat cập nhật thời điểm truy cập.
         * Đặt lại timeout để bảo đảm phiên tiếp tục có tối đa
         * 15 phút không hoạt động.
         */
        session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);

        return true;
    }

    public static void invalidateSession(HttpSession session) {
        if (session == null) {
            return;
        }

        try {
            session.invalidate();
        } catch (IllegalStateException ignored) {
            // Session đã hết hạn hoặc đã bị invalidate trước đó.
        }
    }
}