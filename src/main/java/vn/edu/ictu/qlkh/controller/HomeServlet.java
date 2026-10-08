package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.ictu.qlkh.dao.CustomerDAO;
import vn.edu.ictu.qlkh.model.Customer;
import vn.edu.ictu.qlkh.model.DataScope;
import vn.edu.ictu.qlkh.service.PermissionService;
import vn.edu.ictu.qlkh.util.DBConnection;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {
    private final PermissionService permissions = new PermissionService();

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        try {
            long userId = Long.parseLong(session.getAttribute("userId").toString());
            String role = String.valueOf(session.getAttribute("userRole"));
            DataScope scope = permissions.resolveDataScope(role);
            if (scope == null) { res.sendError(403); return; }

            List<Customer> customers = new CustomerDAO().findVisible(userId, scope.name());
            req.setAttribute("dashboardCustomers", customers);
            req.setAttribute("dashboardCustomerCount", customers.size());
            req.setAttribute("dashboardScope", scope.name());

            Map<String, Integer> byStatus = new LinkedHashMap<>();
            Map<String, Integer> byIndustry = new LinkedHashMap<>();
            Map<String, Integer> byMonth = new TreeMap<>();
            for (Customer c : customers) {
                String status = c.getStatus() == null || c.getStatus().isBlank() ? "Chưa phân loại" : c.getStatus();
                String industry = c.getIndustry() == null || c.getIndustry().isBlank() ? "Chưa cập nhật" : c.getIndustry();
                byStatus.merge(status, 1, Integer::sum);
                byIndustry.merge(industry, 1, Integer::sum);
                if (c.getCreatedAt() != null) {
                    String month = c.getCreatedAt().toLocalDate().withDayOfMonth(1).toString().substring(0, 7);
                    byMonth.merge(month, 1, Integer::sum);
                }
            }
            req.setAttribute("dashboardByStatus", byStatus);
            req.setAttribute("dashboardByIndustry", byIndustry);
            req.setAttribute("dashboardByMonth", byMonth);

            // Dùng danh sách khách hàng đã được DAO lọc quyền, không đếm toàn hệ thống.
            int contacts = 0;
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM contacts WHERE customer_id = ?")) {
                for (Customer c : customers) {
                    ps.setLong(1, c.getId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) contacts += rs.getInt(1);
                    }
                }
                req.setAttribute("dashboardContactCount", contacts);
            } catch (SQLException ex) {
                req.setAttribute("dashboardContactCount", "—");
                req.setAttribute("dashboardContactError", "Không thể đọc bảng người liên hệ.");
            }

            // Nhật ký hiện chứa thao tác quản trị tài khoản. Chỉ ADMIN được xem.
            if (scope == DataScope.ALL) {
                List<Map<String, String>> logs = new ArrayList<>();
                try (Connection con = DBConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement(
                         "SELECT username, action_type, target_object, action_time " +
                         "FROM audit_logs ORDER BY action_time DESC, id DESC LIMIT 6");
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, String> log = new LinkedHashMap<>();
                        log.put("username", rs.getString("username"));
                        log.put("action", rs.getString("action_type"));
                        log.put("target", rs.getString("target_object"));
                        Timestamp t = rs.getTimestamp("action_time");
                        log.put("time", t == null ? "" : t.toString());
                        logs.add(log);
                    }
                    req.setAttribute("dashboardAuditLogs", logs);
                } catch (SQLException ex) {
                    req.setAttribute("dashboardAuditError", "Chưa thể tải nhật ký hệ thống.");
                }
            }
        } catch (SQLException | NumberFormatException ex) {
            req.setAttribute("dashboardError", "Không thể tải thống kê. Vui lòng kiểm tra kết nối dữ liệu.");
        }
        req.getRequestDispatcher("/index.jsp").forward(req, res);
    }
}
