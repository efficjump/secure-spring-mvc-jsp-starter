package com.example.webstarter.admin;

import java.time.Instant;

import com.example.webstarter.audit.AuditEventType;
import com.example.webstarter.audit.AuditOutcome;
import com.example.webstarter.audit.SecurityAuditEvent;

public record LoginAuditSummary(
        Long id,
        AuditEventType eventType,
        String eventLabel,
        AuditOutcome outcome,
        String outcomeLabel,
        String actorUsername,
        String subject,
        String ipAddress,
        String userAgent,
        String requestId,
        String detail,
        Instant createdAt) {

    public static LoginAuditSummary from(SecurityAuditEvent event) {
        return new LoginAuditSummary(
                event.getId(),
                event.getEventType(),
                event.getEventType().getDisplayName(),
                event.getOutcome(),
                event.getOutcome().getDisplayName(),
                event.getActorUsername(),
                event.getSubject(),
                event.getIpAddress(),
                event.getUserAgent(),
                event.getRequestId(),
                event.getDetail(),
                event.getCreatedAt());
    }

}
