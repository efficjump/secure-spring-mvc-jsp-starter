package com.example.webstarter.admin;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;

import com.example.webstarter.audit.AuditEventType;
import com.example.webstarter.audit.AuditOutcome;
import com.example.webstarter.audit.SecurityAuditEvent;
import com.example.webstarter.audit.SecurityAuditEventRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser(username = "audit-admin", roles = "ADMIN")
class AdminLoginAuditServiceIntegrationTest {

    @Autowired
    private AdminLoginAuditService auditService;

    @Autowired
    private SecurityAuditEventRepository repository;

    @Autowired
    private Clock clock;

    @BeforeEach
    void setUpEvents() {
        repository.deleteAll();
        repository.save(event(AuditEventType.LOGIN_SUCCESS, AuditOutcome.SUCCESS, "member", "10.0.0.10"));
        repository.save(event(AuditEventType.LOGIN_FAILURE, AuditOutcome.FAILURE, "blocked", "10.0.0.20"));
        repository.save(event(AuditEventType.ACCESS_DENIED, AuditOutcome.DENIED, "member", "10.0.0.10"));
        repository.flush();
    }

    @Test
    void listContainsOnlyAuthenticationLifecycleEvents() {
        assertThat(auditService.list(0, 25, null, null, "").getContent())
                .extracting(LoginAuditSummary::eventType)
                .containsExactlyInAnyOrder(AuditEventType.LOGIN_SUCCESS, AuditEventType.LOGIN_FAILURE);
    }

    @Test
    void listCanFilterByOutcomeAndIpAddress() {
        assertThat(auditService.list(0, 25, null, AuditOutcome.FAILURE, "10.0.0.20").getContent())
                .singleElement()
                .extracting(LoginAuditSummary::actorUsername)
                .isEqualTo("blocked");
    }

    private SecurityAuditEvent event(
            AuditEventType type,
            AuditOutcome outcome,
            String actor,
            String ipAddress) {
        return new SecurityAuditEvent(
                type,
                outcome,
                actor,
                actor,
                ipAddress,
                "integration-test",
                "test-request",
                null,
                clock.instant());
    }
}
