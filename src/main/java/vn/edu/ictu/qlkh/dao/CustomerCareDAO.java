package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.dto.CustomerCareDTO;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerCareDAO {

    private static final long WARNING_DAYS = 30;

    public List<CustomerCareDTO> findCandidates(
            long userId, String scope) throws SQLException {

        if (!"MY".equals(scope)
                && !"TEAM".equals(scope)
                && !"ALL".equals(scope)) {
            return List.of();
        }

        StringBuilder sql = new StringBuilder("""
                SELECT c.id,
                       c.company_name,
                       c.owner_id,
                       c.status,
                       CAST(NULL AS DECIMAL(15,2)) AS contract_value,
                       COALESCE(
                           DATEDIFF(CURRENT_DATE, (
                               SELECT MAX(a.action_time)
                               FROM audit_logs a
                               WHERE a.action_type = 'CUSTOMER_CONTACTED'
                                 AND a.target_object = CONCAT('CUSTOMER:', c.id)
                           )),
                           DATEDIFF(CURRENT_DATE, c.created_at)
                       ) AS days_inactive
                FROM customers c
                """);

        if ("MY".equals(scope)) {
            sql.append(" WHERE c.owner_id = ?");
        } else if ("TEAM".equals(scope)) {
            sql.append("""
                     WHERE EXISTS (
                         SELECT 1
                         FROM user_business_groups me
                         JOIN user_business_groups owner_group
                           ON owner_group.group_id = me.group_id
                         WHERE me.user_id = ?
                           AND owner_group.user_id = c.owner_id
                     )
                    """);
        }

        sql.append("""
                 HAVING days_inactive >= ?
                 ORDER BY days_inactive DESC, c.id DESC
                """);

        List<CustomerCareDTO> result = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int parameterIndex = 1;

            if ("MY".equals(scope) || "TEAM".equals(scope)) {
                ps.setLong(parameterIndex++, userId);
            }

            ps.setLong(parameterIndex, WARNING_DAYS);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long daysInactive = rs.getLong("days_inactive");

                    result.add(new CustomerCareDTO(
                            rs.getLong("id"),
                            rs.getString("company_name"),
                            rs.getLong("owner_id"),
                            rs.getString("status"),
                            rs.getBigDecimal("contract_value"),
                            daysInactive,
                            daysInactive >= WARNING_DAYS
                    ));
                }
            }
        }

        return result;
    }
}
