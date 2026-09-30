package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.dao.BusinessGroupDAO;
import vn.edu.ictu.qlkh.dao.RoleDAO;
import vn.edu.ictu.qlkh.model.Role;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RoleServiceTest {

    private FakeRoleDAO roleDAO;
    private FakeBusinessGroupDAO businessGroupDAO;
    private RoleService roleService;

    @BeforeEach
    void setUp() {
        roleDAO = new FakeRoleDAO();
        businessGroupDAO = new FakeBusinessGroupDAO();

        roleService = new RoleService(
                roleDAO,
                businessGroupDAO
        );
    }

    /**
     * Constructor không cho phép RoleDAO = null.
     */
    @Test
    void constructor_shouldRejectNullRoleDAO() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new RoleService(
                        null,
                        new FakeBusinessGroupDAO()
                )
        );
    }

    /**
     * Constructor không cho phép BusinessGroupDAO = null.
     */
    @Test
    void constructor_shouldRejectNullBusinessGroupDAO() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new RoleService(
                        new FakeRoleDAO(),
                        null
                )
        );
    }

    /**
     * Lấy toàn bộ danh mục vai trò.
     */
    @Test
    void getAllRoles_shouldReturnAllRoles()
            throws SQLException {

        List<Role> roles =
                roleService.getAllRoles();

        assertEquals(3, roles.size());

        assertEquals(
                "ADMIN",
                roles.get(0).getCode()
        );

        assertEquals(
                "MANAGER",
                roles.get(1).getCode()
        );

        assertEquals(
                "SALES",
                roles.get(2).getCode()
        );
    }

    /**
     * userId không hợp lệ phải trả danh sách rỗng.
     */
    @Test
    void getRolesByUserId_shouldReturnEmptyListForInvalidUserId()
            throws SQLException {

        List<Role> roles =
                roleService.getRolesByUserId(0);

        assertNotNull(roles);
        assertTrue(roles.isEmpty());
    }

    /**
     * Kiểm tra RoleService chuẩn hóa role trước khi gửi xuống DAO.
     */
    @Test
    void userHasRole_shouldNormalizeRoleCode()
            throws SQLException {

        roleDAO.setUserRoles(
                10L,
                Set.of("SALES")
        );

        boolean result =
                roleService.userHasRole(
                        10L,
                        "  sales  "
                );

        assertTrue(result);
        assertEquals(
                "SALES",
                roleDAO.lastCheckedRoleCode
        );
    }

    /**
     * Một người dùng có thể giữ nhiều vai trò cùng lúc.
     *
     * Đồng thời kiểm tra:
     * - chuẩn hóa uppercase
     * - loại role trùng
     * - lưu nhiều role
     * - gán nhóm kinh doanh
     */
    @Test
    void updateRoleAndGroup_shouldAllowMultipleRolesAndAssignGroup()
            throws SQLException {

        roleService.updateRoleAndGroup(
                1L,
                2L,
                List.of(
                        " sales ",
                        "MANAGER",
                        "sales"
                ),
                1L
        );

        assertTrue(roleDAO.replaceCalled);

        assertEquals(
                2L,
                roleDAO.replacedUserId
        );

        assertEquals(
                List.of(
                        "SALES",
                        "MANAGER"
                ),
                roleDAO.replacedRoles
        );

        assertEquals(
                1,
                businessGroupDAO.assignCalls
        );

        assertEquals(
                2L,
                businessGroupDAO.assignedUserId
        );

        assertEquals(
                1L,
                businessGroupDAO.assignedGroupId
        );

        assertEquals(
                0,
                businessGroupDAO.removeCalls
        );
    }

    /**
     * MANAGER bắt buộc phải có nhóm kinh doanh.
     */
    @Test
    void updateRoleAndGroup_shouldRejectManagerWithoutGroup() {

        assertThrows(
                IllegalArgumentException.class,
                () -> roleService.updateRoleAndGroup(
                        1L,
                        2L,
                        List.of("MANAGER"),
                        null
                )
        );

        assertFalse(roleDAO.replaceCalled);

        assertEquals(
                0,
                businessGroupDAO.assignCalls
        );
    }

    /**
     * Không được gán groupId không tồn tại.
     *
     * Quan trọng:
     * role chưa được thay đổi nếu group sai.
     */
    @Test
    void updateRoleAndGroup_shouldRejectNonExistingGroup() {

        assertThrows(
                IllegalArgumentException.class,
                () -> roleService.updateRoleAndGroup(
                        1L,
                        2L,
                        List.of("SALES"),
                        999L
                )
        );

        assertFalse(roleDAO.replaceCalled);

        assertEquals(
                0,
                businessGroupDAO.assignCalls
        );
    }

    /**
     * Admin không được tự thu hồi ADMIN của chính mình.
     */
    @Test
    void updateRoleAndGroup_shouldPreventAdminRemovingOwnAdminRole() {

        roleDAO.setUserRoles(
                1L,
                Set.of("ADMIN")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> roleService.updateRoleAndGroup(
                        1L,
                        1L,
                        List.of("SALES"),
                        null
                )
        );

        assertFalse(roleDAO.replaceCalled);
    }

    /**
     * Admin vẫn được giữ ADMIN và thêm role khác cho chính mình.
     */
    @Test
    void updateRoleAndGroup_shouldAllowAdminKeepingOwnAdminRole()
            throws SQLException {

        roleDAO.setUserRoles(
                1L,
                Set.of("ADMIN")
        );

        roleService.updateRoleAndGroup(
                1L,
                1L,
                List.of(
                        "ADMIN",
                        "SALES"
                ),
                null
        );

        assertTrue(roleDAO.replaceCalled);

        assertEquals(
                List.of(
                        "ADMIN",
                        "SALES"
                ),
                roleDAO.replacedRoles
        );
    }

    /**
     * Khi user không còn MANAGER và không chọn nhóm,
     * quan hệ nhóm kinh doanh cũ phải được gỡ.
     */
    @Test
    void updateRoleAndGroup_shouldRemoveGroupWhenUserIsNotManager()
            throws SQLException {

        roleService.updateRoleAndGroup(
                1L,
                2L,
                List.of("SALES"),
                null
        );

        assertTrue(roleDAO.replaceCalled);

        assertEquals(
                1,
                businessGroupDAO.removeCalls
        );

        assertEquals(
                2L,
                businessGroupDAO.removedUserId
        );

        assertEquals(
                0,
                businessGroupDAO.assignCalls
        );
    }

    /**
     * Role không tồn tại phải bị từ chối.
     */
    @Test
    void updateRoleAndGroup_shouldRejectUnknownRole() {

        assertThrows(
                IllegalArgumentException.class,
                () -> roleService.updateRoleAndGroup(
                        1L,
                        2L,
                        List.of("SUPER_ADMIN"),
                        null
                )
        );

        assertFalse(roleDAO.replaceCalled);
    }

    /**
     * Danh sách role null không hợp lệ.
     */
    @Test
    void updateRoleAndGroup_shouldRejectNullRoles() {

        assertThrows(
                IllegalArgumentException.class,
                () -> roleService.updateRoleAndGroup(
                        1L,
                        2L,
                        null,
                        null
                )
        );

        assertFalse(roleDAO.replaceCalled);
    }

    /**
     * Danh sách role rỗng không hợp lệ.
     */
    @Test
    void updateRoleAndGroup_shouldRejectEmptyRoles() {

        assertThrows(
                IllegalArgumentException.class,
                () -> roleService.updateRoleAndGroup(
                        1L,
                        2L,
                        List.of(),
                        null
                )
        );

        assertFalse(roleDAO.replaceCalled);
    }

    /**
     * currentAdminUserId không hợp lệ.
     */
    @Test
    void updateRoleAndGroup_shouldRejectInvalidCurrentAdminId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> roleService.updateRoleAndGroup(
                        0L,
                        2L,
                        List.of("SALES"),
                        null
                )
        );

        assertFalse(roleDAO.replaceCalled);
    }

    /**
     * targetUserId không hợp lệ.
     */
    @Test
    void updateRoleAndGroup_shouldRejectInvalidTargetUserId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> roleService.updateRoleAndGroup(
                        1L,
                        0L,
                        List.of("SALES"),
                        null
                )
        );

        assertFalse(roleDAO.replaceCalled);
    }


    // =========================================================
    // FAKE ROLE DAO
    // =========================================================

    private static class FakeRoleDAO
            extends RoleDAO {

        private final Set<String> validRoleCodes =
                new HashSet<>(
                        Set.of(
                                "ADMIN",
                                "MANAGER",
                                "SALES"
                        )
                );

        private final Map<Long, Set<String>>
                rolesByUser =
                new HashMap<>();

        private boolean replaceCalled;

        private long replacedUserId = -1L;

        private List<String> replacedRoles =
                new ArrayList<>();

        private String lastCheckedRoleCode;

        @Override
        public List<Role> findAll() {

            return List.of(
                    new Role(
                            "ADMIN",
                            "Quản trị hệ thống"
                    ),
                    new Role(
                            "MANAGER",
                            "Quản lý kinh doanh"
                    ),
                    new Role(
                            "SALES",
                            "Nhân viên kinh doanh"
                    )
            );
        }

        @Override
        public List<Role> findAllByUserId(
                long userId
        ) {

            Set<String> roleCodes =
                    rolesByUser.getOrDefault(
                            userId,
                            Set.of()
                    );

            List<Role> result =
                    new ArrayList<>();

            for (String roleCode : roleCodes) {

                result.add(
                        new Role(
                                roleCode,
                                roleCode
                        )
                );
            }

            return result;
        }

        @Override
        public boolean userHasRole(
                long userId,
                String roleCode
        ) {

            lastCheckedRoleCode =
                    roleCode;

            return rolesByUser
                    .getOrDefault(
                            userId,
                            Set.of()
                    )
                    .contains(roleCode);
        }

        @Override
        public boolean existsByCode(
                String roleCode
        ) {

            return validRoleCodes.contains(
                    roleCode
            );
        }

        @Override
        public void replaceUserRoles(
                long userId,
                Collection<String> roleCodes
        ) {

            replaceCalled = true;

            replacedUserId =
                    userId;

            replacedRoles =
                    new ArrayList<>(
                            roleCodes
                    );

            rolesByUser.put(
                    userId,
                    new LinkedHashSetForTest(
                            roleCodes
                    )
            );
        }

        private void setUserRoles(
                long userId,
                Set<String> roles
        ) {

            rolesByUser.put(
                    userId,
                    new HashSet<>(roles)
            );
        }
    }


    // =========================================================
    // FAKE BUSINESS GROUP DAO
    // =========================================================

    private static class FakeBusinessGroupDAO
            extends BusinessGroupDAO {

        private final Set<Long> existingGroups =
                new HashSet<>(
                        Set.of(
                                1L,
                                2L
                        )
                );

        private int assignCalls;

        private int removeCalls;

        private long assignedUserId = -1L;

        private long assignedGroupId = -1L;

        private long removedUserId = -1L;

        @Override
        public boolean existsById(
                long groupId
        ) {

            return existingGroups.contains(
                    groupId
            );
        }

        @Override
        public void assignUserToGroup(
                long userId,
                long groupId
        ) {

            assignCalls++;

            assignedUserId =
                    userId;

            assignedGroupId =
                    groupId;
        }

        @Override
        public void removeUserFromGroup(
                long userId
        ) {

            removeCalls++;

            removedUserId =
                    userId;
        }
    }


    // =========================================================
    // HELPER
    // =========================================================

    private static class LinkedHashSetForTest
            extends java.util.LinkedHashSet<String> {

        LinkedHashSetForTest(
                Collection<String> values
        ) {
            super(values);
        }
    }
}