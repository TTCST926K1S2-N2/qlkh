package vn.edu.ictu.qlkh.dao;

import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.service.CustomerMergeService;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CustomerMergeIntegrationTest {

    @Test
    void mergeTransfersContactsHistoryAndDeletesSource() throws Exception {
        Fixture f = new Fixture();
        AuditLogDAO audit = mock(AuditLogDAO.class);

        try {
            f.create();
            CustomerMergeDAO dao = new CustomerMergeDAO(audit);

            dao.merge(f.sourceId, f.targetId, f.managerId, f.username);

            assertEquals(0, f.count(
                    "SELECT COUNT(*) FROM customers WHERE id = ?",
                    f.sourceId));

            assertEquals(1, f.count(
                    "SELECT COUNT(*) FROM customers WHERE id = ?",
                    f.targetId));

            assertEquals(1, f.count(
                    "SELECT COUNT(*) FROM contacts WHERE id = ? AND customer_id = ?",
                    f.contactId, f.targetId));

            assertEquals(1, f.count(
                    "SELECT COUNT(*) FROM contact_customer_history "
                            + "WHERE contact_id = ? AND old_customer_id = ? "
                            + "AND new_customer_id = ?",
                    f.contactId, f.sourceId, f.targetId));

            verify(audit, times(1)).logAction(
                    any(Connection.class),
                    eq(f.username),
                    eq("CUSTOMER_MERGE"),
                    eq("customers"),
                    anyString(),
                    anyString(),
                    anyString());
        } finally {
            f.cleanup();
        }
    }

    @Test
    void mergeRollsBackWhenAuditLoggingFails() throws Exception {
        Fixture f = new Fixture();
        AuditLogDAO audit = mock(AuditLogDAO.class);

        doThrow(new SQLException("Simulated audit failure"))
                .when(audit)
                .logAction(
                        any(Connection.class),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString());

        try {
            f.create();
            CustomerMergeDAO dao = new CustomerMergeDAO(audit);

            assertThrows(SQLException.class,
                    () -> dao.merge(
                            f.sourceId,
                            f.targetId,
                            f.managerId,
                            f.username));

            // Source customer must be restored by rollback.
            assertEquals(1, f.count(
                    "SELECT COUNT(*) FROM customers WHERE id = ?",
                    f.sourceId));

            // The contact must still belong to the source.
            assertEquals(1, f.count(
                    "SELECT COUNT(*) FROM contacts WHERE id = ? AND customer_id = ?",
                    f.contactId, f.sourceId));

            // History inserted during the failed transaction must disappear.
            assertEquals(0, f.count(
                    "SELECT COUNT(*) FROM contact_customer_history "
                            + "WHERE contact_id = ? AND old_customer_id = ? "
                            + "AND new_customer_id = ?",
                    f.contactId, f.sourceId, f.targetId));

            verify(audit, times(1)).logAction(
                    any(Connection.class),
                    anyString(),
                    eq("CUSTOMER_MERGE"),
                    anyString(),
                    anyString(),
                    anyString(),
                    anyString());
        } finally {
            f.cleanup();
        }
    }

    @Test
    void salesRoleCannotMergeCustomers() {
        CustomerMergeService service = new CustomerMergeService();

        assertThrows(SecurityException.class,
                () -> service.merge(1, 2, 1, "SALES", "test-sales"));

        assertThrows(SecurityException.class,
                () -> service.merge(1, 2, 1, "ADMIN", "test-admin"));
    }

    private static final class Fixture {
        final String suffix = UUID.randomUUID().toString().replace("-", "");
        final String username = "merge_test_" + suffix;

        long managerId;
        long ownerId;
        long groupId;
        long sourceId;
        long targetId;
        long contactId;

        void create() throws SQLException {
            try (Connection conn = DBConnection.getConnection()) {
                assertEquals("qlkh_s209_test", conn.getCatalog());

                managerId = insertUser(
                        conn, username + "@example.test", "MANAGER");
                ownerId = insertUser(
                        conn, "owner_" + suffix + "@example.test", "SALES");

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO business_groups(name) VALUES (?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, "Merge test group " + suffix);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        assertTrue(rs.next());
                        groupId = rs.getLong(1);
                    }
                }

                addUserToGroup(conn, managerId, groupId);
                addUserToGroup(conn, ownerId, groupId);

                sourceId = insertCustomer(
                        conn, "Merge Test Company " + suffix, ownerId);
                targetId = insertCustomer(
                        conn, "  merge test company " + suffix + "  ", ownerId);

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO contacts "
                                + "(customer_id, full_name, email, phone) "
                                + "VALUES (?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, sourceId);
                    ps.setString(2, "Merge Test Contact " + suffix);
                    ps.setString(3, "contact_" + suffix + "@example.test");
                    ps.setString(4, "0912345678");
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        assertTrue(rs.next());
                        contactId = rs.getLong(1);
                    }
                }
            }
        }

        private long insertUser(Connection conn, String email, String role)
                throws SQLException {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users (email, password_hash, full_name, role) "
                            + "VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, email);
                ps.setString(2, "TEST_ONLY");
                ps.setString(3, "Merge Integration Test");
                ps.setString(4, role);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    assertTrue(rs.next());
                    return rs.getLong(1);
                }
            }
        }

        private void addUserToGroup(
                Connection conn, long userId, long businessGroupId)
                throws SQLException {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user_business_groups(user_id, group_id) "
                            + "VALUES (?, ?)")) {
                ps.setLong(1, userId);
                ps.setLong(2, businessGroupId);
                ps.executeUpdate();
            }
        }

        private long insertCustomer(
                Connection conn, String name, long owner)
                throws SQLException {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO customers(company_name, owner_id, status) "
                            + "VALUES (?, ?, 'POTENTIAL')",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setLong(2, owner);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    assertTrue(rs.next());
                    return rs.getLong(1);
                }
            }
        }

        int count(String sql, long... ids) throws SQLException {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < ids.length; i++) {
                    ps.setLong(i + 1, ids[i]);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    return rs.getInt(1);
                }
            }
        }

        void cleanup() throws SQLException {
            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    if (contactId > 0) {
                        delete(conn,
                                "DELETE FROM contact_customer_history WHERE contact_id = ?",
                                contactId);
                        delete(conn, "DELETE FROM contacts WHERE id = ?", contactId);
                    }

                    if (sourceId > 0) {
                        delete(conn, "DELETE FROM customers WHERE id = ?", sourceId);
                    }
                    if (targetId > 0) {
                        delete(conn, "DELETE FROM customers WHERE id = ?", targetId);
                    }

                    if (managerId > 0) {
                        delete(conn,
                                "DELETE FROM user_business_groups WHERE user_id = ?",
                                managerId);
                    }
                    if (ownerId > 0) {
                        delete(conn,
                                "DELETE FROM user_business_groups WHERE user_id = ?",
                                ownerId);
                    }

                    delete(conn,
                            "DELETE FROM audit_logs WHERE username = ?", username);

                    if (groupId > 0) {
                        delete(conn, "DELETE FROM business_groups WHERE id = ?", groupId);
                    }
                    if (managerId > 0) {
                        delete(conn, "DELETE FROM users WHERE id = ?", managerId);
                    }
                    if (ownerId > 0) {
                        delete(conn, "DELETE FROM users WHERE id = ?", ownerId);
                    }

                    conn.commit();
                } catch (SQLException | RuntimeException ex) {
                    conn.rollback();
                    throw ex;
                }
            }
        }

        private void delete(Connection conn, String sql, long id)
                throws SQLException {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, id);
                ps.executeUpdate();
            }
        }

        private void delete(Connection conn, String sql, String value)
                throws SQLException {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, value);
                ps.executeUpdate();
            }
        }
    }
}