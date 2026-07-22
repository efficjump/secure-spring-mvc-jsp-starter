package com.example.webstarter.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.example.webstarter.audit.AuditEventType;
import com.example.webstarter.audit.AuditOutcome;
import com.example.webstarter.audit.RequestMetadata;
import com.example.webstarter.audit.SecurityAuditService;
import com.example.webstarter.config.SecurityProperties;
import com.example.webstarter.user.UserIdentityNormalizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LoginRateLimitFilter.class);

    private final LoginProtectionService loginProtectionService;
    private final SecurityAuditService auditService;
    private final int maximumPasswordLength;

    public LoginRateLimitFilter(
            LoginProtectionService loginProtectionService,
            SecurityAuditService auditService,
            SecurityProperties securityProperties) {
        this.loginProtectionService = loginProtectionService;
        this.auditService = auditService;
        this.maximumPasswordLength = securityProperties.password().maxLength();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String loginPath = request.getContextPath() + "/login";
        return !"POST".equals(request.getMethod()) || !loginPath.equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        if (exceedsCodePointLimit(username, UserIdentityNormalizer.USERNAME_MAX_LENGTH)
                || exceedsCodePointLimit(
                        UserIdentityNormalizer.username(username),
                        UserIdentityNormalizer.USERNAME_MAX_LENGTH)
                || exceedsCodePointLimit(password, maximumPasswordLength)) {
            loginProtectionService.recordRejectedInput(request.getRemoteAddr());
            recordDenied(
                    request,
                    AuditEventType.LOGIN_INPUT_REJECTED,
                    null,
                    "credential input exceeded configured length");
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/login?error"));
            return;
        }
        if (!loginProtectionService.isRateLimited(request.getRemoteAddr(), username)) {
            filterChain.doFilter(request, response);
            return;
        }

        recordDenied(request, AuditEventType.LOGIN_RATE_LIMITED, username, "login attempt throttled");
        response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/login?rate_limited"));
    }

    private void recordDenied(
            HttpServletRequest request,
            AuditEventType eventType,
            String username,
            String detail) {
        try {
            auditService.record(
                    eventType,
                    AuditOutcome.DENIED,
                    username,
                    username,
                    RequestMetadata.from(request),
                    detail);
        } catch (RuntimeException exception) {
            log.error("Unable to persist login rate-limit audit event", exception);
        }
    }

    private boolean exceedsCodePointLimit(String value, int maximumLength) {
        if (value == null) {
            return false;
        }
        if (value.length() > Math.multiplyExact(maximumLength, 2)) {
            return true;
        }
        return value.codePointCount(0, value.length()) > maximumLength;
    }
}
