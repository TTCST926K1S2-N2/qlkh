package vn.edu.ictu.qlkh.controller;

import jakarta.mail.MessagingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import vn.edu.ictu.qlkh.model.User;
import vn.edu.ictu.qlkh.service.EmailService;
import vn.edu.ictu.qlkh.service.UserService;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Quản lý tài khoản người dùng - HTQLKH-8.
 *
 * Chức năng:
 * - Danh sách tài khoản.
 * - Tìm kiếm theo tên/email.
 * - Lọc theo vai trò/trạng thái.
 * - Phân trang.
 * - Tạo tài khoản.
 * - Gửi email kèm mật khẩu tạm.
 * - Sửa tài khoản.
 */
@WebServlet(urlPatterns = {
        "/users",
        "/users/*"
})
public class UserManagementServlet
        extends HttpServlet {

    private UserService userService;
    private EmailService emailService;

    @Override
    public void init() {

        userService =
                new UserService();

        emailService =
                new EmailService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String path =
                request.getPathInfo();

        try {

            if (path == null
                    || path.isBlank()
                    || "/".equals(path)) {

                showUserList(
                        request,
                        response
                );

                return;
            }

            switch (path) {

                case "/create" ->
                        showCreateForm(
                                request,
                                response
                        );

                case "/edit" ->
                        showEditForm(
                                request,
                                response
                        );

                default ->
                        response.sendError(
                                HttpServletResponse.SC_NOT_FOUND
                        );
            }

        } catch (SQLException e) {

            throw new ServletException(
                    "Không thể xử lý dữ liệu tài khoản.",
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
        response.setCharacterEncoding("UTF-8");

        String path =
                request.getPathInfo();

        try {

            /*
             * FE8 hiện tại submit form về /users.
             *
             * Nếu không có id:
             * -> tạo tài khoản.
             *
             * Nếu có id:
             * -> cập nhật tài khoản.
             */
            if (path == null
                    || path.isBlank()
                    || "/".equals(path)) {

                String idValue =
                        request.getParameter("id");

                if (idValue == null
                        || idValue.isBlank()) {

                    createUser(
                            request,
                            response
                    );

                } else {

                    updateUser(
                            request,
                            response
                    );
                }

                return;
            }

            switch (path) {

                case "/create" ->
                        createUser(
                                request,
                                response
                        );

                case "/edit" ->
                        updateUser(
                                request,
                                response
                        );

                default ->
                        response.sendError(
                                HttpServletResponse.SC_NOT_FOUND
                        );
            }

        } catch (SQLException e) {

            throw new ServletException(
                    "Không thể cập nhật dữ liệu tài khoản.",
                    e
            );
        }
    }

    /**
     * Hiển thị danh sách tài khoản.
     */
    private void showUserList(
            HttpServletRequest request,
            HttpServletResponse response)
            throws SQLException,
            ServletException,
            IOException {

        String keyword =
                request.getParameter("keyword");

        String role =
                request.getParameter("role");

        String status =
                request.getParameter("status");

        int page =
                parsePage(
                        request.getParameter("page")
                );

        UserService.UserPage userPage =
                userService.getUsers(
                        keyword,
                        role,
                        status,
                        page
                );

        request.setAttribute(
                "users",
                userPage.getUsers()
        );

        request.setAttribute(
                "currentPage",
                userPage.getCurrentPage()
        );

        request.setAttribute(
                "totalPages",
                userPage.getTotalPages()
        );

        request.setAttribute(
                "totalItems",
                userPage.getTotalItems()
        );

        request.setAttribute(
                "pageSize",
                userPage.getPageSize()
        );

        request.setAttribute(
                "keyword",
                keyword
        );

        request.setAttribute(
                "selectedRole",
                role
        );

        request.setAttribute(
                "selectedStatus",
                status
        );

        /*
         * Flash message chỉ hiển thị một lần.
         */
        Object successMessage =
                request.getSession()
                        .getAttribute(
                                "successMessage"
                        );

        if (successMessage != null) {

            request.setAttribute(
                    "successMessage",
                    successMessage
            );

            request.getSession()
                    .removeAttribute(
                            "successMessage"
                    );
        }

        Object warningMessage =
                request.getSession()
                        .getAttribute(
                                "warningMessage"
                        );

        if (warningMessage != null) {

            request.setAttribute(
                    "warningMessage",
                    warningMessage
            );

            request.getSession()
                    .removeAttribute(
                            "warningMessage"
                    );
        }

        request.getRequestDispatcher(
                "/user/list.jsp"
        ).forward(
                request,
                response
        );
    }

    /**
     * Mở form tạo tài khoản.
     */
    private void showCreateForm(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,
            IOException {

        request.getRequestDispatcher(
                "/user/form.jsp"
        ).forward(
                request,
                response
        );
    }

    /**
     * Mở form sửa tài khoản.
     */
    private void showEditForm(
            HttpServletRequest request,
            HttpServletResponse response)
            throws SQLException,
            ServletException,
            IOException {

        Long userId =
                parseUserId(
                        request.getParameter("id")
                );

        if (userId == null) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID tài khoản không hợp lệ."
            );

            return;
        }

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

        request.setAttribute(
                "user",
                user
        );

        request.getRequestDispatcher(
                "/user/form.jsp"
        ).forward(
                request,
                response
        );
    }

    /**
     * HTQLKH-8:
     * Tạo tài khoản mới.
     *
     * Tài khoản mới mặc định:
     * - role = SALES
     * - status = ACTIVE
     *
     * UserService tự sinh mật khẩu tạm và chỉ lưu
     * password hash vào database.
     *
     * Sau khi tạo thành công, mật khẩu tạm được
     * gửi tới email của người dùng.
     *
     * Việc gán nhóm kinh doanh thuộc HTQLKH-9.
     */
    private void createUser(
            HttpServletRequest request,
            HttpServletResponse response)
            throws SQLException,
            ServletException,
            IOException {

        String fullName =
                request.getParameter("fullName");

        String email =
                request.getParameter("email");

        UserService.CreateUserResult result =
                userService.createUser(
                        fullName,
                        email,
                        "SALES",
                        "ACTIVE"
                );

        /*
         * Validation thất bại hoặc email bị trùng.
         */
        if (!result.isSuccess()) {

            User formUser =
                    new User();

            formUser.setFullName(
                    fullName
            );

            formUser.setEmail(
                    email
            );

            request.setAttribute(
                    "user",
                    formUser
            );

            request.setAttribute(
                    "errorMessage",
                    result.getMessage()
            );

            request.getRequestDispatcher(
                    "/user/form.jsp"
            ).forward(
                    request,
                    response
            );

            return;
        }

        /*
         * Tài khoản đã được tạo trong database.
         *
         * Gửi email chứa mật khẩu tạm.
         * Không lưu mật khẩu tạm vào session.
         * Không lưu mật khẩu plain text vào database.
         */
        try {

            emailService.sendTemporaryPasswordEmail(
                    email.trim(),
                    fullName,
                    result.getTemporaryPassword()
            );

            request.getSession()
                    .setAttribute(
                            "successMessage",
                            "Tạo tài khoản và gửi email mật khẩu tạm thành công."
                    );

        } catch (MessagingException
                 | IllegalStateException e) {

            /*
             * Tài khoản đã tạo thành công nhưng SMTP lỗi.
             *
             * Không tạo lại tài khoản vì sẽ gây trùng email.
             * Ghi log lỗi để kiểm tra cấu hình SMTP.
             */
            getServletContext().log(
                    "Không thể gửi email mật khẩu tạm cho: "
                            + email,
                    e
            );

            request.getSession()
                    .setAttribute(
                            "warningMessage",
                            "Tạo tài khoản thành công nhưng chưa gửi được email mật khẩu tạm."
                    );
        }

        response.sendRedirect(
                request.getContextPath()
                        + "/users"
        );
    }

    /**
     * HTQLKH-8:
     * Cập nhật tài khoản.
     *
     * FE8 hiện chưa quản lý role/status trực tiếp,
     * vì vậy giữ nguyên role/status hiện tại.
     */
    private void updateUser(
            HttpServletRequest request,
            HttpServletResponse response)
            throws SQLException,
            ServletException,
            IOException {

        Long userId =
                parseUserId(
                        request.getParameter("id")
                );

        if (userId == null) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID tài khoản không hợp lệ."
            );

            return;
        }

        User existingUser =
                userService.getUserById(
                        userId
                );

        if (existingUser == null) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Không tìm thấy tài khoản."
            );

            return;
        }

        String fullName =
                request.getParameter("fullName");

        String email =
                request.getParameter("email");

        UserService.UpdateUserResult result =
                userService.updateUser(
                        userId,
                        fullName,
                        email,
                        existingUser.getRole(),
                        existingUser.getStatus()
                );

        /*
         * Validation thất bại hoặc email
         * đã được tài khoản khác sử dụng.
         */
        if (!result.isSuccess()) {

            existingUser.setFullName(
                    fullName
            );

            existingUser.setEmail(
                    email
            );

            request.setAttribute(
                    "user",
                    existingUser
            );

            request.setAttribute(
                    "errorMessage",
                    result.getMessage()
            );

            request.getRequestDispatcher(
                    "/user/form.jsp"
            ).forward(
                    request,
                    response
            );

            return;
        }

        request.getSession()
                .setAttribute(
                        "successMessage",
                        "Cập nhật tài khoản thành công."
                );

        response.sendRedirect(
                request.getContextPath()
                        + "/users"
        );
    }

    /**
     * Parse số trang.
     */
    private int parsePage(
            String value) {

        if (value == null
                || value.isBlank()) {

            return 1;
        }

        try {

            int page =
                    Integer.parseInt(
                            value
                    );

            return Math.max(
                    page,
                    1
            );

        } catch (NumberFormatException e) {

            return 1;
        }
    }

    /**
     * Parse ID tài khoản.
     */
    private Long parseUserId(
            String value) {

        if (value == null
                || value.isBlank()) {

            return null;
        }

        try {

            long userId =
                    Long.parseLong(
                            value
                    );

            return userId > 0
                    ? userId
                    : null;

        } catch (NumberFormatException e) {

            return null;
        }
    }
}