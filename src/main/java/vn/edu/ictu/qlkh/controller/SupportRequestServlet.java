package vn.edu.ictu.qlkh.controller;

import vn.edu.ictu.qlkh.dao.SupportRequestDAO;
import vn.edu.ictu.qlkh.model.SupportRequest;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/support-request")
public class SupportRequestServlet extends HttpServlet {

    private SupportRequestDAO supportRequestDAO = new SupportRequestDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        String description = request.getParameter("description");
        String priority = request.getParameter("priority");
        
        HttpSession session = request.getSession();
        Long customerId = (Long) session.getAttribute("userId"); 

        if (customerId == null) {
            String customerIdParam = request.getParameter("customerId");
            if (customerIdParam != null) {
                customerId = Long.parseLong(customerIdParam);
            }
        }

        PrintWriter out = response.getWriter();

        if (customerId == null || description == null || description.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"status\": \"error\", \"message\": \"Thiếu thông tin bắt buộc!\"}");
            return;
        }

        SupportRequest supportRequest = new SupportRequest();
        supportRequest.setCustomerId(customerId);
        supportRequest.setDescription(description);
        supportRequest.setPriority(priority != null ? priority : "MEDIUM");
        supportRequest.setStatus("PENDING");

        boolean success = supportRequestDAO.createRequest(supportRequest);

        if (success) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print("{\"status\": \"success\", \"message\": \"Tạo yêu cầu hỗ trợ thành công!\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"status\": \"error\", \"message\": \"Không thể tạo yêu cầu hỗ trợ.\"}");
        }
    }
}