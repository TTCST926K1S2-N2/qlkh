package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.Stage;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

public class StageDAO {

    public List<Stage> findAll()
            throws SQLException {

        String sql = """
                SELECT id,
                       name,
                       code,
                       stage_order,
                       win_probability,
                       exit_condition,
                       status,
                       description
                FROM stages
                ORDER BY stage_order, id
                """;

        List<Stage> stages =
                new ArrayList<>();

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql);

             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {
                stages.add(mapRow(resultSet));
            }
        }

        return stages;
    }


    public Stage findById(long id)
            throws SQLException {

        String sql = """
                SELECT id,
                       name,
                       code,
                       stage_order,
                       win_probability,
                       exit_condition,
                       status,
                       description
                FROM stages
                WHERE id = ?
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }

        return null;
    }


    public boolean existsCode(
            String code,
            long excludeId)
            throws SQLException {

        String sql = """
                SELECT 1
                FROM stages
                WHERE UPPER(code) = UPPER(?)
                  AND id <> ?
                LIMIT 1
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, code);
            statement.setLong(2, excludeId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                return resultSet.next();
            }
        }
    }


    public boolean existsOrder(
            int stageOrder,
            long excludeId)
            throws SQLException {

        String sql = """
                SELECT 1
                FROM stages
                WHERE stage_order = ?
                  AND id <> ?
                LIMIT 1
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, stageOrder);
            statement.setLong(2, excludeId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                return resultSet.next();
            }
        }
    }


    public boolean insert(Stage stage)
            throws SQLException {

        String sql = """
                INSERT INTO stages (
                    name,
                    code,
                    stage_order,
                    win_probability,
                    exit_condition,
                    status,
                    description
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            setParameters(statement, stage);

            if (statement.executeUpdate() != 1) {
                return false;
            }

            try (ResultSet keys =
                         statement.getGeneratedKeys()) {

                if (!keys.next()) {
                    throw new SQLException(
                            "Không lấy được ID giai đoạn mới."
                    );
                }

                stage.setId(
                        keys.getLong(1)
                );
            }

            return true;
        }
    }


    public boolean update(Stage stage)
            throws SQLException {

        String sql = """
                UPDATE stages
                SET name = ?,
                    code = ?,
                    stage_order = ?,
                    win_probability = ?,
                    exit_condition = ?,
                    status = ?,
                    description = ?
                WHERE id = ?
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            setParameters(statement, stage);

            statement.setLong(
                    8,
                    stage.getId()
            );

            return statement.executeUpdate() == 1;
        }
    }


    public boolean deactivate(long id)
            throws SQLException {

        String sql = """
                UPDATE stages
                SET status = 'INACTIVE'
                WHERE id = ?
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            return statement.executeUpdate() == 1;
        }
    }


    private void setParameters(
            PreparedStatement statement,
            Stage stage)
            throws SQLException {

        statement.setString(
                1,
                stage.getName()
        );

        statement.setString(
                2,
                stage.getCode()
        );

        statement.setInt(
                3,
                stage.getStageOrder()
        );

        statement.setBigDecimal(
                4,
                stage.getWinProbability()
        );

        statement.setString(
                5,
                stage.getExitCondition()
        );

        statement.setString(
                6,
                stage.getStatus()
        );

        statement.setString(
                7,
                stage.getDescription()
        );
    }


    private Stage mapRow(
            ResultSet resultSet)
            throws SQLException {

        Stage stage =
                new Stage();

        stage.setId(
                resultSet.getLong("id")
        );

        stage.setName(
                resultSet.getString("name")
        );

        stage.setCode(
                resultSet.getString("code")
        );

        stage.setStageOrder(
                resultSet.getInt("stage_order")
        );

        stage.setWinProbability(
                resultSet.getBigDecimal(
                        "win_probability"
                )
        );

        stage.setExitCondition(
                resultSet.getString(
                        "exit_condition"
                )
        );

        stage.setStatus(
                resultSet.getString("status")
        );

        stage.setDescription(
                resultSet.getString(
                        "description"
                )
        );

        return stage;
    }
}
