package vn.edu.ictu.qlkh.dao;

import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LeadDAOIntegrationTest {

    @Test
    void shouldCreateLeadInTestDatabase() throws SQLException {
        String databaseName;
        String testEmail = "s401-" + UUID.randomUUID() + "@example.test";

        try (Connection connection = DBConnection.getConnection()) {
            databaseName = connection.getCatalog();

            assertEquals(
                    "qlkh_s209_test",
                    databaseName,
                    "SAFETY STOP: Test must only use qlkh_s209_test"
            );
        }

        long leadId = 0;

        try {
            LeadDAO dao = new LeadDAO();

            leadId = dao.create(
                    "S4-01 Integration Test",
                    testEmail,
                    "0912345678",
                    "Test Company",
                    "Test Lead Capture",
                    "website-default"
            );

            assertTrue(leadId > 0);

            try (Connection connection = DBConnection.getConnection()) {
                assertEquals("qlkh_s209_test", connection.getCatalog());

                String sql = """
                    SELECT full_name, email, phone, company,
                           interest, status, source, source_form
                    FROM leads
                    WHERE id = ?
                    """;

                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setLong(1, leadId);

                    try (ResultSet result = statement.executeQuery()) {
                        assertTrue(result.next());

                        assertEquals("S4-01 Integration Test", result.getString("full_name"));
                        assertEquals(testEmail, result.getString("email"));
                        assertEquals("0912345678", result.getString("phone"));
                        assertEquals("Test Company", result.getString("company"));
                        assertEquals("Test Lead Capture", result.getString("interest"));
                        assertEquals("NEW", result.getString("status"));
                        assertEquals("WEBSITE", result.getString("source"));
                        assertEquals("website-default", result.getString("source_form"));

                        assertFalse(result.next());
                    }
                }
            }
        } finally {
            if (leadId > 0) {
                try (Connection connection = DBConnection.getConnection()) {
                    assertEquals(
                            "qlkh_s209_test",
                            connection.getCatalog(),
                            "SAFETY STOP: Refuse cleanup outside test database"
                    );

                    String cleanupSql = """
                        DELETE FROM leads
                        WHERE id = ? AND email = ?
                        """;

                    try (PreparedStatement statement = connection.prepareStatement(cleanupSql)) {
                        statement.setLong(1, leadId);
                        statement.setString(2, testEmail);

                        int deleted = statement.executeUpdate();
                        assertEquals(1, deleted, "Test lead cleanup failed");
                    }
                }
            }
        }
    }
}