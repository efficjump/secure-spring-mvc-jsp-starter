package com.example.webstarter.navigation;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import com.example.webstarter.user.Role;

@Entity
@Table(name = "navigation_menus")
public class NavigationMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "menu_key", nullable = false, unique = true, length = 40)
    private String menuKey;

    @Column(nullable = false, length = 80)
    private String label;

    @Column(name = "menu_group", nullable = false, length = 60)
    private String menuGroup;

    @Column(nullable = false, length = 180)
    private String path;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private NavigationIcon icon;

    @Enumerated(EnumType.STRING)
    @Column(name = "required_role", nullable = false, length = 32)
    private Role requiredRole;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected NavigationMenu() {
    }

    private NavigationMenu(
            String menuKey,
            String label,
            String menuGroup,
            String path,
            NavigationIcon icon,
            Role requiredRole,
            int displayOrder,
            boolean enabled,
            Instant now) {
        this.menuKey = menuKey;
        this.label = label;
        this.menuGroup = menuGroup;
        this.path = path;
        this.icon = icon;
        this.requiredRole = requiredRole;
        this.displayOrder = displayOrder;
        this.enabled = enabled;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static NavigationMenu create(NavigationMenuForm form, Instant now) {
        return new NavigationMenu(
                form.getMenuKey(),
                form.getLabel(),
                form.getMenuGroup(),
                form.getPath(),
                form.getIcon(),
                form.getRequiredRole(),
                form.getDisplayOrder(),
                form.isEnabled(),
                now);
    }

    public void update(NavigationMenuForm form, Instant now) {
        menuKey = form.getMenuKey();
        label = form.getLabel();
        menuGroup = form.getMenuGroup();
        path = form.getPath();
        icon = form.getIcon();
        requiredRole = form.getRequiredRole();
        displayOrder = form.getDisplayOrder();
        enabled = form.isEnabled();
        updatedAt = now;
    }

    public void toggleEnabled(Instant now) {
        enabled = !enabled;
        updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public String getMenuKey() {
        return menuKey;
    }

    public String getLabel() {
        return label;
    }

    public String getMenuGroup() {
        return menuGroup;
    }

    public String getPath() {
        return path;
    }

    public NavigationIcon getIcon() {
        return icon;
    }

    public Role getRequiredRole() {
        return requiredRole;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }
}
