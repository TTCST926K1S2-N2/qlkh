package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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

    private static final String SELECT_COLUMNS =
            "l.id, l.full_name, l.email, l.phone, l.company, l.interest, " +
            "l.status, l.source, l.source_form, l.campaign_id, l.assigned_to, " +
            "l.score, l.converted_customer_id, l.converted_contact_id, " +
            "l.created_at, l.updated_at";

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

    /**
     * Tim kiem, loc va phan trang danh sach Lead.
     *
     * scope:
     * MY   - chi xem Lead duoc giao cho minh.
     * TEAM - xem Lead duoc giao cho thanh vien cung nhom.
     * ALL  - xem tat ca Lead.
     */
    public SearchPage searchVisible(
            long userId,
            String scope,
            String keyword,
            String status,
            String source,
            Long assignedTo,
            int page,
            int size
    ) throws SQLException {
        return searchVisible(userId, scope, keyword, status, source,
                assignedTo, page, size, null, null, "createdAt", "desc");
    }

    public SearchPage searchVisible(
            long userId,
            String scope,
            String keyword,
            String status,
            String source,
            Long assignedTo,
            int page,
            int size,
            java.time.LocalDate createdFrom,
            java.time.LocalDate createdTo,
            String sortBy,
            String sortDirection
    ) throws SQLException {
        return searchVisible(userId, scope, keyword, status, source,
                assignedTo, page, size, createdFrom, createdTo,
                sortBy, sortDirection, null);
    }

    public SearchPage searchVisible(
            long userId,
            String scope,
            String keyword,
            String status,
            String source,
            Long assignedTo,
            int page,
            int size,
            java.time.LocalDate createdFrom,
            java.time.LocalDate createdTo,
            String sortBy,
            String sortDirection,
            String classification
    ) throws SQLException {

        if (userId <= 0) {
            throw new SecurityException("Nguoi dung khong hop le");
        }

        if (!"MY".equals(scope)
                && !"TEAM".equals(scope)
                && !"ALL".equals(scope)) {
            throw new SecurityException("Pham vi du lieu khong hop le");
        }

        if (page < 1 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Tham so phan trang khong hop le");
        }

        StringBuilder conditions = new StringBuilder(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if ("MY".equals(scope)) {
            conditions.append(" AND l.assigned_to = ? ");
            params.add(userId);
        } else if ("TEAM".equals(scope)) {
            conditions.append(
                    " AND EXISTS (" +
                    " SELECT 1 FROM user_business_groups me " +
                    " JOIN user_business_groups member " +
                    " ON member.group_id = me.group_id " +
                    " WHERE me.user_id = ? " +
                    " AND member.user_id = l.assigned_to " +
                    ") ");
            params.add(userId);
        }

        if (keyword != null && !keyword.isBlank()) {
            conditions.append(
                    " AND (l.full_name LIKE ? " +
                    " OR l.email LIKE ? " +
                    " OR l.phone LIKE ? " +
                    " OR l.company LIKE ?) ");

            String pattern = "%" + keyword.trim() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        if (status != null && !status.isBlank()) {
            conditions.append(" AND l.status = ? ");
            params.add(status.trim());
        }

        if (source != null && !source.isBlank()) {
            conditions.append(" AND l.source = ? ");
            params.add(source.trim());
        }

        if (assignedTo != null) {
            conditions.append(" AND l.assigned_to = ? ");
            params.add(assignedTo);
        }

        if (createdFrom != null) {
            conditions.append(" AND l.created_at >= ? ");
            params.add(java.sql.Timestamp.valueOf(createdFrom.atStartOfDay()));
        }

        if (createdTo != null) {
            conditions.append(" AND l.created_at < ? ");
            params.add(java.sql.Timestamp.valueOf(createdTo.plusDays(1).atStartOfDay()));
        }

        if (classification != null && !classification.isBlank()) {
            String category = classification.trim().toUpperCase(java.util.Locale.ROOT);

            if (!"HOT".equals(category)
                    && !"WARM".equals(category)
                    && !"COLD".equals(category)) {
                throw new IllegalArgumentException("Invalid classification");
            }

            String hot = "(SELECT hot_threshold FROM lead_scoring_config WHERE id = 1)";
            String warm = "(SELECT warm_threshold FROM lead_scoring_config WHERE id = 1)";

            switch (category) {
                case "HOT":
                    conditions.append(" AND l.score >= " + hot + " ");
                    break;
                case "WARM":
                    conditions.append(" AND l.score >= " + warm
                            + " AND l.score < " + hot + " ");
                    break;
                case "COLD":
                    conditions.append(" AND l.score < " + warm + " ");
                    break;
                default:
                    throw new IllegalArgumentException("Invalid classification");
            }
        }

        String orderColumn;
        switch (sortBy == null || sortBy.isBlank() ? "createdAt" : sortBy) {
            case "name": case "fullName": orderColumn = "l.full_name"; break;
            case "status": orderColumn = "l.status"; break;
            case "source": orderColumn = "l.source"; break;
            case "score": orderColumn = "l.score"; break;
            case "createdAt": orderColumn = "l.created_at"; break;
            default: throw new IllegalArgumentException("Invalid sortBy");
        }

        String direction;
        if (sortDirection == null || sortDirection.isBlank()
                || "desc".equalsIgnoreCase(sortDirection)) {
            direction = "DESC";
        } else if ("asc".equalsIgnoreCase(sortDirection)) {
            direction = "ASC";
        } else {
            throw new IllegalArgumentException("Invalid sortDirection");
        }

        String fromWhere = " FROM leads l " + conditions;

        String countSql = "SELECT COUNT(*)" + fromWhere;
        String slaColumns = "";
        try (Connection checkConnection = DBConnection.getConnection()) {
            java.sql.DatabaseMetaData metadata = checkConnection.getMetaData();
            boolean hasDeadline = false;
            boolean hasFirstContact = false;

            try (ResultSet columns = metadata.getColumns(
                    checkConnection.getCatalog(), null, "leads", null)) {
                while (columns.next()) {
                    String column = columns.getString("COLUMN_NAME");
                    if ("sla_deadline".equalsIgnoreCase(column)) {
                        hasDeadline = true;
                    }
                    if ("first_contact_at".equalsIgnoreCase(column)) {
                        hasFirstContact = true;
                    }
                }
            }

            if (hasDeadline && hasFirstContact) {
                slaColumns = ", l.sla_deadline, l.first_contact_at ";
            }
        }

        String dataSql =
                "SELECT l.id, l.full_name, l.email, l.phone, " +
                "l.company, l.interest, l.status, l.source, " +
                "l.source_form, l.assigned_to, l.score, " +
                "l.created_at, l.updated_at " +
                slaColumns +
                fromWhere +
                " ORDER BY " + orderColumn + " " + direction + ", l.id DESC " +
                " LIMIT ? OFFSET ?";

        try (Connection connection = DBConnection.getConnection()) {

            long totalItems;

            try (PreparedStatement countStatement =
                         connection.prepareStatement(countSql)) {

                bind(countStatement, params);

                try (ResultSet rs = countStatement.executeQuery()) {
                    rs.next();
                    totalItems = rs.getLong(1);
                }
            }

            List<LeadItem> items = new ArrayList<>();

            try (PreparedStatement dataStatement =
                         connection.prepareStatement(dataSql)) {

                int index = bind(dataStatement, params);
                dataStatement.setInt(index++, size);
                dataStatement.setLong(
                        index, (long) (page - 1) * size);

                try (ResultSet rs = dataStatement.executeQuery()) {
                    while (rs.next()) {
                        long assignedId = rs.getLong("assigned_to");
                        Long assigned = rs.wasNull() ? null : assignedId;

                        java.sql.Timestamp created =
                                rs.getTimestamp("created_at");
                        java.sql.Timestamp updated =
                                rs.getTimestamp("updated_at");

                        java.math.BigDecimal score =
                                rs.getBigDecimal("score");

                        String slaStatus = null;

                        if (!slaColumns.isEmpty()) {
                            java.sql.Timestamp deadline =
                                    rs.getTimestamp("sla_deadline");
                            java.sql.Timestamp firstContact =
                                    rs.getTimestamp("first_contact_at");

                            if (deadline != null) {
                                long remaining = deadline.getTime()
                                        - System.currentTimeMillis();

                                if (firstContact != null) {
                                    slaStatus = firstContact.after(deadline)
                                            ? "overdue" : "on_time";
                                } else if (remaining < 0) {
                                    slaStatus = "overdue";
                                } else if (remaining <= 3600000) {
                                    slaStatus = "urgent";
                                } else {
                                    slaStatus = "on_time";
                                }
                            }
                        }

                        items.add(new LeadItem(
                                rs.getLong("id"),
                                rs.getString("full_name"),
                                rs.getString("email"),
                                rs.getString("phone"),
                                rs.getString("company"),
                                rs.getString("interest"),
                                rs.getString("status"),
                                rs.getString("source"),
                                rs.getString("source_form"),
                                assigned,
                                score,
                                created == null ? null : created.toLocalDateTime(),
                                updated == null ? null : updated.toLocalDateTime(),
                                slaStatus
                        ));
                    }
                }
            }

            return new SearchPage(items, totalItems, page, size);
        }
    }

    private int bind(
            PreparedStatement statement,
            List<Object> params
    ) throws SQLException {

        int index = 1;

        for (Object param : params) {
            statement.setObject(index++, param);
        }

        return index;
    }

    public record LeadItem(
            long id,
            String fullName,
            String email,
            String phone,
            String company,
            String interest,
            String status,
            String source,
            String sourceForm,
            Long assignedTo,
            java.math.BigDecimal score,
            java.time.LocalDateTime createdAt,
            java.time.LocalDateTime updatedAt,
            String slaStatus
    ) {
        public LeadItem(
                long id,
                String fullName,
                String email,
                String phone,
                String company,
                String interest,
                String status,
                String source,
                String sourceForm,
                Long assignedTo,
                java.math.BigDecimal score,
                java.time.LocalDateTime createdAt,
                java.time.LocalDateTime updatedAt
        ) {
            this(id, fullName, email, phone, company, interest,
                    status, source, sourceForm, assignedTo, score,
                    createdAt, updatedAt, null);
        }
    }

    public record SearchPage(
            List<LeadItem> items,
            long totalItems,
            int page,
            int size
    ) {}

    public List<vn.edu.ictu.qlkh.model.Lead> findAll()
            throws SQLException {

        String sql = "SELECT " + SELECT_COLUMNS +
                " FROM leads l ORDER BY l.id DESC";

        List<vn.edu.ictu.qlkh.model.Lead> leads = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                leads.add(map(rs));
            }
        } catch (SQLException e) {
            if (e.getMessage() != null
                    && e.getMessage().toLowerCase(java.util.Locale.ROOT)
                    .contains("leads' doesn't exist")) {
                return leads;
            }
            throw e;
        }

        return leads;
    }

    public List<vn.edu.ictu.qlkh.model.Lead> findVisible(
            long userId,
            String scope
    ) throws SQLException {

        if ("ALL".equalsIgnoreCase(scope)) {
            return findAll();
        }

        String sql;

        if ("TEAM".equalsIgnoreCase(scope)) {
            sql = "SELECT " + SELECT_COLUMNS + " FROM leads l " +
                    "WHERE l.assigned_to IS NULL " +
                    "OR l.assigned_to = ? " +
                    "OR EXISTS (" +
                    "SELECT 1 FROM user_business_groups me " +
                    "JOIN user_business_groups other " +
                    "ON other.group_id = me.group_id " +
                    "WHERE me.user_id = ? " +
                    "AND other.user_id = l.assigned_to" +
                    ") ORDER BY l.id DESC";
        } else {
            sql = "SELECT " + SELECT_COLUMNS + " FROM leads l " +
                    "WHERE l.assigned_to IS NULL OR l.assigned_to = ? " +
                    "ORDER BY l.id DESC";
        }

        List<vn.edu.ictu.qlkh.model.Lead> leads = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if ("TEAM".equalsIgnoreCase(scope)) {
                ps.setLong(1, userId);
                ps.setLong(2, userId);
            } else {
                ps.setLong(1, userId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    leads.add(map(rs));
                }
            }
        } catch (SQLException e) {
            if (e.getMessage() != null
                    && e.getMessage().toLowerCase(java.util.Locale.ROOT)
                    .contains("leads' doesn't exist")) {
                return leads;
            }
            throw e;
        }

        return leads;
    }

    private vn.edu.ictu.qlkh.model.Lead map(ResultSet rs)
            throws SQLException {

        vn.edu.ictu.qlkh.model.Lead lead =
                new vn.edu.ictu.qlkh.model.Lead();

        lead.setId(rs.getLong("id"));
        lead.setFullName(rs.getString("full_name"));
        lead.setEmail(rs.getString("email"));
        lead.setPhone(rs.getString("phone"));
        lead.setCompany(rs.getString("company"));
        lead.setInterest(rs.getString("interest"));
        lead.setStatus(rs.getString("status"));
        lead.setSource(rs.getString("source"));
        lead.setSourceForm(rs.getString("source_form"));

        long campaignId = rs.getLong("campaign_id");
        if (!rs.wasNull()) {
            lead.setCampaignId(campaignId);
        }

        long assignedTo = rs.getLong("assigned_to");
        if (!rs.wasNull()) {
            lead.setAssignedTo(assignedTo);
        }

        lead.setScore(rs.getBigDecimal("score"));

        long convertedCustId = rs.getLong("converted_customer_id");
        if (!rs.wasNull()) {
            lead.setConvertedCustomerId(convertedCustId);
        }

        long convertedContactId = rs.getLong("converted_contact_id");
        if (!rs.wasNull()) {
            lead.setConvertedContactId(convertedContactId);
        }

        java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            lead.setCreatedAt(createdAt.toLocalDateTime());
        }

        java.sql.Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            lead.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return lead;
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
                    emails.add(
                            email.trim().toLowerCase(java.util.Locale.ROOT));
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
