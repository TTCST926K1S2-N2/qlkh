package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.ictu.qlkh.model.User;
import vn.edu.ictu.qlkh.service.AuthService;
import vn.edu.ictu.qlkh.service.SessionService;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final String LOGIN_VIEW =
            "/views/auth/login.jsp";

    /*
     * Dùng chung một thông báo cho:
     * - email không tồn tại
     * - mật khẩu sai
     * - tài khoản đang bị khóa
     * - tài khoản không ACTIVE
     *
     * Không tiết lộ email/tài khoản có tồn tại hay không.
     */
    private static final String INVALID_LOGIN_MESSAGE =
            "Email hoặc mật khẩu không chính xác.";

    /*
     * HTQLKH-2:
     * Thông báo khi phiên đăng nhập đã hết hạn.
     */
    private static final String SESSION_EXPIRED_MESSAGE =
            "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.";

    private AuthService authService;

    @Override
    public void init() {
        authService = new AuthService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);

        /*
         * Nếu phiên đăng nhập vẫn còn hợp lệ
         * thì không cần quay lại trang login.
         */
        if (SessionService.isAuthenticated(session)) {
            response.sendRedirect(
                    request.getContextPath() + "/"
            );
            return;
        }

        /*
         * HTQLKH-2:
         * AuthenticationFilter chuyển tới:
         *
         * /login?expired=1
         *
         * khi phát hiện session đã hết hạn.
         */
        if ("1".equals(request.getParameter("expired"))) {
            request.setAttribute(
                    "errorMessage",
                    SESSION_EXPIRED_MESSAGE
            );
        }

        request.getRequestDispatcher(LOGIN_VIEW)
                .forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        /*
         * FE hiện tại đặt name="username".
         * Theo Jira HTQLKH-1, giá trị này được xử lý như email.
         */
        String email =
                request.getParameter("username");

        String password =
                request.getParameter("password");

        try {
            AuthService.LoginResult result =
                    authService.login(email, password);

            /*
             * Đăng nhập thất bại:
             * luôn sử dụng cùng một thông báo.
             */
            if (!result.isSuccess()) {
                request.setAttribute(
                        "errorMessage",
                        INVALID_LOGIN_MESSAGE
                );

                request.getRequestDispatcher(LOGIN_VIEW)
                        .forward(request, response);

                return;
            }

            User user = result.getUser();

            /*
             * Chống session fixation:
             * hủy session cũ trước khi tạo session đăng nhập mới.
             */
            HttpSession oldSession =
                    request.getSession(false);

            if (oldSession != null) {
                oldSession.invalidate();
            }

            /*
             * Tạo session mới sau khi đăng nhập thành công.
             */
            HttpSession newSession =
                    request.getSession(true);

            /*
             * HTQLKH-2:
             * Phiên hết hạn sau 15 phút không hoạt động.
             */
            SessionService.configureSession(newSession);

            /*
             * Lưu thông tin người dùng vào session.
             */
            newSession.setAttribute(
                    "userId",
                    user.getId()
            );

            newSession.setAttribute(
                    "userEmail",
                    user.getEmail()
            );

            newSession.setAttribute(
                    "userName",
                    user.getFullName()
            );

            newSession.setAttribute(
                    "userRole",
                    user.getRole()
            );

            /*
             * Repo hiện tại chỉ có một trang chủ.
             * Vai trò được lưu trong userRole để giao diện
             * và chức năng hiển thị theo quyền.
             */
            response.sendRedirect(
                    request.getContextPath() + "/"
            );

        } catch (SQLException e) {
            throw new ServletException(
                    "Không thể xử lý yêu cầu đăng nhập.",
                    e
            );
        }
    }
}