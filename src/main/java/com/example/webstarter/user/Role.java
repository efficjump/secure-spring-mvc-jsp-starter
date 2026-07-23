package com.example.webstarter.user;

import java.util.Locale;

public enum Role {
    USER,
    ADMIN;

    public String authority() {
        return "ROLE_" + name();
    }

    public String getMessageCode() {
        return "role." + name().toLowerCase(Locale.ROOT);
    }
}
