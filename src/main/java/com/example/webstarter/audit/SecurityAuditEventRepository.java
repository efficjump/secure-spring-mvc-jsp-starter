package com.example.webstarter.audit;

import java.time.Instant;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SecurityAuditEventRepository extends
        JpaRepository<SecurityAuditEvent, Long>,
        JpaSpecificationExecutor<SecurityAuditEvent> {

    long countByEventTypeInAndCreatedAtGreaterThanEqual(Set<AuditEventType> eventTypes, Instant since);
}
