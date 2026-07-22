package com.example.webstarter.user;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64, unique = true)
    private String username;

    @Column(nullable = false, length = 254, unique = true)
    private String email;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "password_changed_at", nullable = false)
    private Instant passwordChangedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "app_user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role_name", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = EnumSet.noneOf(Role.class);

    protected AppUser() {
    }

    private AppUser(
            String username,
            String email,
            String displayName,
            String passwordHash,
            Set<Role> roles,
            Instant now) {
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
        this.roles = EnumSet.copyOf(roles);
        this.enabled = true;
        this.passwordChangedAt = now;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static AppUser create(
            String username,
            String email,
            String displayName,
            String passwordHash,
            Set<Role> roles,
            Instant now) {
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("At least one role is required");
        }
        return new AppUser(username, email, displayName, passwordHash, roles, now);
    }

    public void recordLoginFailure(Instant now, int lockThreshold, Duration lockDuration) {
        failedLoginAttempts = Math.addExact(failedLoginAttempts, 1);
        if (failedLoginAttempts >= lockThreshold) {
            lockedUntil = now.plus(lockDuration);
            failedLoginAttempts = 0;
        }
        updatedAt = now;
    }

    public void recordLoginSuccess(Instant now) {
        failedLoginAttempts = 0;
        lockedUntil = null;
        lastLoginAt = now;
        updatedAt = now;
    }

    public void unlock(Instant now) {
        failedLoginAttempts = 0;
        lockedUntil = null;
        updatedAt = now;
    }

    public void updatePassword(String newPasswordHash, Instant now) {
        passwordHash = newPasswordHash;
        passwordChangedAt = now;
        failedLoginAttempts = 0;
        lockedUntil = null;
        updatedAt = now;
    }

    public void setEnabled(boolean enabled, Instant now) {
        this.enabled = enabled;
        if (!enabled) {
            failedLoginAttempts = 0;
            lockedUntil = null;
        }
        updatedAt = now;
    }

    public void setPrimaryRole(Role role, Instant now) {
        roles.clear();
        roles.add(Role.USER);
        if (role == Role.ADMIN) {
            roles.add(Role.ADMIN);
        }
        updatedAt = now;
    }

    public boolean isAccountNonLocked(Instant now) {
        return lockedUntil == null || !lockedUntil.isAfter(now);
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public Instant getPasswordChangedAt() {
        return passwordChangedAt;
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

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }
}

