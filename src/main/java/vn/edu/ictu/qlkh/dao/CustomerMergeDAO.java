package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.Customer;
import vn.edu.ictu.qlkh.util.DBConnection;
import java.sql.*;
import java.util.Locale;

public class CustomerMergeDAO {
    private final AuditLogDAO auditLogDAO;

    public CustomerMergeDAO() {
        this(new AuditLogDAO());
    }

    CustomerMergeDAO(AuditLogDAO auditLogDAO) {
        this.auditLogDAO = java.util.Objects.requireNonNull(auditLogDAO);
    }

    public void merge(long sourceId, long targetId, long managerId, String username)
            throws SQLException {
        if (sourceId <= 0 || targetId <= 0)
            throw new IllegalArgumentException("ID khach hang phai lon hon 0");
        if (sourceId == targetId)
            throw new IllegalArgumentException("Khong the gop khach hang voi chinh no");

        try (Connection conn = DBConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                long low = Math.min(sourceId, targetId);
                long high = Math.max(sourceId, targetId);
                Customer first = lockCustomer(conn, low);
                Customer second = lockCustomer(conn, high);
                if (first == null || second == null)
                    throw new IllegalArgumentException("Khong tim thay mot trong hai khach hang");

                Customer source = sourceId == low ? first : second;
                Customer target = targetId == low ? first : second;

                if (!isInManagerTeam(conn, managerId, source.getOwnerId())
                        || !isInManagerTeam(conn, managerId, target.getOwnerId())) {
                    throw new SecurityException("Khach hang nam ngoai pham vi nhom cua ban");
                }
                if (!isDuplicate(source, target)) {
                    throw new IllegalStateException(
                            "Hai khach hang khong trung ma so thue, ten cong ty hoac website");
                }

                String oldValue = "{\"sourceId\":" + sourceId
                        + ",\"sourceName\":" + quote(source.getCompanyName())
                        + ",\"targetId\":" + targetId
                        + ",\"targetName\":" + quote(target.getCompanyName()) + "}";

                transferContacts(conn, sourceId, targetId);

                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM customers WHERE id = ?")) {
                    ps.setLong(1, sourceId);
                    if (ps.executeUpdate() != 1)
                        throw new SQLException("Khong xoa duoc khach hang nguon");
                }

                String newValue = "{\"targetId\":" + targetId
                        + ",\"targetName\":" + quote(target.getCompanyName()) + "}";
                auditLogDAO.logAction(
                        conn,
                        username == null || username.isBlank() ? "unknown" : username,
                        "CUSTOMER_MERGE", "customers", oldValue, newValue,
                        "Gop khach hang nguon " + sourceId + " vao khach hang dich "
                                + targetId + "; da chuyen cac lien he va ghi lich su.");
                conn.commit();
            } catch (SQLException | RuntimeException ex) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }
                throw ex;
            } finally {
                try {
                    conn.setAutoCommit(previousAutoCommit);
                } catch (SQLException ignored) {
                    // Connection closes immediately after this block.
                }
            }
        }
    }

    private Customer lockCustomer(Connection conn, long id) throws SQLException {
        String sql = "SELECT id, company_name, tax_code, industry, company_size, website, "
                + "address, owner_id, status, created_at, updated_at "
                + "FROM customers WHERE id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Customer c = new Customer();
                c.setId(rs.getLong("id"));
                c.setCompanyName(rs.getString("company_name"));
                c.setTaxCode(rs.getString("tax_code"));
                c.setIndustry(rs.getString("industry"));
                c.setCompanySize(rs.getString("company_size"));
                c.setWebsite(rs.getString("website"));
                c.setAddress(rs.getString("address"));
                c.setOwnerId(rs.getLong("owner_id"));
                c.setStatus(rs.getString("status"));
                Timestamp created = rs.getTimestamp("created_at");
                Timestamp updated = rs.getTimestamp("updated_at");
                if (created != null) c.setCreatedAt(created.toLocalDateTime());
                if (updated != null) c.setUpdatedAt(updated.toLocalDateTime());
                return c;
            }
        }
    }

    private boolean isInManagerTeam(Connection conn, long managerId, Long ownerId)
            throws SQLException {
        if (ownerId == null) return false;
        String sql = "SELECT 1 FROM user_business_groups me "
                + "JOIN user_business_groups member ON member.group_id = me.group_id "
                + "WHERE me.user_id = ? AND member.user_id = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, managerId);
            ps.setLong(2, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void transferContacts(Connection conn, long sourceId, long targetId)
            throws SQLException {
        String sql = "SELECT id FROM contacts WHERE customer_id = ? ORDER BY id FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sourceId);
            try (ResultSet rs = ps.executeQuery()) {
                try (PreparedStatement history = conn.prepareStatement(
                        "INSERT INTO contact_customer_history "
                                + "(contact_id, old_customer_id, new_customer_id) VALUES (?, ?, ?)")) {
                    while (rs.next()) {
                        history.setLong(1, rs.getLong("id"));
                        history.setLong(2, sourceId);
                        history.setLong(3, targetId);
                        history.addBatch();
                    }
                    history.executeBatch();
                }
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE contacts SET customer_id = ? WHERE customer_id = ?")) {
            ps.setLong(1, targetId);
            ps.setLong(2, sourceId);
            ps.executeUpdate();
        }
    }

    private boolean isDuplicate(Customer a, Customer b) {
        String ta = normalize(a.getTaxCode(), true), tb = normalize(b.getTaxCode(), true);
        if (ta != null && ta.equals(tb)) return true;
        String na = normalize(a.getCompanyName(), false), nb = normalize(b.getCompanyName(), false);
        if (na != null && na.equals(nb)) return true;
        String wa = normalizeWebsite(a.getWebsite()), wb = normalizeWebsite(b.getWebsite());
        return wa != null && wa.equals(wb);
    }

    private String normalize(String value, boolean removeWhitespace) {
        if (value == null || value.isBlank()) return null;
        String s = value.trim().toLowerCase(Locale.ROOT);
        return removeWhitespace ? s.replaceAll("\\s+", "") : s.replaceAll("\\s+", " ");
    }

    private String normalizeWebsite(String value) {
        if (value == null || value.isBlank()) return null;
        String s = value.trim().toLowerCase(Locale.ROOT)
                .replaceFirst("^https?://", "").replaceFirst("^www\\.", "");
        while (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        return s.isBlank() ? null : s;
    }

    private String quote(String value) {
        if (value == null) return "null";
        StringBuilder b = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> b.append("\\\"");
                case '\\' -> b.append("\\\\");
                case '\n' -> b.append("\\n");
                case '\r' -> b.append("\\r");
                case '\t' -> b.append("\\t");
                default -> {
                    if (c < 0x20) b.append(String.format("\\u%04x", (int) c));
                    else b.append(c);
                }
            }
        }
        return b.append('"').toString();
    }
}
