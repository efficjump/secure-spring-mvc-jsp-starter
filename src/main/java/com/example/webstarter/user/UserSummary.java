package com.example.webstarter.user;

import java.time.Instant;
import java.util.Set;

public record UserSummary(
        Long id,
        String username,
        String email,
        String displayName,
        boolean enabled,
        boolean locked,
        Instant lockedUntil,
        Instant lastLoginAt,
        Instant createdAt,
        boolean admin,
        Set<Role> roles) {

    public static UserSummary from(AppUser user, Instant now) {
        return new UserSummary(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.isEnabled(),
                !user.isAccountNonLocked(now),
                user.getLockedUntil(),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.hasRole(Role.ADMIN),
                Set.copyOf(user.getRoles()));
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isLocked() {
        return locked;
    }
}
