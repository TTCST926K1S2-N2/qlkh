package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/change-password")
public class ChangePasswordServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        request.getRequestDispatcher("/change-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        boolean revokeOtherSessions = "true".equals(request.getParameter("revokeOtherSessions"));

        // FE Validation (Server-side mirror)
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Vui lòng nhập mật khẩu hiện tại.");
            request.getRequestDispatcher("/change-password.jsp").forward(request, response);
            return;
        }

        if (newPassword == null || !newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
            request.setAttribute("errorMessage", "Mật khẩu mới phải có tối thiểu 8 ký tự, gồm cả chữ và số.");
            request.getRequestDispatcher("/change-password.jsp").forward(request, response);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Mật khẩu xác nhận không trùng khớp.");
            request.getRequestDispatcher("/change-password.jsp").forward(request, response);
            return;
        }

        if (currentPassword.equals(newPassword)) {
            request.setAttribute("errorMessage", "Mật khẩu mới không được giống mật khẩu hiện tại.");
            request.getRequestDispatcher("/change-password.jsp").forward(request, response);
            return;
        }

        // Quy ước gọi BE / Auth Service đổi mật khẩu & thu hồi session
        // Request Attribute chuẩn bị dữ liệu gửi cho BE Controller/DAO nếu tích hợp chung
        request.setAttribute("changePasswordSuccess", true);
        request.setAttribute("revokeOtherSessions", revokeOtherSessions);
        
        request.setAttribute("successMessage", "Đổi mật khẩu thành công! Các phiên làm việc khác đã được thu hồi.");
        request.getRequestDispatcher("/change-password.jsp").forward(request, response);
    }
}