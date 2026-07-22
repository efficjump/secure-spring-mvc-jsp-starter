package com.example.webstarter.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import com.example.webstarter.config.SecurityProperties;
import com.example.webstarter.user.UserIdentityNormalizer;

import org.springframework.stereotype.Component;

@Component
public class InMemoryLoginAttemptGuard implements LoginAttemptGuard {

    private final SecurityProperties.LoginProtection properties;
    private final Clock clock;
    private final Cache<String, AttemptState> attempts;

    public InMemoryLoginAttemptGuard(SecurityProperties securityProperties, Clock clock) {
        this.properties = securityProperties.loginProtection();
        this.clock = clock;
        Duration retention = properties.window().plus(properties.blockDuration()).multipliedBy(2);
        this.attempts = Caffeine.newBuilder()
                .maximumSize(properties.maxTrackedKeys())
                .expireAfterAccess(retention)
                .build();
    }

    @Override
    public boolean isBlocked(String ipAddress, String username) {
        Instant now = clock.instant();
        return isBlocked(ipKey(ipAddress), now) || isBlocked(identityKey(username), now);
    }

    @Override
    public void recordFailure(String ipAddress, String username) {
        Instant now = clock.instant();
        update(ipKey(ipAddress), properties.ipAttemptsPerWindow(), now);
        update(identityKey(username), properties.attemptsPerWindow(), now);
    }

    @Override
    public void recordSuccess(String ipAddress, String username) {
        attempts.invalidate(identityKey(username));
    }

    private boolean isBlocked(String key, Instant now) {
        AttemptState state = attempts.getIfPresent(key);
        return state != null && state.blockedUntil() != null && state.blockedUntil().isAfter(now);
    }

    private void update(String key, int threshold, Instant now) {
        attempts.asMap().compute(key, (ignored, previous) -> next(previous, threshold, now));
    }

    private AttemptState next(AttemptState previous, int threshold, Instant now) {
        if (previous != null && previous.blockedUntil() != null && previous.blockedUntil().isAfter(now)) {
            return previous;
        }
        if (previous == null || !previous.windowStartedAt().plus(properties.window()).isAfter(now)) {
            return new AttemptState(now, 1, null);
        }

        int failures = previous.failures() + 1;
        Instant blockedUntil = failures >= threshold ? now.plus(properties.blockDuration()) : null;
        return new AttemptState(previous.windowStartedAt(), failures, blockedUntil);
    }

    private String ipKey(String ipAddress) {
        String value = ipAddress == null || ipAddress.isBlank() ? "unknown" : ipAddress.strip();
        return digest("ip:" + value);
    }

    private String identityKey(String username) {
        return digest("identity:" + UserIdentityNormalizer.username(username));
    }

    private String digest(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required by the Java platform", exception);
        }
    }

    private record AttemptState(Instant windowStartedAt, int failures, Instant blockedUntil) {
    }
}

