package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.Competitor;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

public class CompetitorDAO {

    public List<Competitor> findAll()
            throws SQLException {

        String sql = """
                SELECT
                    id,
                    name,
                    code,
                    status,
                    description
                FROM competitors
                ORDER BY name, id
                """;

        List<Competitor> competitors =
                new ArrayList<>();

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            while (resultSet.next()) {

                competitors.add(
                        mapRow(resultSet)
                );
            }
        }

        return competitors;
    }

    public Competitor findById(
            long id
    ) throws SQLException {

        String sql = """
                SELECT
                    id,
                    name,
                    code,
                    status,
                    description
                FROM competitors
                WHERE id = ?
                LIMIT 1
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    id
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (!resultSet.next()) {
                    return null;
                }

                return mapRow(resultSet);
            }
        }
    }

    public boolean exists(
            String code,
            long excludeId
    ) throws SQLException {

        String sql = """
                SELECT 1
                FROM competitors
                WHERE code = ?
                  AND id <> ?
                LIMIT 1
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    code
            );

            statement.setLong(
                    2,
                    excludeId
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                return resultSet.next();
            }
        }
    }

    public void insert(
            Competitor competitor
    ) throws SQLException {

        String sql = """
                INSERT INTO competitors (
                    name,
                    code,
                    status,
                    description
                )
                VALUES (?, ?, ?, ?)
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {

            statement.setString(
                    1,
                    competitor.getName()
            );

            statement.setString(
                    2,
                    competitor.getCode()
            );

            statement.setString(
                    3,
                    competitor.getStatus()
            );

            statement.setString(
                    4,
                    competitor.getDescription()
            );

            statement.executeUpdate();

            try (
                    ResultSet keys =
                            statement.getGeneratedKeys()
            ) {

                if (keys.next()) {
                    competitor.setId(
                            keys.getLong(1)
                    );
                }
            }
        }
    }

    public boolean update(
            Competitor competitor
    ) throws SQLException {

        String sql = """
                UPDATE competitors
                SET
                    name = ?,
                    code = ?,
                    status = ?,
                    description = ?
                WHERE id = ?
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    competitor.getName()
            );

            statement.setString(
                    2,
                    competitor.getCode()
            );

            statement.setString(
                    3,
                    competitor.getStatus()
            );

            statement.setString(
                    4,
                    competitor.getDescription()
            );

            statement.setLong(
                    5,
                    competitor.getId()
            );

            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(
            long id
    ) throws SQLException {

        String sql = """
                DELETE FROM competitors
                WHERE id = ?
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    id
            );

            return statement.executeUpdate() > 0;
        }
    }

    private Competitor mapRow(
            ResultSet resultSet
    ) throws SQLException {

        Competitor competitor =
                new Competitor();

        competitor.setId(
                resultSet.getLong("id")
        );

        competitor.setName(
                resultSet.getString("name")
        );

        competitor.setCode(
                resultSet.getString("code")
        );

        competitor.setStatus(
                resultSet.getString("status")
        );

        competitor.setDescription(
                resultSet.getString("description")
        );

        return competitor;
    }
}