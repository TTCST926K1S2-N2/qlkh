package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.Test;

import vn.edu.ictu.qlkh.dao.CompetitorDAO;
import vn.edu.ictu.qlkh.model.Competitor;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class CompetitorServiceTest {

    static class FakeDAO
            extends CompetitorDAO {

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
                Competitor competitor
        ) {
            competitor.setId(1L);
        }

        @Override
        public boolean update(
                Competitor competitor
        ) {
            return updated;
        }

        @Override
        public boolean delete(
                long id
        ) {
            return deleted;
        }
    }

    private Competitor valid() {

        Competitor competitor =
                new Competitor();

        competitor.setName(
                " Công ty đối thủ "
        );

        competitor.setCode(
                " competitor_a "
        );

        competitor.setStatus(
                "active"
        );

        competitor.setDescription(
                " Ghi chú "
        );

        return competitor;
    }

    @Test
    void normalizeCompetitor() {

        Competitor competitor =
                valid();

        new CompetitorService(
                new FakeDAO()
        ).validate(competitor);

        assertEquals(
                "Công ty đối thủ",
                competitor.getName()
        );

        assertEquals(
                "COMPETITOR_A",
                competitor.getCode()
        );

        assertEquals(
                "ACTIVE",
                competitor.getStatus()
        );

        assertEquals(
                "Ghi chú",
                competitor.getDescription()
        );
    }

    @Test
    void rejectInvalidCode() {

        Competitor competitor =
                valid();

        competitor.setCode(
                "1BAD"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new CompetitorService(
                        new FakeDAO()
                ).validate(competitor)
        );
    }

    @Test
    void rejectBlankName() {

        Competitor competitor =
                valid();

        competitor.setName(" ");

        assertThrows(
                IllegalArgumentException.class,
                () -> new CompetitorService(
                        new FakeDAO()
                ).validate(competitor)
        );
    }

    @Test
    void rejectInvalidStatus() {

        Competitor competitor =
                valid();

        competitor.setStatus(
                "BAD"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new CompetitorService(
                        new FakeDAO()
                ).validate(competitor)
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
                () -> new CompetitorService(
                        dao
                ).addCompetitor(
                        valid()
                )
        );
    }

    @Test
    void createCompetitor()
            throws SQLException {

        Competitor competitor =
                valid();

        new CompetitorService(
                new FakeDAO()
        ).addCompetitor(competitor);

        assertEquals(
                1L,
                competitor.getId()
        );
    }

    @Test
    void updateNotFound()
            throws SQLException {

        FakeDAO dao =
                new FakeDAO();

        dao.updated = false;

        Competitor competitor =
                valid();

        competitor.setId(10L);

        assertThrows(
                IllegalArgumentException.class,
                () -> new CompetitorService(
                        dao
                ).updateCompetitor(
                        competitor
                )
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
                () -> new CompetitorService(
                        dao
                ).deleteCompetitor(99L)
        );
    }
}