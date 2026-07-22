package com.example.webstarter.web;

import java.time.Clock;
import java.time.Duration;
import java.util.EnumSet;

import com.example.webstarter.admin.AdminSessionService;
import com.example.webstarter.audit.AuditEventType;
import com.example.webstarter.audit.SecurityAuditEventRepository;
import com.example.webstarter.user.AppUserRepository;
import com.example.webstarter.user.Role;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private static final Duration RECENT_LOGIN_WINDOW = Duration.ofHours(24);

    private final AppUserRepository userRepository;
    private final SecurityAuditEventRepository auditRepository;
    private final AdminSessionService sessionService;
    private final Clock clock;

    public DashboardService(
            AppUserRepository userRepository,
            SecurityAuditEventRepository auditRepository,
            AdminSessionService sessionService,
            Clock clock) {
        this.userRepository = userRepository;
        this.auditRepository = auditRepository;
        this.sessionService = sessionService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public OperationsSnapshot snapshot(Authentication authentication) {
        boolean administrator = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(Role.ADMIN.authority()));
        if (!administrator) {
            return OperationsSnapshot.standardUser();
        }
        long rejectedLogins = auditRepository.countByEventTypeInAndCreatedAtGreaterThanEqual(
                EnumSet.of(
                        AuditEventType.LOGIN_FAILURE,
                        AuditEventType.LOGIN_INPUT_REJECTED,
                        AuditEventType.LOGIN_RATE_LIMITED),
                clock.instant().minus(RECENT_LOGIN_WINDOW));
        return new OperationsSnapshot(
                true,
                userRepository.count(),
                userRepository.countByEnabledTrue(),
                sessionService.activeSessionCount(),
                rejectedLogins);
    }
}
