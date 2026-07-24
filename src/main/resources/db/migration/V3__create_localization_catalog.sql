CREATE TABLE supported_locales (
    id BIGINT NOT NULL AUTO_INCREMENT,
    language_tag VARCHAR(35) NOT NULL,
    display_name VARCHAR(80) NOT NULL,
    native_name VARCHAR(80) NOT NULL,
    display_order INT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    default_locale BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_supported_locales_language_tag UNIQUE (language_tag),
    CONSTRAINT ck_supported_locales_order CHECK (display_order BETWEEN 0 AND 9999),
    INDEX ix_supported_locales_enabled_order (enabled, display_order, id),
    INDEX ix_supported_locales_default (default_locale, enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE localized_messages (
    id BIGINT NOT NULL AUTO_INCREMENT,
    locale_id BIGINT NOT NULL,
    message_key VARCHAR(190) NOT NULL,
    message_value VARCHAR(4000) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_localized_messages_locale_key UNIQUE (locale_id, message_key),
    CONSTRAINT fk_localized_messages_locale
        FOREIGN KEY (locale_id) REFERENCES supported_locales (id) ON DELETE CASCADE,
    INDEX ix_localized_messages_key (message_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO supported_locales
    (language_tag, display_name, native_name, display_order, enabled, default_locale, created_at, updated_at, version)
VALUES
    ('en', 'English', 'English', 100, TRUE, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0),
    ('ko', 'Korean', '한국어', 200, TRUE, FALSE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0),
    ('zh', 'Chinese', '中文', 300, TRUE, FALSE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0),
    ('ja', 'Japanese', '日本語', 400, TRUE, FALSE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0);

INSERT INTO navigation_menus
    (menu_key, label, menu_group, path, icon, required_role, display_order, enabled, created_at, updated_at, version)
VALUES
    ('localization-management', 'Localization', 'System settings', '/admin/locales', 'LANGUAGES', 'ADMIN', 310, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 0);
