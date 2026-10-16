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

        String fromWhere = " FROM leads l " + conditions;

        String countSql = "SELECT COUNT(*)" + fromWhere;
        String dataSql =
                "SELECT l.id, l.full_name, l.email, l.phone, " +
                "l.company, l.interest, l.status, l.source, " +
                "l.source_form, l.assigned_to, l.score, " +
                "l.created_at, l.updated_at " +
                fromWhere +
                " ORDER BY l.created_at DESC, l.id DESC " +
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
                                updated == null ? null : updated.toLocalDateTime()
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
            java.time.LocalDateTime updatedAt
    ) {}

    public record SearchPage(
            List<LeadItem> items,
            long totalItems,
            int page,
            int size
    ) {}
}
