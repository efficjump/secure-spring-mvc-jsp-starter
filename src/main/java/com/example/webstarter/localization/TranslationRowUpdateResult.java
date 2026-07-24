package com.example.webstarter.localization;

public record TranslationRowUpdateResult(
        String messageKey,
        int updatedCount,
        int restoredCount) {

    public boolean changed() {
        return updatedCount > 0 || restoredCount > 0;
    }
}
