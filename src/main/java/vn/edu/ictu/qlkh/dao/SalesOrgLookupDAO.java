package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.User;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SalesOrgLookupDAO {

    public List<User> findActiveManagers()
            throws SQLException {

        String sql = """
                SELECT
                    id,
                    email,
                    full_name,
                    role,
                    status
                FROM users
                WHERE UPPER(role) = 'MANAGER'
                  AND UPPER(status) = 'ACTIVE'
                ORDER BY full_name ASC
                """;

        List<User> result =
                new ArrayList<>();

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql);

             ResultSet rs =
                     statement.executeQuery()) {

            while (rs.next()) {

                User user =
                        new User();

                user.setId(
                        rs.getLong("id")
                );

                user.setEmail(
                        rs.getString("email")
                );

                user.setFullName(
                        rs.getString("full_name")
                );

                user.setRole(
                        rs.getString("role")
                );

                user.setStatus(
                        rs.getString("status")
                );

                result.add(user);
            }
        }

        return result;
    }
}