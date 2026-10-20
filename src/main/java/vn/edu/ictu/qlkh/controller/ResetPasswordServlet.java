package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.ictu.qlkh.service.PasswordService;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/reset-password")
public class ResetPasswordServlet extends HttpServlet {

    private static final String RESET_PASSWORD_VIEW =
            "/views/auth/reset-password.jsp";

    private static final String LOGIN_PATH =
            "/login";

    private PasswordService passwordService;

    @Override
    public void init() {
        passwordService = new PasswordService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String token =
                request.getParameter("token");

        try {

            /*
             * Không cho mở form reset nếu:
             * - token không tồn tại
             * - token đã hết hạn
             * - token đã được sử dụng
             */
            if (!passwordService.isTokenValid(token)) {

                request.setAttribute(
                        "errorMessage",
                        "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn."
                );

                request.getRequestDispatcher(RESET_PASSWORD_VIEW)
                        .forward(request, response);

                return;
            }

            request.getRequestDispatcher(RESET_PASSWORD_VIEW)
                    .forward(request, response);

        } catch (SQLException e) {

            throw new ServletException(
                    "Không thể kiểm tra liên kết đặt lại mật khẩu.",
                    e
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String token =
                request.getParameter("token");

        String newPassword =
                request.getParameter("newPassword");

        String confirmPassword =
                request.getParameter("confirmPassword");

        try {

            PasswordService.ResetPasswordResult result =
                    passwordService.resetPassword(
                            token,
                            newPassword,
                            confirmPassword
                    );

            if (!result.isSuccess()) {

                request.setAttribute(
                        "errorMessage",
                        result.getMessage()
                );

                request.getRequestDispatcher(RESET_PASSWORD_VIEW)
                        .forward(request, response);

                return;
            }

            /*
             * Sau khi đổi thành công, token đã được đánh dấu used_at.
             * Chuyển về login và thông báo kết quả.
             */
            response.sendRedirect(
                    request.getContextPath()
                            + LOGIN_PATH
                            + "?reset=success"
            );

        } catch (SQLException e) {

            throw new ServletException(
                    "Không thể đặt lại mật khẩu.",
                    e
            );
        }
    }
}