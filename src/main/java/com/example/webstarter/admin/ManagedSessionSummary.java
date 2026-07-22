package com.example.webstarter.admin;

import java.time.Instant;

public record ManagedSessionSummary(
        String terminationToken,
        String fingerprint,
        String username,
        String ipAddress,
        String userAgent,
        Instant signedInAt,
        Instant lastSeenAt,
        String lastPath,
        boolean current,
        boolean expired) {

    public boolean isCurrent() {
        return current;
    }

    public boolean isExpired() {
        return expired;
    }
}
