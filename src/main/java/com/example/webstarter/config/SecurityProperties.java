package com.example.webstarter.config;

import java.time.Duration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.security")
public record SecurityProperties(
        @Valid @NotNull Password password,
        @Valid @NotNull LoginProtection loginProtection,
        @Valid @NotNull Registration registration,
        @Valid @NotNull Session session,
        @Valid @NotNull Headers headers) {

    public record Password(
            @NotBlank String algorithm,
            @Min(8) @Max(128) int minLength,
            @Min(32) @Max(1024) int maxLength,
            @Valid @NotNull Argon2 argon2,
            @Min(10) @Max(16) int bcryptStrength) {

        public Password {
            if (minLength > maxLength) {
                throw new IllegalArgumentException("Password minimum length cannot exceed maximum length");
            }
        }
    }

    public record Argon2(
            @Min(8) @Max(64) int saltLength,
            @Min(16) @Max(128) int hashLength,
            @Min(1) @Max(16) int parallelism,
            @Min(8192) @Max(1048576) int memoryKib,
            @Min(1) @Max(20) int iterations) {
    }

    public record LoginProtection(
            @Min(2) @Max(100) int attemptsPerWindow,
            @Min(5) @Max(1000) int ipAttemptsPerWindow,
            @NotNull Duration window,
            @NotNull Duration blockDuration,
            @Min(1000) @Max(1000000) long maxTrackedKeys,
            @Min(2) @Max(100) int accountLockThreshold,
            @NotNull Duration accountLockDuration) {
    }

    public record Registration(boolean enabled) {
    }

    public record Session(
            @Min(1) @Max(100) int maximumConcurrentSessions,
            @Min(100) @Max(1000000) int metadataMaxEntries) {
    }

    public record Headers(
            @NotBlank String contentSecurityPolicy,
            @NotBlank String permissionsPolicy,
            @NotBlank String referrerPolicy,
            @NotNull FrameOptions frameOptions,
            @NotNull Duration hstsMaxAge,
            boolean hstsIncludeSubdomains,
            boolean hstsPreload) {
    }

    public enum FrameOptions {
        DENY,
        SAMEORIGIN
    }
}
