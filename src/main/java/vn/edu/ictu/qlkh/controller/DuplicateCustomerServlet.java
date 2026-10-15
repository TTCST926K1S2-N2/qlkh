package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "DuplicateCustomerServlet", urlPatterns = {"/customers/duplicates", "/customers/merge"})
public class DuplicateCustomerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getServletPath();
        if ("/customers/duplicates".equals(action)) {
            // Xử lý hiển thị giao diện cảnh báo và so sánh khách hàng trùng
            request.getRequestDispatcher("/customer/duplicate-merge.jsp").forward(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/customers");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getServletPath();
        
        if ("/customers/merge".equals(action)) {
            // Xử lý logic gộp khách hàng (Nhận ID hồ sơ chính và hồ sơ trùng)
            String primaryId = request.getParameter("primaryId");
            String duplicateId = request.getParameter("duplicateId");
            
            // TODO: Viết logic JDBC/DAO xử lý gộp dữ liệu tại đây
            
            response.sendRedirect(request.getContextPath() + "/customers/duplicates?message=merge_success");
        } else {
            response.sendRedirect(request.getContextPath() + "/customers");
        }
    }
}
