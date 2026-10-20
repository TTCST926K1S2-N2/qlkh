package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationServiceTest {

    private final AuthorizationService authorizationService =
            new AuthorizationService();

    @Test
    void salesCannotAccessUserManagement() {
        assertFalse(
                authorizationService.canAccess(
                        "SALES",
                        "/users"
                )
        );
    }

    @Test
    void salesCannotAccessRoleManagement() {
        assertFalse(
                authorizationService.canAccess(
                        "SALES",
                        "/roles"
                )
        );
    }

    @Test
    void salesCannotAccessAccountHandover() {
        assertFalse(
                authorizationService.canAccess(
                        "SALES",
                        "/lock-transfer"
                )
        );
    }

    @Test
    void managerCannotAccessAdminFunctions() {
        assertFalse(
                authorizationService.canAccess(
                        "MANAGER",
                        "/users"
                )
        );

        assertFalse(
                authorizationService.canAccess(
                        "MANAGER",
                        "/roles"
                )
        );

        assertFalse(
                authorizationService.canAccess(
                        "MANAGER",
                        "/lock-transfer"
                )
        );
    }

    @Test
    void adminCanAccessAdminFunctions() {
        assertTrue(
                authorizationService.canAccess(
                        "ADMIN",
                        "/users"
                )
        );

        assertTrue(
                authorizationService.canAccess(
                        "ADMIN",
                        "/roles"
                )
        );

        assertTrue(
                authorizationService.canAccess(
                        "ADMIN",
                        "/lock-transfer"
                )
        );
    }

    @Test
    void adminCanAccessNestedAdminUrl() {
        assertTrue(
                authorizationService.canAccess(
                        "ADMIN",
                        "/users/123/edit"
                )
        );
    }

    @Test
    void salesCanAccessNormalFunctions() {
        assertTrue(
                authorizationService.canAccess(
                        "SALES",
                        "/customers"
                )
        );

        assertTrue(
                authorizationService.canAccess(
                        "SALES",
                        "/permission"
                )
        );

        assertTrue(
                authorizationService.canAccess(
                        "SALES",
                        "/change-password"
                )
        );
    }

    @Test
    void managerCanAccessNormalFunctions() {
        assertTrue(
                authorizationService.canAccess(
                        "MANAGER",
                        "/customers"
                )
        );

        assertTrue(
                authorizationService.canAccess(
                        "MANAGER",
                        "/permission"
                )
        );
    }

    @Test
    void roleIsCaseInsensitiveAndTrimmed() {
        assertTrue(
                authorizationService.canAccess(
                        "  admin  ",
                        "/users"
                )
        );

        assertFalse(
                authorizationService.canAccess(
                        " manager ",
                        "/users"
                )
        );
    }

    @Test
    void unknownRoleHasNoAccess() {
        assertFalse(
                authorizationService.canAccess(
                        "UNKNOWN",
                        "/customers"
                )
        );

        assertFalse(
                authorizationService.canAccess(
                        null,
                        "/customers"
                )
        );
    }
}