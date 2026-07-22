package com.example.webstarter.audit;

import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SecurityAuditService {

    private static final Logger SECURITY_LOG = LoggerFactory.getLogger("SECURITY_AUDIT");
    private static final int USERNAME_MAX_LENGTH = 64;
    private static final int SUBJECT_MAX_LENGTH = 128;
    private static final int DETAIL_MAX_LENGTH = 512;

    private final SecurityAuditEventRepository repository;
    private final Clock clock;

    public SecurityAuditService(SecurityAuditEventRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public void record(
            AuditEventType type,
            AuditOutcome outcome,
            String actorUsername,
            String subject,
            RequestMetadata request,
            String detail) {
        String safeActor = RequestMetadata.clean(actorUsername, USERNAME_MAX_LENGTH);
        String safeSubject = RequestMetadata.clean(subject, SUBJECT_MAX_LENGTH);
        String safeDetail = RequestMetadata.clean(detail, DETAIL_MAX_LENGTH);
        RequestMetadata safeRequest = request == null ? new RequestMetadata(null, null, null) : request;

        repository.save(new SecurityAuditEvent(
                type,
                outcome,
                safeActor,
                safeSubject,
                safeRequest.ipAddress(),
                safeRequest.userAgent(),
                safeRequest.requestId(),
                safeDetail,
                clock.instant()));

        SECURITY_LOG.info(
                "event={} outcome={} actor={} subject={} ip={} detail={}",
                type,
                outcome,
                logValue(safeActor),
                logValue(safeSubject),
                logValue(safeRequest.ipAddress()),
                logValue(safeDetail));
    }

    private String logValue(String value) {
        return value == null ? "-" : value;
    }
}

