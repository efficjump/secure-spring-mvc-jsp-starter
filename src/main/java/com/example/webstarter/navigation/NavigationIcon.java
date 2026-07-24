package com.example.webstarter.navigation;

import java.util.Locale;

public enum NavigationIcon {
    DASHBOARD("dashboard"),
    USERS("users"),
    LOGIN_HISTORY("login-history"),
    SESSIONS("sessions"),
    MENUS("menus"),
    LANGUAGES("languages"),
    SECURITY("security"),
    DOCUMENT("document"),
    SETTINGS("settings");

    private final String symbolId;

    NavigationIcon(String symbolId) {
        this.symbolId = symbolId;
    }

    public String getSymbolId() {
        return symbolId;
    }

    public String getMessageCode() {
        return "navigation.icon." + name().toLowerCase(Locale.ROOT).replace('_', '.');
    }
}
