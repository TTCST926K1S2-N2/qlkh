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
import vn.edu.ictu.qlkh.util.DBConnection;

@WebServlet(name = "DuplicateLeadServlet", urlPatterns = {"/leads/duplicates", "/leads/merge"})
public class DuplicateLeadServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("role") : null;
        if (role == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getServletPath();
        if ("/leads/duplicates".equals(action)) {
            String id1Str = request.getParameter("id1");
            String id2Str = request.getParameter("id2");

            if (id1Str == null || id2Str == null || id1Str.isEmpty() || id2Str.isEmpty()) {
                request.setAttribute("errorMessage", "Không tìm thấy thông tin lead trùng để đối chiếu.");
                request.getRequestDispatcher("/lead/duplicate-merge.jsp").forward(request, response);
                return;
            }

            try {
                int id1 = Integer.parseInt(id1Str);
                int id2 = Integer.parseInt(id2Str);

                try (Connection conn = DBConnection.getConnection()) {
                    LeadData l1 = getLeadById(conn, id1);
                    LeadData l2 = getLeadById(conn, id2);

                    if (l1 == null || l2 == null) {
                        request.setAttribute("errorMessage", "Không tồn tại dữ liệu lead trong cơ sở dữ liệu.");
                    } else {
                        request.setAttribute("lead1", l1);
                        request.setAttribute("lead2", l2);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("errorMessage", "Lỗi kết nối cơ sở dữ liệu khi tải lead trùng.");
            }

            request.getRequestDispatcher("/lead/duplicate-merge.jsp").forward(request, response);
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
        if ("/leads/merge".equals(action)) {
            String primaryIdStr = request.getParameter("primaryId");
            String duplicateIdStr = request.getParameter("duplicateId");

            if (primaryIdStr == null || duplicateIdStr == null || primaryIdStr.equals(duplicateIdStr)) {
                response.sendRedirect(request.getContextPath() + "/leads/duplicates?error=invalid_selection");
                return;
            }

            try {
                int primaryId = Integer.parseInt(primaryIdStr);
                int duplicateId = Integer.parseInt(duplicateIdStr);

                boolean success = performMerge(primaryId, duplicateId);
                if (success) {
                    response.sendRedirect(request.getContextPath() + "/leads?message=merge_success");
                } else {
                    response.sendRedirect(request.getContextPath() + "/leads/duplicates?error=merge_failed&id1=" + primaryId + "&id2=" + duplicateId);
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.sendRedirect(request.getContextPath() + "/leads/duplicates?error=db_error");
            }
        }
    }

    private LeadData getLeadById(Connection conn, int id) throws SQLException {
        String sql = "SELECT id, full_name, email, phone, company, source, status, owner FROM leads WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new LeadData(
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

    private boolean performMerge(int primaryId, int duplicateId) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Chuyển các hoạt động/lịch sử tương tác sang lead chính
                String updateActivity = "UPDATE lead_activities SET lead_id = ? WHERE lead_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateActivity)) {
                    ps.setInt(1, primaryId);
                    ps.setInt(2, duplicateId);
                    ps.executeUpdate();
                }

                // Vô hiệu hóa lead trùng
                String deactivate = "UPDATE leads SET status = 'MERGED', merged_into = ? WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deactivate)) {
                    ps.setInt(1, primaryId);
                    ps.setInt(2, duplicateId);
                    ps.executeUpdate();
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

    public static class LeadData {
        private int id;
        private String fullName, email, phone, company, source, status, owner;
        public LeadData(int id, String fullName, String email, String phone, String company, String source, String status, String owner) {
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
