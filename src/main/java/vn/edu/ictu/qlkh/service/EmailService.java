package vn.edu.ictu.qlkh.service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * Dịch vụ gửi email cho hệ thống.
 *
 * HTQLKH-3:
 * Gửi liên kết đặt lại mật khẩu cho người dùng.
 *
 * HTQLKH-8:
 * Gửi thông tin tài khoản mới và mật khẩu tạm.
 *
 * Thông tin SMTP được lấy từ biến môi trường.
 * Không lưu mật khẩu email trực tiếp trong source code.
 */
public class EmailService {

    private static final String DEFAULT_SMTP_HOST =
            "smtp.gmail.com";

    private static final String DEFAULT_SMTP_PORT =
            "587";

    private static final String SYSTEM_NAME =
            "Hệ Thống Quản Lý Khách Hàng";

    private final String smtpHost;
    private final String smtpPort;
    private final String smtpUsername;
    private final String smtpPassword;
    private final String fromEmail;

    public EmailService() {

        this.smtpHost = getEnvironmentValue(
                "SMTP_HOST",
                DEFAULT_SMTP_HOST
        );

        this.smtpPort = getEnvironmentValue(
                "SMTP_PORT",
                DEFAULT_SMTP_PORT
        );

        this.smtpUsername =
                System.getenv("SMTP_USERNAME");

        this.smtpPassword =
                System.getenv("SMTP_PASSWORD");

        this.fromEmail = getEnvironmentValue(
                "SMTP_FROM",
                smtpUsername
        );
    }

    /**
     * HTQLKH-3:
     * Gửi email chứa liên kết đặt lại mật khẩu.
     */
    public void sendResetPasswordEmail(
            String recipientEmail,
            String resetLink)
            throws MessagingException {

        validateConfiguration();

        if (recipientEmail == null
                || recipientEmail.isBlank()) {

            throw new IllegalArgumentException(
                    "Email người nhận không được để trống."
            );
        }

        if (resetLink == null
                || resetLink.isBlank()) {

            throw new IllegalArgumentException(
                    "Liên kết đặt lại mật khẩu không được để trống."
            );
        }

        Session mailSession =
                createMailSession();

        MimeMessage message =
                new MimeMessage(mailSession);

        setSender(message);

        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(
                        recipientEmail,
                        false
                )
        );

        message.setSubject(
                "Đặt lại mật khẩu - "
                        + SYSTEM_NAME,
                "UTF-8"
        );

        message.setContent(
                createResetPasswordContent(
                        resetLink
                ),
                "text/html; charset=UTF-8"
        );

