package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.dao.CustomerDAO;
import vn.edu.ictu.qlkh.model.Customer;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CustomerServiceTest {

    private CustomerDAO dao;
    private CustomerService service;

    @BeforeEach
    void setup() {
        dao = mock(CustomerDAO.class);
        service = new CustomerService(
                dao, new PermissionService());
    }

    private Customer valid() {
        Customer c = new Customer();
        c.setCompanyName("Cong ty ABC");
        c.setTaxCode("0101234567");
        c.setOwnerId(10L);
        c.setStatus("POTENTIAL");
        return c;
    }

    @Test
    void salesOnlySeesOwnCustomers() throws SQLException {
        service.list(10L, "SALES");
        verify(dao).findVisible(10L, "MY");
    }

    @Test
    void managerUsesTeamScope() throws SQLException {
        service.list(10L, "MANAGER");
        verify(dao).findVisible(10L, "TEAM");
    }

    @Test
    void adminUsesAllScope() throws SQLException {
        service.list(10L, "ADMIN");
        verify(dao).findVisible(10L, "ALL");
    }

    @Test
    void invalidRoleIsRejected() {
        assertThrows(SecurityException.class,
                () -> service.list(10L, "GUEST"));
    }

    @Test
    void emptyCompanyNameIsRejected() throws SQLException {
        Customer c = valid();
        c.setCompanyName(" ");

        assertThrows(IllegalArgumentException.class,
                () -> service.create(c, 10L, "SALES"));

        verify(dao, never()).insert(any());
    }

    @Test
    void invalidStatusIsRejected() throws SQLException {
        Customer c = valid();
        c.setStatus("UNKNOWN");

        assertThrows(IllegalArgumentException.class,
                () -> service.create(c, 10L, "SALES"));
    }

    @Test
    void duplicateTaxCodeIsRejected() throws SQLException {
        Customer c = valid();

        when(dao.ownerExists(10L)).thenReturn(true);
        when(dao.existsTaxCode("0101234567", null))
                .thenReturn(true);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> service.create(c, 10L, "SALES"));

        assertTrue(ex.getMessage().contains("Ma so thue"));
        verify(dao, never()).insert(any());
    }

    @Test
    void salesCannotAssignOtherOwner() throws SQLException {
        Customer c = valid();
        c.setOwnerId(20L);

        when(dao.ownerExists(20L)).thenReturn(true);

        assertThrows(SecurityException.class,
                () -> service.create(c, 10L, "SALES"));

        verify(dao, never()).insert(any());
    }

    @Test
    void managerCannotAssignOutsideTeam() throws SQLException {
        Customer c = valid();
        c.setOwnerId(20L);

        when(dao.ownerExists(20L)).thenReturn(true);
        when(dao.isOwnerInTeam(20L, 10L))
                .thenReturn(false);

        assertThrows(SecurityException.class,
                () -> service.create(c, 10L, "MANAGER"));
    }

    @Test
    void managerCanAssignTeamMember() throws SQLException {
        Customer c = valid();
        c.setOwnerId(20L);

        when(dao.ownerExists(20L)).thenReturn(true);
        when(dao.isOwnerInTeam(20L, 10L))
                .thenReturn(true);
        when(dao.insert(any(Customer.class)))
                .thenReturn(99L);

        assertEquals(99L,
                service.create(c, 10L, "MANAGER"));
    }

    @Test
    void missingCustomerCannotBeUpdated() throws SQLException {
        Customer c = valid();

        when(dao.findVisibleById(5L, 10L, "MY"))
                .thenReturn(null);

        assertFalse(service.update(
                5L, c, 10L, "SALES"));

        verify(dao, never())
                .updateVisible(any(), anyLong(), anyString());
    }

    @Test
    void salesCannotTransferCustomer() throws SQLException {
        Customer existing = valid();
        existing.setId(5L);

        Customer updated = valid();
        updated.setOwnerId(20L);

        when(dao.findVisibleById(5L, 10L, "MY"))
                .thenReturn(existing);
        when(dao.ownerExists(20L)).thenReturn(true);

        assertThrows(SecurityException.class,
                () -> service.update(
                        5L, updated, 10L, "SALES"));

        verify(dao, never())
                .updateVisible(any(), anyLong(), anyString());
    }

    @Test
    void createDefaultsOwnerAndStatus() throws SQLException {
        Customer c = valid();
        c.setOwnerId(null);
        c.setStatus(null);

        when(dao.ownerExists(10L)).thenReturn(true);
        when(dao.insert(any(Customer.class)))
                .thenReturn(7L);

        assertEquals(7L,
                service.create(c, 10L, "SALES"));

        assertEquals(10L, c.getOwnerId());
        assertEquals("POTENTIAL", c.getStatus());
    }
}