package com.example.webstarter.audit;

import java.util.Locale;

public enum AuditOutcome {
    SUCCESS,
    FAILURE,
    DENIED;

    public String getMessageCode() {
        return "audit.outcome." + name().toLowerCase(Locale.ROOT);
    }
}
