package com.example.webstarter.localization;

public record TranslationGridCell(
        Long localeId,
        String languageTag,
        String nativeName,
        String value,
        boolean overridden) {
}
