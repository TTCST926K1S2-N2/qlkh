package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.edu.ictu.qlkh.dao.ContactDAO;
import vn.edu.ictu.qlkh.dao.CustomerDAO;
import vn.edu.ictu.qlkh.model.Contact;
import vn.edu.ictu.qlkh.model.Customer;
import vn.edu.ictu.qlkh.model.DataScope;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LeadDuplicateServiceTest {

    private ContactDAO contactDAO;
    private CustomerDAO customerDAO;
    private PermissionService permissionService;
    private LeadDuplicateService service;

    @BeforeEach
    void setUp() {
        contactDAO = mock(ContactDAO.class);
        customerDAO = mock(CustomerDAO.class);
        permissionService = mock(PermissionService.class);

        service = new LeadDuplicateService(
                contactDAO,
                customerDAO,
                permissionService
        );

        when(permissionService.resolveDataScope("SALES"))
                .thenReturn(DataScope.MY);
    }

    private Customer createCustomer() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setCompanyName("Cong ty ABC");
        customer.setStatus("POTENTIAL");
        return customer;
    }

    private Contact createContact() {
        Contact contact = new Contact();
        contact.setId(10L);
        contact.setCustomerId(1L);
        contact.setFullName("Nguyen Van A");
        contact.setEmail("abc@example.com");
        contact.setPhone("0987654321");
        return contact;
    }

    private void prepareExistingLead() throws SQLException {
        Customer customer = createCustomer();
        Contact contact = createContact();

        when(customerDAO.findVisible(7L, "MY"))
                .thenReturn(List.of(customer));

        when(contactDAO.findByCustomer(1L))
                .thenReturn(List.of(contact));
    }

    @Test
    void shouldDetectDuplicateEmailIgnoringCase()
            throws SQLException {

        prepareExistingLead();

        List<LeadDuplicateService.DuplicateLead> result =
                service.findDuplicates(
                        7L,
                        "SALES",
                        " ABC@EXAMPLE.COM ",
                        null
                );

        assertEquals(1, result.size());
        assertEquals(
                List.of("EMAIL"),
                result.get(0).reasons()
        );
        assertEquals(
                "Cong ty ABC",
                result.get(0).customer().getCompanyName()
        );
    }

    @Test
    void shouldDetectDuplicatePhoneAfterNormalization()
            throws SQLException {

        prepareExistingLead();

        List<LeadDuplicateService.DuplicateLead> result =
                service.findDuplicates(
                        7L,
                        "SALES",
                        null,
                        "098 765 4321"
                );

        assertEquals(1, result.size());
        assertEquals(
                List.of("PHONE"),
                result.get(0).reasons()
        );
    }

    @Test
    void shouldReturnBothReasonsWhenEmailAndPhoneMatch()
            throws SQLException {

        prepareExistingLead();

        List<LeadDuplicateService.DuplicateLead> result =
                service.findDuplicates(
                        7L,
                        "SALES",
                        "abc@example.com",
                        "0987654321"
                );

        assertEquals(1, result.size());
        assertEquals(
                List.of("EMAIL", "PHONE"),
                result.get(0).reasons()
        );
    }

    @Test
    void shouldReturnEmptyWhenNoDuplicateExists()
            throws SQLException {

        prepareExistingLead();

        List<LeadDuplicateService.DuplicateLead> result =
                service.findDuplicates(
                        7L,
                        "SALES",
                        "different@example.com",
                        "0912345678"
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRejectWhenEmailAndPhoneAreMissing() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.findDuplicates(
                        7L,
                        "SALES",
                        " ",
                        null
                )
        );

        verifyNoInteractions(customerDAO, contactDAO);
    }

    @Test
    void shouldRejectInvalidUserId() {

        assertThrows(
                SecurityException.class,
                () -> service.findDuplicates(
                        0L,
                        "SALES",
                        "abc@example.com",
                        null
                )
        );
    }

    @Test
    void shouldRejectRoleWithoutDataScope() {

        when(permissionService.resolveDataScope("UNKNOWN"))
                .thenReturn(null);

        assertThrows(
                SecurityException.class,
                () -> service.findDuplicates(
                        7L,
                        "UNKNOWN",
                        "abc@example.com",
                        null
                )
        );
    }
}