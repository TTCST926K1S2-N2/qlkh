package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.ictu.qlkh.model.DataScope;
import vn.edu.ictu.qlkh.service.PermissionService;

import java.io.IOException;

@WebServlet("/permission")
public class PermissionServlet extends HttpServlet {

    private final PermissionService permissionService =
            new PermissionService();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // Chưa đăng nhập: để JSP hiển thị giao diện yêu cầu đăng nhập.
        if (session == null) {
            request.getRequestDispatcher(
                    "/views/account/permission-info.jsp"
            ).forward(request, response);
            return;
        }

        String role = (String) session.getAttribute("userRole");

        // Tương thích tạm với code cũ nếu session đang dùng "role".
        if (role == null || role.isBlank()) {
            role = (String) session.getAttribute("role");
        }

        request.setAttribute("userRole", role);

        DataScope dataScope =
                permissionService.resolveDataScope(role);

        if (dataScope != null) {
            // FE hiện nhận dataScope dưới dạng String.
            request.setAttribute(
                    "dataScope",
                    dataScope.name()
            );
        } else if (role != null && !role.isBlank()) {
            request.setAttribute(
                    "permissionError",
                    "Vai trò hiện tại chưa được cấu hình phạm vi dữ liệu."
            );
        }

        request.getRequestDispatcher(
                "/views/account/permission-info.jsp"
        ).forward(request, response);
    }
}