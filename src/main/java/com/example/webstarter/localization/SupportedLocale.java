package com.example.webstarter.localization;

import java.time.Instant;
import java.util.Locale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "supported_locales")
public class SupportedLocale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "language_tag", nullable = false, unique = true, length = 35)
    private String languageTag;

    @Column(name = "display_name", nullable = false, length = 80)
    private String displayName;

    @Column(name = "native_name", nullable = false, length = 80)
    private String nativeName;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "default_locale", nullable = false)
    private boolean defaultLocale;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected SupportedLocale() {
    }

    private SupportedLocale(
            String languageTag,
            String displayName,
            String nativeName,
            int displayOrder,
            boolean enabled,
            Instant now) {
        this.languageTag = languageTag;
        this.displayName = displayName;
        this.nativeName = nativeName;
        this.displayOrder = displayOrder;
        this.enabled = enabled;
        this.defaultLocale = false;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static SupportedLocale create(
            String languageTag,
            SupportedLocaleForm form,
            Instant now) {
        return new SupportedLocale(
                languageTag,
                form.getDisplayName(),
                form.getNativeName(),
                form.getDisplayOrder(),
                form.isEnabled(),
                now);
    }

    public void update(
            String languageTag,
            SupportedLocaleForm form,
            Instant now) {
        this.languageTag = languageTag;
        displayName = form.getDisplayName();
        nativeName = form.getNativeName();
        displayOrder = form.getDisplayOrder();
        enabled = form.isEnabled();
        updatedAt = now;
    }

    public void toggleEnabled(Instant now) {
        enabled = !enabled;
        updatedAt = now;
    }

    public void makeDefault(Instant now) {
        enabled = true;
        defaultLocale = true;
        updatedAt = now;
    }

    public void clearDefault(Instant now) {
        if (defaultLocale) {
            defaultLocale = false;
            updatedAt = now;
        }
    }

    public Long getId() {
        return id;
    }

    public String getLanguageTag() {
        return languageTag;
    }

    public Locale toLocale() {
        return Locale.forLanguageTag(languageTag);
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getNativeName() {
        return nativeName;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isDefaultLocale() {
        return defaultLocale;
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
