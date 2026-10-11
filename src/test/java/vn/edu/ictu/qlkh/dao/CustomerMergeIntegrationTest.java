package vn.edu.ictu.qlkh.dao;

import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class CustomerMergeIntegrationTest {

    @Test
    void mergeRealCustomersInTestDatabase() throws Exception {

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT DATABASE()")) {

            assertTrue(rs.next());
            assertEquals("qlkh_s3_04_test", rs.getString(1),
                    "CAM GOP TREN DATABASE KHAC");
        }

        CustomerMergeDAO dao = new CustomerMergeDAO();

        CustomerMergeDAO.MergeResult result =
                dao.merge(9102, 9101, 9001, "ADMIN");

        assertEquals(9102, result.sourceId());
        assertEquals(9101, result.targetId());
        assertEquals(1, result.contacts());

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {

            try (ResultSet rs = st.executeQuery(
                    "SELECT COUNT(*) FROM contacts WHERE customer_id = 9101")) {
                assertTrue(rs.next());
                assertEquals(2, rs.getInt(1));
            }

            try (ResultSet rs = st.executeQuery(
                    "SELECT COUNT(*) FROM contacts WHERE customer_id = 9102")) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(1));
            }

            try (ResultSet rs = st.executeQuery(
                    "SELECT COUNT(*) FROM customer_merges " +
                    "WHERE source_customer_id = 9102 " +
                    "AND target_customer_id = 9101")) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1));
            }
        }
    }
}