package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LeadDAO {

    private static final String INSERT_SQL =
            "INSERT INTO leads " +
            "(full_name, email, phone, company, interest, status, source, source_form) " +
            "VALUES (?, ?, ?, ?, ?, 'NEW', 'WEBSITE', ?)";

    public long create(
            String fullName,
            String email,
            String phone,
            String company,
            String interest,
            String sourceForm
    ) throws SQLException {

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     INSERT_SQL,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            statement.setString(1, fullName);
            statement.setString(2, email);
            statement.setString(3, phone);
            statement.setString(4, company);
            statement.setString(5, interest);
            statement.setString(6, sourceForm);

            int affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                throw new SQLException("Khong the tao Lead.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }

            throw new SQLException("Khong lay duoc ID Lead.");
        }
    }
}