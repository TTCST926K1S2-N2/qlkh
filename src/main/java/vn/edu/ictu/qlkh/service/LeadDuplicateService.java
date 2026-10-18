package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.ContactDAO;
import vn.edu.ictu.qlkh.dao.CustomerDAO;
import vn.edu.ictu.qlkh.dao.LeadDAO;
import vn.edu.ictu.qlkh.model.Contact;
import vn.edu.ictu.qlkh.model.Customer;
import vn.edu.ictu.qlkh.model.DataScope;
import vn.edu.ictu.qlkh.model.Lead;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * S4-04: Dịch vụ phát hiện trùng lặp Lead.
 * Kiểm tra trùng lặp trên bảng leads của Sprint 4, đồng thời đối chiếu với
 * khách hàng tiềm năng (Customer status = 'POTENTIAL') và người liên hệ.
 */
public class LeadDuplicateService {

    private final LeadDAO leadDAO;
    private final ContactDAO contactDAO;
    private final CustomerDAO customerDAO;
    private final PermissionService permissionService;

    public LeadDuplicateService() {
        this(
            new LeadDAO(),
            new ContactDAO(),
            new CustomerDAO(),
            new PermissionService()
        );
    }

    public LeadDuplicateService(
            LeadDAO leadDAO,
            PermissionService permissionService) {
        this(
            leadDAO,
            new ContactDAO(),
            new CustomerDAO(),
            permissionService
        );
    }

    public LeadDuplicateService(
            ContactDAO contactDAO,
            CustomerDAO customerDAO,
            PermissionService permissionService) {
        this(
            new LeadDAO(),
            contactDAO,
            customerDAO,
            permissionService
        );
    }

    public LeadDuplicateService(
            LeadDAO leadDAO,
            ContactDAO contactDAO,
            CustomerDAO customerDAO,
            PermissionService permissionService) {

        this.leadDAO = leadDAO;
        this.contactDAO = contactDAO;
        this.customerDAO = customerDAO;
        this.permissionService = permissionService;
    }

    public record DuplicateLead(
            Long id,
            String fullName,
            String email,
            String phone,
            String company,
            String status,
            Long assignedTo,
            String duplicateType,
            Lead lead,
            Customer customer,
            Contact contact,
            List<String> reasons) {

        public DuplicateLead(Lead lead, List<String> reasons) {
            this(
                lead != null ? lead.getId() : null,
                lead != null ? lead.getFullName() : null,
                lead != null ? lead.getEmail() : null,
                lead != null ? lead.getPhone() : null,
                lead != null ? lead.getCompany() : null,
                lead != null ? lead.getStatus() : null,
                lead != null ? lead.getAssignedTo() : null,
                "LEAD",
                lead,
                null,
                null,
                reasons
            );
        }

        public DuplicateLead(Customer customer, Contact contact, List<String> reasons) {
            this(
                customer != null ? customer.getId() : (contact != null ? contact.getCustomerId() : null),
                contact != null ? contact.getFullName() : (customer != null ? customer.getCompanyName() : null),
                contact != null ? contact.getEmail() : null,
                contact != null ? contact.getPhone() : null,
                customer != null ? customer.getCompanyName() : null,
                customer != null ? customer.getStatus() : null,
                customer != null ? customer.getOwnerId() : null,
                "CUSTOMER",
                null,
                customer,
                contact,
                reasons
            );
        }
    }

    /**
     * Tìm kiếm Lead trùng theo người dùng và phân quyền phạm vi dữ liệu.
     */
    public List<DuplicateLead> findDuplicates(
            long userId,
            String role,
            String email,
            String phone) throws SQLException {

        if (userId <= 0) {
            throw new SecurityException("Tai khoan khong hop le");
        }

        DataScope scope = permissionService.resolveDataScope(role);

        if (scope == null) {
            throw new SecurityException("Khong co quyen truy cap");
        }

        String normalizedEmail = normalizeEmail(email);
        String normalizedPhone = normalizePhone(phone);

        if (normalizedEmail.isEmpty() && normalizedPhone.isEmpty()) {
            throw new IllegalArgumentException(
                "Can cung cap email hoac so dien thoai"
            );
        }

        List<DuplicateLead> result = new ArrayList<>();

        // 1. Kiểm tra đối tượng Lead trong bảng leads (Sprint 4)
        List<Lead> leads = leadDAO.findVisible(userId, scope.name());
        for (Lead lead : leads) {
            List<String> reasons = new ArrayList<>();

            String existingEmail = normalizeEmail(lead.getEmail());
            String existingPhone = normalizePhone(lead.getPhone());

            if (!normalizedEmail.isEmpty() && normalizedEmail.equals(existingEmail)) {
                reasons.add("EMAIL");
            }

            if (!normalizedPhone.isEmpty() && normalizedPhone.equals(existingPhone)) {
                reasons.add("PHONE");
            }

            if (!reasons.isEmpty()) {
                result.add(new DuplicateLead(lead, List.copyOf(reasons)));
            }
        }

        // 2. Đối chiếu thêm với bảng customers (trạng thái POTENTIAL) và contacts
        List<Customer> customers = customerDAO.findVisible(userId, scope.name());
        for (Customer customer : customers) {
            if (!"POTENTIAL".equalsIgnoreCase(customer.getStatus())) {
                continue;
            }

            List<Contact> contacts = contactDAO.findByCustomer(customer.getId());
            for (Contact contact : contacts) {
                List<String> reasons = new ArrayList<>();

                String existingEmail = normalizeEmail(contact.getEmail());
                String existingPhone = normalizePhone(contact.getPhone());

                if (!normalizedEmail.isEmpty() && normalizedEmail.equals(existingEmail)) {
                    reasons.add("EMAIL");
                }

                if (!normalizedPhone.isEmpty() && normalizedPhone.equals(existingPhone)) {
                    reasons.add("PHONE");
                }

                if (!reasons.isEmpty()) {
                    result.add(new DuplicateLead(customer, contact, List.copyOf(reasons)));
                }
            }
        }

        return result;
    }

