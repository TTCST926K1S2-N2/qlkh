package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.Test;

import vn.edu.ictu.qlkh.dao.WinLossReasonDAO;
import vn.edu.ictu.qlkh.model.WinLossReason;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WinLossReasonServiceTest {

    static class FakeDAO
            extends WinLossReasonDAO {

        boolean duplicate;
        boolean updated = true;
        boolean deleted = true;

        @Override
        public boolean exists(
                String code,
                long excludeId
        ) {
            return duplicate;
        }

        @Override
        public void insert(
                WinLossReason reason
        ) {
            reason.setId(1L);
        }

        @Override
        public boolean update(
                WinLossReason reason
        ) {
            return updated;
        }

        @Override
        public boolean delete(
                long id
        ) {
            return deleted;
        }

        @Override
        public List<WinLossReason> findByType(
                String type
        ) {
            return List.of();
        }
    }

    private WinLossReason valid() {

        WinLossReason reason =
                new WinLossReason();

        reason.setName(
                " Giá phù hợp "
        );

        reason.setCode(
                " won_price "
        );

        reason.setType(
                "won"
        );

        reason.setStatus(
                "active"
        );

        reason.setDescription(
                " Mô tả "
        );

        return reason;
    }

    @Test
    void normalizeReason() {

        WinLossReason reason =
                valid();

        new WinLossReasonService(
                new FakeDAO()
        ).validate(reason);

        assertEquals(
                "Giá phù hợp",
                reason.getName()
        );

        assertEquals(
                "WON_PRICE",
                reason.getCode()
        );

        assertEquals(
                "WON",
                reason.getType()
        );

        assertEquals(
                "ACTIVE",
                reason.getStatus()
        );
    }

    @Test
    void rejectInvalidType() {

        WinLossReason reason =
                valid();

        reason.setType(
                "OTHER"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new WinLossReasonService(
                        new FakeDAO()
                ).validate(reason)
        );
    }

    @Test
    void rejectInvalidCode() {

        WinLossReason reason =
                valid();

        reason.setCode(
                "1BAD"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new WinLossReasonService(
                        new FakeDAO()
                ).validate(reason)
        );
    }

    @Test
    void rejectBlankName() {

        WinLossReason reason =
                valid();

        reason.setName(" ");

        assertThrows(
                IllegalArgumentException.class,
                () -> new WinLossReasonService(
                        new FakeDAO()
                ).validate(reason)
        );
    }

    @Test
    void rejectInvalidStatus() {

        WinLossReason reason =
                valid();

        reason.setStatus(
                "BAD"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new WinLossReasonService(
                        new FakeDAO()
                ).validate(reason)
        );
    }

    @Test
    void rejectDuplicateCode()
            throws SQLException {

        FakeDAO dao =
                new FakeDAO();

        dao.duplicate = true;

        assertThrows(
                IllegalArgumentException.class,
                () -> new WinLossReasonService(
                        dao
                ).addReason(
                        valid()
                )
        );
    }

    @Test
    void createReason()
            throws SQLException {

        WinLossReason reason =
                valid();

        new WinLossReasonService(
                new FakeDAO()
        ).addReason(reason);

        assertEquals(
                1L,
                reason.getId()
        );
    }

    @Test
    void getWonReasons()
            throws SQLException {

        assertNotNull(
                new WinLossReasonService(
                        new FakeDAO()
                ).getReasonsByType(
                        "won"
                )
        );
    }

    @Test
    void getLostReasons()
            throws SQLException {

        assertNotNull(
                new WinLossReasonService(
                        new FakeDAO()
                ).getReasonsByType(
                        "lost"
                )
        );
    }

    @Test
    void updateNotFound()
            throws SQLException {

        FakeDAO dao =
                new FakeDAO();

        dao.updated = false;

        WinLossReason reason =
                valid();

        reason.setId(10L);

        assertThrows(
                IllegalArgumentException.class,
                () -> new WinLossReasonService(
                        dao
                ).updateReason(reason)
        );
    }

    @Test
    void deleteNotFound()
            throws SQLException {

        FakeDAO dao =
                new FakeDAO();

        dao.deleted = false;

        assertThrows(
                IllegalArgumentException.class,
                () -> new WinLossReasonService(
                        dao
                ).deleteReason(99L)
        );
    }
}