package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.Product;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<Product> findAll()
            throws SQLException {

        String sql = """
                SELECT id,
                       code,
                       name,
                       product_type,
                       unit,
                       base_price,
                       floor_price,
                       cost_price,
                       status,
                       description
                FROM products
                ORDER BY name, code
                """;

        List<Product> products = new ArrayList<>();

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql);

             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {
                products.add(
                        mapRow(resultSet)
                );
            }
        }

        return products;
    }


    public Product findById(long id)
            throws SQLException {

        String sql = """
                SELECT id,
                       code,
                       name,
                       product_type,
                       unit,
                       base_price,
                       floor_price,
                       cost_price,
                       status,
                       description
                FROM products
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


    public Product findByCode(String code)
            throws SQLException {

        String sql = """
                SELECT id,
                       code,
                       name,
                       product_type,
                       unit,
                       base_price,
                       floor_price,
                       cost_price,
                       status,
                       description
                FROM products
                WHERE UPPER(code) = UPPER(?)
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, code);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }

        return null;
    }


    public long insert(Product product)
            throws SQLException {

        String sql = """
                INSERT INTO products (
                    code,
                    name,
                    product_type,
                    unit,
                    base_price,
                    floor_price,
                    cost_price,
                    status,
                    description
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            setProductParameters(
                    statement,
                    product
            );

            int affectedRows =
                    statement.executeUpdate();

            if (affectedRows != 1) {
                throw new SQLException(
                        "Không thể thêm sản phẩm/dịch vụ."
                );
            }

            try (ResultSet keys =
                         statement.getGeneratedKeys()) {

                if (!keys.next()) {
                    throw new SQLException(
                            "Không lấy được ID sản phẩm/dịch vụ."
                    );
                }

                long id = keys.getLong(1);
                product.setId(id);

                return id;
            }
        }
    }


    public boolean update(Product product)
            throws SQLException {

        String sql = """
                UPDATE products
                SET code = ?,
                    name = ?,
                    product_type = ?,
                    unit = ?,
                    base_price = ?,
                    floor_price = ?,
                    cost_price = ?,
                    status = ?,
                    description = ?
                WHERE id = ?
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            setProductParameters(
                    statement,
                    product
            );

            statement.setLong(
                    10,
                    product.getId()
            );

            return statement.executeUpdate() == 1;
        }
    }


    public boolean delete(long id)
            throws SQLException {

        String sql = """
                DELETE FROM products
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


    public boolean existsInPriceList(long productId)
            throws SQLException {

        String sql = """
                SELECT 1
                FROM price_list_items
                WHERE product_id = ?
                LIMIT 1
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    productId
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                return resultSet.next();
            }
        }
    }


    private void setProductParameters(
            PreparedStatement statement,
            Product product)
            throws SQLException {

        statement.setString(
                1,
                product.getCode()
        );

        statement.setString(
                2,
                product.getName()
        );

        statement.setString(
                3,
                product.getType()
        );

        statement.setString(
                4,
                product.getUnit()
        );

        statement.setBigDecimal(
                5,
                product.getBasePrice()
        );

        statement.setBigDecimal(
                6,
                product.getFloorPrice()
        );

        if (product.getCostPrice() == null) {
            statement.setNull(
                    7,
                    java.sql.Types.DECIMAL
            );
        } else {
            statement.setBigDecimal(
                    7,
                    product.getCostPrice()
            );
        }

        statement.setString(
                8,
                product.getStatus()
        );

        statement.setString(
                9,
                product.getDescription()
        );
    }


    private Product mapRow(ResultSet resultSet)
            throws SQLException {

        Product product = new Product();

        product.setId(
                resultSet.getLong("id")
        );

        product.setCode(
                resultSet.getString("code")
        );

        product.setName(
                resultSet.getString("name")
        );

        product.setType(
                resultSet.getString("product_type")
        );

        product.setUnit(
                resultSet.getString("unit")
        );

        product.setBasePrice(
                resultSet.getBigDecimal("base_price")
        );

        product.setFloorPrice(
                resultSet.getBigDecimal("floor_price")
        );

        product.setCostPrice(
                resultSet.getBigDecimal("cost_price")
        );

        product.setStatus(
                resultSet.getString("status")
        );

        product.setDescription(
                resultSet.getString("description")
        );

        return product;
    }
}