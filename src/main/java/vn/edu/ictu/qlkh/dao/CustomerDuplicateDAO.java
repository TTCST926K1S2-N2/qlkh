package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.Customer;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDuplicateDAO {

    public List<Customer> findCandidates(long userId, String role)
            throws SQLException {

        String scope;

        switch (role) {
            case "ADMIN" -> scope = "ALL";
            case "MANAGER" -> scope = "TEAM";
            default -> throw new SecurityException("Khong co quyen");
        }

        return new CustomerDAO().findVisible(userId, scope);
    }
}