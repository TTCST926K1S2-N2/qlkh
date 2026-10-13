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
import java.sql.Statement;
import vn.edu.ictu.qlkh.util.DBConnection;

@WebServlet(name = "ConvertLeadServlet", urlPatterns = {"/leads/convert", "/leads/execute-convert"})
public class ConvertLeadServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("role") : null;
        if (role == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getServletPath();
        if ("/leads/convert".equals(action)) {
            String idStr = request.getParameter("id");
            if (idStr == null || idStr.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/leads?error=not_found");
                return;
            }

            try {
                int leadId = Integer.parseInt(idStr);
                try (Connection conn = DBConnection.getConnection()) {
                    LeadInfo lead = getLeadById(conn, leadId);
                    if (lead == null) {
                        request.setAttribute("errorMessage", "Không tìm thấy thông tin Lead trong hệ thống.");
                    } else {
                        boolean isQualified = "Đủ điều kiện".equals(lead.getStatus()) || "Tiềm năng".equals(lead.getStatus()) || "Mới".equals(lead.getStatus());
                        request.setAttribute("lead", lead);
                        request.setAttribute("isQualified", isQualified);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("errorMessage", "Lỗi cơ sở dữ liệu khi tải thông tin lead.");
            }

            request.getRequestDispatcher("/lead/convert.jsp").forward(request, response);
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
        if ("/leads/execute-convert".equals(action)) {
            String idStr = request.getParameter("leadId");
            if (idStr == null || idStr.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/leads?error=invalid_lead");
                return;
            }

            try {
                int leadId = Integer.parseInt(idStr);
                boolean success = convertLeadToCustomer(leadId);
                if (success) {
                    response.sendRedirect(request.getContextPath() + "/leads?message=convert_success");
                } else {
                    response.sendRedirect(request.getContextPath() + "/leads/convert?id=" + leadId + "&error=convert_failed");
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.sendRedirect(request.getContextPath() + "/leads?error=db_error");
            }
        }
    }

    private LeadInfo getLeadById(Connection conn, int id) throws SQLException {
        String sql = "SELECT id, full_name, email, phone, company, source, status, owner FROM leads WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new LeadInfo(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("company"),
                        rs.getString("source"),
                        rs.getString("status"),
                        rs.getString("owner")
                    );
                }
            }
        }
        return null;
    }

    private boolean convertLeadToCustomer(int leadId) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                LeadInfo lead = getLeadById(conn, leadId);
                if (lead == null) {
                    conn.rollback();
                    return false;
                }

                String insertCust = "INSERT INTO customers (name, tax_code, industry, scale, website, address, owner, status) VALUES (?, '', 'Khác', 'Nhỏ', '', '', ?, 'ACTIVE')";
                int newCustomerId = 0;
                try (PreparedStatement psCust = conn.prepareStatement(insertCust, Statement.RETURN_GENERATED_KEYS)) {
                    psCust.setString(1, lead.getCompany() != null && !lead.getCompany().isEmpty() ? lead.getCompany() : lead.getFullName());
                    psCust.setString(2, lead.getOwner());
                    psCust.executeUpdate();
                    try (ResultSet rsKeys = psCust.getGeneratedKeys()) {
                        if (rsKeys.next()) {
                            newCustomerId = rsKeys.getInt(1);
                        }
                    }
                }

                if (newCustomerId == 0) {
                    conn.rollback();
                    return false;
                }

                String updateLead = "UPDATE leads SET status = 'CONVERTED' WHERE id = ?";
                try (PreparedStatement psLead = conn.prepareStatement(updateLead)) {
                    psLead.setInt(1, leadId);
                    psLead.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static class LeadInfo {
        private int id;
        private String fullName, email, phone, company, source, status, owner;
        public LeadInfo(int id, String fullName, String email, String phone, String company, String source, String status, String owner) {
            this.id = id; this.fullName = fullName; this.email = email; this.phone = phone; this.company = company; this.source = source; this.status = status; this.owner = owner;
        }
        public int getId() { return id; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getCompany() { return company; }
        public String getSource() { return source; }
        public String getStatus() { return status; }
        public String getOwner() { return owner; }
    }
}