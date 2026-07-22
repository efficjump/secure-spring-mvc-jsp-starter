package com.example.webstarter.security;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.example.webstarter.audit.AuditEventType;
import com.example.webstarter.audit.AuditOutcome;
import com.example.webstarter.audit.RequestMetadata;
import com.example.webstarter.audit.SecurityAuditService;
import com.example.webstarter.user.UserIdentityNormalizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class AppAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(AppAuthenticationFailureHandler.class);

    private final LoginProtectionService loginProtectionService;
    private final SecurityAuditService auditService;
    private final SimpleUrlAuthenticationFailureHandler delegate;

    public AppAuthenticationFailureHandler(
            LoginProtectionService loginProtectionService,
            SecurityAuditService auditService) {
        this.loginProtectionService = loginProtectionService;
        this.auditService = auditService;
        this.delegate = new SimpleUrlAuthenticationFailureHandler("/login?error");
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        String username = UserIdentityNormalizer.username(request.getParameter("username"));
        loginProtectionService.recordFailure(request.getRemoteAddr(), username);
        try {
            auditService.record(
                    AuditEventType.LOGIN_FAILURE,
                    AuditOutcome.FAILURE,
                    username,
                    username,
                    RequestMetadata.from(request),
                    exception.getClass().getSimpleName());
        } catch (RuntimeException auditException) {
            log.error("Unable to persist failed-login audit event", auditException);
        }
        delegate.onAuthenticationFailure(request, response, exception);
    }
}

