package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.dao.CustomerMergeDAO;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerMergeServiceTest {

    private final CustomerMergeDAO dao = mock(CustomerMergeDAO.class);
    private final CustomerMergeService service =
            new CustomerMergeService(dao);

    @Test
    void salesCannotMerge() {
        assertThrows(SecurityException.class,
                () -> service.merge(1, 2, 3, "SALES"));

        verifyNoInteractions(dao);
    }

    @Test
    void anonymousCannotMerge() {
        assertThrows(SecurityException.class,
                () -> service.merge(1, 2, 0, "ADMIN"));

        verifyNoInteractions(dao);
    }

    @Test
    void cannotMergeSameCustomer() {
        assertThrows(IllegalArgumentException.class,
                () -> service.merge(5, 5, 1, "ADMIN"));

        verifyNoInteractions(dao);
    }

    @Test
    void cannotMergeInvalidIds() {
        assertThrows(IllegalArgumentException.class,
                () -> service.merge(-1, 2, 1, "MANAGER"));

        verifyNoInteractions(dao);
    }

    @Test
    void adminCanRequestMerge() throws SQLException {
        CustomerMergeDAO.MergeResult result =
                new CustomerMergeDAO.MergeResult(1, 2, 3, 4, 5);

        when(dao.merge(1, 2, 9, "ADMIN")).thenReturn(result);

        CustomerMergeDAO.MergeResult actual =
                service.merge(1, 2, 9, "admin");

        assertEquals(1, actual.sourceId());
        assertEquals(2, actual.targetId());
        assertEquals(3, actual.contacts());
        assertEquals(4, actual.opportunities());
        assertEquals(5, actual.attachments());

        verify(dao).merge(1, 2, 9, "ADMIN");
    }

    @Test
    void managerCanRequestMerge() throws SQLException {
        CustomerMergeDAO.MergeResult result =
                new CustomerMergeDAO.MergeResult(4, 7, 1, 0, 0);

        when(dao.merge(4, 7, 8, "MANAGER")).thenReturn(result);

        assertSame(result,
                service.merge(4, 7, 8, " manager "));

        verify(dao).merge(4, 7, 8, "MANAGER");
    }

    @Test
    void databaseFailureIsPropagated() throws SQLException {
        when(dao.merge(1, 2, 9, "ADMIN"))
                .thenThrow(new SQLException("Database error"));

        assertThrows(SQLException.class,
                () -> service.merge(1, 2, 9, "ADMIN"));
    }
}