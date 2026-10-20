package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.dao.LeadDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LeadServiceFilterTest {

    private LeadDAO dao;
    private LeadService service;

    @BeforeEach
    void setUp() {
        dao = mock(LeadDAO.class);
        service = new LeadService(dao, new PermissionService());
    }

    @Test
    void forwardsDateRangeAndSortToDao() throws SQLException {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 18);

        LeadDAO.SearchPage expected =
                new LeadDAO.SearchPage(List.of(), 0L, 1, 10);

        when(dao.searchVisible(
                1L, "ALL", null, null, null, null,
                1, 10, from, to, "name", "asc"
        )).thenReturn(expected);

        LeadDAO.SearchPage actual = service.search(
                1L, "ADMIN", null, null, null, null,
                1, 10, from, to, "name", "asc"
        );

        assertSame(expected, actual);

        verify(dao).searchVisible(
                1L, "ALL", null, null, null, null,
                1, 10, from, to, "name", "asc"
        );
    }

    @Test
    void rejectsReversedDateRange() {
        LocalDate from = LocalDate.of(2026, 10, 18);
        LocalDate to = LocalDate.of(2026, 10, 1);

        assertThrows(IllegalArgumentException.class, () ->
                service.search(
                        1L, "ADMIN", null, null, null, null,
                        1, 10, from, to, "createdAt", "desc"
                )
        );

        verifyNoInteractions(dao);
    }

    @Test
    void preservesSalesDataScopeWithDateFilter() throws SQLException {
        LocalDate from = LocalDate.of(2026, 10, 1);

        service.search(
                7L, "SALES", null, null, null, 7L,
                1, 10, from, null, "createdAt", "desc"
        );

        verify(dao).searchVisible(
                7L, "MY", null, null, null, 7L,
                1, 10, from, null, "createdAt", "desc"
        );
    }

    @Test
    void rejectsCrossUserAccessWithDateFilter() {
        LocalDate from = LocalDate.of(2026, 10, 1);

        assertThrows(SecurityException.class, () ->
                service.search(
                        7L, "SALES", null, null, null, 8L,
                        1, 10, from, null, "createdAt", "desc"
                )
        );

        verifyNoInteractions(dao);
    }

    @Test
    void forwardsHotClassificationToDao() throws SQLException {
        service.search(
                1L, "ADMIN", null, null, null, null,
                1, 10, null, null, "createdAt", "desc", "HOT"
        );

        verify(dao).searchVisible(
                1L, "ALL", null, null, null, null,
                1, 10, null, null, "createdAt", "desc", "HOT"
        );
    }

    @Test
    void forwardsWarmClassificationToDao() throws SQLException {
        service.search(
                1L, "ADMIN", null, null, null, null,
                1, 10, null, null, "createdAt", "desc", "WARM"
        );

        verify(dao).searchVisible(
                1L, "ALL", null, null, null, null,
                1, 10, null, null, "createdAt", "desc", "WARM"
        );
    }

    @Test
    void forwardsColdClassificationToDao() throws SQLException {
        service.search(
                1L, "ADMIN", null, null, null, null,
                1, 10, null, null, "createdAt", "desc", "COLD"
        );

        verify(dao).searchVisible(
                1L, "ALL", null, null, null, null,
                1, 10, null, null, "createdAt", "desc", "COLD"
        );
    }

    @Test
    void rejectsInvalidClassification() {
        assertThrows(IllegalArgumentException.class, () ->
                service.search(
                        1L, "ADMIN", null, null, null, null,
                        1, 10, null, null, "createdAt", "desc", "INVALID"
                )
        );

        verifyNoInteractions(dao);
    }

    @Test
    void rejectsCrossUserAccessWithClassification() {
        assertThrows(SecurityException.class, () ->
                service.search(
                        7L, "SALES", null, null, null, 8L,
                        1, 10, null, null, "createdAt", "desc", "HOT"
                )
        );

        verifyNoInteractions(dao);
    }
}
