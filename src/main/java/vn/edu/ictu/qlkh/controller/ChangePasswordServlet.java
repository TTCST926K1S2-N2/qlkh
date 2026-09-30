package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.ictu.qlkh.service.ChangePasswordService;
import vn.edu.ictu.qlkh.service.SessionService;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/change-password")
public class ChangePasswordServlet extends HttpServlet {

    private static final String CHANGE_PASSWORD_VIEW =
            "/views/account/change-password.jsp";

    private ChangePasswordService changePasswordService;

    @Override
    public void init() {

        changePasswordService =
                new ChangePasswordService();
    }

    /**
     * Hiển thị trang đổi mật khẩu.
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);

        /*
         * AuthenticationFilter đã bảo vệ route này.
         * Kiểm tra thêm để tránh xử lý khi session
         * không còn hợp lệ.
         */
        if (!SessionService.isAuthenticated(session)) {

            response.sendRedirect(
                    request.getContextPath() + "/login"
            );

            return;
        }

        request.getRequestDispatcher(
                CHANGE_PASSWORD_VIEW
        ).forward(
                request,
                response
        );
    }

    /**
     * Xử lý đổi mật khẩu.
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session =
                request.getSession(false);

        /*
         * Người dùng bắt buộc phải đang đăng nhập.
         */
        if (!SessionService.isAuthenticated(session)) {

            response.sendRedirect(
                    request.getContextPath() + "/login"
            );

            return;
        }

        Object userIdValue =
                session.getAttribute("userId");

        if (!(userIdValue instanceof Number)) {

            SessionService.invalidateSession(session);

            response.sendRedirect(
                    request.getContextPath() + "/login"
            );

            return;
        }

        long userId =
                ((Number) userIdValue).longValue();

        /*
         * Contract đã thống nhất với FE HTQLKH-4:
         *
         * currentPassword
         * newPassword
         * confirmPassword
         */
        String currentPassword =
                request.getParameter(
                        "currentPassword"
                );

        String newPassword =
                request.getParameter(
                        "newPassword"
                );

        String confirmPassword =
                request.getParameter(
                        "confirmPassword"
                );

        try {

            ChangePasswordService.ChangePasswordResult result =
                    changePasswordService.changePassword(
                            userId,
                            currentPassword,
                            newPassword,
                            confirmPassword
                    );

            /*
             * Có lỗi:
             * hiển thị lại form với thông báo từ Backend.
             */
            if (!result.isSuccess()) {

                request.setAttribute(
                        "errorMessage",
                        result.getMessage()
                );

                request.getRequestDispatcher(
                        CHANGE_PASSWORD_VIEW
                ).forward(
                        request,
                        response
                );

                return;
            }

            /*
             * Jira HTQLKH-4:
             *
             * "Đổi xong thu hồi các phiên đăng nhập khác"
             *
             * Chỉ thực hiện sau khi mật khẩu đã được
             * cập nhật thành công trong database.
             *
             * Phiên hiện tại được giữ lại.
             */
            SessionService.invalidateOtherSessions(
                    userId,
                    session
            );

            request.setAttribute(
                    "successMessage",
                    result.getMessage()
            );

            request.getRequestDispatcher(
                    CHANGE_PASSWORD_VIEW
            ).forward(
                    request,
                    response
            );

        } catch (SQLException e) {

            throw new ServletException(
                    "Không thể xử lý yêu cầu đổi mật khẩu.",
                    e
            );
        }
    }
}