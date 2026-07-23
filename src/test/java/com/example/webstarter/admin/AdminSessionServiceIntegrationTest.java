package com.example.webstarter.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser(username = "session-admin", roles = "ADMIN")
class AdminSessionServiceIntegrationTest {

    @Autowired
    private AdminSessionService sessionService;

    @Autowired
    private SessionRegistry sessionRegistry;

    @BeforeEach
    void clearSessions() {
        sessionRegistry.getAllPrincipals().stream()
                .flatMap(principal -> sessionRegistry.getAllSessions(principal, true).stream())
                .map(session -> session.getSessionId())
                .toList()
                .forEach(sessionRegistry::removeSessionInformation);
    }

    @Test
    void sessionCanBeTerminatedWithoutExposingRawSessionIdAsActionToken() {
        String rawSessionId = UUID.randomUUID().toString();
        sessionRegistry.registerNewSession(
                rawSessionId,
                User.withUsername("member").password("unused").roles("USER").build());

        ManagedSessionSummary summary = sessionService.list("current-session").getFirst();

        assertThat(summary.terminationToken()).hasSize(64).isNotEqualTo(rawSessionId);
        assertThat(summary.fingerprint()).hasSize(8);
        assertThat(sessionService.terminate(summary.terminationToken(), "current-session")).isEqualTo("member");
        assertThat(sessionRegistry.getSessionInformation(rawSessionId).isExpired()).isTrue();
    }

    @Test
    void invalidTerminationTokenDoesNotSelectAnySession() {
        assertThatThrownBy(() -> sessionService.terminate("not-a-valid-token", "current-session"))
                .isInstanceOf(SessionOperationException.class)
                .hasMessage("admin.session.error.notFound");
    }
}
