package com.example.webstarter.security;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import com.example.webstarter.audit.RequestMetadata;
import com.example.webstarter.config.SecurityProperties;

import org.springframework.context.event.EventListener;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.stereotype.Component;

@Component
public class SessionMetadataStore {

    private static final int LAST_PATH_MAX_LENGTH = 256;

    private final ConcurrentMap<String, SessionMetadata> sessions = new ConcurrentHashMap<>();
    private final int maxEntries;
    private final Clock clock;

    public SessionMetadataStore(SecurityProperties properties, Clock clock) {
        this.maxEntries = properties.session().metadataMaxEntries();
        this.clock = clock;
    }

    public void register(HttpServletRequest request, String username) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        String sessionId = session.getId();
        if (!sessions.containsKey(sessionId) && sessions.size() >= maxEntries) {
            evictOldestMetadata();
        }
        RequestMetadata requestMetadata = RequestMetadata.from(request);
        Instant now = clock.instant();
        sessions.put(sessionId, new SessionMetadata(
                username,
                requestMetadata.ipAddress(),
                requestMetadata.userAgent(),
                now,
                now,
                cleanPath(request.getRequestURI())));
    }

    public void touch(String sessionId, String path) {
        Instant now = clock.instant();
        String safePath = cleanPath(path);
        sessions.computeIfPresent(sessionId, (ignored, metadata) -> metadata.touch(now, safePath));
    }

    public Optional<SessionMetadata> find(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    public void remove(String sessionId) {
        sessions.remove(sessionId);
    }

    @EventListener
    public void onSessionDestroyed(SessionDestroyedEvent event) {
        remove(event.getId());
    }

    private void evictOldestMetadata() {
        sessions.entrySet().stream()
                .min(Comparator.comparing(entry -> entry.getValue().lastSeenAt()))
                .map(Map.Entry::getKey)
                .ifPresent(sessions::remove);
    }

    private String cleanPath(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        String cleaned = path.replace('\r', ' ').replace('\n', ' ').strip();
        return cleaned.length() <= LAST_PATH_MAX_LENGTH
                ? cleaned
                : cleaned.substring(0, LAST_PATH_MAX_LENGTH);
    }
}
