package vn.edu.ictu.qlkh.dao;

import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class CustomerMergeRollbackTest {

    private int count(String sql) throws Exception {
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {

            try (ResultSet db = st.executeQuery("SELECT DATABASE()")) {
                assertTrue(db.next());
                assertEquals("qlkh_s3_04_test", db.getString(1));
            }

            try (ResultSet rs = st.executeQuery(sql)) {
                assertTrue(rs.next());
                return rs.getInt(1);
            }
        }
    }

    @Test
    void rejectSecondMergeWithoutChangingData() throws Exception {

        int contactsBefore = count(
                "SELECT COUNT(*) FROM contacts WHERE customer_id = 9101");

        int mergesBefore = count(
                "SELECT COUNT(*) FROM customer_merges");

        int auditsBefore = count(
                "SELECT COUNT(*) FROM audit_logs " +
                "WHERE action_type = 'CUSTOMER_MERGE'");

        CustomerMergeDAO dao = new CustomerMergeDAO();

        assertThrows(IllegalArgumentException.class,
                () -> dao.merge(9102, 9101, 9001, "ADMIN"));

        assertEquals(contactsBefore, count(
                "SELECT COUNT(*) FROM contacts WHERE customer_id = 9101"));

        assertEquals(mergesBefore, count(
                "SELECT COUNT(*) FROM customer_merges"));

        assertEquals(auditsBefore, count(
                "SELECT COUNT(*) FROM audit_logs " +
                "WHERE action_type = 'CUSTOMER_MERGE'"));
    }
}