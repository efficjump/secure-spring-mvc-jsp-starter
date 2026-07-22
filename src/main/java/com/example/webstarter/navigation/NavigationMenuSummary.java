package com.example.webstarter.navigation;

import com.example.webstarter.user.Role;

public record NavigationMenuSummary(
        Long id,
        String menuKey,
        String label,
        String menuGroup,
        String path,
        NavigationIcon icon,
        Role requiredRole,
        int displayOrder,
        boolean enabled) {

    public static NavigationMenuSummary from(NavigationMenu menu) {
        return new NavigationMenuSummary(
                menu.getId(),
                menu.getMenuKey(),
                menu.getLabel(),
                menu.getMenuGroup(),
                menu.getPath(),
                menu.getIcon(),
                menu.getRequiredRole(),
                menu.getDisplayOrder(),
                menu.isEnabled());
    }

    public boolean isEnabled() {
        return enabled;
    }
}
