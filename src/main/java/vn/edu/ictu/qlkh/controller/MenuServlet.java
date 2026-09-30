package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.edu.ictu.qlkh.dao.RoleDAO;
import vn.edu.ictu.qlkh.service.MenuService;

import java.io.IOException;

/**
 * HTQLKH-6 - Backend cung cấp thông tin quyền
 * phục vụ menu điều hướng.
 */
@WebServlet("/menu")
public class MenuServlet extends HttpServlet {

    private MenuService menuService;
    private RoleDAO roleDAO;

    @Override
    public void init() {
        menuService = new MenuService();
        roleDAO = new RoleDAO();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        /*
         * Chưa đăng nhập -> quay về trang đăng nhập.
         */
        if (session == null
                || session.getAttribute("userId") == null
                || session.getAttribute("userRole") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/login"
            );

            return;
        }

        /*
         * Lấy thông tin người dùng hiện tại từ session.
         */
        String userName =
                (String) session.getAttribute("userName");

        String userRole =
                (String) session.getAttribute("userRole");

        /*
         * Chuyển mã role thành tên hiển thị tiếng Việt.
         *
         * ADMIN   -> Quản trị hệ thống
         * MANAGER -> Quản lý kinh doanh
         * SALES   -> Nhân viên kinh doanh
         */
        String userRoleName =
                roleDAO.getRoleDisplayName(userRole);

        /*
         * Kiểm tra role và cấp quyền menu.
         *
         * Role không hợp lệ sẽ không được cấp
         * bất kỳ menu được bảo vệ nào.
         */
        if (!menuService.isSupportedRole(userRole)) {

            request.setAttribute(
                    "menuError",
                    "Vai trò người dùng không hợp lệ."
            );

            request.setAttribute(
                    "menuPermissions",
                    MenuService.MenuPermissions.noPermission()
            );

        } else {

            request.setAttribute(
                    "menuPermissions",
                    menuService.getMenuPermissions(userRole)
            );
        }

        /*
         * =====================================================
         * THÔNG TIN NGƯỜI DÙNG CUNG CẤP CHO FRONTEND
         * =====================================================
         */

        request.setAttribute(
                "currentUserName",
                userName
        );

        /*
         * Mã vai trò:
         * ADMIN / MANAGER / SALES
         */
        request.setAttribute(
                "currentUserRole",
                userRole
        );

        /*
         * Tên vai trò hiển thị:
         * Quản trị hệ thống
         * Quản lý kinh doanh
         * Nhân viên kinh doanh
         */
        request.setAttribute(
                "currentUserRoleName",
                userRoleName
        );

        /*
         * =====================================================
         * NHÓM KINH DOANH
         * =====================================================
         *
         * HTQLKH-6 yêu cầu hiển thị nhóm kinh doanh
         * mà người dùng đang thuộc về.
         *
         * Schema users hiện tại chưa có business_group_id
         * và chưa có quan hệ user -> business group.
         *
         * Vì vậy backend KHÔNG tự tạo dữ liệu nhóm giả.
         *
         * Khi chức năng quản lý nhóm cung cấp quan hệ
         * user -> business group chính thức, giá trị
         * businessGroupName sẽ được lấy từ database/session.
         */

        String businessGroupName =
                (String) session.getAttribute("businessGroupName");

        if (businessGroupName == null
                || businessGroupName.isBlank()) {

            businessGroupName = "Chưa thuộc nhóm";
        }

        request.setAttribute(
                "currentBusinessGroup",
                businessGroupName
        );

        /*
         * =====================================================
         * CONTRACT BE -> FE HTQLKH-6
         * =====================================================
         *
         * currentUserName
         *      -> Tên người dùng
         *
         * currentUserRole
         *      -> ADMIN / MANAGER / SALES
         *
         * currentUserRoleName
         *      -> Tên vai trò tiếng Việt
         *
         * currentBusinessGroup
         *      -> Tên nhóm kinh doanh hoặc "Chưa thuộc nhóm"
         *
         * menuPermissions
         *      -> Quyền hiển thị từng menu
         */

        /*
         * Forward tới sidebar để frontend hiển thị.
         */
        request.getRequestDispatcher(
                "/components/sidebar.jsp"
        ).forward(request, response);
    }
}