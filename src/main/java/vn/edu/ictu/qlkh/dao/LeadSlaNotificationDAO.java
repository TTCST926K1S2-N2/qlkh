package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LeadSlaNotificationDAO {

    public int notifyOverdueLead(Connection conn,
                                 long leadId,
                                 long assigneeId,
                                 String contextPath)
            throws SQLException {

        if (conn == null || leadId <= 0 || assigneeId <= 0) {
            throw new IllegalArgumentException(
                    "Thong tin gui canh bao khong hop le"
            );
        }

        String sql =
                "INSERT INTO notifications " +
                "(user_id, title, content, type, target_url) " +
                "SELECT DISTINCT bg.leader_id, ?, ?, 'SYSTEM', ? " +
                "FROM user_business_groups ubg " +
                "JOIN business_groups bg ON bg.id = ubg.group_id " +
                "WHERE ubg.user_id = ? " +
                "AND bg.leader_id IS NOT NULL";

        String title = "Canh bao Lead qua han SLA";

        String content =
                "Lead #" + leadId +
                " da qua han phan hoi 72 gio. " +
                "Nhan vien phu trach: #" + assigneeId;

        String targetUrl = contextPath + "/views/sales/lead-sla.jsp";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setString(2, content);
            ps.setString(3, targetUrl);
            ps.setLong(4, assigneeId);

            return ps.executeUpdate();
        }
    }
    public int notifyOverdueLead(
            long leadId,
            long assigneeId
    ) throws SQLException {

        if (leadId <= 0 || assigneeId <= 0) {
            throw new IllegalArgumentException(
                    "ID Lead hoac nhan vien khong hop le"
            );
        }

        String sql =
                "INSERT INTO notifications " +
                "(user_id, title, content, type, target_url) " +
                "SELECT DISTINCT bg.leader_id, ?, ?, 'SYSTEM', ? " +
                "FROM user_business_groups ubg " +
                "JOIN business_groups bg ON bg.id = ubg.group_id " +
                "WHERE ubg.user_id = ? " +
                "AND bg.leader_id IS NOT NULL";

        String title = "Canh bao Lead qua han SLA";

        String content =
                "Lead #" + leadId +
                " da qua han phan hoi 72 gio. " +
                "Nhan vien phu trach: #" + assigneeId;

        String targetUrl = "/views/sales/lead-sla.jsp";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setString(2, content);
            ps.setString(3, targetUrl);
            ps.setLong(4, assigneeId);

            return ps.executeUpdate();
        }
    }
}