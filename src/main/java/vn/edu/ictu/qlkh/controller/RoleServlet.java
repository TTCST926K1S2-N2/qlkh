package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.edu.ictu.qlkh.model.BusinessGroup;
import vn.edu.ictu.qlkh.model.Role;
import vn.edu.ictu.qlkh.model.User;
import vn.edu.ictu.qlkh.service.BusinessGroupService;
import vn.edu.ictu.qlkh.service.RoleService;
import vn.edu.ictu.qlkh.service.UserService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Gán vai trò và nhóm kinh doanh - HTQLKH-9.
 *
 * GET:
 * /users/role-group?id=1
 *
 * POST:
 * /users/role-group
 */
@WebServlet("/users/role-group")
public class RoleServlet extends HttpServlet {

    private RoleService roleService;
    private BusinessGroupService businessGroupService;
    private UserService userService;

    @Override
    public void init() {

        roleService = new RoleService();
        businessGroupService =
                new BusinessGroupService();
        userService = new UserService();
    }

    /**
     * Hiển thị màn hình phân quyền.
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        if (!isAdmin(request)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền thực hiện chức năng này."
            );
            return;
        }

        Long userId =
                parseLong(
                        request.getParameter("id")
                );

        if (userId == null) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID tài khoản không hợp lệ."
            );

            return;
        }

        try {

            loadRoleGroupPage(
                    request,
                    response,
                    userId,
                    null
            );

        } catch (SQLException e) {

            throw new ServletException(
                    "Không thể tải thông tin phân quyền.",
                    e
            );
        }
    }

    /**
     * Lưu vai trò và nhóm kinh doanh.
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        if (!isAdmin(request)) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền thực hiện chức năng này."
            );

            return;
        }

        HttpSession session =
                request.getSession(false);

        Long currentAdminUserId =
                getSessionUserId(session);

        if (currentAdminUserId == null) {

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Phiên đăng nhập không hợp lệ."
            );

            return;
        }

        Long targetUserId =
                parseLong(
                        request.getParameter("userId")
                );

        if (targetUserId == null) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID tài khoản cần phân quyền không hợp lệ."
            );

            return;
        }

        String[] roleValues =
                request.getParameterValues("roles");

        List<String> roles =
                roleValues == null
                        ? new ArrayList<>()
                        : new ArrayList<>(
                                Arrays.asList(roleValues)
                        );

        Long groupId =
                parseLong(
                        request.getParameter("groupId")
                );

        try {

            /*
             * Service chịu trách nhiệm:
             * - kiểm tra role hợp lệ
             * - cho phép nhiều role
             * - MANAGER phải có group
             * - không cho admin tự bỏ ADMIN
             * - lưu role và group
             */
            roleService.updateRoleAndGroup(
                    currentAdminUserId,
                    targetUserId,
                    roles,
                    groupId
            );

            response.sendRedirect(
                    request.getContextPath()
                            + "/users/role-group?id="
                            + targetUserId
                            + "&success=1"
            );

        } catch (IllegalArgumentException e) {

            try {

                loadRoleGroupPage(
                        request,
                        response,
                        targetUserId,
                        e.getMessage()
                );

            } catch (SQLException sqlException) {

                throw new ServletException(
                        "Không thể tải lại thông tin phân quyền.",
                        sqlException
                );
            }

        } catch (SQLException e) {

            throw new ServletException(
                    "Không thể cập nhật vai trò và nhóm kinh doanh.",
                    e
            );
        }
    }

    /**
     * Chuẩn bị dữ liệu cho role-group.jsp.
     */
    private void loadRoleGroupPage(
            HttpServletRequest request,
            HttpServletResponse response,
            long userId,
            String errorMessage)
            throws SQLException,
            ServletException,
            IOException {

        User user =
                userService.getUserById(userId);

        if (user == null) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Không tìm thấy tài khoản."
            );

            return;
        }

        List<Role> userRoles =
                roleService.getRolesByUserId(userId);

        List<String> selectedRoles =
                new ArrayList<>();

        if (userRoles != null) {

            for (Role role : userRoles) {

                if (role != null
                        && role.getCode() != null) {

                    selectedRoles.add(
                            role.getCode()
                    );
                }
            }
        }

        List<BusinessGroup> businessGroups =
                businessGroupService.getAllGroups();

        BusinessGroup selectedGroup =
                businessGroupService
                        .getGroupByUserId(userId);

        Long selectedGroupId =
                selectedGroup == null
                        ? null
                        : selectedGroup.getId();

        request.setAttribute(
                "user",
                user
        );

        request.setAttribute(
                "selectedRoles",
                selectedRoles
        );

        request.setAttribute(
                "businessGroups",
                businessGroups
        );

        request.setAttribute(
                "selectedGroupId",
                selectedGroupId
        );

        if (errorMessage != null
                && !errorMessage.isBlank()) {

            request.setAttribute(
                    "errorMessage",
                    errorMessage
            );
        }

        if ("1".equals(
                request.getParameter("success"))) {

            request.setAttribute(
                    "successMessage",
                    "Cập nhật vai trò và nhóm kinh doanh thành công."
            );
        }

        request.getRequestDispatcher(
                "/user/role-group.jsp"
        ).forward(
                request,
                response
        );
    }

    /**
     * Chỉ ADMIN được sử dụng chức năng HTQLKH-9.
     */
    private boolean isAdmin(
            HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);

        if (session == null) {
            return false;
        }

        Object role =
                session.getAttribute("userRole");

        return role != null
                && "ADMIN".equalsIgnoreCase(
                        role.toString().trim()
                );
    }

    /**
     * Lấy userId của người đang đăng nhập.
     */
    private Long getSessionUserId(
            HttpSession session) {

        if (session == null) {
            return null;
        }

        Object value =
                session.getAttribute("userId");

        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {

            long id =
                    number.longValue();

            return id > 0
                    ? id
                    : null;
        }

        return parseLong(
                value.toString()
        );
    }

    /**
     * Parse số Long dương.
     */
    private Long parseLong(
            String value) {

        if (value == null
                || value.isBlank()) {

            return null;
        }

        try {

            long number =
                    Long.parseLong(
                            value.trim()
                    );

            return number > 0
                    ? number
                    : null;

        } catch (NumberFormatException e) {

            return null;
        }
    }
}