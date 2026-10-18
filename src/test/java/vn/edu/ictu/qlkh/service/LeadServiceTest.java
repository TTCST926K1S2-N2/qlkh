package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.dao.LeadDAO;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LeadServiceTest {

    private LeadDAO leadDAO;
    private PermissionService permissionService;
    private LeadService leadService;

    @BeforeEach
    void setUp() {
        leadDAO = mock(LeadDAO.class);
        permissionService = new PermissionService();
        leadService = new LeadService(leadDAO, permissionService);
    }

    @Test
    void salesSearchUsesMyScope() throws SQLException {
        LeadDAO.SearchPage expected = new LeadDAO.SearchPage(
                List.of(sampleLead()), 1L, 1, 10
        );

        when(leadDAO.searchVisible(
                eq(7L), eq("MY"), eq("Acme"), eq("NEW"),
                eq("WEBSITE"), eq(7L), eq(1), eq(10)
        )).thenReturn(expected);

        LeadDAO.SearchPage actual = leadService.search(
                7L, "SALES", " Acme ", " new ",
                " website ", 7L, 1, 10
        );

        assertSame(expected, actual);
        verify(leadDAO).searchVisible(
                7L, "MY", "Acme", "NEW", "WEBSITE", 7L, 1, 10
        );
    }

    @Test
    void managerSearchSupportsCombinedFilters() throws SQLException {
        LeadDAO.SearchPage expected = new LeadDAO.SearchPage(
                List.of(sampleLead()), 1L, 2, 5
        );

        when(leadDAO.searchVisible(
                eq(12L), eq("TEAM"), eq("090"),
                eq("IN_PROGRESS"), eq("WEBSITE"),
                eq(7L), eq(2), eq(5)
        )).thenReturn(expected);

        LeadDAO.SearchPage actual = leadService.search(
                12L, "MANAGER", "090", "in_progress",
                "website", 7L, 2, 5
        );

        assertSame(expected, actual);
        verify(leadDAO).searchVisible(
                12L, "TEAM", "090", "IN_PROGRESS",
                "WEBSITE", 7L, 2, 5
        );
    }

    @Test
    void adminSearchUsesAllScope() throws SQLException {
        LeadDAO.SearchPage expected = new LeadDAO.SearchPage(
                List.of(sampleLead()), 1L, 1, 10
        );

        when(leadDAO.searchVisible(
                eq(1L), eq("ALL"), isNull(), isNull(),
                isNull(), isNull(), eq(1), eq(10)
        )).thenReturn(expected);

        LeadDAO.SearchPage actual = leadService.search(
                1L, "ADMIN", null, null, null, null, 1, 10
        );

        assertSame(expected, actual);
        verify(leadDAO).searchVisible(
                1L, "ALL", null, null, null, null, 1, 10
        );
    }

    @Test
    void returnsEmptyPageWhenNoLeadsMatch() throws SQLException {
        LeadDAO.SearchPage emptyPage = new LeadDAO.SearchPage(
                List.of(), 0L, 1, 10
        );

        when(leadDAO.searchVisible(
                anyLong(), anyString(), isNull(), isNull(),
                isNull(), isNull(), anyInt(), anyInt()
        )).thenReturn(emptyPage);

        LeadDAO.SearchPage actual = leadService.search(
                1L, "ADMIN", null, null, null, null, 1, 10
        );

        assertNotNull(actual);
        assertTrue(actual.items().isEmpty());
        assertEquals(0L, actual.totalItems());
        assertEquals(1, actual.page());
        assertEquals(10, actual.size());
    }

    @Test
    void salesCannotSearchAnotherUsersAssignedLeads() {
        assertThrows(SecurityException.class, () ->
                leadService.search(
                        7L, "SALES", null, null, null, 8L, 1, 10
                )
        );

        verifyNoInteractions(leadDAO);
    }

    @Test
    void rejectsUnknownRole() {
        assertThrows(SecurityException.class, () ->
                leadService.search(
                        7L, "UNKNOWN", null, null, null, null, 1, 10
                )
        );

        verifyNoInteractions(leadDAO);
    }

    @Test
    void rejectsInvalidUserId() {
        assertThrows(SecurityException.class, () ->
                leadService.search(
                        0L, "ADMIN", null, null, null, null, 1, 10
                )
        );

        verifyNoInteractions(leadDAO);
    }

    private LeadDAO.LeadItem sampleLead() {
        LocalDateTime now = LocalDateTime.of(2026, 10, 16, 12, 0);

        return new LeadDAO.LeadItem(
                100L,
                "Nguyen Van A",
                "a@example.com",
                "0900000000",
                "Acme",
                "Tu van san pham",
                "NEW",
                "WEBSITE",
                "landing-page",
                7L,
                new BigDecimal("10.00"),
                now,
                now
        );
    }
}
