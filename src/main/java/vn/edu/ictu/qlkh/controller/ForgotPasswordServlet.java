package vn.edu.ictu.qlkh.controller;

import jakarta.mail.MessagingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.ictu.qlkh.service.EmailService;
import vn.edu.ictu.qlkh.service.PasswordService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {

    private static final String FORGOT_PASSWORD_VIEW =
            "/views/auth/forgot-password.jsp";

    /*
     * Jira HTQLKH-3:
     * Email tồn tại hay không tồn tại đều phải hiển thị
     * cùng một thông báo để tránh lộ tài khoản.
     */
    private static final String GENERIC_MESSAGE =
            "Nếu email tồn tại trong hệ thống, liên kết đặt lại mật khẩu "
                    + "sẽ được gửi đến email của bạn.";

    private PasswordService passwordService;
    private EmailService emailService;

    @Override
    public void init() {

        passwordService =
                new PasswordService();

        emailService =
                new EmailService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher(
                FORGOT_PASSWORD_VIEW
        ).forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String email =
                request.getParameter("email");

        try {

            /*
             * Nếu email tồn tại:
             * PasswordService tạo token reset.
             *
             * Token được lưu dưới dạng hash trong database
             * và có hiệu lực 30 phút.
             *
             * Nếu email không tồn tại:
             * createResetToken() trả về null.
             */
            String resetToken =
                    passwordService.createResetToken(email);

            /*
             * Chỉ gửi email khi tài khoản thực sự tồn tại.
             *
             * Tuy nhiên thông báo trả về giao diện
             * luôn giống nhau.
             */
            if (resetToken != null) {

                String resetLink =
                        buildResetLink(
                                request,
                                resetToken
                        );

                try {

                    emailService.sendResetPasswordEmail(
                            email,
                            resetLink
                    );

                } catch (MessagingException
                         | IllegalStateException e) {

                    /*
                     * Không trả lỗi gửi email khác ra giao diện.
                     *
                     * Nếu chỉ email tồn tại mới nhận lỗi khác,
                     * người ngoài có thể suy đoán tài khoản
                     * nào tồn tại trong hệ thống.
                     *
                     * Vì vậy lỗi gửi mail chỉ ghi vào log server.
                     */
                    getServletContext().log(
                            "Không thể gửi email đặt lại mật khẩu.",
                            e
                    );
                }
            }

            /*
             * QUAN TRỌNG:
             *
             * forgot-password.jsp đang đọc biến:
             * ${successMessage}
             *
             * Vì vậy Backend phải sử dụng đúng tên
             * "successMessage".
             *
             * Email tồn tại và không tồn tại đều nhận
             * chính xác cùng một thông báo.
             */
            request.setAttribute(
                    "successMessage",
                    GENERIC_MESSAGE
            );

            request.getRequestDispatcher(
                    FORGOT_PASSWORD_VIEW
            ).forward(request, response);

        } catch (SQLException e) {

            throw new ServletException(
                    "Không thể xử lý yêu cầu đặt lại mật khẩu.",
                    e
            );
        }
    }

    /**
     * Tạo URL đặt lại mật khẩu.
     *
     * Ví dụ khi chạy local:
     * http://localhost:8080/qlkh/reset-password?token=...
     */
    private String buildResetLink(
            HttpServletRequest request,
            String resetToken) {

        StringBuilder baseUrl =
                new StringBuilder();

        baseUrl.append(
                request.getScheme()
        );

        baseUrl.append("://");

        baseUrl.append(
                request.getServerName()
        );

        int port =
                request.getServerPort();

        boolean defaultHttpPort =
                "http".equalsIgnoreCase(
                        request.getScheme()
                )
                        && port == 80;

        boolean defaultHttpsPort =
                "https".equalsIgnoreCase(
                        request.getScheme()
                )
                        && port == 443;

        if (!defaultHttpPort
                && !defaultHttpsPort) {

            baseUrl.append(":")
                    .append(port);
        }

        baseUrl.append(
                request.getContextPath()
        );

        baseUrl.append(
                "/reset-password?token="
        );

        baseUrl.append(
                URLEncoder.encode(
                        resetToken,
                        StandardCharsets.UTF_8
                )
        );

        return baseUrl.toString();
    }
}