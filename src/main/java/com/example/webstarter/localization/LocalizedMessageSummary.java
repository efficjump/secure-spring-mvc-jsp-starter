package com.example.webstarter.localization;

public record LocalizedMessageSummary(
        String messageKey,
        String resolvedValue,
        boolean overridden) {
}
