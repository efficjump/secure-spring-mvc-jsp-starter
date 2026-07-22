package com.example.webstarter.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

class InMemoryLoginAttemptGuardTest {

    @Test
    void blocksIdentityWithinWindowAndReleasesAfterDuration() {
        MutableClock clock = new MutableClock(Instant.parse("2026-07-22T00:00:00Z"));
        InMemoryLoginAttemptGuard guard =
                new InMemoryLoginAttemptGuard(SecurityPropertiesFixture.properties(), clock);

        guard.recordFailure("192.0.2.10", "member");
        guard.recordFailure("192.0.2.11", "member");
        assertThat(guard.isBlocked("192.0.2.12", "member")).isFalse();

        guard.recordFailure("192.0.2.12", "member");
        assertThat(guard.isBlocked("192.0.2.13", "member")).isTrue();

        clock.advance(Duration.ofMinutes(16));
        assertThat(guard.isBlocked("192.0.2.13", "member")).isFalse();
    }

    @Test
    void successfulLoginClearsIdentityThrottleWithoutClearingIpThrottle() {
        MutableClock clock = new MutableClock(Instant.parse("2026-07-22T00:00:00Z"));
        InMemoryLoginAttemptGuard guard =
                new InMemoryLoginAttemptGuard(SecurityPropertiesFixture.properties(), clock);

        for (int attempt = 0; attempt < 3; attempt++) {
            guard.recordFailure("192.0.2.20", "member");
        }
        assertThat(guard.isBlocked("192.0.2.21", "member")).isTrue();

        guard.recordSuccess("192.0.2.20", "member");
        assertThat(guard.isBlocked("192.0.2.21", "member")).isFalse();
    }
}

