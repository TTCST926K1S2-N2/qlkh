package vn.edu.ictu.qlkh.dao;

import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

class CustomerMergeIntegrationTest {

    private static final long ACTOR_ID = 9001L;

    @Test
    void mergeRealCustomersInTestDatabase() throws Exception {
        long sourceId = 0;
        long targetId = 0;

        try (Connection conn = DBConnection.getConnection()) {
            assertTestDatabase(conn);

            targetId = insertCustomer(conn, "S304 Integration Target");
            sourceId = insertCustomer(conn, "S304 Integration Source");

            insertContact(conn, targetId, "Target Contact");
            insertContact(conn, sourceId, "Source Contact");

            CustomerMergeDAO.MergeResult result =
                    new CustomerMergeDAO().merge(
                            sourceId, targetId, ACTOR_ID, "ADMIN");

            assertEquals(sourceId, result.sourceId());
            assertEquals(targetId, result.targetId());
            assertEquals(1, result.contacts());

            assertEquals(2, count(conn,
                    "SELECT COUNT(*) FROM contacts WHERE customer_id = ?",
                    targetId));

            assertEquals(0, count(conn,
                    "SELECT COUNT(*) FROM contacts WHERE customer_id = ?",
                    sourceId));

            assertEquals(1, count(conn,
                    "SELECT COUNT(*) FROM customer_merges " +
                    "WHERE source_customer_id = ? AND target_customer_id = ?",
                    sourceId, targetId));
        } finally {
            cleanup(sourceId, targetId);
        }
    }

    private void assertTestDatabase(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT DATABASE()")) {
            assertTrue(rs.next());
            assertEquals("qlkh_s3_04_test", rs.getString(1),
                    "CAM GOP TREN DATABASE KHAC");
        }
    }

    private long insertCustomer(Connection conn, String name)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO customers (company_name, owner_id, status) " +
                "VALUES (?, ?, 'POTENTIAL')",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setLong(2, 9003L);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                assertTrue(rs.next());
                return rs.getLong(1);
            }
        }
    }

    private void insertContact(Connection conn, long customerId, String name)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO contacts (customer_id, full_name) VALUES (?, ?)")) {
            ps.setLong(1, customerId);
            ps.setString(2, name);
            ps.executeUpdate();
        }
    }

    private int count(Connection conn, String sql, long... ids)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < ids.length; i++) {
                ps.setLong(i + 1, ids[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getInt(1);
            }
        }
    }

    private void cleanup(long sourceId, long targetId) throws SQLException {
        if (sourceId == 0 && targetId == 0) return;

        try (Connection conn = DBConnection.getConnection()) {
            assertTestDatabase(conn);
            conn.setAutoCommit(false);

            try {
                if (sourceId != 0) {
                    delete(conn,
                        "DELETE FROM customer_merges WHERE source_customer_id = ?",
                        sourceId);
                }

                if (sourceId != 0 && targetId != 0) {
                    delete(conn,
                        "DELETE FROM contact_customer_history " +
                        "WHERE old_customer_id = ? AND new_customer_id = ?",
                        sourceId, targetId);

                    try (PreparedStatement ps = conn.prepareStatement(
                            "DELETE FROM audit_logs " +
                            "WHERE action_type = 'CUSTOMER_MERGE' " +
                            "AND old_value = ? AND new_value = ?")) {
                        ps.setString(1, String.valueOf(sourceId));
                        ps.setString(2, String.valueOf(targetId));
                        ps.executeUpdate();
                    }
                }

                if (sourceId != 0) {
                    delete(conn,
                        "DELETE FROM contacts WHERE customer_id = ?",
                        sourceId);
                }

                if (targetId != 0) {
                    delete(conn,
                        "DELETE FROM contacts WHERE customer_id = ?",
                        targetId);
                }

                if (sourceId != 0) {
                    delete(conn,
                        "DELETE FROM customers WHERE id = ?",
                        sourceId);
                }

                if (targetId != 0) {
                    delete(conn,
                        "DELETE FROM customers WHERE id = ?",
                        targetId);
                }

                conn.commit();
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    private void delete(Connection conn, String sql, long... ids)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < ids.length; i++) {
                ps.setLong(i + 1, ids[i]);
            }
            ps.executeUpdate();
        }
    }
}
