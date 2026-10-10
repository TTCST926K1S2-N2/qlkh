package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.dto.Customer360DTO.ContactItem;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * S3-03: Read-only adapter for the S3-02 contacts schema.
 * Does not depend on unmerged S3-02 Java classes or create tables.
 */
public class Customer360DAO {

    private volatile Boolean contactsTableAvailable;

    public record ContactSection(List<ContactItem> items, boolean available) {
        public ContactSection {
            items = List.copyOf(items);
        }
    }

    public ContactSection findContacts(long customerId) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            if (!hasContactsTable(connection)) {
                return new ContactSection(List.of(), false);
            }

            String sql = "SELECT id, customer_id, full_name, job_title, "
                    + "email, phone, decision_role, is_primary, created_at, updated_at "
                    + "FROM contacts WHERE customer_id = ? "
                    + "ORDER BY is_primary DESC, id ASC";
            List<ContactItem> items = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, customerId);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        items.add(new ContactItem(
                                rs.getLong("id"),
                                rs.getLong("customer_id"),
                                rs.getString("full_name"),
                                rs.getString("job_title"),
                                rs.getString("email"),
                                rs.getString("phone"),
                                rs.getString("decision_role"),
                                rs.getBoolean("is_primary"),
                                date(rs.getTimestamp("created_at")),
                                date(rs.getTimestamp("updated_at"))));
                    }
                }
            }
            return new ContactSection(items, true);
        }
    }

    private static LocalDateTime date(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private boolean hasContactsTable(Connection connection) throws SQLException {
        Boolean cached = contactsTableAvailable;
        if (Boolean.TRUE.equals(cached)) {
            return cached;
        }
        synchronized (this) {
            if (!Boolean.TRUE.equals(contactsTableAvailable)) {
                String sql = "SELECT 1 FROM information_schema.tables "
                        + "WHERE table_schema = DATABASE() "
                        + "AND table_name = 'contacts' LIMIT 1";
                try (PreparedStatement ps = connection.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    contactsTableAvailable = rs.next();
                }
            }
            return contactsTableAvailable;
        }
    }
}
