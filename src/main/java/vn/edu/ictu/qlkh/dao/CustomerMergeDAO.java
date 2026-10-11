package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.*;
import java.util.Set;

public class CustomerMergeDAO {

    public record MergeResult(
            long sourceId,
            long targetId,
            int contacts,
            int opportunities,
            int attachments) {}

    public MergeResult merge(
            long sourceId,
            long targetId,
            long actorId,
            String role) throws SQLException {

        if (sourceId <= 0 || targetId <= 0 || actorId <= 0
                || sourceId == targetId) {
            throw new IllegalArgumentException(
                    "Thong tin gop khong hop le");
        }

        if (!Set.of("ADMIN", "MANAGER").contains(role)) {
            throw new SecurityException("Khong co quyen gop");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Lock in fixed order to reduce deadlock risk.
                long first = Math.min(sourceId, targetId);
                long second = Math.max(sourceId, targetId);

                long firstOwner = lockCustomer(conn, first);
                long secondOwner = lockCustomer(conn, second);

                long sourceOwner =
                        sourceId == first ? firstOwner : secondOwner;
                long targetOwner =
                        targetId == first ? firstOwner : secondOwner;

                if ("MANAGER".equals(role)) {
                    if (!inManagerScope(conn, actorId, sourceOwner)
                            || !inManagerScope(conn, actorId, targetOwner)) {
                        throw new SecurityException(
                                "Khach hang ngoai pham vi quan ly");
                    }
                }

                if (alreadyMerged(conn, sourceId)
                        || alreadyMerged(conn, targetId)) {
                    throw new IllegalArgumentException(
                            "Khach hang da duoc gop truoc do");
                }

                // Preserve contact transfer history.
                execute(conn, """
                    INSERT INTO contact_customer_history
                        (contact_id, old_customer_id, new_customer_id)
                    SELECT id, ?, ?
                    FROM contacts
                    WHERE customer_id = ?
                    """, sourceId, targetId, sourceId);

                // Avoid two primary contacts in the target account.
                if (hasPrimaryContact(conn, targetId)) {
                    execute(conn, """
                        UPDATE contacts SET is_primary = FALSE
                        WHERE customer_id = ? AND is_primary = TRUE
                        """, sourceId);
                }

                int contacts = execute(conn, """
                    UPDATE contacts SET customer_id = ?
                    WHERE customer_id = ?
                    """, targetId, sourceId);

                int opportunities = execute(conn, """
                    UPDATE opportunities SET customer_id = ?
                    WHERE customer_id = ?
                    """, targetId, sourceId);

                int attachments = execute(conn, """
                    UPDATE attachments SET customer_id = ?
                    WHERE customer_id = ?
                    """, targetId, sourceId);

                execute(conn, """
                    INSERT INTO customer_merges
                        (source_customer_id, target_customer_id, merged_by)
                    VALUES (?, ?, ?)
                    """, sourceId, targetId, actorId);

                new AuditLogDAO().logAction(
                        conn,
                        String.valueOf(actorId),
                        "CUSTOMER_MERGE",
                        "customers",
                        String.valueOf(sourceId),
                        String.valueOf(targetId),
                        "contacts=" + contacts
                            + ", opportunities=" + opportunities
                            + ", attachments=" + attachments
                );

                conn.commit();

                return new MergeResult(
                        sourceId, targetId,
                        contacts, opportunities, attachments);

            } catch (SQLException | RuntimeException ex) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private long lockCustomer(Connection conn, long id)
            throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT owner_id FROM customers WHERE id = ? FOR UPDATE")) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "Khong tim thay khach hang: " + id);
                }
                return rs.getLong(1);
            }
        }
    }

    private boolean inManagerScope(
            Connection conn, long managerId, long ownerId)
            throws SQLException {

        if (managerId == ownerId) {
            return true;
        }

        String sql = """
            SELECT 1
            FROM business_groups bg
            JOIN user_business_groups ubg
                ON ubg.group_id = bg.id
            WHERE bg.leader_id = ?
              AND bg.status = 'ACTIVE'
              AND ubg.user_id = ?
            LIMIT 1
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, managerId);
            ps.setLong(2, ownerId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean alreadyMerged(Connection conn, long id)
            throws SQLException {

        String sql = """
            SELECT 1 FROM customer_merges
            WHERE source_customer_id = ?
            LIMIT 1
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean hasPrimaryContact(Connection conn, long customerId)
            throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM contacts "
                + "WHERE customer_id = ? AND is_primary = TRUE LIMIT 1")) {

            ps.setLong(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private int execute(Connection conn, String sql, long... values)
            throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                ps.setLong(i + 1, values[i]);
            }
            return ps.executeUpdate();
        }
    }
}