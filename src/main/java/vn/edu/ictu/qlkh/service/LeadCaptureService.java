package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.LeadDAO;
import java.sql.SQLException;

public class LeadCaptureService {
    private final LeadDAO dao = new LeadDAO();
    private static final LeadRateLimiter RATE_LIMITER = new LeadRateLimiter();

    public long submit(String fullName, String email, String phone,
                       String company, String interest, String sourceForm,
                       String honeypot, String ip)
            throws SQLException, ValidationException {

        if (honeypot != null && !honeypot.isBlank()) {
            throw new ValidationException(400, "Invalid submission");
        }

        String name = clean(fullName);
        String mail = clean(email);
        String mobile = clean(phone);
        String firm = clean(company);
        String need = clean(interest);
        String form = clean(sourceForm);

        if (name.isEmpty() || name.length() > 255
                || mail.length() > 255 || mobile.length() > 30
                || firm.length() > 255 || need.length() > 5000) {
            throw new ValidationException(400, "Invalid field length or name");
        }
        if (!mail.isEmpty() &&
                !mail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ValidationException(400, "Invalid email");
        }
        if (!mobile.matches("^(0[0-9]{9}|\\+84[0-9]{9})$")) {
            throw new ValidationException(400, "Invalid phone");
        }
        // Only the public website form is registered for S4-01.
        if (!form.isEmpty() && !form.equals("website-default")) {
            throw new ValidationException(400, "Unknown form");
        }

        if (!RATE_LIMITER.allow(ip)) {
            throw new ValidationException(429, "Too many requests");
        }

        return dao.create(name, emptyToNull(mail), mobile,
                emptyToNull(firm), emptyToNull(need), "website-default");
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static String emptyToNull(String value) {
        return value.isEmpty() ? null : value;
    }

    public static class ValidationException extends Exception {
        private final int status;

        public ValidationException(int status, String message) {
            super(message);
            this.status = status;
        }

        public int getStatus() {
            return status;
        }
    }
}