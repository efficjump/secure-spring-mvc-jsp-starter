package com.example.webstarter.admin;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import com.example.webstarter.security.SessionMetadata;
import com.example.webstarter.security.SessionMetadataStore;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class AdminSessionService {

    private final SessionRegistry sessionRegistry;
    private final SessionMetadataStore metadataStore;

    public AdminSessionService(SessionRegistry sessionRegistry, SessionMetadataStore metadataStore) {
        this.sessionRegistry = sessionRegistry;
        this.metadataStore = metadataStore;
    }

    public List<ManagedSessionSummary> list(String currentSessionId) {
        return sessionRegistry.getAllPrincipals().stream()
                .filter(UserDetails.class::isInstance)
                .map(UserDetails.class::cast)
                .flatMap(principal -> sessionRegistry.getAllSessions(principal, true).stream()
                        .map(session -> summarize(principal.getUsername(), session, currentSessionId)))
                .sorted(Comparator.comparing(
                        ManagedSessionSummary::lastSeenAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public long activeSessionCount() {
        return sessionRegistry.getAllPrincipals().stream()
                .flatMap(principal -> sessionRegistry.getAllSessions(principal, false).stream())
                .count();
    }

    public String terminate(String terminationToken, String currentSessionId) {
        if (terminationToken == null || terminationToken.isBlank()) {
            throw new SessionOperationException("종료할 세션을 찾을 수 없습니다.");
        }
        SessionInformation session = findByTerminationToken(terminationToken)
                .orElseThrow(() -> new SessionOperationException("세션이 이미 종료되었거나 존재하지 않습니다."));
        if (session.getSessionId().equals(currentSessionId)) {
            throw new SessionOperationException("현재 사용 중인 세션은 이 화면에서 종료할 수 없습니다.");
        }
        if (session.isExpired()) {
            throw new SessionOperationException("세션이 이미 종료되었거나 존재하지 않습니다.");
        }
        String username = session.getPrincipal() instanceof UserDetails userDetails
                ? userDetails.getUsername()
                : "unknown";
        session.expireNow();
        metadataStore.remove(session.getSessionId());
        return username;
    }

    private ManagedSessionSummary summarize(
            String username,
            SessionInformation session,
            String currentSessionId) {
        SessionMetadata metadata = metadataStore.find(session.getSessionId()).orElse(null);
        Instant registryLastSeen = session.getLastRequest() == null
                ? null
                : session.getLastRequest().toInstant();
        return new ManagedSessionSummary(
                terminationToken(session.getSessionId()),
                fingerprint(session.getSessionId()),
                username,
                metadata == null ? null : metadata.ipAddress(),
                metadata == null ? null : metadata.userAgent(),
                metadata == null ? null : metadata.signedInAt(),
                metadata == null ? registryLastSeen : metadata.lastSeenAt(),
                metadata == null ? null : metadata.lastPath(),
                session.getSessionId().equals(currentSessionId),
                session.isExpired());
    }

    private String fingerprint(String sessionId) {
        return terminationToken(sessionId).substring(0, 8);
    }

    private String terminationToken(String sessionId) {
        return HexFormat.of().withUpperCase().formatHex(digest(sessionId));
    }

    private Optional<SessionInformation> findByTerminationToken(String terminationToken) {
        try {
            byte[] expected = HexFormat.of().parseHex(terminationToken);
            if (expected.length != 32) {
                return Optional.empty();
            }
            return sessionRegistry.getAllPrincipals().stream()
                    .flatMap(principal -> sessionRegistry.getAllSessions(principal, true).stream())
                    .filter(session -> MessageDigest.isEqual(digest(session.getSessionId()), expected))
                    .findFirst();
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private byte[] digest(String sessionId) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(sessionId.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 must be available", exception);
        }
    }
}
