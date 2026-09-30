package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.model.DataScope;

import static org.junit.jupiter.api.Assertions.*;

class PermissionServiceTest {

    private final PermissionService permissionService =
            new PermissionService();

    @Test
    void employeeA_canAccessCustomerOwnedByEmployeeA() {
        long employeeAId = 1L;

        boolean allowed = permissionService.canAccess(
                DataScope.MY,
                employeeAId,
                10L,
                employeeAId,
                10L
        );

        assertTrue(allowed);
    }

    @Test
    void employeeA_cannotAccessCustomerOwnedByEmployeeB() {
        long employeeAId = 1L;
        long employeeBId = 2L;

        boolean allowed = permissionService.canAccess(
                DataScope.MY,
                employeeAId,
                10L,
                employeeBId,
                10L
        );

        assertFalse(
                allowed,
                "Nhân viên A không được đọc khách hàng thuộc sở hữu của nhân viên B."
        );
    }

    @Test
    void manager_canAccessDataInSameTeam() {
        boolean allowed = permissionService.canAccess(
                DataScope.TEAM,
                1L,
                10L,
                2L,
                10L
        );

        assertTrue(allowed);
    }

    @Test
    void manager_cannotAccessDataInDifferentTeam() {
        boolean allowed = permissionService.canAccess(
                DataScope.TEAM,
                1L,
                10L,
                2L,
                20L
        );

        assertFalse(allowed);
    }

    @Test
    void adminWithAllScope_canAccessAnyData() {
        boolean allowed = permissionService.canAccess(
                DataScope.ALL,
                1L,
                null,
                999L,
                null
        );

        assertTrue(allowed);
    }

    @Test
    void nullScope_deniesAccess() {
        boolean allowed = permissionService.canAccess(
                null,
                1L,
                10L,
                1L,
                10L
        );

        assertFalse(allowed);
    }
    @Test
void sales_hasMyScope() {
    assertEquals(
            DataScope.MY,
            permissionService.resolveDataScope("SALES")
    );
}

@Test
void manager_hasTeamScope() {
    assertEquals(
            DataScope.TEAM,
            permissionService.resolveDataScope("MANAGER")
    );
}

@Test
void admin_hasAllScope() {
    assertEquals(
            DataScope.ALL,
            permissionService.resolveDataScope("ADMIN")
    );
}

@Test
void unknownRole_hasNoScope() {
    assertNull(
            permissionService.resolveDataScope("UNKNOWN")
    );
}
}
