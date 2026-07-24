package com.example.webstarter.localization;

import java.text.MessageFormat;
import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.springframework.context.MessageSource;
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
    private final MessageSource messageSource;
    private final DatabaseMessageSource databaseMessageSource;
    private final Clock clock;

    public TranslationCatalogService(
            SupportedLocaleRepository localeRepository,
            LocalizedMessageRepository messageRepository,
            MessageKeyCatalog messageKeyCatalog,
            MessageSource messageSource,
            DatabaseMessageSource databaseMessageSource,
            Clock clock) {
        this.localeRepository = localeRepository;
        this.messageRepository = messageRepository;
        this.messageKeyCatalog = messageKeyCatalog;
        this.messageSource = messageSource;
        this.databaseMessageSource = databaseMessageSource;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Page<LocalizedMessageSummary> list(
            Long localeId,
            int requestedPage,
            int pageSize,
            String search) {
        SupportedLocale locale = findLocale(localeId);
        Map<String, String> overrides = new LinkedHashMap<>();
        messageRepository.findAllByLocaleIdOrderByMessageKeyAsc(localeId)
                .forEach(message -> overrides.put(message.getMessageKey(), message.getMessageValue()));

        Set<String> keys = new TreeSet<>(messageKeyCatalog.keys());
        keys.addAll(overrides.keySet());
        String normalizedSearch = normalizeSearch(search);
        Locale targetLocale = locale.toLocale();
        ArrayList<LocalizedMessageSummary> matches = new ArrayList<>();
        for (String key : keys) {
            String resolved = overrides.containsKey(key)
                    ? overrides.get(key)
                    : messageSource.getMessage(key, null, key, targetLocale);
            if (normalizedSearch.isBlank()
                    || key.toLowerCase(Locale.ROOT).contains(normalizedSearch)
                    || resolved.toLowerCase(Locale.ROOT).contains(normalizedSearch)) {
                matches.add(new LocalizedMessageSummary(key, resolved, overrides.containsKey(key)));
            }
        }

        int page = Math.max(0, requestedPage);
        PageRequest pageable = PageRequest.of(page, pageSize);
        int fromIndex = (int) Math.min(pageable.getOffset(), matches.size());
        int toIndex = Math.min(fromIndex + pageSize, matches.size());
        return new PageImpl<>(matches.subList(fromIndex, toIndex), pageable, matches.size());
    }

    @Transactional(readOnly = true)
    public LocalizedMessageForm formFor(Long localeId, String messageKey) {
        SupportedLocale locale = findLocale(localeId);
        LocalizedMessageForm form = new LocalizedMessageForm();
        if (messageKey == null || messageKey.isBlank()) {
            return form;
        }
        String normalizedKey = validateMessageKey(messageKey);
        String value = messageRepository.findByLocaleIdAndMessageKey(localeId, normalizedKey)
                .map(LocalizedMessage::getMessageValue)
                .orElseGet(() -> messageSource.getMessage(
                        normalizedKey,
                        null,
                        normalizedKey,
                        locale.toLocale()));
        form.setMessageKey(normalizedKey);
        form.setMessageValue(value);
        return form;
    }

    @Transactional
    public LocalizedMessageSummary save(Long localeId, LocalizedMessageForm form) {
        SupportedLocale locale = findLocale(localeId);
        String messageKey = validateMessageKey(form.getMessageKey());
        String messageValue = validateMessageValue(form.getMessageValue(), locale.toLocale());
        try {
            LocalizedMessage message = messageRepository.findByLocaleIdAndMessageKey(localeId, messageKey)
                    .orElseGet(() -> LocalizedMessage.create(
                            locale,
                            messageKey,
                            messageValue,
                            clock.instant()));
            if (message.getId() != null) {
                message.update(messageValue, clock.instant());
            }
            LocalizedMessage saved = messageRepository.saveAndFlush(message);
            clearMessageCacheAfterCommit();
            return new LocalizedMessageSummary(saved.getMessageKey(), saved.getMessageValue(), true);
        } catch (DataIntegrityViolationException exception) {
            throw new LocalizationOperationException("localization.error.invalidStoredValue", exception);
        }
    }

    @Transactional
    public void delete(Long localeId, String messageKey) {
        findLocale(localeId);
        String normalizedKey = validateMessageKey(messageKey);
        LocalizedMessage message = messageRepository.findByLocaleIdAndMessageKey(localeId, normalizedKey)
                .orElseThrow(() -> new LocalizationOperationException("localization.error.messageNotFound"));
        messageRepository.delete(message);
        messageRepository.flush();
        clearMessageCacheAfterCommit();
    }

    private SupportedLocale findLocale(Long localeId) {
        return localeRepository.findById(localeId)
                .orElseThrow(() -> new LocalizationOperationException("localization.error.notFound"));
    }

    private String validateMessageKey(String candidate) {
        String messageKey = candidate == null ? "" : candidate.strip();
        if (!MESSAGE_KEY.matcher(messageKey).matches()) {
            throw new LocalizationOperationException("localization.error.invalidMessageKey");
        }
        return messageKey;
    }

    private String validateMessageValue(String candidate, Locale locale) {
        String messageValue = candidate == null ? "" : candidate.strip();
        if (messageValue.isBlank()) {
            throw new LocalizationOperationException("localization.error.blankMessage");
        }
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
        return messageValue;
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
                    databaseMessageSource.clearCache();
                }
            });
        } else {
            databaseMessageSource.clearCache();
        }
    }
}
