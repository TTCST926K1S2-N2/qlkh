package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/lead-list")
public class LeadListPageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Object roleObject = session.getAttribute("userRole");
        String role = roleObject == null ? "" : roleObject.toString();

        if (!"SALES".equals(role)
                && !"MANAGER".equals(role)
                && !"ADMIN".equals(role)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Access denied"
            );
            return;
        }

        request.getRequestDispatcher(
                "/views/sales/lead-list.jsp"
        ).forward(request, response);
    }
}