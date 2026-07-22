package com.example.webstarter.web;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.example.webstarter.config.SecurityProperties;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class BaselineSecurityHeadersFilter extends OncePerRequestFilter {

    private final SecurityProperties.Headers headers;

    public BaselineSecurityHeadersFilter(SecurityProperties properties) {
        this.headers = properties.headers();
        rejectHeaderInjection(headers.contentSecurityPolicy());
        rejectHeaderInjection(headers.permissionsPolicy());
        rejectHeaderInjection(headers.referrerPolicy());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        response.setHeader("Content-Security-Policy", headers.contentSecurityPolicy());
        response.setHeader("Permissions-Policy", headers.permissionsPolicy());
        response.setHeader("Referrer-Policy", headers.referrerPolicy());
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", headers.frameOptions().name());
        response.setHeader("X-XSS-Protection", "0");
        response.setHeader("X-Permitted-Cross-Domain-Policies", "none");

        if (request.isSecure()) {
            StringBuilder hsts = new StringBuilder("max-age=").append(headers.hstsMaxAge().toSeconds());
            if (headers.hstsIncludeSubdomains()) {
                hsts.append("; includeSubDomains");
            }
            if (headers.hstsPreload()) {
                hsts.append("; preload");
            }
            response.setHeader("Strict-Transport-Security", hsts.toString());
        }

        if (!request.getRequestURI().startsWith(request.getContextPath() + "/assets/")) {
            response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
        }
        filterChain.doFilter(request, response);
    }

    private void rejectHeaderInjection(String value) {
        if (value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0) {
            throw new IllegalArgumentException("Security header configuration cannot contain line breaks");
        }
    }
}
