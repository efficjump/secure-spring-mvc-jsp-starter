package com.example.webstarter.security;

import java.time.Duration;

import com.example.webstarter.config.SecurityProperties;

final class SecurityPropertiesFixture {

    private SecurityPropertiesFixture() {
    }

    static SecurityProperties properties() {
        return new SecurityProperties(
                new SecurityProperties.Password(
                        "argon2",
                        12,
                        128,
                        new SecurityProperties.Argon2(16, 32, 1, 19456, 2),
                        12),
                new SecurityProperties.LoginProtection(
                        3,
                        10,
                        Duration.ofMinutes(15),
                        Duration.ofMinutes(15),
                        10_000,
                        5,
                        Duration.ofMinutes(15)),
                new SecurityProperties.Registration(false),
                new SecurityProperties.Session(2, 10_000),
                new SecurityProperties.Headers(
                        "default-src 'self'",
                        "camera=()",
                        "strict-origin-when-cross-origin",
                        SecurityProperties.FrameOptions.SAMEORIGIN,
                        Duration.ofDays(365),
                        true,
                        false));
    }
}
