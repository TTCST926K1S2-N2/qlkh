package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.edu.ictu.qlkh.model.User;
import vn.edu.ictu.qlkh.service.SessionService;
import vn.edu.ictu.qlkh.service.UserService;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    private UserService userService;

    @Override
    public void init() {
        userService = new UserService();
    }

    /**
     * HTQLKH-44 / S2-02:
     * Lay ho so cua nguoi dung dang dang nhap.
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        prepareResponse(response);

        Long userId = getCurrentUserId(request);

        if (userId == null) {
            writeError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Phien dang nhap khong hop le."
            );
            return;
        }

        try {
            User user =
                    userService.getProfile(userId);

            if (user == null) {
                writeError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Khong tim thay ho so nguoi dung."
                );
                return;
            }

            writeProfile(
                    response,
                    user,
                    "Lay ho so thanh cong."
            );

        } catch (SQLException e) {

            writeError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Khong the lay ho so nguoi dung."
            );
        }
    }

    /**
     * HTQLKH-44 / S2-02:
     * Cap nhat ho so ca nhan.
     *
     * Chi nhan:
     * - fullName
     * - phone
     * - emailSignature
     *
     * Khong nhan email, role, status, group.
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        prepareResponse(response);

        Long userId = getCurrentUserId(request);

        if (userId == null) {
            writeError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Phien dang nhap khong hop le."
            );
            return;
        }

        String fullName =
                request.getParameter("fullName");

        String phone =
                request.getParameter("phone");

        String emailSignature =
                request.getParameter("emailSignature");

        try {
            UserService.ProfileUpdateResult result =
                    userService.updateProfile(
                            userId,
                            fullName,
                            phone,
                            emailSignature
                    );

            if (!result.isSuccess()) {
                writeError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        result.getMessage()
                );
                return;
            }

            User updatedUser =
                    result.getUser();

            HttpSession session =
                    request.getSession(false);

            if (session != null
                    && updatedUser != null) {

                session.setAttribute(
                        "userName",
                        updatedUser.getFullName()
                );
            }

            writeProfile(
                    response,
                    updatedUser,
                    "Cap nhat ho so thanh cong."
            );

        } catch (SQLException e) {

            writeError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Khong the cap nhat ho so."
            );
        }
    }

    private Long getCurrentUserId(
            HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);

        if (!SessionService.isAuthenticated(session)) {
            return null;
        }

        Object userIdValue =
                session.getAttribute("userId");

        if (!(userIdValue instanceof Number)) {
            return null;
        }

        long userId =
                ((Number) userIdValue).longValue();

        return userId > 0
                ? userId
                : null;
    }

    private void prepareResponse(
            HttpServletResponse response) {

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        response.setHeader(
                "Cache-Control",
                "no-store"
        );
    }

    private void writeProfile(
            HttpServletResponse response,
            User user,
            String message)
            throws IOException {

        if (user == null) {
            writeError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Du lieu ho so khong hop le."
            );
            return;
        }

        response.setStatus(
                HttpServletResponse.SC_OK
        );

        String json =
                "{"
                + "\"success\":true,"
                + "\"message\":\""
                + escapeJson(message)
                + "\","
                + "\"data\":{"
                + "\"id\":" + user.getId() + ","
                + "\"fullName\":\""
                + escapeJson(user.getFullName()) + "\","
                + "\"email\":\""
                + escapeJson(user.getEmail()) + "\","
                + "\"phone\":\""
                + escapeJson(user.getPhone()) + "\","
                + "\"emailSignature\":\""
                + escapeJson(user.getEmailSignature()) + "\","
                + "\"role\":\""
                + escapeJson(user.getRole()) + "\""
                + "}"
                + "}";

        response.getWriter().write(json);
    }

    private void writeError(
            HttpServletResponse response,
            int status,
            String message)
            throws IOException {

        response.setStatus(status);

        response.getWriter().write(
                "{"
                + "\"success\":false,"
                + "\"message\":\""
                + escapeJson(message)
                + "\""
                + "}"
        );
    }

    private String escapeJson(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }
}