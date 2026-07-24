package com.example.webstarter.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import com.example.webstarter.audit.AuditEventType;
import com.example.webstarter.audit.AuditOutcome;
import com.example.webstarter.audit.RequestMetadata;
import com.example.webstarter.audit.SecurityAuditService;
import com.example.webstarter.config.AdminProperties;
import com.example.webstarter.localization.LocalizedMessageForm;
import com.example.webstarter.localization.LocalizedMessageSummary;
import com.example.webstarter.localization.LocaleCatalogService;
import com.example.webstarter.localization.LocalizationOperationException;
import com.example.webstarter.localization.SupportedLocaleForm;
import com.example.webstarter.localization.SupportedLocaleSummary;
import com.example.webstarter.localization.TranslationCatalogService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/locales")
public class AdminLocalizationController {

    private static final Logger log = LoggerFactory.getLogger(AdminLocalizationController.class);

    private final LocaleCatalogService localeCatalogService;
    private final TranslationCatalogService translationCatalogService;
    private final SecurityAuditService auditService;
    private final AdminProperties adminProperties;

    public AdminLocalizationController(
            LocaleCatalogService localeCatalogService,
            TranslationCatalogService translationCatalogService,
            SecurityAuditService auditService,
            AdminProperties adminProperties) {
        this.localeCatalogService = localeCatalogService;
        this.translationCatalogService = translationCatalogService;
        this.auditService = auditService;
        this.adminProperties = adminProperties;
    }

    @GetMapping
    public String locales(
            @RequestParam(required = false) Long edit,
            Model model) {
        if (!model.containsAttribute("localeForm")) {
            model.addAttribute(
                    "localeForm",
                    edit == null ? new SupportedLocaleForm() : localeCatalogService.formFor(edit));
        }
        return renderLocales(model, edit);
    }

