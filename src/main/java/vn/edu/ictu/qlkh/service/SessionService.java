package vn.edu.ictu.qlkh.service;

import jakarta.servlet.http.HttpSession;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionService {

    public static final int SESSION_TIMEOUT_SECONDS = 15 * 60;

    /*
     * HTQLKH-4:
     * Lưu các session đang đăng nhập theo userId.
     *
     * Một tài khoản có thể đăng nhập trên nhiều trình duyệt.
     */
    private static final ConcurrentHashMap<Long, Set<HttpSession>>
            USER_SESSIONS = new ConcurrentHashMap<>();

    private SessionService() {
    }

    /**
     * HTQLKH-2:
     * Cấu hình thời gian hết hạn phiên.
     */
    public static void configureSession(HttpSession session) {

        if (session == null) {
            return;
        }

        session.setMaxInactiveInterval(
                SESSION_TIMEOUT_SECONDS
        );
    }

    /**
     * Kiểm tra session đã đăng nhập hay chưa.
     */
    public static boolean isAuthenticated(
            HttpSession session) {

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

    /**
     * HTQLKH-2:
     * Gia hạn phiên khi người dùng còn hoạt động.
     */
    public static boolean extendSession(
            HttpSession session) {

        if (!isAuthenticated(session)) {
            return false;
        }

        session.setMaxInactiveInterval(
                SESSION_TIMEOUT_SECONDS
        );

        return true;
    }

    /**
     * HTQLKH-4:
     * Đăng ký một session sau khi đăng nhập thành công.
     */
    public static void registerSession(
            long userId,
            HttpSession session) {

        if (session == null) {
            return;
        }

        USER_SESSIONS
                .computeIfAbsent(
                        userId,
                        key -> ConcurrentHashMap.newKeySet()
                )
                .add(session);
    }

    /**
     * HTQLKH-4:
     * Thu hồi tất cả phiên đăng nhập khác của tài khoản,
     * nhưng giữ lại phiên hiện tại.
     *
     * Ví dụ:
     *
     * Chrome = currentSession
     * Edge   = session khác
     *
     * Đổi mật khẩu trên Chrome:
     * - Chrome vẫn đăng nhập.
     * - Edge bị invalidate.
     */
    public static void invalidateOtherSessions(
            long userId,
            HttpSession currentSession) {

        Set<HttpSession> sessions =
                USER_SESSIONS.get(userId);

        if (sessions == null) {
            return;
        }

        for (HttpSession session : sessions) {

            if (session == currentSession) {
                continue;
            }

            try {

                session.invalidate();

            } catch (IllegalStateException ignored) {

                // Session đã hết hạn hoặc đã bị thu hồi.
            }
        }

        /*
         * Sau khi thu hồi, chỉ giữ lại session hiện tại.
         */
        sessions.clear();

        if (currentSession != null) {
            sessions.add(currentSession);
        }
    }

    /**
     * HTQLKH-2:
     * Đăng xuất làm session hiện tại mất hiệu lực ngay.
     *
     * HTQLKH-4:
     * Đồng thời loại session khỏi danh sách quản lý.
     */
    public static void invalidateSession(
            HttpSession session) {

        if (session == null) {
            return;
        }

        Long userId = null;

        try {

            Object value =
                    session.getAttribute("userId");

            if (value instanceof Long) {
                userId = (Long) value;
            }

        } catch (IllegalStateException ignored) {

            return;
        }

        if (userId != null) {

            Set<HttpSession> sessions =
                    USER_SESSIONS.get(userId);

            if (sessions != null) {

                sessions.remove(session);

                if (sessions.isEmpty()) {
                    USER_SESSIONS.remove(
                            userId,
                            sessions
                    );
                }
            }
        }

        try {

            session.invalidate();

        } catch (IllegalStateException ignored) {

            // Session đã hết hạn hoặc đã invalidate.
        }
    }
}