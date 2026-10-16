package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import vn.edu.ictu.qlkh.dao.ContactDAO;
import vn.edu.ictu.qlkh.dao.CustomerDAO;
import vn.edu.ictu.qlkh.dao.LeadDAO;
import vn.edu.ictu.qlkh.model.Contact;
import vn.edu.ictu.qlkh.model.Customer;
import vn.edu.ictu.qlkh.model.DataScope;
import vn.edu.ictu.qlkh.model.Lead;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LeadDuplicateServiceTest {

    private LeadDAO leadDAO;
    private ContactDAO contactDAO;
    private CustomerDAO customerDAO;
    private PermissionService permissionService;
    private LeadDuplicateService service;

    @BeforeEach
    void setUp() {
        leadDAO = mock(LeadDAO.class);
        contactDAO = mock(ContactDAO.class);
        customerDAO = mock(CustomerDAO.class);
        permissionService = mock(PermissionService.class);

        service = new LeadDuplicateService(
                leadDAO,
                contactDAO,
                customerDAO,
                permissionService
        );

        when(permissionService.resolveDataScope("SALES"))
                .thenReturn(DataScope.MY);
        when(permissionService.resolveDataScope("MANAGER"))
                .thenReturn(DataScope.TEAM);
        when(permissionService.resolveDataScope("ADMIN"))
                .thenReturn(DataScope.ALL);
    }

    private Lead createSampleLead() {
        Lead lead = new Lead();
        lead.setId(100L);
        lead.setFullName("Le Van Lead");
        lead.setEmail("lead@example.com");
        lead.setPhone("0987654321");
        lead.setCompany("Cong ty Lead S4");
        lead.setStatus("NEW");
        lead.setAssignedTo(7L);
        return lead;
    }

    private Customer createCustomer() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setCompanyName("Cong ty ABC");
        customer.setStatus("POTENTIAL");
        customer.setOwnerId(7L);
        return customer;
    }

    private Contact createContact() {
        Contact contact = new Contact();
        contact.setId(10L);
        contact.setCustomerId(1L);
        contact.setFullName("Nguyen Van A");
        contact.setEmail("abc@example.com");
        contact.setPhone("0912345678");
        return contact;
    }

    @Test
    @DisplayName("Phát hiện trùng email trong bảng leads (không phân biệt chữ hoa/thường, khoảng trắng)")
    void shouldDetectDuplicateEmailInLeadsTable() throws SQLException {
        Lead lead = createSampleLead();
        when(leadDAO.findVisible(7L, "MY")).thenReturn(List.of(lead));
        when(customerDAO.findVisible(7L, "MY")).thenReturn(List.of());

        List<LeadDuplicateService.DuplicateLead> result =
                service.findDuplicates(7L, "SALES", "  LEAD@EXAMPLE.COM  ", null);

        assertEquals(1, result.size());
        assertEquals("LEAD", result.get(0).duplicateType());
        assertEquals(List.of("EMAIL"), result.get(0).reasons());
        assertEquals("Cong ty Lead S4", result.get(0).company());
        assertEquals(100L, result.get(0).id());
    }

    @Test
    @DisplayName("Phát hiện trùng số điện thoại trong bảng leads với các định dạng khác nhau (+84, dấu cách, gạch nối, ngoặc)")
    void shouldDetectDuplicatePhoneWithVariousFormats() throws SQLException {
        Lead lead = createSampleLead(); // Phone: 0987654321
        when(leadDAO.findVisible(7L, "MY")).thenReturn(List.of(lead));
        when(customerDAO.findVisible(7L, "MY")).thenReturn(List.of());

        // Kiểm tra định dạng +84 có dấu cách
        List<LeadDuplicateService.DuplicateLead> res1 =
                service.findDuplicates(7L, "SALES", null, "+84 987 654 321");
        assertEquals(1, res1.size());
        assertEquals(List.of("PHONE"), res1.get(0).reasons());

        // Kiểm tra định dạng có dấu chấm và ngoặc
        List<LeadDuplicateService.DuplicateLead> res2 =
                service.findDuplicates(7L, "SALES", null, "(098) 765.4321");
        assertEquals(1, res2.size());
        assertEquals(List.of("PHONE"), res2.get(0).reasons());

        // Kiểm tra định dạng có dấu gạch ngang
        List<LeadDuplicateService.DuplicateLead> res3 =
                service.findDuplicates(7L, "SALES", null, "098-765-4321");
        assertEquals(1, res3.size());
        assertEquals(List.of("PHONE"), res3.get(0).reasons());
    }

    @Test
    @DisplayName("Phát hiện trùng cả email và số điện thoại trong bảng leads")
    void shouldReturnBothReasonsWhenLeadMatchesEmailAndPhone() throws SQLException {
        Lead lead = createSampleLead();
        when(leadDAO.findVisible(7L, "MY")).thenReturn(List.of(lead));
        when(customerDAO.findVisible(7L, "MY")).thenReturn(List.of());

        List<LeadDuplicateService.DuplicateLead> result =
                service.findDuplicates(7L, "SALES", "lead@example.com", "0987654321");

        assertEquals(1, result.size());
        assertEquals(List.of("EMAIL", "PHONE"), result.get(0).reasons());
    }

    @Test
    @DisplayName("Phát hiện trùng trong danh sách khách hàng tiềm năng POTENTIAL và liên hệ")
    void shouldDetectDuplicateInPotentialCustomersAndContacts() throws SQLException {
        when(leadDAO.findVisible(7L, "MY")).thenReturn(List.of());

        Customer customer = createCustomer();
        Contact contact = createContact();
        when(customerDAO.findVisible(7L, "MY")).thenReturn(List.of(customer));
        when(contactDAO.findByCustomer(1L)).thenReturn(List.of(contact));

        List<LeadDuplicateService.DuplicateLead> result =
                service.findDuplicates(7L, "SALES", "ABC@EXAMPLE.COM", "0912345678");

        assertEquals(1, result.size());
        assertEquals("CUSTOMER", result.get(0).duplicateType());
        assertEquals(List.of("EMAIL", "PHONE"), result.get(0).reasons());
        assertEquals("Cong ty ABC", result.get(0).customer().getCompanyName());
    }

    @Test
    @DisplayName("Bỏ qua khách hàng không có trạng thái POTENTIAL")
    void shouldIgnoreNonPotentialCustomers() throws SQLException {
        when(leadDAO.findVisible(7L, "MY")).thenReturn(List.of());

        Customer activeCustomer = createCustomer();
        activeCustomer.setStatus("ACTIVE");
        when(customerDAO.findVisible(7L, "MY")).thenReturn(List.of(activeCustomer));

        List<LeadDuplicateService.DuplicateLead> result =
                service.findDuplicates(7L, "SALES", "abc@example.com", "0912345678");

        assertTrue(result.isEmpty());
        verifyNoInteractions(contactDAO);
    }

    @Test
    @DisplayName("Không trùng lặp khi thông tin email và số điện thoại khác biệt")
    void shouldReturnEmptyWhenNoDuplicateExists() throws SQLException {
        Lead lead = createSampleLead();
        when(leadDAO.findVisible(7L, "MY")).thenReturn(List.of(lead));
        when(customerDAO.findVisible(7L, "MY")).thenReturn(List.of());

        List<LeadDuplicateService.DuplicateLead> result =
                service.findDuplicates(7L, "SALES", "other@example.com", "0933333333");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Báo lỗi khi thiếu cả email và số điện thoại (null hoặc rỗng)")
    void shouldRejectWhenEmailAndPhoneAreMissing() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.findDuplicates(7L, "SALES", null, null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.findDuplicates(7L, "SALES", "   ", "  ")
        );

        verifyNoInteractions(leadDAO, customerDAO, contactDAO);
    }

    @Test
    @DisplayName("Cho phép tìm kiếm khi chỉ có email hoặc chỉ có số điện thoại")
    void shouldAllowWhenOnlyOneFieldProvided() throws SQLException {
        when(leadDAO.findVisible(7L, "MY")).thenReturn(List.of());
        when(customerDAO.findVisible(7L, "MY")).thenReturn(List.of());

        assertDoesNotThrow(() -> service.findDuplicates(7L, "SALES", "test@example.com", null));
        assertDoesNotThrow(() -> service.findDuplicates(7L, "SALES", null, "0987654321"));
    }

    @Test
    @DisplayName("Từ chối khi tài khoản không hợp lệ (userId <= 0)")
    void shouldRejectInvalidUserId() {
        assertThrows(
                SecurityException.class,
                () -> service.findDuplicates(0L, "SALES", "lead@example.com", null)
        );

        assertThrows(
                SecurityException.class,
                () -> service.findDuplicates(-1L, "SALES", "lead@example.com", null)
        );
    }

    @Test
    @DisplayName("Từ chối khi vai trò không được cấp quyền phạm vi dữ liệu")
    void shouldRejectRoleWithoutDataScope() {
        when(permissionService.resolveDataScope("GUEST")).thenReturn(null);

        assertThrows(
                SecurityException.class,
                () -> service.findDuplicates(7L, "GUEST", "lead@example.com", null)
        );
    }

    @Test
    @DisplayName("Quản lý (MANAGER) sử dụng phạm vi TEAM, Admin sử dụng phạm vi ALL")
    void shouldResolveScopeCorrectlyForManagerAndAdmin() throws SQLException {
        when(leadDAO.findVisible(2L, "TEAM")).thenReturn(List.of());
        when(customerDAO.findVisible(2L, "TEAM")).thenReturn(List.of());

        when(leadDAO.findVisible(1L, "ALL")).thenReturn(List.of());
        when(customerDAO.findVisible(1L, "ALL")).thenReturn(List.of());

        List<LeadDuplicateService.DuplicateLead> managerRes =
                service.findDuplicates(2L, "MANAGER", "lead@example.com", null);
        assertTrue(managerRes.isEmpty());
        verify(leadDAO).findVisible(2L, "TEAM");

        List<LeadDuplicateService.DuplicateLead> adminRes =
                service.findDuplicates(1L, "ADMIN", "lead@example.com", null);
        assertTrue(adminRes.isEmpty());
        verify(leadDAO).findVisible(1L, "ALL");
    }

    @Test
    @DisplayName("Kiểm tra phương thức kiểm tra trùng hệ thống và trước khi lưu dữ liệu")
    void shouldCheckDuplicateBeforeSave() throws SQLException {
        Lead lead = createSampleLead();
        when(leadDAO.findAll()).thenReturn(List.of(lead));
        when(customerDAO.findVisible(1L, "ALL")).thenReturn(List.of());

        assertTrue(service.hasDuplicate("lead@example.com", null));
        assertTrue(service.hasDuplicate(null, "0987654321"));
        assertFalse(service.hasDuplicate("new@example.com", "0944444444"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.checkDuplicateBeforeSave("lead@example.com", null)
        );

        assertDoesNotThrow(() -> service.checkDuplicateBeforeSave("new@example.com", "0944444444"));
    }
}