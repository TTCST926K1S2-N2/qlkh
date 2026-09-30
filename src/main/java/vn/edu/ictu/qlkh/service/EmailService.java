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

        Session mailSession =
                Session.getInstance(
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

        MimeMessage message =
                new MimeMessage(mailSession);

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
                """.formatted(safeResetLink);
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
     * Escape URL trước khi đưa vào HTML.
     */
    private String escapeHtml(String value) {

        return value
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}