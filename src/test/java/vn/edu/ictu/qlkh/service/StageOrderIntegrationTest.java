package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.dao.StageDAO;
import vn.edu.ictu.qlkh.model.Stage;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class StageOrderIntegrationTest {

    private final StageDAO dao = new StageDAO();

    private void verifyTestDatabase() throws Exception {
        try (Connection c = DBConnection.getConnection()) {
            assertEquals("qlkh_s209_test", c.getCatalog(),
                    "Refusing to modify a non-test database");
        }
    }

    private Stage stage(String code, int order) {
        Stage s = new Stage();
        s.setName(code);
        s.setCode(code);
        s.setStageOrder(order);
        s.setWinProbability(BigDecimal.TEN);
        s.setExitCondition("Test");
        s.setStatus("ACTIVE");
        s.setDescription("S2-09 test");
        return s;
    }

    private void reset() throws Exception {
        verifyTestDatabase();
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement()) {
            st.executeUpdate("DELETE FROM stages");
        }
    }

    private int order(long id) throws Exception {
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT stage_order FROM stages WHERE id=" + id)) {
            assertTrue(rs.next());
            return rs.getInt(1);
        }
    }

    @Test
    void swapAndMoveToEmptyPosition() throws Exception {
        reset();
        try {
            Stage a = stage("S209_A", 1);
            Stage b = stage("S209_B", 2);

            assertTrue(dao.insert(a));
            assertTrue(dao.insert(b));

            a.setStageOrder(2);
            assertTrue(dao.updateWithOrderSwap(a));

            assertEquals(2, order(a.getId()));
            assertEquals(1, order(b.getId()));

            a.setStageOrder(5);
            assertTrue(dao.updateWithOrderSwap(a));

            assertEquals(5, order(a.getId()));
            assertEquals(1, order(b.getId()));
        } finally {
            reset();
        }
    }

    @Test
    void rollbackOnDuplicateCode() throws Exception {
        reset();
        try {
            Stage a = stage("S209_A", 1);
            Stage b = stage("S209_B", 2);

            assertTrue(dao.insert(a));
            assertTrue(dao.insert(b));

            a.setStageOrder(2);
            a.setCode("S209_B");

            assertThrows(java.sql.SQLException.class,
                    () -> dao.updateWithOrderSwap(a));

            assertEquals(1, order(a.getId()));
            assertEquals(2, order(b.getId()));
        } finally {
            reset();
        }
    }
}