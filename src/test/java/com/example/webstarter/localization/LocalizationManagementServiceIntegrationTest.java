package com.example.webstarter.localization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        LocalizedMessageForm form = new LocalizedMessageForm();
        form.setMessageKey("custom.greeting");
        form.setMessageValue("最初のメッセージ");

        translationCatalogService.save(japanese.id(), form);
        assertThat(messageSource.getMessage(
                "custom.greeting",
                null,
                "fallback",
                Locale.JAPANESE))
                .isEqualTo("最初のメッセージ");

        form.setMessageValue("更新したメッセージ");
        translationCatalogService.save(japanese.id(), form);
        assertThat(messageSource.getMessage(
                "custom.greeting",
                null,
                "fallback",
                Locale.JAPANESE))
                .isEqualTo("更新したメッセージ");

        translationCatalogService.delete(japanese.id(), "custom.greeting");
        assertThat(messageSource.getMessage(
                "custom.greeting",
                null,
                "fallback",
                Locale.JAPANESE))
                .isEqualTo("fallback");
    }

    @Test
    void translatedMessagesRejectHtmlAndInvalidMessageFormats() {
        SupportedLocaleSummary chinese = localeCatalogService.create(localeForm(
                "zh",
                "Chinese",
                "中文",
                100));

        LocalizedMessageForm html = new LocalizedMessageForm();
        html.setMessageKey("custom.unsafe");
        html.setMessageValue("<strong>unsafe</strong>");
        assertThatThrownBy(() -> translationCatalogService.save(chinese.id(), html))
                .isInstanceOf(LocalizationOperationException.class)
                .hasMessage("localization.error.htmlNotAllowed");

        LocalizedMessageForm invalidFormat = new LocalizedMessageForm();
        invalidFormat.setMessageKey("custom.invalid");
        invalidFormat.setMessageValue("Broken placeholder {0");
        assertThatThrownBy(() -> translationCatalogService.save(chinese.id(), invalidFormat))
                .isInstanceOf(LocalizationOperationException.class)
                .hasMessage("localization.error.invalidPattern");
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
