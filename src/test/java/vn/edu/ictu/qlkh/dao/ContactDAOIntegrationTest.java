package vn.edu.ictu.qlkh.dao;

import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.model.Contact;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class ContactDAOIntegrationTest {

    @Test
    void missingContactReturnsNull() throws Exception {
        try (Connection conn = DBConnection.getConnection()) {
            assertEquals("qlkh_s209_test", conn.getCatalog());
        }

        ContactDAO dao = new ContactDAO();

        assertNull(dao.findById(Long.MAX_VALUE));
    }

    @Test
    void missingCustomerReturnsEmptyList() throws Exception {
        try (Connection conn = DBConnection.getConnection()) {
            assertEquals("qlkh_s209_test", conn.getCatalog());
        }

        ContactDAO dao = new ContactDAO();

        assertTrue(
            dao.findByCustomer(Long.MAX_VALUE).isEmpty()
        );
    }

    @Test
    void invalidCustomerCannotBeInserted() throws Exception {
        try (Connection conn = DBConnection.getConnection()) {
            assertEquals("qlkh_s209_test", conn.getCatalog());
        }

        Contact contact = new Contact();

        contact.setCustomerId(Long.MAX_VALUE);
        contact.setFullName("S302 Invalid Customer");
        contact.setPrimary(true);

        ContactDAO dao = new ContactDAO();

        assertThrows(
            SQLException.class,
            () -> dao.insert(contact)
        );
    }

    @Test
    void missingContactCannotBeUpdatedOrDeleted()
            throws Exception {

        try (Connection conn = DBConnection.getConnection()) {
            assertEquals("qlkh_s209_test", conn.getCatalog());
        }

        ContactDAO dao = new ContactDAO();

        Contact contact = new Contact();

        contact.setId(Long.MAX_VALUE);
        contact.setCustomerId(Long.MAX_VALUE);
        contact.setFullName("S302 Missing Contact");

        assertFalse(dao.update(contact));
        assertFalse(dao.delete(Long.MAX_VALUE));
    }
}