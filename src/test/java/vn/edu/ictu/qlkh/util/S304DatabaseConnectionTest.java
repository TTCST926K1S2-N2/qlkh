package vn.edu.ictu.qlkh.util;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class S304DatabaseConnectionTest {

    @Test
    void mustConnectToTestDatabase() throws Exception {

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT DATABASE()")) {

            assertTrue(rs.next());

            String database = rs.getString(1);

            assertEquals(
                    "qlkh_s3_04_test",
                    database,
                    "SAI DATABASE - KHONG DUOC CHAY MERGE TEST"
            );
        }
    }
}