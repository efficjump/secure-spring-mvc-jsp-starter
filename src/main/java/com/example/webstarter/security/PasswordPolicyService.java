package com.example.webstarter.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.example.webstarter.config.SecurityProperties;
import com.example.webstarter.user.UserIdentityNormalizer;

import org.springframework.stereotype.Service;

@Service
public class PasswordPolicyService {

    private final SecurityProperties properties;

    public PasswordPolicyService(SecurityProperties properties) {
        this.properties = properties;
    }

    public List<PasswordPolicyViolation> validate(String password, String username, String email) {
        List<PasswordPolicyViolation> violations = new ArrayList<>();
        if (password == null || password.isBlank()) {
            violations.add(PasswordPolicyViolation.BLANK);
            return Collections.unmodifiableList(violations);
        }

        int length = password.codePointCount(0, password.length());
        if (length < properties.password().minLength()) {
            violations.add(PasswordPolicyViolation.TOO_SHORT);
        }
        if (length > properties.password().maxLength()) {
            violations.add(PasswordPolicyViolation.TOO_LONG);
        }
        if (password.codePoints().anyMatch(Character::isISOControl)) {
            violations.add(PasswordPolicyViolation.CONTROL_CHARACTER);
        }

        String foldedPassword = password.toLowerCase(Locale.ROOT);
        String normalizedUsername = UserIdentityNormalizer.username(username);
        String normalizedEmail = UserIdentityNormalizer.email(email);
        if (normalizedUsername.length() >= 3 && foldedPassword.contains(normalizedUsername)) {
            violations.add(PasswordPolicyViolation.CONTAINS_USERNAME);
        }
        if (normalizedEmail.length() >= 3 && foldedPassword.contains(normalizedEmail)) {
            violations.add(PasswordPolicyViolation.CONTAINS_EMAIL);
        }

        return Collections.unmodifiableList(violations);
    }

    public boolean isValid(String password, String username, String email) {
        return validate(password, username, email).isEmpty();
    }
}

