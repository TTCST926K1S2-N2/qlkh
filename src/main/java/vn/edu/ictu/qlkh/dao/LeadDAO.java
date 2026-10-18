package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LeadDAO {

    private static final String INSERT_SQL =
            "INSERT INTO leads " +
            "(full_name, email, phone, company, interest, status, source, source_form) " +
            "VALUES (?, ?, ?, ?, ?, 'NEW', 'WEBSITE', ?)";

    private static final String INSERT_IMPORTED_LEAD_SQL =
            "INSERT INTO leads " +
            "(full_name, email, phone, company, job_title, tax_code, industry, " +
            "company_size, website, address, status, source) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'NEW', 'EXCEL')";

    private static final String EXISTS_BY_EMAIL_OR_PHONE_SQL =
            "SELECT 1 FROM leads " +
            "WHERE " +
            "(? IS NOT NULL AND LOWER(TRIM(email)) = ?) " +
            "OR " +
            "(? IS NOT NULL AND " +
            "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(phone, ' ', ''), '-', ''), '.', ''), '(', ''), ')', ''), '+', '') = ?) " +
            "LIMIT 1";

    public long create(
            String fullName,
            String email,
            String phone,
            String company,
            String interest,
            String sourceForm
    ) throws SQLException {

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     INSERT_SQL,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            statement.setString(1, fullName);
            statement.setString(2, email);
            statement.setString(3, phone);
            statement.setString(4, company);
            statement.setString(5, interest);
            statement.setString(6, sourceForm);

            int affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                throw new SQLException("Khong the tao Lead.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }

            throw new SQLException("Khong lay duoc ID Lead.");
        }
    }

    public long createImportedLead(
            String fullName,
            String email,
            String phone,
            String company,
            String jobTitle,
            String taxCode,
            String industry,
            String companySize,
            String website,
            String address
    ) throws SQLException {

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     INSERT_IMPORTED_LEAD_SQL,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            statement.setString(1, fullName);
            statement.setString(2, email);
            statement.setString(3, phone);
            statement.setString(4, company);
            statement.setString(5, jobTitle);
            statement.setString(6, taxCode);
            statement.setString(7, industry);
            statement.setString(8, companySize);
            statement.setString(9, website);
            statement.setString(10, address);

            int affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                throw new SQLException("Khong the tao Lead tu Excel.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }

            throw new SQLException("Khong lay duoc ID Lead.");
        }
    }

    public boolean existsByEmailOrPhone(
            String normalizedEmail,
            String normalizedPhone
    ) throws SQLException {

        if (normalizedEmail == null && normalizedPhone == null) {
            return false;
        }

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(EXISTS_BY_EMAIL_OR_PHONE_SQL)) {

            statement.setString(1, normalizedEmail);
            statement.setString(2, normalizedEmail);
            statement.setString(3, normalizedPhone);
            statement.setString(4, normalizedPhone);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public void findExistingLeadKeys(
            java.util.Set<String> emails,
            java.util.Set<String> phones
    ) throws SQLException {

        String sql = "SELECT email, phone FROM leads";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                String email = rs.getString("email");
                String phone = rs.getString("phone");

                if (email != null && !email.isBlank()) {
                    emails.add(email.trim().toLowerCase(java.util.Locale.ROOT));
                }

                if (phone != null && !phone.isBlank()) {
                    String normalized = phone.replaceAll("[^0-9]", "");
                    if (!normalized.isEmpty()) {
                        phones.add(normalized);
                    }
                }
            }
        }
    }
}
