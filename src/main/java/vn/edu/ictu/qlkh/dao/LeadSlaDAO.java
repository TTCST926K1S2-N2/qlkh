package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.*;
import java.util.*;

public class LeadSlaDAO {

    public List<Map<String, Object>> findAssigned(long userId)
            throws SQLException {

        String sql =
                "SELECT l.id, l.full_name, l.email, l.phone, " +
                "l.status, l.sla_deadline, l.accepted_at, l.first_contact_at, " +
                "u.full_name AS assignee " +
                "FROM leads l " +
                "LEFT JOIN users u ON l.assigned_to = u.id " +
                "WHERE l.assigned_to = ? " +
                "ORDER BY l.created_at DESC";

        List<Map<String, Object>> result = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Map<String, Object> lead = new LinkedHashMap<>();

                    lead.put("id", rs.getLong("id"));
                    lead.put("fullName", rs.getString("full_name"));
                    lead.put("email", rs.getString("email"));
                    lead.put("phone", rs.getString("phone"));
                    lead.put("assignee", rs.getString("assignee"));

                    Timestamp deadline =
                            rs.getTimestamp("sla_deadline");

                    lead.put(
                            "deadline",
                            deadline == null
                                    ? null
                                    : deadline.toLocalDateTime().toString()
                    );

                    String status = rs.getString("status");

                    lead.put("status", mapStatus(status));

                    String slaStatus = null;

                    Timestamp firstContact =
                            rs.getTimestamp("first_contact_at");

                    if (deadline != null) {
                        long remaining =
                                deadline.getTime()
                                        - System.currentTimeMillis();

                        if (firstContact != null) {
                            slaStatus = firstContact.after(deadline)
                                    ? "overdue"
                                    : "on_time";
                        } else if (remaining < 0) {
                            slaStatus = "overdue";
                        } else if (remaining <= 3600000) {
                            slaStatus = "urgent";
                        } else {
                            slaStatus = "on_time";
                        }
                    }

                    lead.put("slaStatus", slaStatus);

                    result.add(lead);
                }
            }
        }

        return result;
    }

    public boolean accept(long leadId, long userId)
            throws SQLException {

        String sql =
                "UPDATE leads " +
                "SET status = 'IN_PROGRESS', " +
                "accepted_at = CURRENT_TIMESTAMP " +
                "WHERE id = ? " +
                "AND assigned_to = ? " +
                "AND status IN ('NEW', 'ASSIGNED')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, leadId);
            ps.setLong(2, userId);

            return ps.executeUpdate() == 1;
        }
    }

    public boolean reject(
            long leadId,
            long userId,
            String reason
    ) throws SQLException {

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Bat buoc nhap ly do tu choi"
            );
        }

        String sql =
                "UPDATE leads " +
                "SET status = 'NEW', " +
                "assigned_to = NULL, " +
                "rejected_at = CURRENT_TIMESTAMP, " +
                "rejection_reason = ?, " +
                "sla_deadline = NULL, " +
                "assigned_at = NULL, " +
                "accepted_at = NULL, " +
                "first_contact_at = NULL, " +
                "sla_notified_at = NULL " +
                "WHERE id = ? " +
                "AND assigned_to = ? " +
                "AND status IN ('NEW', 'ASSIGNED')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, reason.trim());
            ps.setLong(2, leadId);
            ps.setLong(3, userId);

            return ps.executeUpdate() == 1;
        }
    }

    public boolean canAssign(
            long actorId,
            String actorRole,
            long assigneeId
    ) throws SQLException {

        if (actorId <= 0 || assigneeId <= 0
                || actorRole == null) {
            return false;
        }

        String role = actorRole.trim()
                .toUpperCase(java.util.Locale.ROOT);

        if (!role.equals("ADMIN") && !role.equals("MANAGER")) {
            return false;
        }

        String sql =
                "SELECT u.id " +
                "FROM users u " +
                "WHERE u.id = ? " +
                "AND (UPPER(TRIM(u.role)) = 'SALES' " +
                "OR EXISTS ( " +
                "SELECT 1 FROM user_roles ur " +
                "WHERE ur.user_id = u.id " +
                "AND UPPER(ur.role_code) = 'SALES' " +
                ")) " +
                "AND UPPER(u.status) = 'ACTIVE' " +
                "AND ( " +
                "    ? = 'ADMIN' " +
                "    OR EXISTS ( " +
                "        SELECT 1 " +
                "        FROM user_business_groups ubg " +
                "        JOIN business_groups bg " +
                "          ON bg.id = ubg.group_id " +
                "        WHERE ubg.user_id = u.id " +
                "          AND bg.leader_id = ? " +
                "    ) " +
                ")";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, assigneeId);
            ps.setString(2, role);
            ps.setLong(3, actorId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    public boolean assign(long leadId, long assigneeId)
            throws SQLException {

        if (leadId <= 0 || assigneeId <= 0) {
            throw new IllegalArgumentException(
                    "ID Lead hoac nhan vien khong hop le"
            );
        }

        String sql =
                "UPDATE leads " +
                "SET assigned_to = ?, " +
                "status = 'ASSIGNED', " +
                "assigned_at = CURRENT_TIMESTAMP, " +
                "sla_deadline = DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 72 HOUR), " +
                "accepted_at = NULL, " +
                "rejected_at = NULL, " +
                "rejection_reason = NULL, " +
                "first_contact_at = NULL, " +
                "sla_notified_at = NULL " +
                "WHERE id = ? " +
                "AND assigned_to IS NULL " +
                "AND status = 'NEW'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, assigneeId);
            ps.setLong(2, leadId);

            return ps.executeUpdate() == 1;
        }
    }
    private String mapStatus(String status) {

        if (status == null) {
            return "pending";
        }

        return switch (status.toUpperCase(Locale.ROOT)) {
            case "IN_PROGRESS" -> "accepted";
            case "REJECTED" -> "rejected";
            default -> "pending";
        };
    }
}