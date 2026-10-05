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


    public boolean updateWithOrderSwap(Stage stage)
            throws SQLException {

        try (Connection connection =
                     DBConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                // Lock the table against concurrent stage-order changes.
                try (PreparedStatement lock =
                             connection.prepareStatement(
                                 "SELECT id FROM stages ORDER BY id FOR UPDATE");
                     ResultSet ignored = lock.executeQuery()) {
                    while (ignored.next()) {
                        // Consume all locked rows.
                    }
                }

                int oldOrder;
                try (PreparedStatement query =
                             connection.prepareStatement(
                                 "SELECT stage_order FROM stages WHERE id = ?")) {
                    query.setLong(1, stage.getId());
                    try (ResultSet rs = query.executeQuery()) {
                        if (!rs.next()) {
                            connection.rollback();
                            return false;
                        }
                        oldOrder = rs.getInt(1);
                    }
                }

                int newOrder = stage.getStageOrder();

                if (oldOrder != newOrder) {
                    long otherId = 0;

                    try (PreparedStatement query =
                                 connection.prepareStatement(
                                     "SELECT id FROM stages WHERE stage_order = ?")) {
                        query.setInt(1, newOrder);

                        try (ResultSet rs = query.executeQuery()) {
                            if (rs.next()) {
                                otherId = rs.getLong(1);
                            }
                        }
                    }

                    if (otherId != 0) {
                        int temporaryOrder;

                        try (PreparedStatement query =
                                     connection.prepareStatement(
                                         "SELECT COALESCE(MAX(stage_order),0) FROM stages");
                             ResultSet rs = query.executeQuery()) {
                            rs.next();
                            long candidate = Math.max(
                                rs.getLong(1), (long) newOrder) + 1L;
                            if (candidate > Integer.MAX_VALUE) {
                                throw new SQLException("No free temporary stage order.");
                            }
                            temporaryOrder = (int) candidate;
                        }

                        setOrder(connection, stage.getId(), temporaryOrder);
                        setOrder(connection, otherId, oldOrder);
                    }
                }

                String sql = """
                    UPDATE stages
                    SET name = ?, code = ?, stage_order = ?,
                        win_probability = ?, exit_condition = ?,
                        status = ?, description = ?
                    WHERE id = ?
                    """;

                try (PreparedStatement statement =
                             connection.prepareStatement(sql)) {
                    setParameters(statement, stage);
                    statement.setLong(8, stage.getId());

                    if (statement.executeUpdate() != 1) {
                        connection.rollback();
                        return false;
                    }
                }

                connection.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private void setOrder(
            Connection connection, long id, int order)
            throws SQLException {

        try (PreparedStatement statement =
                     connection.prepareStatement(
                         "UPDATE stages SET stage_order = ? WHERE id = ?")) {
            statement.setInt(1, order);
            statement.setLong(2, id);

            if (statement.executeUpdate() != 1) {
                throw new SQLException("Cannot update stage order.");
            }
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
