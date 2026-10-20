package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.CustomerSupportRequest;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerSupportDAO {

    private CustomerSupportRequest map(ResultSet rs) throws SQLException {
        CustomerSupportRequest r = new CustomerSupportRequest();

        r.setId(rs.getLong("id"));
        r.setCustomerId(rs.getLong("customer_id"));
        r.setTitle(rs.getString("title"));
        r.setDescription(rs.getString("description"));
        r.setPriority(rs.getString("priority"));
        r.setStatus(rs.getString("status"));

        long assignee = rs.getLong("assigned_to");
        r.setAssignedTo(rs.wasNull() ? null : assignee);

        r.setCreatedBy(rs.getLong("created_by"));
        r.setResolutionNote(rs.getString("resolution_note"));

        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");
        Timestamp resolved = rs.getTimestamp("resolved_at");

        if (created != null) {
            r.setCreatedAt(created.toLocalDateTime());
        }
        if (updated != null) {
            r.setUpdatedAt(updated.toLocalDateTime());
        }
        if (resolved != null) {
            r.setResolvedAt(resolved.toLocalDateTime());
        }

        return r;
    }

    public long create(CustomerSupportRequest r) throws SQLException {
        String sql = """
            INSERT INTO customer_support_requests
            (customer_id, title, description, priority,
             status, assigned_to, created_by)
            VALUES (?, ?, ?, ?, 'OPEN', ?, ?)
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, r.getCustomerId());
            ps.setString(2, r.getTitle());
            ps.setString(3, r.getDescription());
            ps.setString(4, r.getPriority());

            if (r.getAssignedTo() == null) {
                ps.setNull(5, Types.BIGINT);
            } else {
                ps.setLong(5, r.getAssignedTo());
            }

            ps.setLong(6, r.getCreatedBy());

            if (ps.executeUpdate() != 1) {
                throw new SQLException("Khong tao duoc yeu cau");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }

            throw new SQLException("Khong lay duoc ID yeu cau");
        }
    }

    public CustomerSupportRequest findById(long id) throws SQLException {
        String sql = """
            SELECT *
            FROM customer_support_requests
            WHERE id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<CustomerSupportRequest> findByCustomer(
            long customerId) throws SQLException {

        String sql = """
            SELECT *
            FROM customer_support_requests
            WHERE customer_id = ?
            ORDER BY created_at DESC, id DESC
            """;

        List<CustomerSupportRequest> result = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        }

        return result;
    }

    public boolean update(
            long id,
            String status,
            String priority,
            Long assignedTo,
            String resolutionNote) throws SQLException {

        String sql = """
            UPDATE customer_support_requests
            SET status = ?,
                priority = ?,
                assigned_to = ?,
                resolution_note = ?,
                resolved_at = CASE
                    WHEN ? IN ('RESOLVED', 'CLOSED')
                    THEN COALESCE(resolved_at, CURRENT_TIMESTAMP)
                    ELSE NULL
                END
            WHERE id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setString(2, priority);

            if (assignedTo == null) {
                ps.setNull(3, Types.BIGINT);
            } else {
                ps.setLong(3, assignedTo);
            }

            ps.setString(4, resolutionNote);
            ps.setString(5, status);
            ps.setLong(6, id);

            return ps.executeUpdate() == 1;
        }
    }

    public int countUnresolved(long customerId) throws SQLException {
        String sql = """
            SELECT COUNT(*)
            FROM customer_support_requests
            WHERE customer_id = ?
              AND status IN ('OPEN', 'IN_PROGRESS')
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public boolean userExists(long userId) throws SQLException {
        String sql = """
            SELECT 1
            FROM users
            WHERE id = ? AND status = 'ACTIVE'
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}