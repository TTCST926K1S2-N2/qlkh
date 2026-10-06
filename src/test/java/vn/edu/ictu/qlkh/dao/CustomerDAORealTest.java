package vn.edu.ictu.qlkh.dao;

import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.model.Customer;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CustomerDAORealTest {

    @Test
    void testCustomerDaoAndPermissions() throws Exception {
        CustomerDAO dao = new CustomerDAO();
        String tag = UUID.randomUUID().toString().replace("-", "");
        long manager = 0, sales = 0, outsider = 0;
        long group = 0, customerId = 0;

        try (Connection conn = DBConnection.getConnection()) {
            assertEquals("qlkh_s209_test", conn.getCatalog());

            try {
                manager = createUser(conn, tag + "m", "MANAGER");
                sales = createUser(conn, tag + "s", "SALES");
                outsider = createUser(conn, tag + "o", "SALES");

                group = createGroup(conn, tag);
                joinGroup(conn, manager, group);
                joinGroup(conn, sales, group);

                Customer c = new Customer();
                c.setCompanyName("S301 DAO Test");
                c.setTaxCode("S301_" + tag);
                c.setOwnerId(sales);
                c.setStatus("POTENTIAL");

                customerId = dao.insert(c);
                final long createdId = customerId;
                assertTrue(customerId > 0);

                assertNotNull(dao.findVisibleById(
                        customerId, sales, "MY"));

                assertNull(dao.findVisibleById(
                        customerId, outsider, "MY"));

                assertNotNull(dao.findVisibleById(
                        customerId, manager, "TEAM"));

                assertNull(dao.findVisibleById(
                        customerId, outsider, "TEAM"));

                assertNotNull(dao.findVisibleById(
                        customerId, outsider, "ALL"));

                assertTrue(dao.findVisible(sales, "MY")
                        .stream().anyMatch(x ->
                                x.getId() == createdId));

                assertTrue(dao.findVisible(manager, "TEAM")
                        .stream().anyMatch(x ->
                                x.getId() == createdId));

                assertTrue(dao.existsTaxCode(
                        c.getTaxCode(), null));

                assertFalse(dao.existsTaxCode(
                        c.getTaxCode(), customerId));

                c.setId(customerId);
                c.setCompanyName("Updated S301");
                c.setStatus("CUSTOMER");

                assertFalse(dao.updateVisible(
                        c, outsider, "MY"));

                assertFalse(dao.updateVisible(
                        c, outsider, "TEAM"));

                assertTrue(dao.updateVisible(
                        c, manager, "TEAM"));

                Customer updated = dao.findVisibleById(
                        customerId, sales, "MY");

                assertEquals("Updated S301",
                        updated.getCompanyName());

                assertEquals("CUSTOMER",
                        updated.getStatus());

                Customer duplicate = new Customer();
                duplicate.setCompanyName("Duplicate");
                duplicate.setTaxCode(c.getTaxCode());
                duplicate.setOwnerId(sales);
                duplicate.setStatus("POTENTIAL");

                SQLException ex = assertThrows(
                        SQLException.class,
                        () -> dao.insert(duplicate));

                assertEquals(1062, ex.getErrorCode());

            } finally {
                if (customerId != 0)
                    delete(conn, "DELETE FROM customers WHERE id=?",
                            customerId);

                for (long id : new long[]{manager, sales, outsider}) {
                    if (id != 0) {
                        delete(conn,
                                "DELETE FROM user_business_groups WHERE user_id=?",
                                id);
                        delete(conn,
                                "DELETE FROM user_roles WHERE user_id=?",
                                id);
                        delete(conn, "DELETE FROM users WHERE id=?", id);
                    }
                }

                if (group != 0)
                    delete(conn,
                            "DELETE FROM business_groups WHERE id=?",
                            group);
            }
        }
    }

    private long createUser(
            Connection conn, String tag, String role)
            throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO users " +
                "(email,password_hash,full_name,role) " +
                "VALUES (?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, tag + "@example.test");
            ps.setString(2, "TEST_ONLY");
            ps.setString(3, "S301 Test");
            ps.setString(4, role);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                assertTrue(rs.next());
                return rs.getLong(1);
            }
        }
    }

    private long createGroup(Connection conn, String tag)
            throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO business_groups(name) VALUES(?)",
                Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, "S301_" + tag);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                assertTrue(rs.next());
                return rs.getLong(1);
            }
        }
    }

    private void joinGroup(
            Connection conn, long userId, long groupId)
            throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO user_business_groups " +
                "(user_id,group_id) VALUES (?,?)")) {

            ps.setLong(1, userId);
            ps.setLong(2, groupId);
            ps.executeUpdate();
        }
    }

    private void delete(
            Connection conn, String sql, long id)
            throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
}