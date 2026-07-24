package com.example.webstarter.config;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.i18n")
public record LocalizationProperties(
        @NotNull Locale defaultLocale,
        @NotNull @Size(min = 1, max = 10) List<@NotNull Locale> supportedLocales,
        @NotBlank @Pattern(regexp = "[A-Za-z][A-Za-z0-9_-]{0,39}") String cookieName,
        @NotBlank @Pattern(regexp = "/[A-Za-z0-9/_-]*") String cookiePath,
        @NotNull Duration cookieMaxAge,
        @NotBlank @Pattern(regexp = "(?i:lax|strict)") String cookieSameSite,
        boolean cookieSecure,
        boolean respectAcceptLanguage,
        @Min(100) @Max(100000) int cacheMaxEntries,
        @NotNull Duration cacheTtl) {

    public LocalizationProperties {
        if (defaultLocale == null) {
            throw new IllegalArgumentException("app.i18n.default-locale is required");
        }
        if (supportedLocales == null || supportedLocales.isEmpty()) {
            throw new IllegalArgumentException("app.i18n.supported-locales must contain at least one locale");
        }
        if (supportedLocales.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("app.i18n.supported-locales cannot contain null values");
        }
        if (cookieMaxAge == null || cookieMaxAge.isNegative() || cookieMaxAge.isZero()) {
            throw new IllegalArgumentException("app.i18n.cookie-max-age must be positive");
        }
        if (cacheTtl == null || cacheTtl.isNegative() || cacheTtl.isZero()) {
            throw new IllegalArgumentException("app.i18n.cache-ttl must be positive");
        }

        defaultLocale = normalize(defaultLocale);
        Map<String, Locale> uniqueLocales = new LinkedHashMap<>();
        supportedLocales.stream()
                .map(LocalizationProperties::normalize)
                .forEach(locale -> uniqueLocales.putIfAbsent(normalizedTag(locale), locale));
        supportedLocales = List.copyOf(uniqueLocales.values());
        cookieSameSite = cookieSameSite == null ? null : cookieSameSite.strip();

        if (!uniqueLocales.containsKey(normalizedTag(defaultLocale))) {
            throw new IllegalArgumentException("app.i18n.default-locale must be included in supported-locales");
        }
    }

    public Optional<Locale> findSupported(String languageTag) {
        if (languageTag == null || languageTag.isBlank()) {
            return Optional.empty();
        }
        Locale requested = normalize(Locale.forLanguageTag(languageTag.strip()));
        String requestedTag = normalizedTag(requested);
        return supportedLocales.stream()
                .filter(locale -> normalizedTag(locale).equals(requestedTag))
                .findFirst();
    }

    public Optional<Locale> match(Locale requested) {
        if (requested == null || requested.getLanguage().isBlank()) {
            return Optional.empty();
        }
        String requestedTag = normalizedTag(normalize(requested));
        Optional<Locale> exactMatch = supportedLocales.stream()
                .filter(locale -> normalizedTag(locale).equals(requestedTag))
                .findFirst();
        if (exactMatch.isPresent()) {
            return exactMatch;
        }
        return supportedLocales.stream()
                .filter(locale -> locale.getLanguage().equalsIgnoreCase(requested.getLanguage()))
                .findFirst();
    }

    private static Locale normalize(Locale locale) {
        Locale normalized = Locale.forLanguageTag(locale.toLanguageTag());
        if (normalized.getLanguage().isBlank()) {
            throw new IllegalArgumentException("Locales must use a valid BCP 47 language tag");
        }
        return normalized;
    }

    private static String normalizedTag(Locale locale) {
        return locale.toLanguageTag().toLowerCase(Locale.ROOT);
    }
}
