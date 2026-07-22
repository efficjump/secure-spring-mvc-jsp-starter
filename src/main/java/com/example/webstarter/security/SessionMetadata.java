package com.example.webstarter.security;

import java.time.Instant;

public record SessionMetadata(
        String username,
        String ipAddress,
        String userAgent,
        Instant signedInAt,
        Instant lastSeenAt,
        String lastPath) {

    SessionMetadata touch(Instant now, String path) {
        return new SessionMetadata(
                username,
                ipAddress,
                userAgent,
                signedInAt,
                now,
                path == null ? lastPath : path);
    }
}
