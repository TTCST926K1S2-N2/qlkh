package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.dao.Customer360DAO;
import vn.edu.ictu.qlkh.dao.CustomerDAO;
import vn.edu.ictu.qlkh.dto.Customer360DTO;
import vn.edu.ictu.qlkh.model.Customer;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Customer360ServiceTest {
    private static class CustomerStub extends CustomerDAO {
        Customer visible;
        String lastScope;
        int calls;
        @Override
        public Customer findVisibleById(long id, long userId, String scope)
                throws SQLException {
            calls++;
            lastScope = scope;
            return visible;
        }
    }

    private static class ContactsStub extends Customer360DAO {
        int calls;
        ContactSection section = new ContactSection(List.of(), false);
        @Override
        public ContactSection findContacts(long customerId) throws SQLException {
            calls++;
            return section;
        }
    }

    private Customer customer() {
        Customer c = new Customer();
        c.setId(42L);
        c.setOwnerId(7L);
        c.setCompanyName("Công ty A");
        return c;
    }

    @Test
    void returnsCustomerAndContactsFromAvailableSource() throws Exception {
        CustomerStub customers = new CustomerStub();
        customers.visible = customer();
        ContactsStub contacts = new ContactsStub();
        contacts.section = new Customer360DAO.ContactSection(
                List.of(new Customer360DTO.ContactItem(3, 42, "An", null,
                        null, null, null, true, null, null)), true);

        Customer360DTO result = new Customer360Service(
                customers, contacts, new PermissionService())
                .get(42, 7, "SALES");

        assertNotNull(result);
        assertEquals("MY", customers.lastScope);
        assertEquals(1, contacts.calls);
        assertEquals(1, result.contacts().size());
        assertTrue(result.availability().contacts());
        assertTrue(result.opportunities().isEmpty());
        assertFalse(result.availability().opportunities());
    }

    @Test
    void invisibleCustomerReturnsNullWithoutLoadingContacts() throws Exception {
        CustomerStub customers = new CustomerStub();
        ContactsStub contacts = new ContactsStub();
        Customer360DTO result = new Customer360Service(
                customers, contacts, new PermissionService())
                .get(42, 7, "SALES");
        assertNull(result);
        assertEquals(0, contacts.calls);
    }

    @Test
    void managerUsesTeamScope() throws Exception {
        CustomerStub customers = new CustomerStub();
        customers.visible = customer();
        ContactsStub contacts = new ContactsStub();
        new Customer360Service(customers, contacts, new PermissionService())
                .get(42, 8, "MANAGER");
        assertEquals("TEAM", customers.lastScope);
    }

    @Test
    void adminUsesAllScope() throws Exception {
        CustomerStub customers = new CustomerStub();
        customers.visible = customer();
        ContactsStub contacts = new ContactsStub();
        new Customer360Service(customers, contacts, new PermissionService())
                .get(42, 8, "ADMIN");
        assertEquals("ALL", customers.lastScope);
    }

    @Test
    void unknownRoleIsRejectedBeforeDatabase() {
        CustomerStub customers = new CustomerStub();
        ContactsStub contacts = new ContactsStub();
        Customer360Service service = new Customer360Service(
                customers, contacts, new PermissionService());
        assertThrows(SecurityException.class,
                () -> service.get(42, 7, "GUEST"));
        assertEquals(0, customers.calls);
        assertEquals(0, contacts.calls);
    }

    @Test
    void invalidIdIsRejected() {
        Customer360Service service = new Customer360Service(
                new CustomerStub(), new ContactsStub(), new PermissionService());
        assertThrows(IllegalArgumentException.class,
                () -> service.get(0, 7, "ADMIN"));
    }

    @Test
    void unavailableContactsIsDistinctFromEmptyAvailableContacts() throws Exception {
        CustomerStub customers = new CustomerStub();
        customers.visible = customer();
        ContactsStub contacts = new ContactsStub();
        Customer360Service service = new Customer360Service(
                customers, contacts, new PermissionService());
        assertFalse(service.get(42, 7, "ADMIN").availability().contacts());
        contacts.section = new Customer360DAO.ContactSection(List.of(), true);
        assertTrue(service.get(42, 7, "ADMIN").availability().contacts());
    }
}
