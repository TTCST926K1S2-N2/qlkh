package vn.edu.ictu.qlkh.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.ictu.qlkh.service.SessionService;

import java.io.IOException;

@WebFilter("/*")
public class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest servletRequest,
            ServletResponse servletResponse,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request =
                (HttpServletRequest) servletRequest;

        HttpServletResponse response =
                (HttpServletResponse) servletResponse;

        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();

        String path = uri.substring(contextPath.length());

        /*
         * Các tài nguyên/chức năng không yêu cầu đăng nhập.
         */
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);

        /*
         * Session hợp lệ -> cho phép request tiếp tục.
         *
         * Mỗi request hợp lệ cũng được servlet container ghi nhận
         * là hoạt động của session.
         */
        if (SessionService.isAuthenticated(session)) {
            chain.doFilter(request, response);
            return;
        }

        /*
         * Nếu trình duyệt gửi session id nhưng session đó không còn
         * hợp lệ thì có thể xác định phiên đã hết hạn.
         */
        boolean expiredSession =
                request.getRequestedSessionId() != null
                        && !request.isRequestedSessionIdValid();

        /*
         * API gia hạn phiên cần trả 401 để session.js biết
         * phiên không còn hợp lệ, không redirect HTML.
         */
        if (path.equals("/api/v1/customers")
                || path.startsWith("/api/v1/customers/")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Cache-Control", "no-store");
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Chua dang nhap\"}"
            );
            return;
        }

        if ("/extend-session".equals(path)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Cache-Control", "no-store");

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Phiên đăng nhập đã hết hạn.\"}"
            );
            return;
        }

        /*
         * Request trang thông thường:
         * chuyển về servlet /login.
         */
        if (expiredSession) {
            response.sendRedirect(
                    contextPath + "/login?expired=1"
            );
        } else {
            response.sendRedirect(
                    contextPath + "/login"
            );
        }
    }

    private boolean isPublicPath(String path) {

        if (path == null) {
            return false;
        }

        return path.equals("/login")
                || path.equals("/logout")
                || path.equals("/forgot-password")
                || path.equals("/reset-password")
                || path.startsWith("/views/auth/")
                || path.startsWith("/assets/")
                || path.startsWith("/favicon");
    }
}