package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LeadDAO {

    private static final String INSERT_SQL =
            "INSERT INTO leads " +
            "(full_name, email, phone, company, interest, status, source, source_form) " +
            "VALUES (?, ?, ?, ?, ?, 'NEW', 'WEBSITE', ?)";

    public long create(
            String fullName,
            String email,
            String phone,
            String company,
            String interest,
            String sourceForm
    ) throws SQLException {

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     INSERT_SQL,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            statement.setString(1, fullName);
            statement.setString(2, email);
            statement.setString(3, phone);
            statement.setString(4, company);
            statement.setString(5, interest);
            statement.setString(6, sourceForm);

            int affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                throw new SQLException("Khong the tao Lead.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }

            throw new SQLException("Khong lay duoc ID Lead.");
        }
    }

    private static final String SELECT_COLUMNS =
            "l.id, l.full_name, l.email, l.phone, l.company, l.interest, " +
            "l.status, l.source, l.source_form, l.campaign_id, l.assigned_to, " +
            "l.score, l.converted_customer_id, l.converted_contact_id, " +
            "l.created_at, l.updated_at";

    public java.util.List<vn.edu.ictu.qlkh.model.Lead> findAll() throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM leads l ORDER BY l.id DESC";
        java.util.List<vn.edu.ictu.qlkh.model.Lead> leads = new java.util.ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                leads.add(map(rs));
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase(java.util.Locale.ROOT).contains("leads' doesn't exist")) {
                return leads;
            }
            throw e;
        }
        return leads;
    }

    public java.util.List<vn.edu.ictu.qlkh.model.Lead> findVisible(long userId, String scope) throws SQLException {
        if ("ALL".equalsIgnoreCase(scope)) {
            return findAll();
        }

        String sql;
        if ("TEAM".equalsIgnoreCase(scope)) {
            sql = "SELECT " + SELECT_COLUMNS + " FROM leads l " +
                  "WHERE l.assigned_to IS NULL " +
                  "   OR l.assigned_to = ? " +
                  "   OR EXISTS (" +
                  "       SELECT 1 FROM user_business_groups me " +
                  "       JOIN user_business_groups other ON other.group_id = me.group_id " +
                  "       WHERE me.user_id = ? AND other.user_id = l.assigned_to" +
                  "   ) " +
                  "ORDER BY l.id DESC";
        } else {
            // MY or default
            sql = "SELECT " + SELECT_COLUMNS + " FROM leads l " +
                  "WHERE l.assigned_to IS NULL OR l.assigned_to = ? " +
                  "ORDER BY l.id DESC";
        }

        java.util.List<vn.edu.ictu.qlkh.model.Lead> leads = new java.util.ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if ("TEAM".equalsIgnoreCase(scope)) {
                ps.setLong(1, userId);
                ps.setLong(2, userId);
            } else {
                ps.setLong(1, userId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    leads.add(map(rs));
                }
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase(java.util.Locale.ROOT).contains("leads' doesn't exist")) {
                return leads;
            }
            throw e;
        }
        return leads;
    }

    private vn.edu.ictu.qlkh.model.Lead map(ResultSet rs) throws SQLException {
        vn.edu.ictu.qlkh.model.Lead lead = new vn.edu.ictu.qlkh.model.Lead();
        lead.setId(rs.getLong("id"));
        lead.setFullName(rs.getString("full_name"));
        lead.setEmail(rs.getString("email"));
        lead.setPhone(rs.getString("phone"));
        lead.setCompany(rs.getString("company"));
        lead.setInterest(rs.getString("interest"));
        lead.setStatus(rs.getString("status"));
        lead.setSource(rs.getString("source"));
        lead.setSourceForm(rs.getString("source_form"));

        long campaignId = rs.getLong("campaign_id");
        if (!rs.wasNull()) {
            lead.setCampaignId(campaignId);
        }

        long assignedTo = rs.getLong("assigned_to");
        if (!rs.wasNull()) {
            lead.setAssignedTo(assignedTo);
        }

        lead.setScore(rs.getBigDecimal("score"));

        long convertedCustId = rs.getLong("converted_customer_id");
        if (!rs.wasNull()) {
            lead.setConvertedCustomerId(convertedCustId);
        }

        long convertedContactId = rs.getLong("converted_contact_id");
        if (!rs.wasNull()) {
            lead.setConvertedContactId(convertedContactId);
        }

        java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            lead.setCreatedAt(createdAt.toLocalDateTime());
        }

        java.sql.Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            lead.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return lead;
    }
}