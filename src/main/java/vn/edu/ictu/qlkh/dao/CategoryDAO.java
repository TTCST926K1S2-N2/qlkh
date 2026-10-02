package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.Category;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    public List<Category> findAll() throws SQLException {

        String sql = """
                SELECT id,
                       category_type,
                       category_code,
                       category_name,
                       description,
                       status
                FROM common_categories
                ORDER BY category_type, category_name
                """;

        List<Category> categories = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                categories.add(mapRow(resultSet));
            }
        }

        return categories;
    }

    public Category findById(long id) throws SQLException {

        String sql = """
                SELECT id,
                       category_type,
                       category_code,
                       category_name,
                       description,
                       status
                FROM common_categories
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
            String categoryType,
            String categoryCode,
            long excludeId) throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM common_categories
                WHERE category_type = ?
                  AND category_code = ?
                  AND id <> ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, categoryType);
            statement.setString(2, categoryCode);
            statement.setLong(3, excludeId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    public boolean insert(Category category) throws SQLException {

        String sql = """
                INSERT INTO common_categories
                    (category_type,
                     category_code,
                     category_name,
                     description,
                     status)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql,
                     java.sql.Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, category.getCategoryType());
            statement.setString(2, category.getCategoryCode());
            statement.setString(3, category.getCategoryName());
            statement.setString(4, category.getDescription());
            statement.setBoolean(5, category.isStatus());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys =
                         statement.getGeneratedKeys()) {

                if (!generatedKeys.next()) {
                    throw new SQLException(
                            "Không lấy được ID danh mục sau khi thêm."
                    );
                }

                category.setId(generatedKeys.getLong(1));
            }

            return true;
        }
    }
    public boolean update(Category category) throws SQLException {

        String sql = """
                UPDATE common_categories
                SET category_type = ?,
                    category_code = ?,
                    category_name = ?,
                    description = ?,
                    status = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, category.getCategoryType());
            statement.setString(2, category.getCategoryCode());
            statement.setString(3, category.getCategoryName());
            statement.setString(4, category.getDescription());
            statement.setBoolean(5, category.isStatus());
            statement.setLong(6, category.getId());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(long id) throws SQLException {

        String sql = """
                DELETE FROM common_categories
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            return statement.executeUpdate() > 0;
        }
    }

    private Category mapRow(ResultSet resultSet) throws SQLException {

        Category category = new Category();

        category.setId(resultSet.getLong("id"));
        category.setCategoryType(
                resultSet.getString("category_type")
        );
        category.setCategoryCode(
                resultSet.getString("category_code")
        );
        category.setCategoryName(
                resultSet.getString("category_name")
        );
        category.setDescription(
                resultSet.getString("description")
        );
        category.setStatus(
                resultSet.getBoolean("status")
        );

        return category;
    }
}