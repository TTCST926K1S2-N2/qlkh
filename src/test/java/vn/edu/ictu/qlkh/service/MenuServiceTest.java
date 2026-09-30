package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MenuServiceTest {

    private MenuService menuService;

    @BeforeEach
    void setUp() {
        menuService = new MenuService();
    }

    @Test
    void salesShouldOnlySeeAllowedMenus() {

        MenuService.MenuPermissions permissions =
                menuService.getMenuPermissions("SALES");

        assertTrue(permissions.isHome());
        assertTrue(permissions.isCustomerManagement());
        assertTrue(permissions.isProfile());
        assertTrue(permissions.isChangePassword());

        assertFalse(permissions.isUserManagement());
        assertFalse(permissions.isRoleGroupManagement());
        assertFalse(permissions.isAccountHandover());
    }

    @Test
    void managerShouldOnlySeeAllowedMenus() {

        MenuService.MenuPermissions permissions =
                menuService.getMenuPermissions("MANAGER");

        assertTrue(permissions.isHome());
        assertTrue(permissions.isCustomerManagement());
        assertTrue(permissions.isProfile());
        assertTrue(permissions.isChangePassword());

        assertFalse(permissions.isUserManagement());
        assertFalse(permissions.isRoleGroupManagement());
        assertFalse(permissions.isAccountHandover());
    }

    @Test
    void adminShouldSeeAllMenus() {

        MenuService.MenuPermissions permissions =
                menuService.getMenuPermissions("ADMIN");

        assertTrue(permissions.isHome());
        assertTrue(permissions.isCustomerManagement());
        assertTrue(permissions.isUserManagement());
        assertTrue(permissions.isRoleGroupManagement());
        assertTrue(permissions.isAccountHandover());
        assertTrue(permissions.isProfile());
        assertTrue(permissions.isChangePassword());
    }

    @Test
    void unknownRoleShouldNotSeeAnyProtectedMenu() {

        MenuService.MenuPermissions permissions =
                menuService.getMenuPermissions("UNKNOWN");

        assertFalse(permissions.isHome());
        assertFalse(permissions.isCustomerManagement());
        assertFalse(permissions.isUserManagement());
        assertFalse(permissions.isRoleGroupManagement());
        assertFalse(permissions.isAccountHandover());
        assertFalse(permissions.isProfile());
        assertFalse(permissions.isChangePassword());
    }

    @Test
    void nullRoleShouldNotSeeAnyProtectedMenu() {

        MenuService.MenuPermissions permissions =
                menuService.getMenuPermissions(null);

        assertFalse(permissions.isHome());
        assertFalse(permissions.isCustomerManagement());
        assertFalse(permissions.isUserManagement());
        assertFalse(permissions.isRoleGroupManagement());
        assertFalse(permissions.isAccountHandover());
        assertFalse(permissions.isProfile());
        assertFalse(permissions.isChangePassword());
    }

    @Test
    void roleShouldBeCaseInsensitiveAndTrimmed() {

        MenuService.MenuPermissions permissions =
                menuService.getMenuPermissions("  admin  ");

        assertTrue(permissions.isHome());
        assertTrue(permissions.isCustomerManagement());
        assertTrue(permissions.isUserManagement());
        assertTrue(permissions.isRoleGroupManagement());
        assertTrue(permissions.isAccountHandover());
    }

    @Test
    void supportedRolesShouldBeRecognized() {

        assertTrue(menuService.isSupportedRole("SALES"));
        assertTrue(menuService.isSupportedRole("MANAGER"));
        assertTrue(menuService.isSupportedRole("ADMIN"));

        assertFalse(menuService.isSupportedRole("UNKNOWN"));
        assertFalse(menuService.isSupportedRole(null));
        assertFalse(menuService.isSupportedRole(""));
    }
}