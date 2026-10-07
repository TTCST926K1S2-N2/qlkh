```java
package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.dto.CustomerCareDTO;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerCareDAO {

    public List<CustomerCareDTO> findCandidates(
            long userId,
            String scope) throws SQLException {

        String sql;

        if ("ALL".equals(scope)) {
            sql = "SELECT id, company_name, owner_id, status " +
                  "FROM customers ORDER BY id DESC";

        } else if ("TEAM".equals(scope)) {
            sql = "SELECT c.id, c.company_name, c.owner_id, c.status " +
                  "FROM customers c " +
                  "WHERE EXISTS (" +
                  "SELECT 1 FROM user_business_groups me " +
                  "JOIN user_business_groups owner_group " +
                  "ON owner_group.group_id = me.group_id " +
                  "WHERE me.user_id = ? " +
                  "AND owner_group.user_id = c.owner_id" +
                  ") ORDER BY c.id DESC";

        } else if ("MY".equals(scope)) {
            sql = "SELECT id, company_name, owner_id, status " +
                  "FROM customers " +
                  "WHERE owner_id = ? ORDER BY id DESC";

        } else {
            return List.of();
        }

        List<CustomerCareDTO> result = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if ("MY".equals(scope) || "TEAM".equals(scope)) {
                ps.setLong(1, userId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new CustomerCareDT
```