        Transport.send(message);
    }

    /**
     * HTQLKH-8:
     * Gửi thông tin tài khoản mới và mật khẩu tạm.
     */
    public void sendTemporaryPasswordEmail(
            String recipientEmail,
            String fullName,
            String temporaryPassword)
            throws MessagingException {

        validateConfiguration();

        if (recipientEmail == null
                || recipientEmail.isBlank()) {

            throw new IllegalArgumentException(
                    "Email người nhận không được để trống."
            );
        }

        if (temporaryPassword == null
                || temporaryPassword.isBlank()) {

            throw new IllegalArgumentException(
                    "Mật khẩu tạm không được để trống."
            );
        }

        Session mailSession =
                createMailSession();

        MimeMessage message =
                new MimeMessage(mailSession);

        setSender(message);

        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(
                        recipientEmail,
                        false
                )
        );

        message.setSubject(
                "Thông tin tài khoản - "
                        + SYSTEM_NAME,
                "UTF-8"
        );

        message.setContent(
                createTemporaryPasswordContent(
                        fullName,
                        recipientEmail,
                        temporaryPassword
                ),
                "text/html; charset=UTF-8"
        );

        Transport.send(message);
    }

    /**
     * Tạo SMTP Session dùng chung cho các loại email.
     */
    private Session createMailSession() {

        Properties properties =
                new Properties();

        properties.put(
                "mail.smtp.auth",
                "true"
        );

        properties.put(
                "mail.smtp.starttls.enable",
                "true"
        );

        properties.put(
                "mail.smtp.starttls.required",
                "true"
        );

        properties.put(
                "mail.smtp.host",
                smtpHost
        );

        properties.put(
                "mail.smtp.port",
                smtpPort
        );

        properties.put(
                "mail.smtp.connectiontimeout",
                "10000"
        );

        properties.put(
                "mail.smtp.timeout",
                "10000"
        );

        properties.put(
                "mail.smtp.writetimeout",
                "10000"
        );

        return Session.getInstance(
                properties,
                new Authenticator() {

                    @Override
                    protected PasswordAuthentication
                    getPasswordAuthentication() {

                        return new PasswordAuthentication(
                                smtpUsername,
                                smtpPassword
                        );
                    }
                }
        );
    }

    /**
     * Thiết lập địa chỉ người gửi.
     */
    private void setSender(
            MimeMessage message)
            throws MessagingException {

        try {

            message.setFrom(
                    new InternetAddress(
                            fromEmail,
                            SYSTEM_NAME,
                            "UTF-8"
                    )
            );

        } catch (UnsupportedEncodingException e) {

            throw new MessagingException(
                    "Không thể tạo địa chỉ email gửi.",
                    e
            );
        }
    }

    /**
     * HTQLKH-3:
     * Nội dung email đặt lại mật khẩu.
     */
    private String createResetPasswordContent(
            String resetLink) {

        String safeResetLink =
                escapeHtml(resetLink);

        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                </head>

                <body style="
                    font-family: Arial, sans-serif;
                    color: #333333;
                    line-height: 1.6;
                ">

                    <h2>
                        Đặt lại mật khẩu
                    </h2>

                    <p>
                        Chúng tôi nhận được yêu cầu
                        đặt lại mật khẩu cho tài khoản của bạn.
                    </p>

                    <p>
                        Nhấn vào nút bên dưới để
                        đặt lại mật khẩu:
                    </p>

                    <p style="margin: 25px 0;">
                        <a href="%s"
                           style="
                               background-color: #4f46e5;
                               color: white;
                               padding: 12px 20px;
                               text-decoration: none;
                               border-radius: 6px;
                               display: inline-block;
                           ">
                            Đặt lại mật khẩu
                        </a>
                    </p>

                    <p>
                        Liên kết này có hiệu lực trong
                        <strong>30 phút</strong>
                        và chỉ được sử dụng
                        <strong>một lần</strong>.
                    </p>

                    <p>
                        Nếu bạn không yêu cầu đặt lại
                        mật khẩu, hãy bỏ qua email này.
                    </p>

                    <hr>

                    <p style="
                        color: #777777;
                        font-size: 13px;
                    ">
                        Hệ Thống Quản Lý Khách Hàng
                    </p>

                </body>
                </html>
                """.formatted(
                        safeResetLink
                );
    }

    /**
     * HTQLKH-8:
     * Nội dung email thông báo tài khoản mới.
     */
    private String createTemporaryPasswordContent(
            String fullName,
            String email,
            String temporaryPassword) {

        String safeFullName =
                escapeHtml(
                        fullName == null
                                ? ""
                                : fullName
                );

        String safeEmail =
                escapeHtml(email);

        String safeTemporaryPassword =
                escapeHtml(
                        temporaryPassword
                );

        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                </head>

                <body style="
                    font-family: Arial, sans-serif;
                    color: #333333;
                    line-height: 1.6;
                ">

                    <h2>
                        Tài khoản của bạn đã được tạo
                    </h2>

                    <p>
                        Xin chào <strong>%s</strong>,
                    </p>

                    <p>
                        Quản trị viên đã tạo tài khoản cho bạn
                        trên Hệ Thống Quản Lý Khách Hàng.
                    </p>

                    <p>
                        <strong>Email đăng nhập:</strong>
                        %s
                    </p>

                    <p>
                        <strong>Mật khẩu tạm:</strong>
                        %s
                    </p>

                    <p>
                        Vui lòng đăng nhập và đổi mật khẩu
                        sau khi nhận được tài khoản.
                    </p>

                    <p>
                        Không chia sẻ mật khẩu này
                        cho người khác.
                    </p>

                    <hr>

                    <p style="
                        color: #777777;
                        font-size: 13px;
                    ">
                        Hệ Thống Quản Lý Khách Hàng
                    </p>

                </body>
                </html>
                """.formatted(
                        safeFullName,
                        safeEmail,
                        safeTemporaryPassword
                );
    }

    /**
     * Kiểm tra cấu hình SMTP.
     */
    private void validateConfiguration() {

        if (smtpUsername == null
                || smtpUsername.isBlank()) {

            throw new IllegalStateException(
                    "Chưa cấu hình biến môi trường SMTP_USERNAME."
            );
        }

        if (smtpPassword == null
                || smtpPassword.isBlank()) {

            throw new IllegalStateException(
                    "Chưa cấu hình biến môi trường SMTP_PASSWORD."
            );
        }

        if (fromEmail == null
                || fromEmail.isBlank()) {

            throw new IllegalStateException(
                    "Chưa cấu hình địa chỉ email gửi."
            );
        }
    }

    /**
     * Lấy biến môi trường.
     * Nếu không có thì dùng giá trị mặc định.
     */
    private String getEnvironmentValue(
            String name,
            String defaultValue) {

        String value =
                System.getenv(name);

        if (value == null
                || value.isBlank()) {

            return defaultValue;
        }

        return value.trim();
    }

    /**
     * Escape dữ liệu trước khi đưa vào HTML.
     */
    private String escapeHtml(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}