package com.example.webstarter.localization;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LocalizedMessageRepository extends JpaRepository<LocalizedMessage, Long> {

    Optional<LocalizedMessage> findByLocaleIdAndMessageKey(Long localeId, String messageKey);

    List<LocalizedMessage> findAllByLocaleIdOrderByMessageKeyAsc(Long localeId);

    void deleteAllByLocaleId(Long localeId);

    @Query("""
            select m.messageValue
            from LocalizedMessage m
            join m.locale l
            where lower(l.languageTag) = lower(:languageTag)
              and l.enabled = true
              and m.messageKey = :messageKey
            """)
    Optional<String> findEnabledValue(
            @Param("languageTag") String languageTag,
            @Param("messageKey") String messageKey);
}
