package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.WinLossReason;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

public class WinLossReasonDAO {

    public List<WinLossReason> findAll()
            throws SQLException {

        String sql = """
                SELECT
                    id,
                    name,
                    code,
                    type,
                    status,
                    description
                FROM win_loss_reasons
                ORDER BY type, name, id
                """;

        List<WinLossReason> reasons =
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

                reasons.add(
                        mapRow(resultSet)
                );
            }
        }

        return reasons;
    }

    public List<WinLossReason> findByType(
            String type
    ) throws SQLException {

        String sql = """
                SELECT
                    id,
                    name,
                    code,
                    type,
                    status,
                    description
                FROM win_loss_reasons
                WHERE type = ?
                ORDER BY name, id
                """;

        List<WinLossReason> reasons =
                new ArrayList<>();

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    type
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                while (resultSet.next()) {

                    reasons.add(
                            mapRow(resultSet)
                    );
                }
            }
        }

        return reasons;
    }

    public WinLossReason findById(
            long id
    ) throws SQLException {

        String sql = """
                SELECT
                    id,
                    name,
                    code,
                    type,
                    status,
                    description
                FROM win_loss_reasons
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
                FROM win_loss_reasons
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
            WinLossReason reason
    ) throws SQLException {

        String sql = """
                INSERT INTO win_loss_reasons (
                    name,
                    code,
                    type,
                    status,
                    description
                )
                VALUES (?, ?, ?, ?, ?)
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
                    reason.getName()
            );

            statement.setString(
                    2,
                    reason.getCode()
            );

            statement.setString(
                    3,
                    reason.getType()
            );

            statement.setString(
                    4,
                    reason.getStatus()
            );

            statement.setString(
                    5,
                    reason.getDescription()
            );

            statement.executeUpdate();

            try (
                    ResultSet keys =
                            statement.getGeneratedKeys()
            ) {

                if (keys.next()) {
                    reason.setId(
                            keys.getLong(1)
                    );
                }
            }
        }
    }

    public boolean update(
            WinLossReason reason
    ) throws SQLException {

        String sql = """
                UPDATE win_loss_reasons
                SET
                    name = ?,
                    code = ?,
                    type = ?,
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
                    reason.getName()
            );

            statement.setString(
                    2,
                    reason.getCode()
            );

            statement.setString(
                    3,
                    reason.getType()
            );

            statement.setString(
                    4,
                    reason.getStatus()
            );

            statement.setString(
                    5,
                    reason.getDescription()
            );

            statement.setLong(
                    6,
                    reason.getId()
            );

            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(
            long id
    ) throws SQLException {

        String sql = """
                DELETE FROM win_loss_reasons
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

    private WinLossReason mapRow(
            ResultSet resultSet
    ) throws SQLException {

        WinLossReason reason =
                new WinLossReason();

        reason.setId(
                resultSet.getLong("id")
        );

        reason.setName(
                resultSet.getString("name")
        );

        reason.setCode(
                resultSet.getString("code")
        );

        reason.setType(
                resultSet.getString("type")
        );

        reason.setStatus(
                resultSet.getString("status")
        );

        reason.setDescription(
                resultSet.getString("description")
        );

        return reason;
    }
}