package vn.edu.ictu.qlkh.dao;

import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class CustomerDAOIntegrationTest {

    @Test
    void customerTableExists() throws Exception {
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT COUNT(*) FROM customers")) {

            assertTrue(rs.next());
            assertTrue(rs.getLong(1) >= 0);
        }
    }

    @Test
    void customerListQueryWorks() throws Exception {
        CustomerDAO dao = new CustomerDAO();

        assertNotNull(dao.findVisible(1L, "ALL"));
        assertNotNull(dao.findVisible(1L, "MY"));
        assertNotNull(dao.findVisible(1L, "TEAM"));
    }

    @Test
    void customerDetailQueryWorks() throws Exception {
        CustomerDAO dao = new CustomerDAO();

        assertNull(dao.findVisibleById(
                Long.MAX_VALUE, 1L, "ALL"));
    }

    @Test
    void taxCodeLookupWorks() throws Exception {
        CustomerDAO dao = new CustomerDAO();

        assertFalse(dao.existsTaxCode(
                "S301_TEST_NONEXISTENT_987654321", null));
    }
}