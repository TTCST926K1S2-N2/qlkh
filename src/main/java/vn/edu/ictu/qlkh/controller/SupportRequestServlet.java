package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import vn.edu.ictu.qlkh.util.DBConnection;

@WebServlet(name = "SupportRequestServlet", urlPatterns = {"/supports", "/supports/create", "/supports/detail", "/supports/update-status"})
public class SupportRequestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("role") : null;
        if (role == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getServletPath();
        switch (action) {
            case "/supports":
                listSupports(request, response);
                break;
            case "/supports/create":
                showCreateForm(request, response);
                break;
            case "/supports/detail":
                showDetail(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/supports");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("role") : null;
        if (role == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getServletPath();
        if ("/supports/create".equals(action)) {
            handleCreate(request, response);
        } else if ("/supports/update-status".equals(action)) {
            handleUpdateStatus(request, response);
        }
    }

    private void listSupports(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<SupportRequest> list = new ArrayList<>();
        String sql = "SELECT s.id, s.title, s.priority, s.assignee, s.status, c.name as customer_name " +
                     "FROM support_requests s LEFT JOIN customers c ON s.customer_id = c.id ORDER BY s.id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new SupportRequest(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("priority"),
                    rs.getString("assignee"),
                    rs.getString("status"),
                    rs.getString("customer_name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        request.setAttribute("supportList", list);
        request.getRequestDispatcher("/support/list.jsp").forward(request, response);
    }

    private void showCreateForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<CustomerOption> customers = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, name FROM customers");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                customers.add(new CustomerOption(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        request.setAttribute("customerList", customers);
        request.getRequestDispatcher("/support/form.jsp").forward(request, response);
    }

    private void handleCreate(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String title = request.getParameter("title");
        String customerIdStr = request.getParameter("customerId");
        String priority = request.getParameter("priority");
        String assignee = request.getParameter("assignee");
        String description = request.getParameter("description");

        if (title == null || title.trim().isEmpty() || customerIdStr == null || customerIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/supports/create?error=invalid_input");
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO support_requests (title, customer_id, priority, assignee, description, status) VALUES (?, ?, ?, ?, ?, 'Mới')")) {
            ps.setString(1, title);
            ps.setInt(2, Integer.parseInt(customerIdStr));
            ps.setString(3, priority);
            ps.setString(4, assignee);
            ps.setString(5, description);
            ps.executeUpdate();
            response.sendRedirect(request.getContextPath() + "/supports?message=create_success");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/supports/create?error=db_error");
        }
    }

    private void showDetail(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null) {
            response.sendRedirect(request.getContextPath() + "/supports");
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT s.*, c.name as customer_name FROM support_requests s LEFT JOIN customers c ON s.customer_id = c.id WHERE s.id = ?")) {
            ps.setInt(1, Integer.parseInt(idStr));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SupportRequest req = new SupportRequest(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("priority"),
                        rs.getString("assignee"),
                        rs.getString("status"),
                        rs.getString("customer_name")
                    );
                    req.setDescription(rs.getString("description"));
                    request.setAttribute("support", req);
                    request.getRequestDispatcher("/support/detail.jsp").forward(request, response);
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.sendRedirect(request.getContextPath() + "/supports?error=not_found");
    }

    private void handleUpdateStatus(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idStr = request.getParameter("id");
        String status = request.getParameter("status");

        if (idStr == null || status == null) {
            response.sendRedirect(request.getContextPath() + "/supports?error=invalid_update");
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE support_requests SET status = ? WHERE id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, Integer.parseInt(idStr));
            ps.executeUpdate();
            response.sendRedirect(request.getContextPath() + "/supports/detail?id=" + idStr + "&message=update_success");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/supports?error=db_error");
        }
    }

    public static class SupportRequest {
        private int id;
        private String title, priority, assignee, status, customerName, description;
        public SupportRequest(int id, String title, String priority, String assignee, String status, String customerName) {
            this.id = id; this.title = title; this.priority = priority; this.assignee = assignee; this.status = status; this.customerName = customerName;
        }
        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getPriority() { return priority; }
        public String getAssignee() { return assignee; }
        public String getStatus() { return status; }
        public String getCustomerName() { return customerName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class CustomerOption {
        private int id;
        private String name;
        public CustomerOption(int id, String name) { this.id = id; this.name = name; }
        public int getId() { return id; }
        public String getName() { return name; }
    }
}
