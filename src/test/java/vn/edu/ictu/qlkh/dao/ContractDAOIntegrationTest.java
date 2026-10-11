package vn.edu.ictu.qlkh.dao;

import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ContractDAOIntegrationTest {

    @Test
    void createUpdateAndSummarizeContracts() throws Exception {
        long customerId = 0;
        String code = "S3TEST-" + UUID.randomUUID().toString().substring(0, 12);

        try (Connection conn = DBConnection.getConnection()) {
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT DATABASE()")) {
                assertTrue(rs.next());
                assertEquals("qlkh_s3_04_test", rs.getString(1),
                        "KHONG DUOC CHAY TREN DATABASE CHINH");
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO customers(company_name,owner_id,status) VALUES(?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, "Contract Test " + code);
                ps.setLong(2, 9003L);
                ps.setString(3, "POTENTIAL");
                assertEquals(1, ps.executeUpdate());

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    assertTrue(rs.next());
                    customerId = rs.getLong(1);
                }
            }

            ContractDAO dao = new ContractDAO();

            ContractDAO.Contract draft = new ContractDAO.Contract(
                    0, code, customerId, "Hop dong kiem thu",
                    new BigDecimal("1000000.00"), "DRAFT", null);

            long contractId = dao.create(draft);
            assertTrue(contractId > 0);

            ContractDAO.Contract created = dao.find(contractId);
            assertNotNull(created);
            assertEquals("DRAFT", created.status());
            assertEquals(0, new BigDecimal("1000000.00")
                    .compareTo(created.value()));

            ContractSummaryDAO summaryDAO = new ContractSummaryDAO();
            ContractSummaryDAO.Summary before = summaryDAO.forCustomer(customerId);

            assertEquals(1, before.unsignedCount());
            assertEquals(0, before.signedCount());

            ContractDAO.Contract signed = new ContractDAO.Contract(
                    contractId, code, customerId, "Hop dong da ky",
                    new BigDecimal("1500000.00"), "SIGNED",
                    LocalDate.of(2026, 10, 12));

            assertTrue(dao.update(signed));

            ContractDAO.Contract updated = dao.find(contractId);
            assertNotNull(updated);
            assertEquals("SIGNED", updated.status());
            assertEquals(0, new BigDecimal("1500000.00")
                    .compareTo(updated.value()));

            ContractSummaryDAO.Summary after = summaryDAO.forCustomer(customerId);

            assertEquals(1, after.signedCount());
            assertEquals(0, after.unsignedCount());
            assertEquals(0, new BigDecimal("1500000.00")
                    .compareTo(after.signed()));

        } finally {
            if (customerId > 0) {
                try (Connection conn = DBConnection.getConnection()) {
                    try (Statement st = conn.createStatement();
                         ResultSet rs = st.executeQuery("SELECT DATABASE()")) {
                        assertTrue(rs.next());
                        assertEquals("qlkh_s3_04_test", rs.getString(1));
                    }

                    try (PreparedStatement ps = conn.prepareStatement(
                            "DELETE FROM contracts WHERE customer_id=? AND contract_code=?")) {
                        ps.setLong(1, customerId);
                        ps.setString(2, code);
                        ps.executeUpdate();
                    }

                    try (PreparedStatement ps = conn.prepareStatement(
                            "DELETE FROM customers WHERE id=?")) {
                        ps.setLong(1, customerId);
                        ps.executeUpdate();
                    }
                }
            }
        }
    }
}