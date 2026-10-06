package vn.edu.ictu.qlkh.dao;

import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.model.Contact;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

class ContactCRUDIntegrationTest {

    @Test
    void crudTransferAndHistory() throws Exception {
        ContactDAO dao = new ContactDAO();

        long firstCustomer = 0;
        long secondCustomer = 0;
        long contactId = 0;
        long ownerId = 0;

        try (Connection conn = DBConnection.getConnection()) {
            assertEquals("qlkh_s209_test", conn.getCatalog());

            try {
                // Create an isolated test user.
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO users " +
                        "(email,password_hash,full_name,role) " +
                        "VALUES (?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1,
                            "s302_" + java.util.UUID.randomUUID()
                                    + "@example.test");
                    ps.setString(2, "TEST_NOT_A_REAL_PASSWORD");
                    ps.setString(3, "S3-02 Integration Test");
                    ps.setString(4, "SALES");

                    assertEquals(1, ps.executeUpdate());

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        assertTrue(rs.next());
                        ownerId = rs.getLong(1);
                    }
                }

                // Create two isolated customers.
                for (int i = 0; i < 2; i++) {
                    try (PreparedStatement ps =
                                 conn.prepareStatement(
                            "INSERT INTO customers " +
                            "(company_name,tax_code,owner_id,status) " +
                            "VALUES (?,?,?,?)",
                            Statement.RETURN_GENERATED_KEYS)) {

                        ps.setString(1, "S3-02 Test Customer " + i);
                        ps.setString(2,
                                "S302_" + java.util.UUID.randomUUID());
                        ps.setLong(3, ownerId);
                        ps.setString(4, "POTENTIAL");

                        assertEquals(1, ps.executeUpdate());

                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            assertTrue(rs.next());

                            if (i == 0) {
                                firstCustomer = rs.getLong(1);
                            } else {
                                secondCustomer = rs.getLong(1);
                            }
                        }
                    }
                }

                // CREATE
                Contact contact = new Contact();
                contact.setCustomerId(firstCustomer);
                contact.setFullName("S3-02 Contact Test");
                contact.setEmail("contact@example.test");
                contact.setPhone("0912345678");
                contact.setDecisionRole("DECISION_MAKER");
                contact.setPrimary(true);

                contactId = dao.insert(contact);
                assertTrue(contactId > 0);

                // READ
                Contact created = dao.findById(contactId);
                assertNotNull(created);
                assertEquals(firstCustomer,
                        created.getCustomerId());
                assertEquals("DECISION_MAKER",
                        created.getDecisionRole());
                assertTrue(created.isPrimary());

                assertTrue(dao.findByCustomer(firstCustomer)
                        .stream()
                        .anyMatch(c -> c.getId() == created.getId()));

                // UPDATE AND TRANSFER
                created.setCustomerId(secondCustomer);
                created.setFullName("S3-02 Updated Contact");
                created.setDecisionRole("INFLUENCER");

                assertTrue(dao.update(created));

                Contact updated = dao.findById(contactId);
                assertNotNull(updated);
                assertEquals(secondCustomer,
                        updated.getCustomerId());
                assertEquals("S3-02 Updated Contact",
                        updated.getFullName());
                assertEquals("INFLUENCER",
                        updated.getDecisionRole());

                // VERIFY HISTORY
                try (PreparedStatement ps =
                             conn.prepareStatement(
                        "SELECT COUNT(*) " +
                        "FROM contact_customer_history " +
                        "WHERE contact_id=? " +
                        "AND old_customer_id=? " +
                        "AND new_customer_id=?")) {

                    ps.setLong(1, contactId);
                    ps.setLong(2, firstCustomer);
                    ps.setLong(3, secondCustomer);

                    try (ResultSet rs = ps.executeQuery()) {
                        assertTrue(rs.next());
                        assertEquals(1, rs.getInt(1));
                    }
                }

                // DELETE
                assertTrue(dao.delete(contactId));
                assertNull(dao.findById(contactId));

                // History must survive contact deletion.
                try (PreparedStatement ps =
                             conn.prepareStatement(
                        "SELECT COUNT(*) " +
                        "FROM contact_customer_history " +
                        "WHERE contact_id=?")) {

                    ps.setLong(1, contactId);

                    try (ResultSet rs = ps.executeQuery()) {
                        assertTrue(rs.next());
                        assertEquals(1, rs.getInt(1));
                    }
                }

            } finally {
                // Cleanup only data created by this test.
                if (contactId > 0) {
                    try (PreparedStatement ps =
                                 conn.prepareStatement(
                            "DELETE FROM contact_customer_history " +
                            "WHERE contact_id=?")) {

                        ps.setLong(1, contactId);
                        ps.executeUpdate();
                    }

                    try (PreparedStatement ps =
                                 conn.prepareStatement(
                            "DELETE FROM contacts WHERE id=?")) {

                        ps.setLong(1, contactId);
                        ps.executeUpdate();
                    }
                }

                for (long id : new long[]{
                        firstCustomer, secondCustomer}) {

                    if (id > 0) {
                        try (PreparedStatement ps =
                                     conn.prepareStatement(
                                "DELETE FROM customers WHERE id=?")) {

                            ps.setLong(1, id);
                            ps.executeUpdate();
                        }
                    }
                }

                if (ownerId > 0) {
                    try (PreparedStatement ps =
                                 conn.prepareStatement(
                            "DELETE FROM users WHERE id=?")) {

                        ps.setLong(1, ownerId);
                        ps.executeUpdate();
                    }
                }
            }
        }
    }
}