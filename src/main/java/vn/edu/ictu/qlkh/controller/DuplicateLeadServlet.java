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

@WebServlet(name = "DuplicateLeadServlet", urlPatterns = {"/leads/duplicates", "/leads/merge", "/leads/link-customer"})
public class DuplicateLeadServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!vn.edu.ictu.qlkh.service.SessionService.isAuthenticated(session)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getServletPath();
        if ("/leads/duplicates".equals(action)) {
            String id1Str = request.getParameter("id1");
            String id2Str = request.getParameter("id2");

            if (id1Str == null || id2Str == null || id1Str.trim().isEmpty() || id2Str.trim().isEmpty()) {
                // Load merge history for the history tab.
                java.util.List<Object[]> mergeHistory = new java.util.ArrayList<>();

                String historySql =
                        "SELECT source_lead_id, target_lead_id, merged_by, merged_at " +
                        "FROM lead_merges ORDER BY merged_at DESC, id DESC LIMIT 100";

                try (Connection historyConn = DBConnection.getConnection();
                     PreparedStatement historyPs = historyConn.prepareStatement(historySql);
                     ResultSet historyRs = historyPs.executeQuery()) {

                    while (historyRs.next()) {
                        mergeHistory.add(new Object[]{
                                historyRs.getLong("source_lead_id"),
                                historyRs.getLong("target_lead_id"),
                                historyRs.getLong("merged_by"),
                                historyRs.getTimestamp("merged_at")
                        });
                    }

                    request.setAttribute("mergeHistory", mergeHistory);
                } catch (SQLException e) {
                    e.printStackTrace();
                    request.setAttribute("historyError", "Không thể tải lịch sử gộp Lead.");
                }

                java.util.List<long[]> duplicatePairs = new java.util.ArrayList<>();

                String sql = "SELECT a.id AS id1, b.id AS id2 " +
                        "FROM leads a JOIN leads b ON a.id < b.id " +
                        "WHERE a.status <> 'MERGED' AND b.status <> 'MERGED' " +
                        "AND NOT EXISTS (SELECT 1 FROM lead_merges m WHERE m.source_lead_id IN (a.id,b.id)) " +
                        "AND ((a.email IS NOT NULL AND TRIM(a.email) <> '' " +
                        "AND LOWER(TRIM(a.email)) = LOWER(TRIM(b.email))) " +
                        "OR (a.phone IS NOT NULL AND TRIM(a.phone) <> '' " +
                        "AND TRIM(a.phone) = TRIM(b.phone))) " +
                        "ORDER BY a.id DESC, b.id DESC LIMIT 100";

                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        duplicatePairs.add(new long[]{
                                rs.getLong("id1"),
                                rs.getLong("id2")
                        });
                    }

                    request.setAttribute("duplicatePairs", duplicatePairs);
                } catch (SQLException e) {
                    e.printStackTrace();
                    request.setAttribute("errorMessage", "Không thể tải danh sách Lead trùng.");
                }

                request.getRequestDispatcher("/lead/duplicate-list.jsp").forward(request, response);
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
                        // Verify merge result against database, not URL alone.
                        boolean mergeVerified = false;

                        if ("merge_success".equals(request.getParameter("message"))) {
                            String verifySql =
                                    "SELECT COUNT(*) FROM lead_merges m " +
                                    "JOIN leads d ON d.id = m.source_lead_id " +
                                    "WHERE m.target_lead_id = ? " +
                                    "AND m.source_lead_id = ? " +
                                    "AND d.status = 'MERGED'";

                            try (PreparedStatement verifyPs = conn.prepareStatement(verifySql)) {
                                verifyPs.setLong(1, id1);
                                verifyPs.setLong(2, id2);

                                try (ResultSet verifyRs = verifyPs.executeQuery()) {
                                    mergeVerified = verifyRs.next()
                                            && verifyRs.getLong(1) > 0;
                                }
                            }
                        }

                        request.setAttribute("mergeVerified", mergeVerified);
                        request.setAttribute("lead1", l1);
                        request.setAttribute("lead2", l2);
                        request.setAttribute("suggestedCustomer", findMatchingCustomer(conn, l1));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("errorMessage", "Lỗi cơ sở dữ liệu khi tải lead trùng.");
            }
            request.getRequestDispatcher("/lead/duplicate-merge.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        if (!vn.edu.ictu.qlkh.service.SessionService.isAuthenticated(session)) {
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
                boolean success = performMerge(primaryId, duplicateId, ((Number) session.getAttribute("userId")).longValue());
                if (success) {
                    response.sendRedirect(request.getContextPath() + "/leads/duplicates?message=merge_success&id1=" + primaryId + "&id2=" + duplicateId);
                } else {
                    response.sendRedirect(request.getContextPath() + "/leads/duplicates?error=merge_failed&id1=" + primaryId + "&id2=" + duplicateId);
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.sendRedirect(request.getContextPath() + "/leads/duplicates?error=db_error");
            }
        } else if ("/leads/link-customer".equals(action)) {
            String leadIdStr = request.getParameter("leadId");
            String customerIdStr = request.getParameter("customerId");
            try {
                int leadId = Integer.parseInt(leadIdStr);
                int customerId = Integer.parseInt(customerIdStr);
                boolean linked = linkLeadToExistingCustomer(leadId, customerId);
                if (linked) {
                    response.sendRedirect(request.getContextPath() + "/leads?message=link_success");
                } else {
                    response.sendRedirect(request.getContextPath() + "/leads?error=link_failed");
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.sendRedirect(request.getContextPath() + "/leads?error=db_error");
            }
        }
    }

    private LeadData getLeadById(Connection conn, int id) throws SQLException {
        String sql = "SELECT id, full_name, email, phone, company, source, status, assigned_to FROM leads WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new LeadData(rs.getInt("id"), rs.getString("full_name"), rs.getString("email"), rs.getString("phone"), rs.getString("company"), rs.getString("source"), rs.getString("status"), rs.getString("assigned_to"));
                }
            }
        }
        return null;
    }

    private String findMatchingCustomer(Connection conn, LeadData lead) throws SQLException {
        if (lead.getCompany() == null || lead.getCompany().trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT id, company_name FROM customers WHERE LOWER(TRIM(company_name)) = LOWER(TRIM(?))";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, lead.getCompany().trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("company_name") + "|" + rs.getLong("id");
                }
            }
        }
        return null;
    }

    private boolean performMerge(long primaryId, long duplicateId, long userId) {
        if (primaryId <= 0 || duplicateId <= 0 || primaryId == duplicateId || userId <= 0) {
            return false;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Lock theo thu tu ID de giam nguy co deadlock.
                String lockSql =
                        "SELECT id, status, converted_customer_id FROM leads " +
                        "WHERE id IN (?, ?) ORDER BY id FOR UPDATE";

                int found = 0;

                try (PreparedStatement ps = conn.prepareStatement(lockSql)) {
                    ps.setLong(1, primaryId);
                    ps.setLong(2, duplicateId);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            found++;

                            String status = rs.getString("status");
                            Object convertedCustomer = rs.getObject("converted_customer_id");

                            if ("MERGED".equalsIgnoreCase(status)
                                    || "CONVERTED".equalsIgnoreCase(status)
                                    || convertedCustomer != null) {
                                conn.rollback();
                                return false;
                            }
                        }
                    }
                }

                if (found != 2) {
                    conn.rollback();
                    return false;
                }

                String checkSql =
                        "SELECT COUNT(*) FROM lead_merges " +
                        "WHERE source_lead_id IN (?, ?)";

                try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                    ps.setLong(1, primaryId);
                    ps.setLong(2, duplicateId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getLong(1) > 0) {
                            conn.rollback();
                            return false;
                        }
                    }
                }

                String insertSql =
                        "INSERT INTO lead_merges " +
                        "(source_lead_id, target_lead_id, merged_by) " +
                        "VALUES (?, ?, ?)";

                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setLong(1, duplicateId);
                    ps.setLong(2, primaryId);
                    ps.setLong(3, userId);

                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return false;
                    }
                }

                String updateSql =
                        "UPDATE leads SET status = 'MERGED' " +
                        "WHERE id = ? AND status <> 'MERGED'";

                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setLong(1, duplicateId);

                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return false;
                    }
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
    private boolean linkLeadToExistingCustomer(int leadId, int customerId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE leads SET status = 'CONVERTED', converted_customer_id = ? WHERE id = ? AND converted_customer_id IS NULL AND status NOT IN ('MERGED', 'CONVERTED') AND EXISTS (SELECT 1 FROM customers WHERE customers.id = ? AND LOWER(TRIM(customers.company_name)) = LOWER(TRIM(leads.company)))")) {
            ps.setInt(1, customerId);
            ps.setInt(2, leadId);
            ps.setInt(3, customerId);
            return ps.executeUpdate() > 0;
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