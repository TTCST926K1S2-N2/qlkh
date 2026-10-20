package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.CustomField;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomFieldDAO {

    public List<CustomField> findAll() throws SQLException {
        String sql = """
                SELECT id,
                       entity_type,
                       field_key,
                       field_name,
                       field_type,
                       required,
                       status,
                       display_order,
                       config
                FROM custom_fields
                ORDER BY entity_type, display_order, field_name
                """;

        List<CustomField> fields = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                fields.add(mapRow(resultSet));
            }
        }

        return fields;
    }

    public List<CustomField> findByEntityType(String entityType)
            throws SQLException {

        String sql = """
                SELECT id,
                       entity_type,
                       field_key,
                       field_name,
                       field_type,
                       required,
                       status,
                       display_order,
                       config
                FROM custom_fields
                WHERE entity_type = ?
                ORDER BY display_order, field_name
                """;

        List<CustomField> fields = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, entityType);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    fields.add(mapRow(resultSet));
                }
            }
        }

        return fields;
    }

    public CustomField findById(long id) throws SQLException {
        String sql = """
                SELECT id,
                       entity_type,
                       field_key,
                       field_name,
                       field_type,
                       required,
                       status,
                       display_order,
                       config
                FROM custom_fields
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

    public boolean exists(
            String entityType,
            String fieldKey,
            long excludeId) throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM custom_fields
                WHERE entity_type = ?
                  AND field_key = ?
                  AND id <> ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, entityType);
            statement.setString(2, fieldKey);
            statement.setLong(3, excludeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    public boolean insert(CustomField field) throws SQLException {
        String sql = """
                INSERT INTO custom_fields
                    (entity_type,
                     field_key,
                     field_name,
                     field_type,
                     required,
                     status,
                     display_order,
                     config)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, field.getEntityType());
            statement.setString(2, field.getFieldKey());
            statement.setString(3, field.getFieldName());
            statement.setString(4, field.getFieldType());
            statement.setBoolean(5, field.isRequired());
            statement.setBoolean(6, field.isStatus());
            statement.setInt(7, field.getDisplayOrder());
            statement.setString(8, field.getConfig());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(CustomField field) throws SQLException {
        String sql = """
                UPDATE custom_fields
                SET entity_type = ?,
                    field_key = ?,
                    field_name = ?,
                    field_type = ?,
                    required = ?,
                    status = ?,
                    display_order = ?,
                    config = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, field.getEntityType());
            statement.setString(2, field.getFieldKey());
            statement.setString(3, field.getFieldName());
            statement.setString(4, field.getFieldType());
            statement.setBoolean(5, field.isRequired());
            statement.setBoolean(6, field.isStatus());
            statement.setInt(7, field.getDisplayOrder());
            statement.setString(8, field.getConfig());
            statement.setLong(9, field.getId());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = """
                DELETE FROM custom_fields
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            return statement.executeUpdate() > 0;
        }
    }

    private CustomField mapRow(ResultSet resultSet)
            throws SQLException {

        CustomField field = new CustomField();

        field.setId(resultSet.getLong("id"));
        field.setEntityType(resultSet.getString("entity_type"));
        field.setFieldKey(resultSet.getString("field_key"));
        field.setFieldName(resultSet.getString("field_name"));
        field.setFieldType(resultSet.getString("field_type"));
        field.setRequired(resultSet.getBoolean("required"));
        field.setStatus(resultSet.getBoolean("status"));
        field.setDisplayOrder(resultSet.getInt("display_order"));
        field.setConfig(resultSet.getString("config"));

        return field;
    }
}
