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
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class AppAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(AppAuthenticationSuccessHandler.class);

    private final LoginProtectionService loginProtectionService;
    private final SecurityAuditService auditService;
    private final SessionMetadataStore sessionMetadataStore;
    private final SavedRequestAwareAuthenticationSuccessHandler delegate;

    public AppAuthenticationSuccessHandler(
            LoginProtectionService loginProtectionService,
            SecurityAuditService auditService,
            SessionMetadataStore sessionMetadataStore) {
        this.loginProtectionService = loginProtectionService;
        this.auditService = auditService;
        this.sessionMetadataStore = sessionMetadataStore;
        this.delegate = new SavedRequestAwareAuthenticationSuccessHandler();
        this.delegate.setDefaultTargetUrl("/workspace");
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        loginProtectionService.recordSuccess(request.getRemoteAddr(), username);
        sessionMetadataStore.register(request, username);
        try {
            auditService.record(
                    AuditEventType.LOGIN_SUCCESS,
                    AuditOutcome.SUCCESS,
                    username,
                    username,
                    RequestMetadata.from(request),
                    null);
        } catch (RuntimeException exception) {
            log.error("Unable to persist successful-login audit event", exception);
        }
        delegate.onAuthenticationSuccess(request, response, authentication);
    }
}
