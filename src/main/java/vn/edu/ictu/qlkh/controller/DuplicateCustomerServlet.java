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

@WebServlet(name = "DuplicateCustomerServlet", urlPatterns = {"/customers/duplicates", "/customers/merge"})
public class DuplicateCustomerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("role") : null;
        
        if (role == null || (!role.equals("Trưởng nhóm kinh doanh") && !role.equals("ADMIN"))) {
            response.sendRedirect(request.getContextPath() + "/customers?error=unauthorized");
            return;
        }

        String action = request.getServletPath();
        if ("/customers/duplicates".equals(action)) {
            String id1Str = request.getParameter("id1");
            String id2Str = request.getParameter("id2");

            if (id1Str == null || id2Str == null || id1Str.isEmpty() || id2Str.isEmpty()) {
                request.setAttribute("errorMessage", "Không tìm thấy hồ sơ khách hàng trùng để so sánh.");
                request.getRequestDispatcher("/customer/duplicate-merge.jsp").forward(request, response);
                return;
            }

            try {
                int id1 = Integer.parseInt(id1Str);
                int id2 = Integer.parseInt(id2Str);

                try (Connection conn = DBConnection.getConnection()) {
                    CustomerData c1 = getCustomerById(conn, id1);
                    CustomerData c2 = getCustomerById(conn, id2);

                    if (c1 == null || c2 == null) {
                        request.setAttribute("errorMessage", "Không tìm thấy thông tin khách hàng trong cơ sở dữ liệu.");
                    } else {
                        request.setAttribute("customer1", c1);
                        request.setAttribute("customer2", c2);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("errorMessage", "Lỗi kết nối cơ sở dữ liệu khi tải dữ liệu trùng.");
            }

            request.getRequestDispatcher("/customer/duplicate-merge.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("role") : null;
        
        if (role == null || (!role.equals("Trưởng nhóm kinh doanh") && !role.equals("ADMIN"))) {
            response.sendRedirect(request.getContextPath() + "/customers?error=unauthorized");
            return;
        }

        String action = request.getServletPath();
        if ("/customers/merge".equals(action)) {
            String primaryIdStr = request.getParameter("primaryId");
            String duplicateIdStr = request.getParameter("duplicateId");

            if (primaryIdStr == null || duplicateIdStr == null || primaryIdStr.equals(duplicateIdStr)) {
                response.sendRedirect(request.getContextPath() + "/customers/duplicates?error=invalid_selection");
                return;
            }

            try {
                int primaryId = Integer.parseInt(primaryIdStr);
                int duplicateId = Integer.parseInt(duplicateIdStr);

                boolean success = performMerge(primaryId, duplicateId);
                if (success) {
                    response.sendRedirect(request.getContextPath() + "/customers?message=merge_success");
                } else {
                    response.sendRedirect(request.getContextPath() + "/customers/duplicates?error=merge_failed&id1=" + primaryId + "&id2=" + duplicateId);
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.sendRedirect(request.getContextPath() + "/customers/duplicates?error=db_error");
            }
        }
    }

    private CustomerData getCustomerById(Connection conn, int id) throws SQLException {
        String sql = "SELECT id, name, tax_code, industry, scale, website, address, owner FROM customers WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new CustomerData(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("tax_code"),
                        rs.getString("industry"),
                        rs.getString("scale"),
                        rs.getString("website"),
                        rs.getString("address"),
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
                String[] updateQueries = {
                    "UPDATE contacts SET customer_id = ? WHERE customer_id = ?",
                    "UPDATE opportunities SET customer_id = ? WHERE customer_id = ?",
                    "UPDATE activities SET customer_id = ? WHERE customer_id = ?"
                };
                for (String q : updateQueries) {
                    try (PreparedStatement ps = conn.prepareStatement(q)) {
                        ps.setInt(1, primaryId);
                        ps.setInt(2, duplicateId);
                        ps.executeUpdate();
                    }
                }

                String deactivate = "UPDATE customers SET status = 'MERGED', merged_into = ? WHERE id = ?";
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

    public static class CustomerData {
        private int id;
        private String name, taxCode, industry, scale, website, address, owner;
        public CustomerData(int id, String name, String taxCode, String industry, String scale, String website, String address, String owner) {
            this.id = id; this.name = name; this.taxCode = taxCode; this.industry = industry; this.scale = scale; this.website = website; this.address = address; this.owner = owner;
        }
        public int getId() { return id; }
        public String getName() { return name; }
        public String getTaxCode() { return taxCode; }
        public String getIndustry() { return industry; }
        public String getScale() { return scale; }
        public String getWebsite() { return website; }
        public String getAddress() { return address; }
        public String getOwner() { return owner; }
    }
}
