package vn.edu.ictu.qlkh.dao;

import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

class CustomerWriteIntegrationTest {

    @Test
    void insertUpdateAndDuplicateTaxCode() throws Exception {
        try (Connection conn = DBConnection.getConnection()) {
            assertEquals("qlkh_s209_test", conn.getCatalog(),
                    "Chi cho phep database test");

            conn.setAutoCommit(false);

            try {
                long ownerId;

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO users " +
                        "(email,password_hash,full_name,role) " +
                        "VALUES (?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1,
                            "s301_test_" + System.nanoTime() + "@example.test");
                    ps.setString(2, "TEST_NOT_A_REAL_PASSWORD");
                    ps.setString(3, "S3-01 Test");
                    ps.setString(4, "SALES");

                    assertEquals(1, ps.executeUpdate());

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        assertTrue(keys.next());
                        ownerId = keys.getLong(1);
                    }
                }

                long customerId;

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO customers " +
                        "(company_name,tax_code,owner_id,status) " +
                        "VALUES (?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, "S3-01 Test Company");
                    ps.setString(2, "S301_" + System.nanoTime());
                    ps.setLong(3, ownerId);
                    ps.setString(4, "POTENTIAL");

                    assertEquals(1, ps.executeUpdate());

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        assertTrue(keys.next());
                        customerId = keys.getLong(1);
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE customers SET company_name=?,status=? " +
                        "WHERE id=?")) {

                    ps.setString(1, "S3-01 Updated Company");
                    ps.setString(2, "CUSTOMER");
                    ps.setLong(3, customerId);

                    assertEquals(1, ps.executeUpdate());
                }

                String taxCode;

                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT company_name,status,tax_code " +
                        "FROM customers WHERE id=?")) {

                    ps.setLong(1, customerId);

                    try (ResultSet rs = ps.executeQuery()) {
                        assertTrue(rs.next());
                        assertEquals("S3-01 Updated Company",
                                rs.getString("company_name"));
                        assertEquals("CUSTOMER",
                                rs.getString("status"));

                        taxCode = rs.getString("tax_code");
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO customers " +
                        "(company_name,tax_code,owner_id) " +
                        "VALUES (?,?,?)")) {

                    ps.setString(1, "Duplicate Tax Code");
                    ps.setString(2, taxCode);
                    ps.setLong(3, ownerId);

                    SQLException ex = assertThrows(
                            SQLException.class, ps::executeUpdate);

                    assertEquals(1062, ex.getErrorCode());
                }
            } finally {
                conn.rollback();
            }
        }
    }
}