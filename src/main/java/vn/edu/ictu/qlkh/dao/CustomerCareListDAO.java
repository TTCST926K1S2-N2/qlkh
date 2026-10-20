package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.util.DBConnection;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class CustomerCareListDAO {
    public record Item(long id, String companyName, String status, BigDecimal contractValue,
                       LocalDateTime lastContactAt, long daysSinceContact) {}

    public List<Item> find(long userId, String scope, int days) throws SQLException {
        String access;
        if ("ALL".equals(scope)) access = "";
        else if ("MY".equals(scope)) access = " AND c.owner_id = ?";
        else if ("TEAM".equals(scope)) access = " AND (c.owner_id = ? OR EXISTS (SELECT 1 FROM user_business_groups me JOIN user_business_groups og ON og.group_id = me.group_id WHERE me.user_id = ? AND og.user_id = c.owner_id))";
        else throw new SecurityException("Khong co quyen truy cap");
        String sql = "SELECT c.id, c.company_name, c.status, COALESCE(t.contract_value, 0) contract_value, " +
                "t.last_contact_at, DATEDIFF(NOW(), COALESCE(t.last_contact_at, c.created_at)) days_since " +
                "FROM customers c LEFT JOIN customer_care_tracking t ON t.customer_id = c.id " +
                "WHERE COALESCE(t.last_contact_at, c.created_at) <= DATE_SUB(NOW(), INTERVAL ? DAY)" +
                access + " ORDER BY contract_value DESC, days_since DESC, c.id DESC";
        List<Item> items = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days);
            if ("MY".equals(scope)) {
                ps.setLong(2, userId);
            } else if ("TEAM".equals(scope)) {
                ps.setLong(2, userId);
                ps.setLong(3, userId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("last_contact_at");
                    items.add(new Item(rs.getLong("id"), rs.getString("company_name"), rs.getString("status"),
                            rs.getBigDecimal("contract_value"), ts == null ? null : ts.toLocalDateTime(),
                            rs.getLong("days_since")));
                }
            }
        }
        return items;
    }

    public void markContacted(long customerId, long userId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO customer_care_contact_history(customer_id, contacted_by) VALUES (?, ?)")) {
                    ps.setLong(1, customerId); ps.setLong(2, userId); ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO customer_care_tracking(customer_id, last_contact_at) VALUES (?, NOW()) " +
                        "ON DUPLICATE KEY UPDATE last_contact_at = NOW()")) {
                    ps.setLong(1, customerId); ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback(); throw e;
            } finally { conn.setAutoCommit(true); }
        }
    }
}
