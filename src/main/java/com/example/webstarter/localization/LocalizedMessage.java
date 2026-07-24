package com.example.webstarter.localization;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "localized_messages")
public class LocalizedMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "locale_id", nullable = false)
    private SupportedLocale locale;

    @Column(name = "message_key", nullable = false, length = 190)
    private String messageKey;

    @Column(name = "message_value", nullable = false, length = 4000)
    private String messageValue;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected LocalizedMessage() {
    }

    private LocalizedMessage(
            SupportedLocale locale,
            String messageKey,
            String messageValue,
            Instant now) {
        this.locale = locale;
        this.messageKey = messageKey;
        this.messageValue = messageValue;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static LocalizedMessage create(
            SupportedLocale locale,
            String messageKey,
            String messageValue,
            Instant now) {
        return new LocalizedMessage(locale, messageKey, messageValue, now);
    }

    public void update(String messageValue, Instant now) {
        this.messageValue = messageValue;
        updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public SupportedLocale getLocale() {
        return locale;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getMessageValue() {
        return messageValue;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }
}
