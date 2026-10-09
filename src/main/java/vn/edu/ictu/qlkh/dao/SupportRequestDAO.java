package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.SupportRequest;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SupportRequestDAO {

    public boolean createRequest(SupportRequest request) {
        String sql = "INSERT INTO support_request (customer_id, description, priority, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, request.getCustomerId());
            ps.setString(2, request.getDescription());
            ps.setString(3, request.getPriority() != null ? request.getPriority() : "MEDIUM");
            ps.setString(4, request.getStatus() != null ? request.getStatus() : "PENDING");

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}