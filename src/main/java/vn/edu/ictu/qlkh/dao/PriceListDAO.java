package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.PriceList;
import vn.edu.ictu.qlkh.model.PriceListItem;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

public class PriceListDAO {

    public List<PriceList> findAll()
            throws SQLException {

        String sql = """
                SELECT id,
                       code,
                       name,
                       start_date,
                       end_date,
                       status,
                       description
                FROM price_lists
                ORDER BY start_date DESC, id DESC
                """;

        List<PriceList> result =
                new ArrayList<>();

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql);

             ResultSet rs =
                     statement.executeQuery()) {

            while (rs.next()) {
                result.add(mapPriceList(rs));
            }
        }

        return result;
    }


    public PriceList findById(long id)
            throws SQLException {

        String sql = """
                SELECT id,
                       code,
                       name,
                       start_date,
                       end_date,
                       status,
                       description
                FROM price_lists
                WHERE id = ?
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs =
                         statement.executeQuery()) {

                if (rs.next()) {
                    return mapPriceList(rs);
                }
            }
        }

        return null;
    }


    public PriceList findByCode(String code)
            throws SQLException {

        String sql = """
                SELECT id,
                       code,
                       name,
                       start_date,
                       end_date,
                       status,
                       description
                FROM price_lists
                WHERE UPPER(code) = UPPER(?)
                LIMIT 1
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, code);

            try (ResultSet rs =
                         statement.executeQuery()) {

                if (rs.next()) {
                    return mapPriceList(rs);
                }
            }
        }

        return null;
    }


    public long insert(PriceList priceList)
            throws SQLException {

        String sql = """
                INSERT INTO price_lists (
                    code,
                    name,
                    start_date,
                    end_date,
                    status,
                    description
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            setPriceListParameters(
                    statement,
                    priceList
            );

            int affected =
                    statement.executeUpdate();

            if (affected != 1) {
                throw new SQLException(
                        "Không thể tạo bảng giá."
                );
            }

            try (ResultSet keys =
                         statement.getGeneratedKeys()) {

                if (!keys.next()) {
                    throw new SQLException(
                            "Không lấy được ID bảng giá."
                    );
                }

                long id =
                        keys.getLong(1);

                priceList.setId(id);

                return id;
            }
        }
    }


    public boolean update(PriceList priceList)
            throws SQLException {

        String sql = """
                UPDATE price_lists
                SET code = ?,
                    name = ?,
                    start_date = ?,
                    end_date = ?,
                    status = ?,
                    description = ?
                WHERE id = ?
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            setPriceListParameters(
                    statement,
                    priceList
            );

            statement.setLong(
                    7,
                    priceList.getId()
            );

            return statement.executeUpdate() == 1;
        }
    }


    public boolean delete(long id)
            throws SQLException {

        String sql = """
                DELETE FROM price_lists
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


    public boolean hasItems(long priceListId)
            throws SQLException {

        String sql = """
                SELECT 1
                FROM price_list_items
                WHERE price_list_id = ?
                LIMIT 1
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    priceListId
            );

            try (ResultSet rs =
                         statement.executeQuery()) {

                return rs.next();
            }
        }
    }


    public boolean deactivate(long id)
            throws SQLException {

        String sql = """
                UPDATE price_lists
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


    public List<PriceListItem> findItems(
            long priceListId)
            throws SQLException {

        String sql = """
                SELECT pli.id,
                       pli.price_list_id,
                       pli.product_id,
                       p.code AS product_code,
                       p.name AS product_name,
                       p.product_type,
                       p.unit,
                       pli.list_price,
                       pli.floor_price
                FROM price_list_items pli
                JOIN products p
                  ON p.id = pli.product_id
                WHERE pli.price_list_id = ?
                ORDER BY p.name, p.code
                """;

        List<PriceListItem> result =
                new ArrayList<>();

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    priceListId
            );

            try (ResultSet rs =
                         statement.executeQuery()) {

                while (rs.next()) {
                    result.add(
                            mapPriceListItem(rs)
                    );
                }
            }
        }

        return result;
    }


    public PriceListItem findItemById(
            long priceListId,
            long itemId)
            throws SQLException {

        String sql = """
                SELECT pli.id,
                       pli.price_list_id,
                       pli.product_id,
                       p.code AS product_code,
                       p.name AS product_name,
                       p.product_type,
                       p.unit,
                       pli.list_price,
                       pli.floor_price
                FROM price_list_items pli
                JOIN products p
                  ON p.id = pli.product_id
                WHERE pli.price_list_id = ?
                  AND pli.id = ?
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    priceListId
            );

            statement.setLong(
                    2,
                    itemId
            );

            try (ResultSet rs =
                         statement.executeQuery()) {

                if (rs.next()) {
                    return mapPriceListItem(rs);
                }
            }
        }

        return null;
    }


    public PriceListItem findItemByProduct(
            long priceListId,
            long productId)
            throws SQLException {

        String sql = """
                SELECT pli.id,
                       pli.price_list_id,
                       pli.product_id,
                       p.code AS product_code,
                       p.name AS product_name,
                       p.product_type,
                       p.unit,
                       pli.list_price,
                       pli.floor_price
                FROM price_list_items pli
                JOIN products p
                  ON p.id = pli.product_id
                WHERE pli.price_list_id = ?
                  AND pli.product_id = ?
                LIMIT 1
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    priceListId
            );

            statement.setLong(
                    2,
                    productId
            );

            try (ResultSet rs =
                         statement.executeQuery()) {

                if (rs.next()) {
                    return mapPriceListItem(rs);
                }
            }
        }

        return null;
    }


    public long insertItem(
            PriceListItem item)
            throws SQLException {

        String sql = """
                INSERT INTO price_list_items (
                    price_list_id,
                    product_id,
                    list_price,
                    floor_price
                )
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setLong(
                    1,
                    item.getPriceListId()
            );

            statement.setLong(
                    2,
                    item.getProductId()
            );

            statement.setBigDecimal(
                    3,
                    item.getListPrice()
            );

            statement.setBigDecimal(
                    4,
                    item.getFloorPrice()
            );

            int affected =
                    statement.executeUpdate();

            if (affected != 1) {
                throw new SQLException(
                        "Không thể thêm sản phẩm vào bảng giá."
                );
            }

            try (ResultSet keys =
                         statement.getGeneratedKeys()) {

                if (!keys.next()) {
                    throw new SQLException(
                            "Không lấy được ID chi tiết bảng giá."
                    );
                }

                long id =
                        keys.getLong(1);

                item.setId(id);

                return id;
            }
        }
    }


    public boolean updateItem(
            PriceListItem item)
            throws SQLException {

        String sql = """
                UPDATE price_list_items
                SET product_id = ?,
                    list_price = ?,
                    floor_price = ?
                WHERE id = ?
                  AND price_list_id = ?
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    item.getProductId()
            );

            statement.setBigDecimal(
                    2,
                    item.getListPrice()
            );

            statement.setBigDecimal(
                    3,
                    item.getFloorPrice()
            );

            statement.setLong(
                    4,
                    item.getId()
            );

            statement.setLong(
                    5,
                    item.getPriceListId()
            );

            return statement.executeUpdate() == 1;
        }
    }


    public boolean deleteItem(
            long priceListId,
            long itemId)
            throws SQLException {

        String sql = """
                DELETE FROM price_list_items
                WHERE id = ?
                  AND price_list_id = ?
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    itemId
            );

            statement.setLong(
                    2,
                    priceListId
            );

            return statement.executeUpdate() == 1;
        }
    }


    private void setPriceListParameters(
            PreparedStatement statement,
            PriceList priceList)
            throws SQLException {

        statement.setString(
                1,
                priceList.getCode()
        );

        statement.setString(
                2,
                priceList.getName()
        );

        statement.setDate(
                3,
                Date.valueOf(
                        priceList.getStartDate()
                )
        );

        if (priceList.getEndDate() == null) {

            statement.setNull(
                    4,
                    java.sql.Types.DATE
            );

        } else {

            statement.setDate(
                    4,
                    Date.valueOf(
                            priceList.getEndDate()
                    )
            );
        }

        statement.setString(
                5,
                priceList.getStatus()
        );

        statement.setString(
                6,
                priceList.getDescription()
        );
    }


    private PriceList mapPriceList(
            ResultSet rs)
            throws SQLException {

        PriceList priceList =
                new PriceList();

        priceList.setId(
                rs.getLong("id")
        );

        priceList.setCode(
                rs.getString("code")
        );

        priceList.setName(
                rs.getString("name")
        );

        Date startDate =
                rs.getDate("start_date");

        if (startDate != null) {
            priceList.setStartDate(
                    startDate.toLocalDate()
            );
        }

        Date endDate =
                rs.getDate("end_date");

        if (endDate != null) {
            priceList.setEndDate(
                    endDate.toLocalDate()
            );
        }

        priceList.setStatus(
                rs.getString("status")
        );

        priceList.setDescription(
                rs.getString("description")
        );

        return priceList;
    }


    private PriceListItem mapPriceListItem(
            ResultSet rs)
            throws SQLException {

        PriceListItem item =
                new PriceListItem();

        item.setId(
                rs.getLong("id")
        );

        item.setPriceListId(
                rs.getLong("price_list_id")
        );

        item.setProductId(
                rs.getLong("product_id")
        );

        item.setProductCode(
                rs.getString("product_code")
        );

        item.setProductName(
                rs.getString("product_name")
        );

        item.setProductType(
                rs.getString("product_type")
        );

        item.setUnit(
                rs.getString("unit")
        );

        item.setListPrice(
                rs.getBigDecimal("list_price")
        );

        item.setFloorPrice(
                rs.getBigDecimal("floor_price")
        );

        return item;
    }
}