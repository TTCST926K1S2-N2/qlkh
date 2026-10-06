package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "SupportRequestServlet", urlPatterns = {"/supports", "/supports/create", "/supports/update"})
public class SupportRequestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getServletPath();
        if ("/supports/create".equals(action)) {
            request.getRequestDispatcher("/support/form.jsp").forward(request, response);
        } else {
            request.getRequestDispatcher("/support/list.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getServletPath();
        
        if ("/supports/create".equals(action)) {
            String title = request.getParameter("title");
            String customerId = request.getParameter("customerId");
            String priority = request.getParameter("priority");
            String assignee = request.getParameter("assignee");
            String description = request.getParameter("description");
            
            response.sendRedirect(request.getContextPath() + "/supports?message=create_success");
        } else if ("/supports/update".equals(action)) {
            String id = request.getParameter("id");
            String status = request.getParameter("status");
            
            response.sendRedirect(request.getContextPath() + "/supports?message=update_success");
        } else {
            response.sendRedirect(request.getContextPath() + "/supports");
        }
    }
}
