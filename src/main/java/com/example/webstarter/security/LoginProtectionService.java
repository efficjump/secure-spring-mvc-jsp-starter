package com.example.webstarter.security;

import java.time.Clock;
import java.time.Instant;

import com.example.webstarter.config.SecurityProperties;
import com.example.webstarter.user.AppUser;
import com.example.webstarter.user.AppUserRepository;
import com.example.webstarter.user.UserIdentityNormalizer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginProtectionService {

    private final AppUserRepository userRepository;
    private final LoginAttemptGuard attemptGuard;
    private final SecurityProperties.LoginProtection properties;
    private final Clock clock;

    public LoginProtectionService(
            AppUserRepository userRepository,
            LoginAttemptGuard attemptGuard,
            SecurityProperties securityProperties,
            Clock clock) {
        this.userRepository = userRepository;
        this.attemptGuard = attemptGuard;
        this.properties = securityProperties.loginProtection();
        this.clock = clock;
    }

    public boolean isRateLimited(String ipAddress, String username) {
        return attemptGuard.isBlocked(ipAddress, username);
    }

    public void recordRejectedInput(String ipAddress) {
        attemptGuard.recordFailure(ipAddress, "");
    }

    @Transactional
    public void recordFailure(String ipAddress, String username) {
        attemptGuard.recordFailure(ipAddress, username);
        String normalizedUsername = UserIdentityNormalizer.username(username);
        if (normalizedUsername.isBlank()) {
            return;
        }

        Instant now = clock.instant();
        userRepository.findByUsernameForUpdate(normalizedUsername).ifPresent(user -> {
            if (user.isEnabled() && user.isAccountNonLocked(now)) {
                user.recordLoginFailure(now, properties.accountLockThreshold(), properties.accountLockDuration());
            }
        });
    }

    @Transactional
    public void recordSuccess(String ipAddress, String username) {
        attemptGuard.recordSuccess(ipAddress, username);
        String normalizedUsername = UserIdentityNormalizer.username(username);
        AppUser user = userRepository.findByUsernameForUpdate(normalizedUsername)
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));
        user.recordLoginSuccess(clock.instant());
    }
}
