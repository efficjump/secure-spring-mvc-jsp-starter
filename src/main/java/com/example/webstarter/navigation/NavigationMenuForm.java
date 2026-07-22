package com.example.webstarter.navigation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.example.webstarter.user.Role;

public class NavigationMenuForm {

    @NotBlank
    @Pattern(regexp = "[a-z][a-z0-9-]{1,39}")
    private String menuKey;

    @NotBlank
    @Size(max = 80)
    private String label;

    @NotBlank
    @Size(max = 60)
    private String menuGroup;

    @NotBlank
    @Size(max = 180)
    @Pattern(regexp = "/(?:[A-Za-z0-9._~-]+(?:/[A-Za-z0-9._~-]+)*)?")
    private String path;

    @NotNull
    private NavigationIcon icon = NavigationIcon.DOCUMENT;

    @NotNull
    private Role requiredRole = Role.USER;

    @Min(0)
    @Max(9999)
    private int displayOrder;

    private boolean enabled = true;

    public String getMenuKey() {
        return menuKey;
    }

    public void setMenuKey(String menuKey) {
        this.menuKey = menuKey == null ? null : menuKey.strip();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label == null ? null : label.strip();
    }

    public String getMenuGroup() {
        return menuGroup;
    }

    public void setMenuGroup(String menuGroup) {
        this.menuGroup = menuGroup == null ? null : menuGroup.strip();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path == null ? null : path.strip();
    }

    public NavigationIcon getIcon() {
        return icon;
    }

    public void setIcon(NavigationIcon icon) {
        this.icon = icon;
    }

    public Role getRequiredRole() {
        return requiredRole;
    }

    public void setRequiredRole(Role requiredRole) {
        this.requiredRole = requiredRole;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
