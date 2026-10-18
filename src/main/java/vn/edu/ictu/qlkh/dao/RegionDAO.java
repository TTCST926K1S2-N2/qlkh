package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.Region;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RegionDAO {

    public List<Region> findAll()
            throws SQLException {

        String sql = """
                SELECT id, code, name, status
                FROM regions
                ORDER BY name ASC
                """;

        List<Region> result =
                new ArrayList<>();

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql);

             ResultSet rs =
                     statement.executeQuery()) {

            while (rs.next()) {
                result.add(map(rs));
            }
        }

        return result;
    }


    public Region findById(long id)
            throws SQLException {

        String sql = """
                SELECT id, code, name, status
                FROM regions
                WHERE id = ?
                LIMIT 1
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs =
                         statement.executeQuery()) {

                return rs.next()
                        ? map(rs)
                        : null;
            }
        }
    }


    public boolean existsById(long id)
            throws SQLException {

        return findById(id) != null;
    }


    private Region map(ResultSet rs)
            throws SQLException {

        return new Region(
                rs.getLong("id"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("status")
        );
    }
}