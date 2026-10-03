package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.Stage;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StageDAO {

    public List<Stage> findAll() throws SQLException {
        String sql = """
                SELECT id, name, code, status, description
                FROM stages
                ORDER BY id
                """;

        List<Stage> stages = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                stages.add(mapRow(resultSet));
            }
        }

        return stages;
    }

    public Stage findById(long id) throws SQLException {
        String sql = """
                SELECT id, name, code, status, description
                FROM stages
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }

        return null;
    }

    public boolean exists(String code, long excludeId) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM stages
                WHERE UPPER(code) = UPPER(?)
                  AND id <> ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, code);
            statement.setLong(2, excludeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    public boolean insert(Stage stage) throws SQLException {
        String sql = """
                INSERT INTO stages
                    (name, code, status, description)
                VALUES
                    (?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, stage.getName());
            statement.setString(2, stage.getCode());
            statement.setString(3, stage.getStatus());
            statement.setString(4, stage.getDescription());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(Stage stage) throws SQLException {
        String sql = """
                UPDATE stages
                SET name = ?,
                    code = ?,
                    status = ?,
                    description = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, stage.getName());
            statement.setString(2, stage.getCode());
            statement.setString(3, stage.getStatus());
            statement.setString(4, stage.getDescription());
            statement.setLong(5, stage.getId());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM stages WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            return statement.executeUpdate() > 0;
        }
    }

    private Stage mapRow(ResultSet resultSet) throws SQLException {
        Stage stage = new Stage();

        stage.setId(resultSet.getLong("id"));
        stage.setName(resultSet.getString("name"));
        stage.setCode(resultSet.getString("code"));
        stage.setStatus(resultSet.getString("status"));
        stage.setDescription(resultSet.getString("description"));

        return stage;
    }
}
