package vn.edu.ictu.qlkh.dao;

import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.dto.CustomerCareDTO;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomerCareDAOIntegrationTest {

    private final CustomerCareDAO dao = new CustomerCareDAO();

    @Test
    void unsupportedScopeReturnsEmptyList() throws SQLException {
        List<CustomerCareDTO> result =
                dao.findCandidates(1L, "INVALID");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void allScopeCanQueryCustomers() throws SQLException {
        List<CustomerCareDTO> result =
                dao.findCandidates(1L, "ALL");

        assertNotNull(result);
    }

    @Test
    void myScopeCanQueryCustomers() throws SQLException {
        List<CustomerCareDTO> result =
                dao.findCandidates(1L, "MY");

        assertNotNull(result);
    }

    @Test
    void teamScopeCanQueryCustomers() throws SQLException {
        List<CustomerCareDTO> result =
                dao.findCandidates(1L, "TEAM");

        assertNotNull(result);
    }
}