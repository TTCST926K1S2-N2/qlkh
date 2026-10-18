package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.dao.RegionDAO;
import vn.edu.ictu.qlkh.dao.SalesOrgDAO;
import vn.edu.ictu.qlkh.dao.SalesOrgLookupDAO;
import vn.edu.ictu.qlkh.dao.UserDAO;
import vn.edu.ictu.qlkh.dto.SalesOrgDTO;
import vn.edu.ictu.qlkh.model.Region;
import vn.edu.ictu.qlkh.model.SalesOrganization;
import vn.edu.ictu.qlkh.model.User;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SalesOrgServiceTest {

    @Test
    void createSalesOrg_shouldRejectDuplicateCode()
            throws Exception {

        FakeSalesOrgDAO salesOrgDAO =
                new FakeSalesOrgDAO();

        salesOrgDAO.duplicateCode = true;

        SalesOrgServiceImpl service =
                createService(salesOrgDAO);

        SalesOrgDTO input =
                validInput();

        IllegalArgumentException ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.createSalesOrg(input)
                );

        assertEquals(
                "Mã nhóm đã tồn tại.",
                ex.getMessage()
        );
    }


    @Test
    void createSalesOrg_shouldRejectNonManagerLeader()
            throws Exception {

        FakeSalesOrgDAO salesOrgDAO =
                new FakeSalesOrgDAO();

        FakeUserDAO userDAO =
                new FakeUserDAO();

        userDAO.role = "SALES";

        SalesOrgServiceImpl service =
                new SalesOrgServiceImpl(
                        salesOrgDAO,
                        new FakeRegionDAO(),
                        userDAO,
                        new FakeLookupDAO()
                );

        IllegalArgumentException ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.createSalesOrg(
                                validInput()
                        )
                );

        assertEquals(
                "Trưởng nhóm phải có vai trò MANAGER.",
                ex.getMessage()
        );
    }


    @Test
    void updateSalesOrg_shouldRejectSelfParent()
            throws Exception {

        FakeSalesOrgDAO salesOrgDAO =
                new FakeSalesOrgDAO();

        SalesOrgServiceImpl service =
                createService(salesOrgDAO);

        SalesOrgDTO input =
                validInput();

        input.setParentId(10L);

        IllegalArgumentException ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.updateSalesOrg(
                                10L,
                                input
                        )
                );

        assertEquals(
                "Nhóm không thể là cha của chính nó.",
                ex.getMessage()
        );
    }


    @Test
    void updateSalesOrg_shouldRejectCycle()
            throws Exception {

        FakeSalesOrgDAO salesOrgDAO =
                new FakeSalesOrgDAO();

        salesOrgDAO.descendant = true;

        SalesOrgServiceImpl service =
                createService(salesOrgDAO);

        SalesOrgDTO input =
                validInput();

        input.setParentId(20L);

        IllegalArgumentException ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.updateSalesOrg(
                                10L,
                                input
                        )
                );

        assertEquals(
                "Không thể tạo vòng lặp trong cây tổ chức.",
                ex.getMessage()
        );
    }


    @Test
    void createSalesOrg_shouldCreateValidGroup()
            throws Exception {

        FakeSalesOrgDAO salesOrgDAO =
                new FakeSalesOrgDAO();

        SalesOrgServiceImpl service =
                createService(salesOrgDAO);

        SalesOrgDTO result =
                service.createSalesOrg(
                        validInput()
                );

        assertNotNull(result);
        assertEquals(99L, result.getId());
        assertEquals(
                "S206_TEST",
                result.getOrgCode()
        );
        assertEquals(
                "Nhóm test",
                result.getOrgName()
        );
        assertEquals(
                "ACTIVE",
                result.getStatus()
        );
    }


    @Test
    void getMembers_shouldRejectInvalidId() throws Exception {
        FakeSalesOrgDAO dao = new FakeSalesOrgDAO();
        SalesOrgServiceImpl service = createService(dao);

        assertThrows(
            IllegalArgumentException.class,
            () -> service.getMembers(0)
        );
    }

    @Test
    void getMembers_shouldReturnMembers() throws Exception {
        FakeSalesOrgDAO dao = new FakeSalesOrgDAO();
        SalesOrgServiceImpl service = createService(dao);

        List<User> members = service.getMembers(10);

        assertEquals(1, members.size());
        assertEquals("Test Member", members.get(0).getFullName());
    }

    @Test
    void getMembers_shouldReturnEmptyList() throws Exception {
        FakeSalesOrgDAO dao = new FakeSalesOrgDAO();
        dao.emptyMembers = true;

        SalesOrgServiceImpl service = createService(dao);

        assertTrue(service.getMembers(10).isEmpty());
    }
    @Test
    void getMembers_shouldRejectMissingGroup() throws Exception {
        FakeSalesOrgDAO dao = new FakeSalesOrgDAO() {
            @Override
            public SalesOrganization findById(long id) {
                return null;
            }
        };

        SalesOrgServiceImpl service = createService(dao);

        assertThrows(
            IllegalArgumentException.class,
            () -> service.getMembers(999999L)
        );
    }
    private SalesOrgServiceImpl createService(
            FakeSalesOrgDAO salesOrgDAO
    ) {

        return new SalesOrgServiceImpl(
                salesOrgDAO,
                new FakeRegionDAO(),
                new FakeUserDAO(),
                new FakeLookupDAO()
        );
    }


    private SalesOrgDTO validInput() {

        SalesOrgDTO dto =
                new SalesOrgDTO();

        dto.setOrgCode("S206_TEST");
        dto.setOrgName("Nhóm test");
        dto.setLeaderId(2L);
        dto.setRegionId(1L);
        dto.setStatus("ACTIVE");

        return dto;
    }


    static class FakeSalesOrgDAO
            extends SalesOrgDAO {

        boolean emptyMembers;

        @Override
        public List<User> findMembersByGroupId(long groupId) {
            if (emptyMembers) {
                return List.of();
            }

            User user = new User();
            user.setId(1L);
            user.setFullName("Test Member");

            return List.of(user);
        }
        boolean duplicateCode;
        boolean descendant;

        @Override
        public boolean existsByCode(
                String code,
                Long excludeId
        ) {

            return duplicateCode;
        }

        @Override
        public SalesOrganization findById(
                long id
        ) {

            SalesOrganization org =
                    new SalesOrganization();

            org.setId(id);
            org.setCode("EXISTING");
            org.setName("Existing");
            org.setStatus("ACTIVE");

            return org;
        }

        @Override
        public boolean isDescendant(
                long candidateParentId,
                long groupId
        ) {

            return descendant;
        }

        @Override
        public SalesOrganization insert(
                SalesOrganization organization
        ) {

            organization.setId(99L);

            return organization;
        }

        @Override
        public SalesOrganization update(
                SalesOrganization organization
        ) {

            return organization;
        }
    }


    static class FakeRegionDAO
            extends RegionDAO {

        @Override
        public Region findById(long id) {

            return new Region(
                    id,
                    "MB",
                    "Miền Bắc",
                    "ACTIVE"
            );
        }
    }


    static class FakeUserDAO
            extends UserDAO {

        String role = "MANAGER";

        @Override
        public User findById(long userId) {

            User user =
                    new User();

            user.setId(userId);
            user.setFullName(
                    "Quản lý test"
            );
            user.setRole(role);
            user.setStatus("ACTIVE");

            return user;
        }
    }


    static class FakeLookupDAO
            extends SalesOrgLookupDAO {

        @Override
        public List<User> findActiveManagers() {

            return List.of();
        }
    }
}