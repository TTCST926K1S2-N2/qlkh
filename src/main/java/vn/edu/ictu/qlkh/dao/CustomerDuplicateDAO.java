package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.Customer;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class CustomerDuplicateDAO {
    private final CustomerDAO customerDAO = new CustomerDAO();

    public List<Customer> findPotentialDuplicates(Customer base, long userId, String scope)
            throws SQLException {
        if (base == null || base.getId() == null) {
            throw new IllegalArgumentException("Khach hang khong hop le");
        }
        return customerDAO.findVisible(userId, scope).stream()
                .filter(c -> !c.getId().equals(base.getId()))
                .filter(c -> isDuplicate(base, c))
                .toList();
    }

    public boolean isDuplicate(Customer a, Customer b) {
        if (a == null || b == null || a.getId() == null || b.getId() == null
                || a.getId().equals(b.getId())) return false;

        String taxA = normalize(a.getTaxCode(), true);
        String taxB = normalize(b.getTaxCode(), true);
        if (taxA != null && taxA.equals(taxB)) return true;

        String nameA = normalize(a.getCompanyName(), false);
        String nameB = normalize(b.getCompanyName(), false);
        if (nameA != null && nameA.equals(nameB)) return true;

        String webA = normalizeWebsite(a.getWebsite());
        String webB = normalizeWebsite(b.getWebsite());
        return webA != null && webA.equals(webB);
    }

    private String normalize(String value, boolean removeWhitespace) {
        if (value == null || value.isBlank()) return null;
        String s = value.trim().toLowerCase(Locale.ROOT);
        return removeWhitespace ? s.replaceAll("\\s+", "") : s.replaceAll("\\s+", " ");
    }

    private String normalizeWebsite(String value) {
        if (value == null || value.isBlank()) return null;
        String s = value.trim().toLowerCase(Locale.ROOT)
                .replaceFirst("^https?://", "")
                .replaceFirst("^www\\.", "");
        while (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        return s.isBlank() ? null : s;
    }
}
