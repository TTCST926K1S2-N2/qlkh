package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.CustomerSupportDAO;
import vn.edu.ictu.qlkh.model.CustomerSupportRequest;
import vn.edu.ictu.qlkh.model.Customer;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CustomerSupportService {

    private static final int RISK_THRESHOLD = 3;

    private static final Set<String> PRIORITIES =
            Set.of("LOW", "MEDIUM", "HIGH", "URGENT");

    private static final Set<String> STATUSES =
            Set.of("OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED");

    private final CustomerSupportDAO dao;
    private final CustomerService customerService;

    public CustomerSupportService() {
        this.dao = new CustomerSupportDAO();
        this.customerService = new CustomerService();
    }

    private void checkCustomerAccess(
            long customerId,
            long userId,
            String role) throws SQLException {

        if (customerId <= 0 || userId <= 0) {
            throw new IllegalArgumentException("ID khong hop le");
        }

        Customer customer = customerService.detail(
                customerId, userId, role);

        if (customer == null) {
            throw new SecurityException(
                    "Khong co quyen truy cap khach hang hoac khach hang khong ton tai");
        }
    }

    private String normalize(
            String value,
            Set<String> allowed,
            String field) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " khong duoc de trong");
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);

        if (!allowed.contains(normalized)) {
            throw new IllegalArgumentException(field + " khong hop le");
        }

        return normalized;
    }

    private String requiredText(
            String value,
            String field,
            int maxLength) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " khong duoc de trong");
        }

        String text = value.trim();

        if (text.length() > maxLength) {
            throw new IllegalArgumentException(field + " qua dai");
        }

        return text;
    }

    private void checkAssignee(Long assignedTo) throws SQLException {
        if (assignedTo == null) {
            return;
        }

        if (assignedTo <= 0 || !dao.userExists(assignedTo)) {
            throw new IllegalArgumentException(
                    "Nguoi xu ly khong ton tai hoac khong hoat dong");
        }
    }

    public CustomerSupportRequest create(
            CustomerSupportRequest request,
            long userId,
            String role) throws SQLException {

        if (request == null || request.getCustomerId() == null) {
            throw new IllegalArgumentException("Thieu thong tin khach hang");
        }

        checkCustomerAccess(request.getCustomerId(), userId, role);

        request.setTitle(requiredText(
                request.getTitle(), "Tieu de", 255));

        request.setDescription(requiredText(
                request.getDescription(), "Noi dung", 10000));

        request.setPriority(normalize(
                request.getPriority(), PRIORITIES, "Muc uu tien"));

        checkAssignee(request.getAssignedTo());

        request.setCreatedBy(userId);
        request.setStatus("OPEN");

        long id = dao.create(request);

        return dao.findById(id);
    }

    public List<CustomerSupportRequest> listByCustomer(
            long customerId,
            long userId,
            String role) throws SQLException {

        checkCustomerAccess(customerId, userId, role);

        return dao.findByCustomer(customerId);
    }

    public CustomerSupportRequest detail(
            long requestId,
            long userId,
            String role) throws SQLException {

        if (requestId <= 0) {
            throw new IllegalArgumentException("ID yeu cau khong hop le");
        }

        CustomerSupportRequest request = dao.findById(requestId);

        if (request == null) {
            return null;
        }

        checkCustomerAccess(request.getCustomerId(), userId, role);

        return request;
    }

    public CustomerSupportRequest update(
            long requestId,
            String status,
            String priority,
            Long assignedTo,
            String resolutionNote,
            long userId,
            String role) throws SQLException {

        CustomerSupportRequest current =
                detail(requestId, userId, role);

        if (current == null) {
            return null;
        }

        String validStatus =
                normalize(status, STATUSES, "Trang thai");

        String validPriority =
                normalize(priority, PRIORITIES, "Muc uu tien");

        checkAssignee(assignedTo);

        String note = resolutionNote == null
                ? null : resolutionNote.trim();

        if (note != null && note.length() > 10000) {
            throw new IllegalArgumentException(
                    "Ghi chu xu ly qua dai");
        }

        if (("RESOLVED".equals(validStatus)
                || "CLOSED".equals(validStatus))
                && (note == null || note.isBlank())) {

            throw new IllegalArgumentException(
                    "Can nhap ghi chu khi hoan thanh yeu cau");
        }

        boolean updated = dao.update(
                requestId,
                validStatus,
                validPriority,
                assignedTo,
                note
        );

        return updated ? dao.findById(requestId) : null;
    }

    public int countUnresolved(
            long customerId,
            long userId,
            String role) throws SQLException {

        checkCustomerAccess(customerId, userId, role);

        return dao.countUnresolved(customerId);
    }

    public boolean isAtRisk(
            long customerId,
            long userId,
            String role) throws SQLException {

        return countUnresolved(customerId, userId, role)
                >= RISK_THRESHOLD;
    }
}