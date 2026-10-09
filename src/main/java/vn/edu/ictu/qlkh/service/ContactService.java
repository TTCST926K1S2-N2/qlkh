package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.ContactDAO;
import vn.edu.ictu.qlkh.dao.CustomerDAO;
import vn.edu.ictu.qlkh.model.Contact;
import vn.edu.ictu.qlkh.model.DataScope;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class ContactService {

    private static final Set<String> DECISION_ROLES = Set.of(
            "DECISION_MAKER",
            "INFLUENCER",
            "END_USER",
            "BLOCKER"
    );

    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE = Pattern.compile(
            "^\\+?[0-9][0-9 .()-]{6,28}$"
    );

    private final ContactDAO contactDAO;
    private final CustomerDAO customerDAO;
    private final PermissionService permissionService;

    public ContactService() {
        this(
                new ContactDAO(),
                new CustomerDAO(),
                new PermissionService()
        );
    }

    public ContactService(
            ContactDAO contactDAO,
            CustomerDAO customerDAO,
            PermissionService permissionService) {

        this.contactDAO = contactDAO;
        this.customerDAO = customerDAO;
        this.permissionService = permissionService;
    }

    private DataScope requireScope(String role) {
        DataScope scope =
                permissionService.resolveDataScope(role);

        if (scope == null) {
            throw new SecurityException(
                    "Khong co quyen quan ly nguoi lien he"
            );
        }

        return scope;
    }

    private boolean canAccessCustomer(
            long customerId,
            long userId,
            DataScope scope) throws SQLException {

        return customerDAO.findVisibleById(
                customerId,
                userId,
                scope.name()
        ) != null;
    }

    public List<Contact> list(
            long customerId,
            long userId,
            String role) throws SQLException {

        DataScope scope = requireScope(role);

        checkId(customerId);

        if (!canAccessCustomer(customerId, userId, scope)) {
            return null;
        }

        return contactDAO.findByCustomer(customerId);
    }

    public Contact detail(
            long id,
            long userId,
            String role) throws SQLException {

        DataScope scope = requireScope(role);

        checkId(id);

        Contact contact = contactDAO.findById(id);

        if (contact == null) {
            return null;
        }

        if (!canAccessCustomer(
                contact.getCustomerId(),
                userId,
                scope)) {
            return null;
        }

        return contact;
    }

    public long create(
            Contact contact,
            long userId,
            String role) throws SQLException {

        DataScope scope = requireScope(role);

        validate(contact);

        if (!canAccessCustomer(
                contact.getCustomerId(),
                userId,
                scope)) {

            throw new SecurityException(
                    "Khong co quyen them nguoi lien he"
            );
        }

        return contactDAO.insert(contact);
    }

    public boolean update(
            long id,
            Contact contact,
            long userId,
            String role) throws SQLException {

        DataScope scope = requireScope(role);

        checkId(id);

        if (contact == null) {
            throw new IllegalArgumentException(
                    "Du lieu khong hop le"
            );
        }

        Contact existing = contactDAO.findById(id);

        if (existing == null) {
            return false;
        }

        // Kiem tra quyen voi khach hang hien tai.
        if (!canAccessCustomer(
                existing.getCustomerId(),
                userId,
                scope)) {
            return false;
        }

        // Khong gui customerId thi giu khach hang cu.
        if (contact.getCustomerId() == null) {
            contact.setCustomerId(existing.getCustomerId());
        }

        validate(contact);

        // Kiem tra quyen voi khach hang dich.
        if (!canAccessCustomer(
                contact.getCustomerId(),
                userId,
                scope)) {

            throw new SecurityException(
                    "Khong co quyen chuyen nguoi lien he"
            );
        }

        contact.setId(id);

        return contactDAO.update(contact);
    }

    public boolean delete(
            long id,
            long userId,
            String role) throws SQLException {

        DataScope scope = requireScope(role);

        checkId(id);

        Contact existing = contactDAO.findById(id);

        if (existing == null) {
            return false;
        }

        if (!canAccessCustomer(
                existing.getCustomerId(),
                userId,
                scope)) {
            return false;
        }

        return contactDAO.delete(id);
    }

    private void validate(Contact c) {

        if (c == null) {
            throw new IllegalArgumentException(
                    "Du lieu khong hop le"
            );
        }

        if (c.getCustomerId() == null ||
                c.getCustomerId() <= 0) {

            throw new IllegalArgumentException(
                    "Khach hang khong hop le"
            );
        }

        c.setFullName(trim(c.getFullName()));
        c.setJobTitle(trim(c.getJobTitle()));
        c.setEmail(trim(c.getEmail()));
        c.setPhone(trim(c.getPhone()));
        c.setDecisionRole(trim(c.getDecisionRole()));

        if (c.getFullName() == null ||
                c.getFullName().length() > 255) {

            throw new IllegalArgumentException(
                    "Ho ten khong hop le"
            );
        }

        checkLength(c.getJobTitle(), 150, "Chuc danh");
        checkLength(c.getEmail(), 255, "Email");
        checkLength(c.getPhone(), 30, "So dien thoai");

        if (c.getEmail() != null &&
                !EMAIL.matcher(c.getEmail()).matches()) {

            throw new IllegalArgumentException(
                    "Email khong hop le"
            );
        }

        if (c.getPhone() != null &&
                !PHONE.matcher(c.getPhone()).matches()) {

            throw new IllegalArgumentException(
                    "So dien thoai khong hop le"
            );
        }

        if (c.getDecisionRole() != null &&
                !DECISION_ROLES.contains(
                        c.getDecisionRole())) {

            throw new IllegalArgumentException(
                    "Vai tro quyet dinh mua khong hop le"
            );
        }
    }

    private void checkId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "ID khong hop le"
            );
        }
    }

    private void checkLength(
            String value,
            int max,
            String field) {

        if (value != null && value.length() > max) {
            throw new IllegalArgumentException(
                    field + " vuot qua " + max + " ky tu"
            );
        }
    }

    private String trim(String value) {

        if (value == null) {
            return null;
        }

        String result = value.trim();

        return result.isEmpty() ? null : result;
    }
}