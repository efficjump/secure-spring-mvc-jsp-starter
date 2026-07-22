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
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class AppLogoutSuccessHandler implements LogoutSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(AppLogoutSuccessHandler.class);

    private final SecurityAuditService auditService;
    private final SimpleUrlLogoutSuccessHandler delegate;

    public AppLogoutSuccessHandler(SecurityAuditService auditService) {
        this.auditService = auditService;
        this.delegate = new SimpleUrlLogoutSuccessHandler();
        this.delegate.setDefaultTargetUrl("/login?logout");
    }

    @Override
    public void onLogoutSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        String username = authentication == null ? null : authentication.getName();
        try {
            auditService.record(
                    AuditEventType.LOGOUT,
                    AuditOutcome.SUCCESS,
                    username,
                    username,
                    RequestMetadata.from(request),
                    null);
        } catch (RuntimeException exception) {
            log.error("Unable to persist logout audit event", exception);
        }
        delegate.onLogoutSuccess(request, response, authentication);
    }
}

