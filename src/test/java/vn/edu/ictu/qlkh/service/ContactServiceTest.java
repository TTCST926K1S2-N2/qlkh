package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.edu.ictu.qlkh.dao.ContactDAO;
import vn.edu.ictu.qlkh.dao.CustomerDAO;
import vn.edu.ictu.qlkh.model.Contact;
import vn.edu.ictu.qlkh.model.Customer;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ContactServiceTest {

    private ContactDAO contactDAO;
    private CustomerDAO customerDAO;
    private ContactService service;

    @BeforeEach
    void setup() {
        contactDAO = mock(ContactDAO.class);
        customerDAO = mock(CustomerDAO.class);

        service = new ContactService(
                contactDAO,
                customerDAO,
                new PermissionService()
        );
    }

    private Contact validContact() {
        Contact c = new Contact();
        c.setCustomerId(10L);
        c.setFullName("Nguyen Van A");
        c.setEmail("a@example.com");
        c.setPhone("0912345678");
        c.setDecisionRole("DECISION_MAKER");
        c.setPrimary(true);
        return c;
    }

    private void allowCustomer() throws SQLException {
        when(customerDAO.findVisibleById(
                eq(10L), eq(1L), anyString()
        )).thenReturn(new Customer());
    }

    @Test
    void createValidContact() throws Exception {
        allowCustomer();
        when(contactDAO.insert(any(Contact.class)))
                .thenReturn(20L);

        long id = service.create(
                validContact(), 1L, "ADMIN"
        );

        assertEquals(20L, id);
        verify(contactDAO).insert(any(Contact.class));
    }

    @Test
    void rejectMissingName() {
        Contact c = validContact();
        c.setFullName("  ");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(c, 1L, "ADMIN")
        );
    }

    @Test
    void rejectInvalidEmail() {
        Contact c = validContact();
        c.setEmail("invalid-email");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(c, 1L, "ADMIN")
        );
    }

    @Test
    void rejectInvalidPhone() {
        Contact c = validContact();
        c.setPhone("abc");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(c, 1L, "ADMIN")
        );
    }

    @Test
    void rejectInvalidDecisionRole() {
        Contact c = validContact();
        c.setDecisionRole("INVALID");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(c, 1L, "ADMIN")
        );
    }

    @Test
    void rejectMissingCustomer() {
        Contact c = validContact();
        c.setCustomerId(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(c, 1L, "ADMIN")
        );
    }

    @Test
    void rejectUnauthorizedCustomer() throws Exception {
        when(customerDAO.findVisibleById(
                eq(10L), eq(1L), eq("MY")
        )).thenReturn(null);

        assertThrows(
                SecurityException.class,
                () -> service.create(
                        validContact(), 1L, "SALES"
                )
        );

        verify(contactDAO, never())
                .insert(any(Contact.class));
    }

    @Test
    void inaccessibleContactDetailReturnsNull()
            throws Exception {

        Contact c = validContact();

        when(contactDAO.findById(20L))
                .thenReturn(c);

        when(customerDAO.findVisibleById(
                eq(10L), eq(1L), eq("MY")
        )).thenReturn(null);

        assertNull(service.detail(
                20L, 1L, "SALES"
        ));
    }

    @Test
    void cannotTransferToUnauthorizedCustomer()
            throws Exception {

        Contact existing = validContact();

        Contact updated = validContact();
        updated.setCustomerId(99L);

        when(contactDAO.findById(20L))
                .thenReturn(existing);

        when(customerDAO.findVisibleById(
                eq(10L), eq(1L), eq("MY")
        )).thenReturn(new Customer());

        when(customerDAO.findVisibleById(
                eq(99L), eq(1L), eq("MY")
        )).thenReturn(null);

        assertThrows(
                SecurityException.class,
                () -> service.update(
                        20L, updated, 1L, "SALES"
                )
        );

        verify(contactDAO, never())
                .update(any(Contact.class));
    }

    @Test
    void cannotDeleteUnauthorizedContact()
            throws Exception {

        when(contactDAO.findById(20L))
                .thenReturn(validContact());

        assertFalse(service.delete(
                20L, 1L, "SALES"
        ));

        verify(contactDAO, never())
                .delete(anyLong());
    }

    @Test
    void listMissingCustomerReturnsNull() throws Exception {
        when(customerDAO.findVisibleById(
                eq(999999999L), eq(1L), eq("ALL")
        )).thenReturn(null);

        assertNull(service.list(
                999999999L, 1L, "ADMIN"
        ));

        verify(contactDAO, never())
                .findByCustomer(anyLong());
    }

    @Test
    void listUnauthorizedCustomerReturnsNull() throws Exception {
        when(customerDAO.findVisibleById(
                eq(10L), eq(1L), eq("MY")
        )).thenReturn(null);

        assertNull(service.list(
                10L, 1L, "SALES"
        ));

        verify(contactDAO, never())
                .findByCustomer(anyLong());
    }
}