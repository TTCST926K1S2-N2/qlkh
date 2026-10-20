package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.edu.ictu.qlkh.model.User;
import vn.edu.ictu.qlkh.service.AccountHandoverService;
import vn.edu.ictu.qlkh.service.SessionService;
import vn.edu.ictu.qlkh.service.UserService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * HTQLKH-10:
 * Khóa/mở khóa tài khoản và ghi nhận người tiếp nhận bàn giao.
 */
@WebServlet("/users/handover")
public class AccountHandoverServlet extends HttpServlet {

    private static final String VIEW =
            "/views/admin/account-handover.jsp";

    private AccountHandoverService handoverService;
    private UserService userService;

    @Override
    public void init() {

        handoverService =
                new AccountHandoverService();

        userService =
                new UserService();
    }

    /**
     * Mở form khóa/mở khóa và bàn giao.
     *
     * URL:
     * /users/handover?id=...
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        Long userId =
                parseLong(
                        request.getParameter("id")
                );

        if (userId == null
                || userId <= 0) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID tài khoản không hợp lệ."
            );

            return;
        }

        try {

            User user =
                    userService.getUserById(
                            userId
                    );

            if (user == null) {

                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Không tìm thấy tài khoản."
                );

                return;
            }

            List<User> users =
                    userService
                            .getActiveUsersForHandover();

            request.setAttribute(
                    "user",
                    user
            );

            request.setAttribute(
                    "targetUsers",
                    users
            );

            request.getRequestDispatcher(
                    VIEW
            ).forward(
                    request,
                    response
            );

        } catch (SQLException e) {

            throw new ServletException(
                    "Không thể tải thông tin bàn giao tài khoản.",
                    e
            );
        }
    }

    /**
     * Xử lý form:
     * - LOCKED: bắt buộc chọn người tiếp nhận.
     * - ACTIVE: mở khóa.
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session =
                request.getSession(false);

        if (session == null) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/login"
            );

            return;
        }

        Long performedBy =
                getSessionUserId(session);

        if (performedBy == null) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/login"
            );

            return;
        }

        Long sourceUserId =
                parseLong(
                        request.getParameter(
                                "userId"
                        )
                );

        if (sourceUserId == null
                || sourceUserId <= 0) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID tài khoản không hợp lệ."
            );

            return;
        }

        String status =
                request.getParameter(
                        "status"
                );

        Long targetUserId =
                parseLong(
                        request.getParameter(
                                "targetUserId"
                        )
                );

        try {

            AccountHandoverService.HandoverResult result =
                    handoverService.changeStatus(
                            sourceUserId,
                            status,
                            targetUserId,
                            performedBy
                    );

            if (!result.isSuccess()) {

                session.setAttribute(
                        "warningMessage",
                        result.getMessage()
                );

                response.sendRedirect(
                        request.getContextPath()
                                + "/users/handover?id="
                                + sourceUserId
                );

                return;
            }

            /*
             * Nếu vừa khóa tài khoản:
             * DB đã commit thành công trước,
             * sau đó mới thu hồi toàn bộ session.
             */
            if (result.isRevokeSessions()) {

                /*
                 * Nếu admin tự khóa chính mình,
                 * invalidateAllSessions() sẽ hủy luôn
                 * session hiện tại.
                 */
                if (sourceUserId.equals(
                        performedBy)) {

                    SessionService
                            .invalidateAllSessions(
                                    sourceUserId
                            );

                    response.sendRedirect(
                            request.getContextPath()
                                    + "/login"
                    );

                    return;
                }

                session.setAttribute(
                        "successMessage",
                        result.getMessage()
                );

                SessionService
                        .invalidateAllSessions(
                                sourceUserId
                        );

            } else {

                session.setAttribute(
                        "successMessage",
                        result.getMessage()
                );
            }

            response.sendRedirect(
                    request.getContextPath()
                            + "/users"
            );

        } catch (SQLException e) {

            throw new ServletException(
                    "Không thể xử lý khóa hoặc bàn giao tài khoản.",
                    e
            );
        }
    }

    /**
     * Đọc userId từ session an toàn.
     */
    private Long getSessionUserId(
            HttpSession session) {

        Object value =
                session.getAttribute(
                        "userId"
                );

        if (value instanceof Number number) {

            return number.longValue();
        }

        if (value instanceof String text) {

            return parseLong(text);
        }

        return null;
    }

    /**
     * Chuyển String -> Long.
     * Giá trị rỗng/sai định dạng trả về null.
     */
    private Long parseLong(
            String value) {

        if (value == null
                || value.isBlank()) {

            return null;
        }

        try {

            return Long.parseLong(
                    value.trim()
            );

        } catch (NumberFormatException e) {

            return null;
        }
    }
}