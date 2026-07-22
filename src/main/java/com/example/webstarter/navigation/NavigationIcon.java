package com.example.webstarter.navigation;

public enum NavigationIcon {
    DASHBOARD("dashboard", "대시보드"),
    USERS("users", "사용자"),
    LOGIN_HISTORY("login-history", "로그인 이력"),
    SESSIONS("sessions", "세션"),
    MENUS("menus", "메뉴"),
    SECURITY("security", "보안"),
    DOCUMENT("document", "문서"),
    SETTINGS("settings", "설정");

    private final String symbolId;
    private final String label;

    NavigationIcon(String symbolId, String label) {
        this.symbolId = symbolId;
        this.label = label;
    }

    public String getSymbolId() {
        return symbolId;
    }

    public String getLabel() {
        return label;
    }
}
