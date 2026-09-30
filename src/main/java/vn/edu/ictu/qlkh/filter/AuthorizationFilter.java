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
import vn.edu.ictu.qlkh.service.AuthorizationService;
import vn.edu.ictu.qlkh.service.SessionService;

import java.io.IOException;

/**
 * HTQLKH-7
 *
 * Kiểm tra quyền truy cập chức năng sau khi người dùng đã đăng nhập.
 *
 * AuthenticationFilter:
 * - Kiểm tra đăng nhập.
 *
 * AuthorizationFilter:
 * - Kiểm tra người dùng đã đăng nhập có quyền truy cập URL hay không.
 * - Nếu không có quyền: trả HTTP 403 và chuyển đến trang Access Denied.
 */
@WebFilter("/*")
public class AuthorizationFilter implements Filter {

    private final AuthorizationService authorizationService =
            new AuthorizationService();

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
         * Không kiểm tra lại chính trang Access Denied.
         * Tránh vòng lặp khi request bị từ chối và được forward đến đây.
         */
        if ("/access-denied".equals(path)
                || path.startsWith("/views/errors/")
                || path.startsWith("/assets/")
                || path.startsWith("/favicon")) {

            chain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);

        /*
         * AuthorizationFilter không xử lý trường hợp chưa đăng nhập.
         * AuthenticationFilter của HTQLKH-2 sẽ chịu trách nhiệm
         * chuyển người dùng về trang đăng nhập.
         *
         * Cách này cũng giúp hai filter hoạt động an toàn
         * bất kể filter nào được gọi trước.
         */
        if (!SessionService.isAuthenticated(session)) {
            chain.doFilter(request, response);
            return;
        }

        Object roleObject = session.getAttribute("userRole");

        String role = roleObject == null
                ? null
                : roleObject.toString();

        /*
         * Người dùng đã đăng nhập nhưng không đủ quyền.
         */
        if (!authorizationService.canAccess(role, path)) {

            request.setAttribute(
                    "accessDeniedMessage",
                    "Bạn không có quyền truy cập chức năng này."
            );

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);

            request.getRequestDispatcher("/access-denied")
                    .forward(request, response);

            return;
        }

        /*
         * Có quyền -> tiếp tục request.
         */
        chain.doFilter(request, response);
    }
}