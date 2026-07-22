package com.example.webstarter.audit;

import jakarta.servlet.http.HttpServletRequest;

import com.example.webstarter.web.RequestContext;

public record RequestMetadata(String ipAddress, String userAgent, String requestId) {

    private static final int IP_MAX_LENGTH = 45;
    private static final int USER_AGENT_MAX_LENGTH = 512;
    private static final int REQUEST_ID_MAX_LENGTH = 64;

    public static RequestMetadata from(HttpServletRequest request) {
        Object requestId = request.getAttribute(RequestContext.REQUEST_ID_ATTRIBUTE);
        return new RequestMetadata(
                clean(request.getRemoteAddr(), IP_MAX_LENGTH),
                clean(request.getHeader("User-Agent"), USER_AGENT_MAX_LENGTH),
                clean(requestId == null ? null : requestId.toString(), REQUEST_ID_MAX_LENGTH));
    }

    static String clean(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String cleaned = value.replace('\r', ' ').replace('\n', ' ').strip();
        return cleaned.length() <= maxLength ? cleaned : cleaned.substring(0, maxLength);
    }
}

