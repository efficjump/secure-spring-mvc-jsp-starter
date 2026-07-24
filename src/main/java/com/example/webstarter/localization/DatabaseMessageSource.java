package com.example.webstarter.localization;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;

import com.example.webstarter.config.LocalizationProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.support.AbstractMessageSource;

public class DatabaseMessageSource extends AbstractMessageSource {

    private final ObjectProvider<LocalizedMessageRepository> repositoryProvider;
    private final Cache<MessageCacheKey, Optional<String>> cache;

    public DatabaseMessageSource(
            ObjectProvider<LocalizedMessageRepository> repositoryProvider,
            LocalizationProperties properties) {
        this.repositoryProvider = repositoryProvider;
        this.cache = Caffeine.newBuilder()
                .maximumSize(properties.cacheMaxEntries())
                .expireAfterWrite(properties.cacheTtl())
                .build();
    }

    @Override
    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        return resolveOverride(code, locale).orElse(null);
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        return resolveOverride(code, locale)
                .map(value -> createMessageFormat(value, locale))
                .orElse(null);
    }

    public void clearCache() {
        cache.invalidateAll();
    }

    private Optional<String> resolveOverride(String code, Locale locale) {
        Locale requested = locale == null ? Locale.ROOT : locale;
        return cache.get(new MessageCacheKey(requested.toLanguageTag(), code), this::loadOverride);
    }

    private Optional<String> loadOverride(MessageCacheKey key) {
        LocalizedMessageRepository repository = repositoryProvider.getIfAvailable();
        if (repository == null || key.languageTag().isBlank()) {
            return Optional.empty();
        }

        Optional<String> exact = repository.findEnabledValue(key.languageTag(), key.messageKey());
        if (exact.isPresent()) {
            return exact;
        }

        Locale locale = Locale.forLanguageTag(key.languageTag());
        String baseLanguage = locale.getLanguage();
        if (!baseLanguage.isBlank() && !baseLanguage.equalsIgnoreCase(key.languageTag())) {
            return repository.findEnabledValue(baseLanguage, key.messageKey());
        }
        return Optional.empty();
    }

    private record MessageCacheKey(String languageTag, String messageKey) {
    }
}
