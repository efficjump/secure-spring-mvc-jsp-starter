package com.example.webstarter.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;

import com.example.webstarter.audit.AuditEventType;
import com.example.webstarter.audit.AuditOutcome;
import com.example.webstarter.navigation.NavigationIcon;
import com.example.webstarter.user.Role;

import org.junit.jupiter.api.Test;

class MessageBundleConsistencyTest {

    @Test
    void koreanAndEnglishBundlesHaveMatchingNonBlankKeys() throws IOException {
        Properties korean = load("messages.properties");
        Properties english = load("messages_en.properties");

        assertThat(english.stringPropertyNames()).containsExactlyInAnyOrderElementsOf(korean.stringPropertyNames());
        assertThat(korean.stringPropertyNames())
                .allSatisfy(key -> {
                    assertThat(korean.getProperty(key)).as("Korean value for %s", key).isNotBlank();
                    assertThat(english.getProperty(key)).as("English value for %s", key).isNotBlank();
                });
    }

    @Test
    void everyDynamicEnumMessageCodeExistsInBothBundles() throws IOException {
        Properties korean = load("messages.properties");
        Properties english = load("messages_en.properties");

        Arrays.stream(AuditEventType.values())
                .map(AuditEventType::getMessageCode)
                .forEach(code -> assertCodeExists(code, korean, english));
        Arrays.stream(AuditOutcome.values())
                .map(AuditOutcome::getMessageCode)
                .forEach(code -> assertCodeExists(code, korean, english));
        Arrays.stream(NavigationIcon.values())
                .map(NavigationIcon::getMessageCode)
                .forEach(code -> assertCodeExists(code, korean, english));
        Arrays.stream(Role.values())
                .map(Role::getMessageCode)
                .forEach(code -> assertCodeExists(code, korean, english));
    }

    @Test
    void menuLocalizationUsesConventionAndKeepsCustomFallbackText() {
        org.springframework.context.support.ResourceBundleMessageSource messageSource =
                new org.springframework.context.support.ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setFallbackToSystemLocale(false);

        com.example.webstarter.navigation.NavigationMenuLocalizer localizer =
                new com.example.webstarter.navigation.NavigationMenuLocalizer(messageSource);
        com.example.webstarter.navigation.NavigationMenuSummary builtIn =
                new com.example.webstarter.navigation.NavigationMenuSummary(
                        1L,
                        "dashboard",
                        "Stored label",
                        "Stored group",
                        "/dashboard",
                        NavigationIcon.DASHBOARD,
                        Role.USER,
                        100,
                        true);
        com.example.webstarter.navigation.NavigationMenuSummary custom =
                new com.example.webstarter.navigation.NavigationMenuSummary(
                        2L,
                        "reports",
                        "Custom reports",
                        "Custom group",
                        "/reports",
                        NavigationIcon.DOCUMENT,
                        Role.USER,
                        200,
                        true);

        assertThat(localizer.localize(builtIn, Locale.ENGLISH))
                .extracting(
                        com.example.webstarter.navigation.NavigationMenuSummary::label,
                        com.example.webstarter.navigation.NavigationMenuSummary::menuGroup)
                .containsExactly("Business status", "Workspace");
        assertThat(localizer.localize(custom, Locale.ENGLISH))
                .extracting(
                        com.example.webstarter.navigation.NavigationMenuSummary::label,
                        com.example.webstarter.navigation.NavigationMenuSummary::menuGroup)
                .containsExactly("Custom reports", "Custom group");
    }

    private Properties load(String resourceName) throws IOException {
        Properties properties = new Properties();
        try (InputStreamReader reader = new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(resourceName),
                StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties;
    }

    private void assertCodeExists(String code, Properties... bundles) {
        Arrays.stream(bundles)
                .forEach(bundle -> assertThat(bundle.getProperty(code)).as(code).isNotBlank());
    }
}
