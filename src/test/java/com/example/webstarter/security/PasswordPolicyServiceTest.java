package com.example.webstarter.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PasswordPolicyServiceTest {

    private final PasswordPolicyService service =
            new PasswordPolicyService(SecurityPropertiesFixture.properties());

    @Test
    void acceptsLongUnicodePassphrase() {
        assertThat(service.validate("긴 문장형 암호 2026 바다와구름", "starter", "owner@example.com"))
                .isEmpty();
    }

    @Test
    void rejectsIdentityFragmentsAndControlCharacters() {
        assertThat(service.validate("Starter\n-secret", "starter", "owner@example.com"))
                .contains(
                        PasswordPolicyViolation.CONTAINS_USERNAME,
                        PasswordPolicyViolation.CONTROL_CHARACTER);
    }

    @Test
    void reportsConfiguredLengthBounds() {
        assertThat(service.validate("short", "user", "user@example.com"))
                .contains(PasswordPolicyViolation.TOO_SHORT);
        assertThat(service.validate("x".repeat(129), "user", "user@example.com"))
                .contains(PasswordPolicyViolation.TOO_LONG);
    }
}

