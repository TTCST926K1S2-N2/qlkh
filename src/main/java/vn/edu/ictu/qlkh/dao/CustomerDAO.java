package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.Customer;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    private static final String COLUMNS =
        "c.id, c.company_name, c.tax_code, c.industry, " +
        "c.company_size, c.website, c.address, c.owner_id, " +
        "c.status, c.created_at, c.updated_at";

    private Customer map(ResultSet rs) throws SQLException {
        Customer c = new Customer();

        c.setId(rs.getLong("id"));
        c.setCompanyName(rs.getString("company_name"));
        c.setTaxCode(rs.getString("tax_code"));
        c.setIndustry(rs.getString("industry"));
        c.setCompanySize(rs.getString("company_size"));
        c.setWebsite(rs.getString("website"));
        c.setAddress(rs.getString("address"));
        c.setOwnerId(rs.getLong("owner_id"));
        c.setStatus(rs.getString("status"));

        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");

        if (created != null) {
            c.setCreatedAt(created.toLocalDateTime());
        }

        if (updated != null) {
            c.setUpdatedAt(updated.toLocalDateTime());
        }

        return c;
    }

    public List<Customer> findVisible(
            long userId,
            String scope) throws SQLException {

        String sql;

        if ("ALL".equals(scope)) {
            sql = "SELECT " + COLUMNS +
                  " FROM customers c ORDER BY c.id DESC";

        } else if ("TEAM".equals(scope)) {
            sql = "SELECT " + COLUMNS +
                  " FROM customers c " +
                  "WHERE EXISTS (" +
                  "SELECT 1 FROM user_business_groups me " +
                  "JOIN user_business_groups owner_group " +
                  "ON owner_group.group_id = me.group_id " +
                  "WHERE me.user_id = ? " +
                  "AND owner_group.user_id = c.owner_id" +
                  ") ORDER BY c.id DESC";

        } else if ("MY".equals(scope)) {
            sql = "SELECT " + COLUMNS +
                  " FROM customers c " +
                  "WHERE c.owner_id = ? ORDER BY c.id DESC";

        } else {
            return List.of();
        }

        List<Customer> result = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if ("TEAM".equals(scope)) {
                ps.setLong(1, userId);
            } else if ("MY".equals(scope)) {
                ps.setLong(1, userId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        }

        return result;
    }

    public Customer findVisibleById(
            long id,
            long userId,
            String scope) throws SQLException {

        String sql;

        if ("ALL".equals(scope)) {
            sql = "SELECT " + COLUMNS +
                  " FROM customers c WHERE c.id = ?";

        } else if ("TEAM".equals(scope)) {
            sql = "SELECT " + COLUMNS +
                  " FROM customers c " +
                  "WHERE c.id = ? AND EXISTS (" +
                  "SELECT 1 FROM user_business_groups me " +
                  "JOIN user_business_groups owner_group " +
                  "ON owner_group.group_id = me.group_id " +
                  "WHERE me.user_id = ? " +
                  "AND owner_group.user_id = c.owner_id)";

        } else if ("MY".equals(scope)) {
            sql = "SELECT " + COLUMNS +
                  " FROM customers c " +
                  "WHERE c.id = ? AND c.owner_id = ?";

        } else {
            return null;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            if ("TEAM".equals(scope)) {
                ps.setLong(2, userId);
            } else if ("MY".equals(scope)) {
                ps.setLong(2, userId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean existsTaxCode(
            String taxCode,
            Long excludeId) throws SQLException {

        if (taxCode == null || taxCode.isBlank()) {
            return false;
        }

        String sql =
            "SELECT 1 FROM customers " +
            "WHERE tax_code = ? " +
            "AND (? IS NULL OR id <> ?) LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, taxCode);

            if (excludeId == null) {
                ps.setNull(2, Types.BIGINT);
                ps.setNull(3, Types.BIGINT);
            } else {
                ps.setLong(2, excludeId);
                ps.setLong(3, excludeId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean ownerExists(long ownerId)
            throws SQLException {

        String sql = "SELECT 1 FROM users WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, ownerId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public long insert(Customer c) throws SQLException {

        String sql =
            "INSERT INTO customers (" +
            "company_name, tax_code, industry, company_size, " +
            "website, address, owner_id, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getCompanyName());
            ps.setString(2, c.getTaxCode());
            ps.setString(3, c.getIndustry());
            ps.setString(4, c.getCompanySize());
            ps.setString(5, c.getWebsite());
            ps.setString(6, c.getAddress());
            ps.setLong(7, c.getOwnerId());
            ps.setString(8, c.getStatus());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }

            throw new SQLException("Cannot obtain customer ID");
        }
    }

    public boolean updateVisible(
            Customer c,
            long userId,
            String scope) throws SQLException {

        String accessCondition;

        if ("ALL".equals(scope)) {
            accessCondition = "";

        } else if ("TEAM".equals(scope)) {
            accessCondition =
                " AND EXISTS (" +
                "SELECT 1 FROM user_business_groups me " +
                "JOIN user_business_groups og " +
                "ON og.group_id = me.group_id " +
                "WHERE me.user_id = ? " +
                "AND og.user_id = customers.owner_id)";

        } else if ("MY".equals(scope)) {
            accessCondition = " AND owner_id = ?";

        } else {
            return false;
        }

        String sql =
            "UPDATE customers SET " +
            "company_name = ?, tax_code = ?, industry = ?, " +
            "company_size = ?, website = ?, address = ?, " +
            "owner_id = ?, status = ? " +
            "WHERE id = ?" + accessCondition;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getCompanyName());
            ps.setString(2, c.getTaxCode());
            ps.setString(3, c.getIndustry());
            ps.setString(4, c.getCompanySize());
            ps.setString(5, c.getWebsite());
            ps.setString(6, c.getAddress());
            ps.setLong(7, c.getOwnerId());
            ps.setString(8, c.getStatus());
            ps.setLong(9, c.getId());

            if ("TEAM".equals(scope)) {
                ps.setLong(10, userId);
            } else if ("MY".equals(scope)) {
                ps.setLong(10, userId);
            }

            return ps.executeUpdate() > 0;
        }
    }

    public boolean isOwnerInTeam(
            long ownerId,
            long managerId) throws SQLException {

        String sql =
            "SELECT 1 FROM user_business_groups me " +
            "JOIN user_business_groups member " +
            "ON member.group_id = me.group_id " +
            "WHERE me.user_id = ? " +
            "AND member.user_id = ? LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, managerId);
            ps.setLong(2, ownerId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}