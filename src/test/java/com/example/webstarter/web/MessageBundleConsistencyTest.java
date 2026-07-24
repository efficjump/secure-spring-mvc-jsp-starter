package com.example.webstarter.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import com.example.webstarter.audit.AuditEventType;
import com.example.webstarter.audit.AuditOutcome;
import com.example.webstarter.navigation.NavigationIcon;
import com.example.webstarter.user.Role;

import org.junit.jupiter.api.Test;

class MessageBundleConsistencyTest {

    @Test
    void allSupportedBundlesHaveMatchingNonBlankKeys() throws IOException {
        List<Properties> bundles = loadSupportedBundles();
        Properties english = bundles.getFirst();

        bundles.forEach(bundle -> {
            assertThat(bundle.stringPropertyNames())
                    .containsExactlyInAnyOrderElementsOf(english.stringPropertyNames());
            english.stringPropertyNames()
                    .forEach(key -> assertThat(bundle.getProperty(key)).as(key).isNotBlank());
        });
    }

    @Test
    void everyDynamicEnumMessageCodeExistsInAllBundles() throws IOException {
        Properties[] bundles = loadSupportedBundles().toArray(Properties[]::new);

        Arrays.stream(AuditEventType.values())
                .map(AuditEventType::getMessageCode)
                .forEach(code -> assertCodeExists(code, bundles));
        Arrays.stream(AuditOutcome.values())
                .map(AuditOutcome::getMessageCode)
                .forEach(code -> assertCodeExists(code, bundles));
        Arrays.stream(NavigationIcon.values())
                .map(NavigationIcon::getMessageCode)
                .forEach(code -> assertCodeExists(code, bundles));
        Arrays.stream(Role.values())
                .map(Role::getMessageCode)
                .forEach(code -> assertCodeExists(code, bundles));
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

    private List<Properties> loadSupportedBundles() throws IOException {
        return List.of(
                load("messages.properties"),
                load("messages_ko.properties"),
                load("messages_zh.properties"),
                load("messages_ja.properties"));
    }

    private void assertCodeExists(String code, Properties... bundles) {
        Arrays.stream(bundles)
                .forEach(bundle -> assertThat(bundle.getProperty(code)).as(code).isNotBlank());
    }
}
