package com.example.webstarter.localization;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import com.example.webstarter.config.LocalizationProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class LocaleCatalogService {

    private static final String SNAPSHOT_KEY = "enabled-locales";
    private static final Pattern LANGUAGE_TAG =
            Pattern.compile("[A-Za-z]{2,8}(?:-[A-Za-z0-9]{1,8})*");

    private final SupportedLocaleRepository repository;
    private final LocalizationProperties properties;
    private final DatabaseMessageSource messageSource;
    private final Clock clock;
    private final Cache<String, LocaleSnapshot> snapshotCache;

    public LocaleCatalogService(
            SupportedLocaleRepository repository,
            LocalizationProperties properties,
            DatabaseMessageSource messageSource,
            Clock clock) {
        this.repository = repository;
        this.properties = properties;
        this.messageSource = messageSource;
        this.clock = clock;
        this.snapshotCache = Caffeine.newBuilder()
                .maximumSize(1)
                .expireAfterWrite(properties.cacheTtl())
                .build();
    }

    @Transactional(readOnly = true)
    public List<SupportedLocaleSummary> enabledLocales() {
        return snapshot().enabledLocales();
    }

    @Transactional(readOnly = true)
    public Locale defaultLocale() {
        return snapshot().defaultLocale();
    }

    @Transactional(readOnly = true)
    public Optional<Locale> findEnabled(String languageTag) {
        if (languageTag == null || languageTag.isBlank()) {
            return Optional.empty();
        }
        String normalized;
        try {
            normalized = normalizeLanguageTag(languageTag);
        } catch (LocalizationOperationException exception) {
            return Optional.empty();
        }
        return snapshot().enabledLocales().stream()
                .filter(item -> item.languageTag().equalsIgnoreCase(normalized))
                .map(SupportedLocaleSummary::toLocale)
                .findFirst();
    }

    @Transactional(readOnly = true)
    public Optional<Locale> matchEnabled(Locale requested) {
        if (requested == null || requested.getLanguage().isBlank()) {
            return Optional.empty();
        }
        List<SupportedLocaleSummary> enabled = snapshot().enabledLocales();
        String requestedTag = requested.toLanguageTag();
        Optional<Locale> exact = enabled.stream()
                .filter(item -> item.languageTag().equalsIgnoreCase(requestedTag))
                .map(SupportedLocaleSummary::toLocale)
                .findFirst();
        if (exact.isPresent()) {
            return exact;
        }
        return enabled.stream()
                .map(SupportedLocaleSummary::toLocale)
                .filter(locale -> locale.getLanguage().equalsIgnoreCase(requested.getLanguage()))
                .findFirst();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<SupportedLocaleSummary> listAll() {
        return repository.findAllByOrderByDisplayOrderAscIdAsc().stream()
                .map(SupportedLocaleSummary::from)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public SupportedLocaleSummary get(Long id) {
        return SupportedLocaleSummary.from(find(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public SupportedLocaleForm formFor(Long id) {
        SupportedLocale locale = find(id);
        SupportedLocaleForm form = new SupportedLocaleForm();
        form.setLanguageTag(locale.getLanguageTag());
        form.setDisplayName(locale.getDisplayName());
        form.setNativeName(locale.getNativeName());
        form.setDisplayOrder(locale.getDisplayOrder());
        form.setEnabled(locale.isEnabled());
        return form;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public SupportedLocaleSummary create(SupportedLocaleForm form) {
        validateForm(form);
        String languageTag = normalizeLanguageTag(form.getLanguageTag());
        if (repository.existsByLanguageTagIgnoreCase(languageTag)) {
            throw new LocalizationOperationException("localization.error.duplicateTag");
        }
        try {
            SupportedLocale created = repository.saveAndFlush(
                    SupportedLocale.create(languageTag, form, clock.instant()));
            invalidateCachesAfterCommit();
            return SupportedLocaleSummary.from(created);
        } catch (DataIntegrityViolationException exception) {
            throw new LocalizationOperationException("localization.error.invalidStoredValue", exception);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public SupportedLocaleSummary update(Long id, SupportedLocaleForm form) {
        validateForm(form);
        String languageTag = normalizeLanguageTag(form.getLanguageTag());
        SupportedLocale locale = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new LocalizationOperationException("localization.error.notFound"));
        if (locale.isDefaultLocale() && !form.isEnabled()) {
            throw new LocalizationOperationException("localization.error.defaultRequired");
        }
        if (repository.existsByLanguageTagIgnoreCaseAndIdNot(languageTag, id)) {
            throw new LocalizationOperationException("localization.error.duplicateTag");
        }
        try {
            locale.update(languageTag, form, clock.instant());
            repository.flush();
            invalidateCachesAfterCommit();
            return SupportedLocaleSummary.from(locale);
        } catch (DataIntegrityViolationException exception) {
            throw new LocalizationOperationException("localization.error.invalidStoredValue", exception);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public SupportedLocaleSummary toggleEnabled(Long id) {
        SupportedLocale locale = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new LocalizationOperationException("localization.error.notFound"));
        if (locale.isDefaultLocale() && locale.isEnabled()) {
            throw new LocalizationOperationException("localization.error.defaultRequired");
        }
        locale.toggleEnabled(clock.instant());
        invalidateCachesAfterCommit();
        return SupportedLocaleSummary.from(locale);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public SupportedLocaleSummary makeDefault(Long id) {
        List<SupportedLocale> locales = repository.findAllForUpdate();
        SupportedLocale selected = locales.stream()
                .filter(locale -> locale.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new LocalizationOperationException("localization.error.notFound"));
        Instant now = clock.instant();
        locales.forEach(locale -> locale.clearDefault(now));
        selected.makeDefault(now);
        repository.flush();
        invalidateCachesAfterCommit();
        return SupportedLocaleSummary.from(selected);
    }

    public static String normalizeLanguageTag(String candidate) {
        String stripped = candidate == null ? "" : candidate.strip();
        if (!LANGUAGE_TAG.matcher(stripped).matches()) {
            throw new LocalizationOperationException("localization.error.invalidTag");
        }
        Locale locale = Locale.forLanguageTag(stripped);
        if (locale.getLanguage().isBlank() || locale.toLanguageTag().equalsIgnoreCase("und")) {
            throw new LocalizationOperationException("localization.error.invalidTag");
        }
        return locale.toLanguageTag();
    }

    private LocaleSnapshot snapshot() {
        return snapshotCache.get(SNAPSHOT_KEY, ignored -> loadSnapshot());
    }

    private LocaleSnapshot loadSnapshot() {
        List<SupportedLocaleSummary> stored = repository.findAllByEnabledTrueOrderByDisplayOrderAscIdAsc().stream()
                .map(SupportedLocaleSummary::from)
                .toList();
        List<SupportedLocaleSummary> enabled = stored.isEmpty() ? fallbackLocales() : stored;
        Locale defaultLocale = enabled.stream()
                .filter(SupportedLocaleSummary::defaultLocale)
                .findFirst()
                .map(SupportedLocaleSummary::toLocale)
                .orElseGet(() -> matchFallbackDefault(enabled));
        return new LocaleSnapshot(enabled, defaultLocale);
    }

    private List<SupportedLocaleSummary> fallbackLocales() {
        Map<String, SupportedLocaleSummary> fallbacks = new LinkedHashMap<>();
        int index = 0;
        for (Locale locale : properties.supportedLocales()) {
            boolean defaultLocale = locale.equals(properties.defaultLocale());
            SupportedLocaleSummary summary =
                    SupportedLocaleSummary.fallback(locale, ++index * 100, defaultLocale);
            fallbacks.putIfAbsent(summary.languageTag().toLowerCase(Locale.ROOT), summary);
        }
        return List.copyOf(fallbacks.values());
    }

    private Locale matchFallbackDefault(List<SupportedLocaleSummary> enabled) {
        return enabled.stream()
                .filter(item -> item.languageTag().equalsIgnoreCase(properties.defaultLocale().toLanguageTag()))
                .findFirst()
                .orElse(enabled.getFirst())
                .toLocale();
    }

    private SupportedLocale find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new LocalizationOperationException("localization.error.notFound"));
    }

    private void validateForm(SupportedLocaleForm form) {
        if (form == null
                || isInvalidName(form.getDisplayName())
                || isInvalidName(form.getNativeName())
                || form.getDisplayOrder() < 0
                || form.getDisplayOrder() > 9999) {
            throw new LocalizationOperationException("localization.error.invalidStoredValue");
        }
    }

    private boolean isInvalidName(String value) {
        return value == null
                || value.isBlank()
                || value.length() > 80
                || value.chars().anyMatch(Character::isISOControl);
    }

    private void invalidateCachesAfterCommit() {
        Runnable invalidation = () -> {
            snapshotCache.invalidateAll();
            messageSource.clearCache();
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    invalidation.run();
                }
            });
        } else {
            invalidation.run();
        }
    }

    private record LocaleSnapshot(
            List<SupportedLocaleSummary> enabledLocales,
            Locale defaultLocale) {
    }
}
