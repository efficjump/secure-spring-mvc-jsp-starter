package com.example.webstarter.user;

import java.text.Normalizer;
import java.util.Locale;

public final class UserIdentityNormalizer {

    public static final int USERNAME_MAX_LENGTH = 64;

    private UserIdentityNormalizer() {
    }

    public static String username(String value) {
        return normalize(value);
    }

    public static String email(String value) {
        return normalize(value);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value.strip(), Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
    }
}
