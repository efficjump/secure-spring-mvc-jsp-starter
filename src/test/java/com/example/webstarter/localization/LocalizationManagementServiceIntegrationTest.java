package com.example.webstarter.localization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser(username = "localization-admin", roles = "ADMIN")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class LocalizationManagementServiceIntegrationTest {

    @Autowired
    private LocaleCatalogService localeCatalogService;

    @Autowired
    private TranslationCatalogService translationCatalogService;

    @Autowired
    private SupportedLocaleRepository localeRepository;

    @Autowired
    private LocalizedMessageRepository messageRepository;

    @Autowired
    private MessageSource messageSource;

    @AfterEach
    void cleanCatalog() {
        messageRepository.deleteAll();
        localeRepository.deleteAll();
    }

    @Test
    void defaultLocaleRemainsEnabledUntilAnotherDefaultIsSelected() {
        SupportedLocaleSummary english = localeCatalogService.create(localeForm(
                "en",
                "English",
                "English",
                100));
        SupportedLocaleSummary korean = localeCatalogService.create(localeForm(
                "ko",
                "Korean",
                "한국어",
                200));

        localeCatalogService.makeDefault(english.id());

        assertThat(localeCatalogService.defaultLocale()).isEqualTo(Locale.ENGLISH);
        assertThatThrownBy(() -> localeCatalogService.toggleEnabled(english.id()))
                .isInstanceOf(LocalizationOperationException.class)
                .hasMessage("localization.error.defaultRequired");

        localeCatalogService.makeDefault(korean.id());
        SupportedLocaleSummary disabledEnglish = localeCatalogService.toggleEnabled(english.id());

        assertThat(disabledEnglish.enabled()).isFalse();
        assertThat(localeCatalogService.defaultLocale()).isEqualTo(Locale.KOREAN);
    }

    @Test
    void canonicalLanguageTagsCannotBeRegisteredTwice() {
        localeCatalogService.create(localeForm(
                "en-US",
                "English (United States)",
                "English (United States)",
                100));

        assertThatThrownBy(() -> localeCatalogService.create(localeForm(
                "EN-us",
                "Duplicate",
                "Duplicate",
                200)))
                .isInstanceOf(LocalizationOperationException.class)
                .hasMessage("localization.error.duplicateTag");
    }

    @Test
    void databaseTranslationChangesInvalidateTheRuntimeMessageCache() {
        SupportedLocaleSummary japanese = localeCatalogService.create(localeForm(
                "ja",
                "Japanese",
                "日本語",
                100));
        TranslationGridRowForm form = translationForm(
                "custom.greeting",
                japanese.id(),
                "最初のメッセージ");

        translationCatalogService.saveRow(form);
        assertThat(messageSource.getMessage(
                "custom.greeting",
                null,
                "fallback",
                Locale.JAPANESE))
                .isEqualTo("最初のメッセージ");

        form.getValues().put(japanese.id(), "更新したメッセージ");
        translationCatalogService.saveRow(form);
        assertThat(messageSource.getMessage(
                "custom.greeting",
                null,
                "fallback",
                Locale.JAPANESE))
                .isEqualTo("更新したメッセージ");

        form.getValues().put(japanese.id(), "");
        translationCatalogService.saveRow(form);
        assertThat(messageSource.getMessage(
                "custom.greeting",
                null,
                "fallback",
                Locale.JAPANESE))
                .isEqualTo("fallback");
    }

    @Test
    void translationGridListsEveryLocaleBesideTheMessageKey() {
        SupportedLocaleSummary english = localeCatalogService.create(localeForm(
                "en",
                "English",
                "English",
                100));
        SupportedLocaleSummary korean = localeCatalogService.create(localeForm(
                "ko",
                "Korean",
                "한국어",
                200));

        var page = translationCatalogService.listGrid(
                List.of(english, korean),
                0,
                20,
                "home.heading");

        assertThat(page.getContent()).singleElement().satisfies(row -> {
            assertThat(row.messageKey()).isEqualTo("home.heading");
            assertThat(row.cells())
                    .extracting(TranslationGridCell::languageTag)
                    .containsExactly("en", "ko");
            assertThat(row.cells())
                    .extracting(TranslationGridCell::value)
                    .containsExactly("Spring MVC Starter", "Spring MVC Starter");
        });
    }

    @Test
    void translatedMessagesRejectHtmlAndInvalidMessageFormats() {
        SupportedLocaleSummary chinese = localeCatalogService.create(localeForm(
                "zh",
                "Chinese",
                "中文",
                100));

        TranslationGridRowForm html = translationForm(
                "custom.unsafe",
                chinese.id(),
                "<strong>unsafe</strong>");
        assertThatThrownBy(() -> translationCatalogService.saveRow(html))
                .isInstanceOf(LocalizationOperationException.class)
                .hasMessage("localization.error.htmlNotAllowed");

        TranslationGridRowForm invalidFormat = translationForm(
                "custom.invalid",
                chinese.id(),
                "Broken placeholder {0");
        assertThatThrownBy(() -> translationCatalogService.saveRow(invalidFormat))
                .isInstanceOf(LocalizationOperationException.class)
                .hasMessage("localization.error.invalidPattern");
    }

    private TranslationGridRowForm translationForm(
            String messageKey,
            Long localeId,
            String messageValue) {
        TranslationGridRowForm form = new TranslationGridRowForm();
        form.setMessageKey(messageKey);
        form.getValues().put(localeId, messageValue);
        return form;
    }

    private SupportedLocaleForm localeForm(
            String languageTag,
            String displayName,
            String nativeName,
            int displayOrder) {
        SupportedLocaleForm form = new SupportedLocaleForm();
        form.setLanguageTag(languageTag);
        form.setDisplayName(displayName);
        form.setNativeName(nativeName);
        form.setDisplayOrder(displayOrder);
        form.setEnabled(true);
        return form;
    }
}
