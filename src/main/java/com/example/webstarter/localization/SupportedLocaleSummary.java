package com.example.webstarter.localization;

import java.util.Locale;

public record SupportedLocaleSummary(
        Long id,
        String languageTag,
        String displayName,
        String nativeName,
        int displayOrder,
        boolean enabled,
        boolean defaultLocale) {

    public static SupportedLocaleSummary from(SupportedLocale locale) {
        return new SupportedLocaleSummary(
                locale.getId(),
                locale.getLanguageTag(),
                locale.getDisplayName(),
                locale.getNativeName(),
                locale.getDisplayOrder(),
                locale.isEnabled(),
                locale.isDefaultLocale());
    }

    public static SupportedLocaleSummary fallback(Locale locale, int displayOrder, boolean defaultLocale) {
        return new SupportedLocaleSummary(
                null,
                locale.toLanguageTag(),
                locale.getDisplayLanguage(Locale.ENGLISH),
                locale.getDisplayLanguage(locale),
                displayOrder,
                true,
                defaultLocale);
    }

    public Locale toLocale() {
        return Locale.forLanguageTag(languageTag);
    }
}
