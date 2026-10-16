package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.dto.Customer360DTO.ContactItem;
import vn.edu.ictu.qlkh.dto.Customer360DTO.ActivityItem;
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

    public List<ActivityItem> findContactTransferActivities(long customerId)
            throws SQLException {
        String sql = "SELECT id, contact_id, old_customer_id, "
                + "new_customer_id, transferred_at "
                + "FROM contact_customer_history "
                + "WHERE old_customer_id = ? OR new_customer_id = ? "
                + "ORDER BY transferred_at DESC, id DESC";

        List<ActivityItem> items = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, customerId);
            statement.setLong(2, customerId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    long contactId = rs.getLong("contact_id");
                    long oldId = rs.getLong("old_customer_id");
                    long newId = rs.getLong("new_customer_id");

                    String title = oldId == customerId
                            ? "Người liên hệ #" + contactId
                                + " được chuyển sang khách hàng #" + newId
                            : "Nhận người liên hệ #" + contactId
                                + " từ khách hàng #" + oldId;

                    items.add(new ActivityItem(
                            rs.getLong("id"),
                            "CONTACT_TRANSFER",
                            title,
                            date(rs.getTimestamp("transferred_at"))));
                }
            }
        }

        return items;
    }
    public List<vn.edu.ictu.qlkh.dto.Customer360DTO.OpportunityItem>
            findOpportunities(long customerId) throws SQLException {

        String sql = "SELECT o.id, o.name, s.name AS stage_name, "
                + "o.expected_value, o.updated_at "
                + "FROM opportunities o "
                + "LEFT JOIN stages s ON s.id = o.stage_id "
                + "WHERE o.customer_id = ? "
                + "ORDER BY o.updated_at DESC, o.id DESC";

        List<vn.edu.ictu.qlkh.dto.Customer360DTO.OpportunityItem> items =
                new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, customerId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    items.add(
                            new vn.edu.ictu.qlkh.dto.Customer360DTO.OpportunityItem(
                                    rs.getLong("id"),
                                    rs.getString("name"),
                                    rs.getString("stage_name"),
                                    rs.getBigDecimal("expected_value"),
                                    date(rs.getTimestamp("updated_at"))
                            )
                    );
                }
            }
        }

        return items;
    }

    public List<vn.edu.ictu.qlkh.dto.Customer360DTO.AttachmentItem>
            findAttachments(long customerId) throws SQLException {

        String sql = "SELECT id, file_name, uploaded_at "
                + "FROM attachments "
                + "WHERE customer_id = ? "
                + "ORDER BY uploaded_at DESC, id DESC";

        List<vn.edu.ictu.qlkh.dto.Customer360DTO.AttachmentItem> items =
                new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, customerId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    items.add(
                            new vn.edu.ictu.qlkh.dto.Customer360DTO.AttachmentItem(
                                    rs.getLong("id"),
                                    rs.getString("file_name"),
                                    "/api/v1/customer-attachments/" + customerId + "/" + rs.getLong("id"),
                                    date(rs.getTimestamp("uploaded_at"))
                            )
                    );
                }
            }
        }

        return items;
    }
    /**
     * Tao co hoi kinh doanh cho khach hang.
     * Phan quyen phai duoc kiem tra tai Service truoc khi goi DAO.
     */
    public long createOpportunity(
            long customerId,
            String name,
            Long stageId,
            java.math.BigDecimal expectedValue,
            java.time.LocalDate expectedCloseDate)
            throws SQLException {

        if (customerId <= 0) {
            throw new IllegalArgumentException("Khach hang khong hop le");
        }

        if (name == null || name.isBlank() || name.length() > 255) {
            throw new IllegalArgumentException("Ten co hoi khong hop le");
        }

        if (expectedValue != null && expectedValue.signum() < 0) {
            throw new IllegalArgumentException("Gia tri du kien khong hop le");
        }

        if (stageId != null && stageId <= 0) {
            throw new IllegalArgumentException("Giai doan khong hop le");
        }

        String sql = """
                INSERT INTO opportunities
                    (customer_id, name, stage_id, expected_value,
                     expected_close_date, status)
                VALUES (?, ?, ?, ?, ?, 'OPEN')
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, customerId);
            ps.setString(2, name.trim());

            if (stageId == null) {
                ps.setNull(3, java.sql.Types.BIGINT);
            } else {
                ps.setLong(3, stageId);
            }

            if (expectedValue == null) {
                ps.setNull(4, java.sql.Types.DECIMAL);
            } else {
                ps.setBigDecimal(4, expectedValue);
            }

            if (expectedCloseDate == null) {
                ps.setNull(5, java.sql.Types.DATE);
            } else {
                ps.setDate(5, java.sql.Date.valueOf(expectedCloseDate));
            }

            int rows = ps.executeUpdate();

            if (rows != 1) {
                throw new SQLException("Khong tao duoc co hoi");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }

            throw new SQLException("Khong lay duoc ID co hoi");
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
