package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.Customer;
import vn.edu.ictu.qlkh.model.CustomerDuplicate;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerDuplicateDAO {

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

        if (rs.getTimestamp("created_at") != null) {
            c.setCreatedAt(
                    rs.getTimestamp("created_at").toLocalDateTime()
            );
        }

        if (rs.getTimestamp("updated_at") != null) {
            c.setUpdatedAt(
                    rs.getTimestamp("updated_at").toLocalDateTime()
            );
        }

        return c;
    }

    public List<CustomerDuplicate> findDuplicates(
            long customerId,
            long userId,
            String scope) throws SQLException {

        if (customerId <= 0) {
            throw new IllegalArgumentException(
                    "ID khach hang khong hop le"
            );
        }

        String visibility;

        if ("ALL".equals(scope)) {
            visibility = "";
        } else if ("TEAM".equals(scope)) {
            visibility =
                    " AND EXISTS (" +
                    "SELECT 1 FROM user_business_groups me " +
                    "JOIN user_business_groups owner_group " +
                    "ON owner_group.group_id = me.group_id " +
                    "WHERE me.user_id = ? " +
                    "AND owner_group.user_id = c.owner_id" +
                    ")";
        } else if ("MY".equals(scope)) {
            visibility = " AND c.owner_id = ?";
        } else {
            return List.of();
        }

        String sql =
                "SELECT " + COLUMNS +
                " FROM customers c " +
                "JOIN customers source " +
                "ON source.id = ? " +
                "WHERE c.id <> ? " +
                visibility +
                " AND (" +
                "    (source.tax_code IS NOT NULL " +
                "     AND TRIM(source.tax_code) <> '' " +
                "     AND c.tax_code = source.tax_code)" +
                " OR " +
                "    (source.company_name IS NOT NULL " +
                "     AND TRIM(source.company_name) <> '' " +
                "     AND LOWER(TRIM(c.company_name)) = " +
                "         LOWER(TRIM(source.company_name)))" +
                " OR " +
                "    (source.website IS NOT NULL " +
                "     AND TRIM(source.website) <> '' " +
                "     AND LOWER(TRIM(c.website)) = " +
                "         LOWER(TRIM(source.website)))" +
                ")" +
                " ORDER BY c.id DESC";

        List<CustomerDuplicate> result = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setLong(1, customerId);
            ps.setLong(2, customerId);

            if ("TEAM".equals(scope) ||
                    "MY".equals(scope)) {
                ps.setLong(3, userId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Customer customer = map(rs);

                    List<String> matchedFields =
                            findMatchedFields(
                                    customerId,
                                    customer
                            );

                    result.add(
                            new CustomerDuplicate(
                                    customer,
                                    matchedFields
                            )
                    );
                }
            }
        }

        return result;
    }

    private List<String> findMatchedFields(
            long sourceId,
            Customer target) throws SQLException {

        String sql =
                "SELECT tax_code, company_name, website " +
                "FROM customers WHERE id = ?";

        List<String> fields = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setLong(1, sourceId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return fields;
                }

                String sourceTaxCode =
                        rs.getString("tax_code");

                String sourceCompanyName =
                        rs.getString("company_name");

                String sourceWebsite =
                        rs.getString("website");

                if (sameValue(
                        sourceTaxCode,
                        target.getTaxCode())) {

                    fields.add("taxCode");
                }

                if (sameText(
                        sourceCompanyName,
                        target.getCompanyName())) {

                    fields.add("companyName");
                }

                if (sameText(
                        sourceWebsite,
                        target.getWebsite())) {

                    fields.add("website");
                }
            }
        }

        return fields;
    }

    private boolean sameValue(
            String first,
            String second) {

        if (first == null ||
                first.isBlank() ||
                second == null ||
                second.isBlank()) {
            return false;
        }

        return first.trim().equals(second.trim());
    }

    private boolean sameText(
            String first,
            String second) {

        if (first == null ||
                first.isBlank() ||
                second == null ||
                second.isBlank()) {
            return false;
        }

        return first.trim().equalsIgnoreCase(
                second.trim()
        );
    }
}