    @PostMapping
    public String createLocale(
            @Valid @ModelAttribute("localeForm") SupportedLocaleForm form,
            BindingResult bindingResult,
            Authentication authentication,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return renderLocales(model, null);
        }
        try {
            SupportedLocaleSummary created = localeCatalogService.create(form);
            audit(AuditEventType.LOCALE_CREATED, authentication.getName(), created.languageTag(), request);
            redirectAttributes.addFlashAttribute("message", "admin.locale.created");
            redirectAttributes.addAttribute("edit", created.id());
            return "redirect:/admin/locales";
        } catch (LocalizationOperationException exception) {
            bindingResult.reject(exception.getMessage());
            return renderLocales(model, null);
        }
    }

    @PostMapping("/{localeId}")
    public String updateLocale(
            @PathVariable Long localeId,
            @Valid @ModelAttribute("localeForm") SupportedLocaleForm form,
            BindingResult bindingResult,
            Authentication authentication,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return renderLocales(model, localeId);
        }
        try {
            SupportedLocaleSummary updated = localeCatalogService.update(localeId, form);
            audit(AuditEventType.LOCALE_UPDATED, authentication.getName(), updated.languageTag(), request);
            redirectAttributes.addFlashAttribute("message", "admin.locale.updated");
            redirectAttributes.addAttribute("edit", localeId);
            return "redirect:/admin/locales";
        } catch (LocalizationOperationException exception) {
            bindingResult.reject(exception.getMessage());
            return renderLocales(model, localeId);
        }
    }

    @PostMapping("/{localeId}/toggle")
    public String toggleLocale(
            @PathVariable Long localeId,
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            SupportedLocaleSummary locale = localeCatalogService.toggleEnabled(localeId);
            AuditEventType eventType = locale.enabled()
                    ? AuditEventType.LOCALE_ENABLED
                    : AuditEventType.LOCALE_DISABLED;
            audit(eventType, authentication.getName(), locale.languageTag(), request);
            redirectAttributes.addFlashAttribute(
                    "message",
                    locale.enabled() ? "admin.locale.enabled" : "admin.locale.disabled");
        } catch (LocalizationOperationException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/locales";
    }

    @PostMapping("/{localeId}/default")
    public String makeDefaultLocale(
            @PathVariable Long localeId,
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            SupportedLocaleSummary locale = localeCatalogService.makeDefault(localeId);
            audit(AuditEventType.LOCALE_DEFAULT_CHANGED, authentication.getName(), locale.languageTag(), request);
            redirectAttributes.addFlashAttribute("message", "admin.locale.default.updated");
        } catch (LocalizationOperationException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/locales";
    }

    @GetMapping("/{localeId}/messages")
    public String messages(
            @PathVariable Long localeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) String edit,
            Model model) {
        if (!model.containsAttribute("messageForm")) {
            model.addAttribute("messageForm", translationCatalogService.formFor(localeId, edit));
        }
        return renderMessages(model, localeId, page, search, edit);
    }

    @PostMapping("/{localeId}/messages")
    public String saveMessage(
            @PathVariable Long localeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String search,
            @Valid @ModelAttribute("messageForm") LocalizedMessageForm form,
            BindingResult bindingResult,
            Authentication authentication,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return renderMessages(model, localeId, page, search, form.getMessageKey());
        }
        try {
            LocalizedMessageSummary saved = translationCatalogService.save(localeId, form);
            SupportedLocaleSummary locale = localeCatalogService.get(localeId);
            audit(
                    AuditEventType.TRANSLATION_UPDATED,
                    authentication.getName(),
                    locale.languageTag() + ":" + saved.messageKey(),
                    request);
            redirectAttributes.addFlashAttribute("message", "admin.translation.updated");
            redirectAttributes.addAttribute("edit", saved.messageKey());
            if (!search.isBlank()) {
                redirectAttributes.addAttribute("search", search);
            }
            return "redirect:/admin/locales/" + localeId + "/messages";
        } catch (LocalizationOperationException exception) {
            bindingResult.reject(exception.getMessage());
            return renderMessages(model, localeId, page, search, form.getMessageKey());
        }
    }

    @PostMapping("/{localeId}/messages/delete")
    public String deleteMessage(
            @PathVariable Long localeId,
            @RequestParam String messageKey,
            @RequestParam(defaultValue = "") String search,
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            SupportedLocaleSummary locale = localeCatalogService.get(localeId);
            translationCatalogService.delete(localeId, messageKey);
            audit(
                    AuditEventType.TRANSLATION_DELETED,
                    authentication.getName(),
                    locale.languageTag() + ":" + messageKey,
                    request);
            redirectAttributes.addFlashAttribute("message", "admin.translation.deleted");
        } catch (LocalizationOperationException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        if (!search.isBlank()) {
            redirectAttributes.addAttribute("search", search);
        }
        return "redirect:/admin/locales/" + localeId + "/messages";
    }

    private String renderLocales(Model model, Long editingLocaleId) {
        model.addAttribute("locales", localeCatalogService.listAll());
        model.addAttribute("editingLocaleId", editingLocaleId);
        return "admin/locales";
    }

    private String renderMessages(
            Model model,
            Long localeId,
            int page,
            String search,
            String editingMessageKey) {
        SupportedLocaleSummary locale = localeCatalogService.get(localeId);
        model.addAttribute("locale", locale);
        model.addAttribute(
                "messages",
                translationCatalogService.list(
                        localeId,
                        page,
                        adminProperties.pageSize(),
                        search));
        model.addAttribute("search", search);
        model.addAttribute("editingMessageKey", editingMessageKey);
        return "admin/translations";
    }

    private void audit(
            AuditEventType type,
            String actor,
            String subject,
            HttpServletRequest request) {
        try {
            auditService.record(
                    type,
                    AuditOutcome.SUCCESS,
                    actor,
                    subject,
                    RequestMetadata.from(request),
                    null);
        } catch (RuntimeException exception) {
            log.error("Unable to persist localization administration audit event", exception);
        }
    }
}
