package com.example.webstarter.localization;

import java.text.MessageFormat;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class TranslationCatalogService {

    private static final Pattern MESSAGE_KEY =
            Pattern.compile("[A-Za-z][A-Za-z0-9_.-]{0,189}");
    private static final int SEARCH_MAX_LENGTH = 128;
    private static final int MESSAGE_VALUE_MAX_LENGTH = 4000;

    private final SupportedLocaleRepository localeRepository;
    private final LocalizedMessageRepository messageRepository;
    private final MessageKeyCatalog messageKeyCatalog;
    private final DatabaseMessageSource messageSource;
    private final Clock clock;

    public TranslationCatalogService(
            SupportedLocaleRepository localeRepository,
            LocalizedMessageRepository messageRepository,
            MessageKeyCatalog messageKeyCatalog,
            DatabaseMessageSource messageSource,
            Clock clock) {
        this.localeRepository = localeRepository;
        this.messageRepository = messageRepository;
        this.messageKeyCatalog = messageKeyCatalog;
        this.messageSource = messageSource;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Page<TranslationGridRow> listGrid(
            List<SupportedLocaleSummary> locales,
            int requestedPage,
            int pageSize,
            String search) {
        Map<Long, Map<String, String>> overridesByLocale = new HashMap<>();
        Set<String> keys = new TreeSet<>(messageKeyCatalog.keys());
        messageRepository.findAllByOrderByMessageKeyAsc().forEach(message -> {
            overridesByLocale
                    .computeIfAbsent(message.getLocale().getId(), ignored -> new HashMap<>())
                    .put(message.getMessageKey(), message.getMessageValue());
            keys.add(message.getMessageKey());
        });

        String normalizedSearch = normalizeSearch(search);
        ArrayList<TranslationGridRow> matches = new ArrayList<>();
        for (String key : keys) {
            ArrayList<TranslationGridCell> cells = new ArrayList<>();
            boolean rowMatches = normalizedSearch.isBlank()
                    || key.toLowerCase(Locale.ROOT).contains(normalizedSearch);
            for (SupportedLocaleSummary locale : locales) {
                Map<String, String> localeOverrides =
                        overridesByLocale.getOrDefault(locale.id(), Map.of());
                boolean overridden = localeOverrides.containsKey(key);
                String value = overridden
                        ? localeOverrides.get(key)
                        : messageSource.resolveBundle(key, locale.toLocale()).orElse("");
                if (!rowMatches && value.toLowerCase(Locale.ROOT).contains(normalizedSearch)) {
                    rowMatches = true;
                }
                cells.add(new TranslationGridCell(
                        locale.id(),
                        locale.languageTag(),
                        locale.nativeName(),
                        value,
                        overridden));
            }
            if (rowMatches) {
                matches.add(new TranslationGridRow(key, List.copyOf(cells)));
            }
        }

        int safePageSize = Math.max(1, pageSize);
        int lastPage = matches.isEmpty() ? 0 : (matches.size() - 1) / safePageSize;
        int page = Math.min(Math.max(0, requestedPage), lastPage);
        PageRequest pageable = PageRequest.of(page, safePageSize);
        int fromIndex = (int) Math.min(pageable.getOffset(), matches.size());
        int toIndex = Math.min(fromIndex + safePageSize, matches.size());
        return new PageImpl<>(matches.subList(fromIndex, toIndex), pageable, matches.size());
    }

    @Transactional(readOnly = true)
    public List<TranslationGridCell> emptyCells(List<SupportedLocaleSummary> locales) {
        return locales.stream()
                .map(locale -> new TranslationGridCell(
                        locale.id(),
                        locale.languageTag(),
                        locale.nativeName(),
                        "",
                        false))
                .toList();
    }

    @Transactional
    public TranslationRowUpdateResult saveRow(TranslationGridRowForm form) {
        String messageKey = validateMessageKey(form == null ? null : form.getMessageKey());
        Map<Long, String> submittedValues = form == null ? Map.of() : form.getValues();
        if (submittedValues == null || submittedValues.isEmpty()) {
            throw new LocalizationOperationException("localization.error.atLeastOneTranslation");
        }

        List<SupportedLocale> locales = localeRepository.findAllByOrderByDisplayOrderAscIdAsc();
        Map<Long, SupportedLocale> localesById = locales.stream()
                .collect(Collectors.toMap(SupportedLocale::getId, Function.identity()));
        if (submittedValues.keySet().stream()
                .anyMatch(localeId -> localeId == null || !localesById.containsKey(localeId))) {
            throw new LocalizationOperationException("localization.error.notFound");
        }

        Map<Long, LocalizedMessage> existingByLocale =
                messageRepository.findAllByMessageKey(messageKey).stream()
                        .collect(Collectors.toMap(
                                message -> message.getLocale().getId(),
                                Function.identity()));
        Map<Long, String> normalizedValues = new LinkedHashMap<>();
        boolean hasCustomValue = false;
        for (Map.Entry<Long, String> entry : submittedValues.entrySet()) {
            SupportedLocale locale = localesById.get(entry.getKey());
            String value = normalizeMessageValue(entry.getValue());
            String bundleValue = messageSource
                    .resolveBundle(messageKey, locale.toLocale())
                    .orElse("");
            if (!value.isBlank() && !value.equals(bundleValue)) {
                validateMessageValue(value, locale.toLocale());
                hasCustomValue = true;
            }
            normalizedValues.put(entry.getKey(), value);
        }

        boolean knownMessageKey = messageKeyCatalog.keys().contains(messageKey)
                || !existingByLocale.isEmpty();
        if (!knownMessageKey && !hasCustomValue) {
            throw new LocalizationOperationException("localization.error.atLeastOneTranslation");
        }

        int updatedCount = 0;
        int restoredCount = 0;
        try {
            for (Map.Entry<Long, String> entry : normalizedValues.entrySet()) {
                SupportedLocale locale = localesById.get(entry.getKey());
                String value = entry.getValue();
                String bundleValue = messageSource
                        .resolveBundle(messageKey, locale.toLocale())
                        .orElse("");
                LocalizedMessage existing = existingByLocale.get(entry.getKey());
                if (value.isBlank() || value.equals(bundleValue)) {
                    if (existing != null) {
                        messageRepository.delete(existing);
                        restoredCount++;
                    }
                    continue;
                }
                if (existing == null) {
                    messageRepository.save(LocalizedMessage.create(
                            locale,
                            messageKey,
                            value,
                            clock.instant()));
                    updatedCount++;
                } else if (!existing.getMessageValue().equals(value)) {
                    existing.update(value, clock.instant());
                    updatedCount++;
                }
            }
            if (updatedCount > 0 || restoredCount > 0) {
                messageRepository.flush();
                clearMessageCacheAfterCommit();
            }
            return new TranslationRowUpdateResult(messageKey, updatedCount, restoredCount);
        } catch (DataIntegrityViolationException exception) {
            throw new LocalizationOperationException("localization.error.invalidStoredValue", exception);
        }
    }

    private String validateMessageKey(String candidate) {
        String messageKey = candidate == null ? "" : candidate.strip();
        if (!MESSAGE_KEY.matcher(messageKey).matches()) {
            throw new LocalizationOperationException("localization.error.invalidMessageKey");
        }
        return messageKey;
    }

    private String normalizeMessageValue(String candidate) {
        return candidate == null ? "" : candidate.strip();
    }

    private void validateMessageValue(String messageValue, Locale locale) {
        if (messageValue.length() > MESSAGE_VALUE_MAX_LENGTH) {
            throw new LocalizationOperationException("localization.error.invalidStoredValue");
        }
        if (messageValue.indexOf('<') >= 0 || messageValue.indexOf('>') >= 0) {
            throw new LocalizationOperationException("localization.error.htmlNotAllowed");
        }
        boolean containsUnsafeControl = messageValue.chars()
                .anyMatch(character -> Character.isISOControl(character)
                        && character != '\n'
                        && character != '\r'
                        && character != '\t');
        if (containsUnsafeControl) {
            throw new LocalizationOperationException("localization.error.controlCharacter");
        }
        try {
            new MessageFormat(messageValue, locale);
        } catch (IllegalArgumentException exception) {
            throw new LocalizationOperationException("localization.error.invalidPattern", exception);
        }
    }

    private String normalizeSearch(String search) {
        if (search == null) {
            return "";
        }
        String stripped = search.strip();
        String limited = stripped.substring(0, Math.min(stripped.length(), SEARCH_MAX_LENGTH));
        return limited.toLowerCase(Locale.ROOT);
    }

    private void clearMessageCacheAfterCommit() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    messageSource.clearCache();
                }
            });
        } else {
            messageSource.clearCache();
        }
    }
}
