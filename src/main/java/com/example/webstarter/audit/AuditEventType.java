package com.example.webstarter.audit;

import java.util.Locale;

public enum AuditEventType {
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    LOGIN_INPUT_REJECTED,
    LOGIN_RATE_LIMITED,
    LOGOUT,
    REGISTRATION_SUCCESS,
    REGISTRATION_FAILURE,
    PASSWORD_CHANGED,
    USER_ENABLED,
    USER_DISABLED,
    USER_UNLOCKED,
    USER_ROLE_CHANGED,
    SESSION_TERMINATED,
    MENU_CREATED,
    MENU_UPDATED,
    MENU_ENABLED,
    MENU_DISABLED,
    LOCALE_CREATED,
    LOCALE_UPDATED,
    LOCALE_ENABLED,
    LOCALE_DISABLED,
    LOCALE_DEFAULT_CHANGED,
    TRANSLATION_UPDATED,
    TRANSLATION_DELETED,
    ACCESS_DENIED,
    BOOTSTRAP_ADMIN_CREATED;

    public String getMessageCode() {
        return "audit.event." + name().toLowerCase(Locale.ROOT).replace('_', '.');
    }
}
