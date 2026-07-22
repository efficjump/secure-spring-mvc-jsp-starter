package com.example.webstarter.security;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.example.webstarter.audit.AuditEventType;
import com.example.webstarter.audit.AuditOutcome;
import com.example.webstarter.audit.RequestMetadata;
import com.example.webstarter.audit.SecurityAuditService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.stereotype.Component;

@Component
public class AuditingAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(AuditingAccessDeniedHandler.class);

    private final SecurityAuditService auditService;
    private final AccessDeniedHandlerImpl delegate;

    public AuditingAccessDeniedHandler(SecurityAuditService auditService) {
        this.auditService = auditService;
        this.delegate = new AccessDeniedHandlerImpl();
        this.delegate.setErrorPage("/access-denied");
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String actor = authentication == null ? null : authentication.getName();
        try {
            auditService.record(
                    AuditEventType.ACCESS_DENIED,
                    AuditOutcome.DENIED,
                    actor,
                    request.getRequestURI(),
                    RequestMetadata.from(request),
                    accessDeniedException.getClass().getSimpleName());
        } catch (RuntimeException exception) {
            log.error("Unable to persist access-denied audit event", exception);
        }
        delegate.handle(request, response, accessDeniedException);
    }
}

