package com.example.webstarter.audit;

public enum AuditOutcome {
    SUCCESS("성공"),
    FAILURE("실패"),
    DENIED("거부");

    private final String displayName;

    AuditOutcome(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
