package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.ContactDAO;
import vn.edu.ictu.qlkh.dao.CustomerDAO;
import vn.edu.ictu.qlkh.model.Contact;
import vn.edu.ictu.qlkh.model.Customer;
import vn.edu.ictu.qlkh.model.DataScope;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LeadDuplicateService {

    private final ContactDAO contactDAO;
    private final CustomerDAO customerDAO;
    private final PermissionService permissionService;

    public LeadDuplicateService() {
        this(
            new ContactDAO(),
            new CustomerDAO(),
            new PermissionService()
        );
    }

    public LeadDuplicateService(
            ContactDAO contactDAO,
            CustomerDAO customerDAO,
            PermissionService permissionService) {

        this.contactDAO = contactDAO;
        this.customerDAO = customerDAO;
        this.permissionService = permissionService;
    }

    public record DuplicateLead(
            Customer customer,
            Contact contact,
            List<String> reasons) {
    }

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

        List<Customer> customers =
            customerDAO.findVisible(userId, scope.name());

        List<DuplicateLead> result = new ArrayList<>();

        for (Customer customer : customers) {

            // Chi kiem tra khach hang tiem nang.
            if (!"POTENTIAL".equalsIgnoreCase(customer.getStatus())) {
                continue;
            }

            List<Contact> contacts =
                contactDAO.findByCustomer(customer.getId());

            for (Contact contact : contacts) {

                List<String> reasons = new ArrayList<>();

                String existingEmail =
                    normalizeEmail(contact.getEmail());

                String existingPhone =
                    normalizePhone(contact.getPhone());

                if (!normalizedEmail.isEmpty()
                        && normalizedEmail.equals(existingEmail)) {
                    reasons.add("EMAIL");
                }

                if (!normalizedPhone.isEmpty()
                        && normalizedPhone.equals(existingPhone)) {
                    reasons.add("PHONE");
                }

                if (!reasons.isEmpty()) {
                    result.add(new DuplicateLead(
                        customer,
                        contact,
                        List.copyOf(reasons)
                    ));
                }
            }
        }

        return result;
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizePhone(String phone) {
        if (phone == null) {
            return "";
        }

        // Bo khoang trang, dau cham, dau ngoac va dau gach.
        return phone.replaceAll("[^0-9]", "");
    }
}