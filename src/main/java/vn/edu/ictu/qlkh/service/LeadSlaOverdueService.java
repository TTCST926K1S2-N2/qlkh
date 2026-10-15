package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.LeadSlaNotificationDAO;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LeadSlaOverdueService {

    private final LeadSlaNotificationDAO notificationDAO =
            new LeadSlaNotificationDAO();

    private final String contextPath;

    public LeadSlaOverdueService() {
        this("");
    }

    public LeadSlaOverdueService(String contextPath) {
        if (contextPath == null || contextPath.isBlank()) {
            this.contextPath = "";
        } else if (contextPath.startsWith("/")
                && !contextPath.endsWith("/")
                && !contextPath.startsWith("//")) {
            this.contextPath = contextPath;
        } else {
            throw new IllegalArgumentException(
                    "Context path khong hop le"
            );
        }
    }

    public int processOverdueLeads() throws SQLException {

        List<Long> leadIds = findOverdueLeadIds();

        int notifiedCount = 0;

        for (Long leadId : leadIds) {

            if (processOneLead(leadId)) {
                notifiedCount++;
            }
        }

        return notifiedCount;
    }

    private List<Long> findOverdueLeadIds()
            throws SQLException {

        String sql =
                "SELECT id FROM leads " +
                "WHERE assigned_to IS NOT NULL " +
                "AND sla_deadline IS NOT NULL " +
                "AND sla_deadline < CURRENT_TIMESTAMP " +
                "AND first_contact_at IS NULL " +
                "AND sla_notified_at IS NULL " +
                "AND status IN ('ASSIGNED', 'IN_PROGRESS') " +
                "ORDER BY sla_deadline ASC " +
                "LIMIT 100";

        List<Long> ids = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ids.add(rs.getLong("id"));
            }
        }

        return ids;
    }

    private boolean processOneLead(long leadId)
            throws SQLException {

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                String selectSql =
                        "SELECT assigned_to FROM leads " +
                        "WHERE id = ? " +
                        "AND assigned_to IS NOT NULL " +
                        "AND sla_deadline < CURRENT_TIMESTAMP " +
                        "AND first_contact_at IS NULL " +
                        "AND sla_notified_at IS NULL " +
                        "AND status IN ('ASSIGNED', 'IN_PROGRESS') " +
                        "FOR UPDATE";

                long assigneeId;

                try (PreparedStatement ps =
                             conn.prepareStatement(selectSql)) {

                    ps.setLong(1, leadId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }

                        assigneeId = rs.getLong("assigned_to");
                    }
                }

                int inserted =
                        notificationDAO.notifyOverdueLead(
                                conn,
                                leadId,
                                assigneeId,
                                contextPath
                        );

                if (inserted <= 0) {
                    conn.rollback();
                    return false;
                }

                String updateSql =
                        "UPDATE leads " +
                        "SET sla_notified_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ? " +
                        "AND sla_notified_at IS NULL";

                try (PreparedStatement ps =
                             conn.prepareStatement(updateSql)) {

                    ps.setLong(1, leadId);

                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return false;
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException | RuntimeException ex) {

                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    ex.addSuppressed(rollbackEx);
                }

                throw ex;
            }
        }
    }
}