    /**
     * Kiểm tra trùng lặp trên toàn hệ thống (phục vụ tạo Lead và import Lead).
     */
    public List<DuplicateLead> findDuplicates(String email, String phone) throws SQLException {
        String normalizedEmail = normalizeEmail(email);
        String normalizedPhone = normalizePhone(phone);

        if (normalizedEmail.isEmpty() && normalizedPhone.isEmpty()) {
            throw new IllegalArgumentException("Can cung cap email hoac so dien thoai");
        }

        List<DuplicateLead> result = new ArrayList<>();

        // 1. Bảng leads
        List<Lead> leads = leadDAO.findAll();
        for (Lead lead : leads) {
            List<String> reasons = new ArrayList<>();

            String existingEmail = normalizeEmail(lead.getEmail());
            String existingPhone = normalizePhone(lead.getPhone());

            if (!normalizedEmail.isEmpty() && normalizedEmail.equals(existingEmail)) {
                reasons.add("EMAIL");
            }

            if (!normalizedPhone.isEmpty() && normalizedPhone.equals(existingPhone)) {
                reasons.add("PHONE");
            }

            if (!reasons.isEmpty()) {
                result.add(new DuplicateLead(lead, List.copyOf(reasons)));
            }
        }

        // 2. Bảng customers (POTENTIAL)
        List<Customer> customers = customerDAO.findVisible(1L, "ALL");
        for (Customer customer : customers) {
            if (!"POTENTIAL".equalsIgnoreCase(customer.getStatus())) {
                continue;
            }

            List<Contact> contacts = contactDAO.findByCustomer(customer.getId());
            for (Contact contact : contacts) {
                List<String> reasons = new ArrayList<>();

                String existingEmail = normalizeEmail(contact.getEmail());
                String existingPhone = normalizePhone(contact.getPhone());

                if (!normalizedEmail.isEmpty() && normalizedEmail.equals(existingEmail)) {
                    reasons.add("EMAIL");
                }

                if (!normalizedPhone.isEmpty() && normalizedPhone.equals(existingPhone)) {
                    reasons.add("PHONE");
                }

                if (!reasons.isEmpty()) {
                    result.add(new DuplicateLead(customer, contact, List.copyOf(reasons)));
                }
            }
        }

        return result;
    }

    /**
     * Kiểm tra có trùng hay không trước khi lưu dữ liệu.
     */
    public boolean hasDuplicate(String email, String phone) throws SQLException {
        return !findDuplicates(email, phone).isEmpty();
    }

    /**
     * Xác thực và ném ngoại lệ nếu phát hiện trùng lặp trước khi lưu dữ liệu.
     */
    public void checkDuplicateBeforeSave(String email, String phone) throws SQLException {
        List<DuplicateLead> duplicates = findDuplicates(email, phone);
        if (!duplicates.isEmpty()) {
            List<String> reasons = duplicates.get(0).reasons();
            if (reasons.contains("EMAIL") && reasons.contains("PHONE")) {
                throw new IllegalArgumentException("Email va so dien thoai da ton tai trong he thong");
            } else if (reasons.contains("EMAIL")) {
                throw new IllegalArgumentException("Email da ton tai trong he thong");
            } else {
                throw new IllegalArgumentException("So dien thoai da ton tai trong he thong");
            }
        }
    }

    /**
     * Chuẩn hóa email: cắt khoảng trắng đầu/cuối và chuyển về chữ thường.
     */
    public static String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Chuẩn hóa số điện thoại:
     * - Cắt khoảng trắng đầu/cuối
     * - Chuyển đổi mã quốc tế +84 hoặc 84 về đầu số 0
     * - Loại bỏ toàn bộ khoảng trắng, dấu gạch nối, dấu chấm, dấu ngoặc
     */
    public static String normalizePhone(String phone) {
        if (phone == null) {
            return "";
        }

        String p = phone.trim();

        if (p.startsWith("+84")) {
            p = "0" + p.substring(3);
        }

        // Bỏ tất cả các ký tự không phải số
        String digits = p.replaceAll("[^0-9]", "");

        if (digits.startsWith("0084") && digits.length() == 13) {
            digits = "0" + digits.substring(4);
        } else if (digits.startsWith("84") && digits.length() == 11) {
            digits = "0" + digits.substring(2);
        }

        return digits;
    }
}