package com.example.webstarter.navigation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import com.example.webstarter.user.Role;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser(username = "menu-admin", roles = "ADMIN")
class NavigationMenuServiceIntegrationTest {

    @Autowired
    private NavigationMenuService menuService;

    @Autowired
    private NavigationMenuRepository menuRepository;

    @BeforeEach
    void clearMenus() {
        menuRepository.deleteAll();
    }

    @Test
    void visibleMenusHonorRoleAndConfiguredOrder() {
        menuService.create(form("reports", "/reports", Role.USER, 200));
        menuService.create(form("dashboard", "/dashboard", Role.USER, 100));
        menuService.create(form("administration", "/admin/users", Role.ADMIN, 150));

        Authentication user = new UsernamePasswordAuthenticationToken(
                "member",
                "password",
                List.of(new SimpleGrantedAuthority(Role.USER.authority())));

        assertThat(menuService.visibleMenus(user))
                .extracting(NavigationMenuSummary::menuKey)
                .containsExactly("dashboard", "reports");
    }

    @Test
    void externalMenuPathIsRejectedEvenWhenServiceIsCalledDirectly() {
        NavigationMenuForm form = form("external", "/safe", Role.USER, 100);
        form.setPath("https://example.com/account");

        assertThatThrownBy(() -> menuService.create(form))
                .isInstanceOf(NavigationMenuOperationException.class)
                .hasMessageContaining("내부 절대 경로");
    }

    @Test
    void workspaceShellCannotBeRegisteredAsRecursiveMenu() {
        NavigationMenuForm form = form("recursive", "/workspace", Role.USER, 100);

        assertThatThrownBy(() -> menuService.create(form))
                .isInstanceOf(NavigationMenuOperationException.class)
                .hasMessageContaining("업무 셸");
    }

    @Test
    void toggledMenuDisappearsFromVisibleNavigation() {
        NavigationMenuSummary created = menuService.create(form("dashboard", "/dashboard", Role.USER, 100));
        menuService.toggleEnabled(created.id());
        Authentication user = new UsernamePasswordAuthenticationToken(
                "member",
                "password",
                List.of(new SimpleGrantedAuthority(Role.USER.authority())));

        assertThat(menuService.visibleMenus(user)).isEmpty();
    }

    private NavigationMenuForm form(String key, String path, Role role, int displayOrder) {
        NavigationMenuForm form = new NavigationMenuForm();
        form.setMenuKey(key);
        form.setLabel(key);
        form.setMenuGroup("테스트");
        form.setPath(path);
        form.setIcon(NavigationIcon.DOCUMENT);
        form.setRequiredRole(role);
        form.setDisplayOrder(displayOrder);
        form.setEnabled(true);
        return form;
    }
}
