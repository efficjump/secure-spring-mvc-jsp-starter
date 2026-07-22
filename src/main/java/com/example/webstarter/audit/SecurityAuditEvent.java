package com.example.webstarter.audit;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "security_audit_events")
public class SecurityAuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 64)
    private AuditEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AuditOutcome outcome;

    @Column(name = "actor_username", length = 64)
    private String actorUsername;

    @Column(length = 128)
    private String subject;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "request_id", length = 64)
    private String requestId;

    @Column(length = 512)
    private String detail;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected SecurityAuditEvent() {
    }

    public SecurityAuditEvent(
            AuditEventType eventType,
            AuditOutcome outcome,
            String actorUsername,
            String subject,
            String ipAddress,
            String userAgent,
            String requestId,
            String detail,
            Instant createdAt) {
        this.eventType = eventType;
        this.outcome = outcome;
        this.actorUsername = actorUsername;
        this.subject = subject;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.requestId = requestId;
        this.detail = detail;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public AuditEventType getEventType() {
        return eventType;
    }

    public AuditOutcome getOutcome() {
        return outcome;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public String getSubject() {
        return subject;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getDetail() {
        return detail;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

