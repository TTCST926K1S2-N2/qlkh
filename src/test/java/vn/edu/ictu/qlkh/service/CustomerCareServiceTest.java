package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.dao.CustomerCareDAO;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CustomerCareServiceTest {

    private CustomerCareDAO dao;
    private CustomerCareService service;

    @BeforeEach
    void setup() {
        dao = mock(CustomerCareDAO.class);
        service = new CustomerCareService(
                dao,
                new PermissionService());
    }

    @Test
    void salesUsesMyScope() throws SQLException {
        service.list(10L, "SALES");

        verify(dao).findCandidates(10L, "MY");
    }

    @Test
    void managerUsesTeamScope() throws SQLException {
        service.list(10L, "MANAGER");

        verify(dao).findCandidates(10L, "TEAM");
    }

    @Test
    void adminUsesAllScope() throws SQLException {
        service.list(10L, "ADMIN");

        verify(dao).findCandidates(10L, "ALL");
    }

    @Test
    void invalidRoleIsRejected() {
        assertThrows(
                SecurityException.class,
                () -> service.list(10L, "GUEST"));
    }
}