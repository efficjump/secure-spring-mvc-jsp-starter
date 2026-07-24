package com.example.webstarter.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import com.example.webstarter.audit.AuditEventType;
import com.example.webstarter.audit.AuditOutcome;
import com.example.webstarter.audit.RequestMetadata;
import com.example.webstarter.audit.SecurityAuditService;
import com.example.webstarter.config.AdminProperties;
import com.example.webstarter.localization.LocaleCatalogService;
import com.example.webstarter.localization.LocalizationOperationException;
import com.example.webstarter.localization.SupportedLocaleForm;
import com.example.webstarter.localization.SupportedLocaleSummary;
import com.example.webstarter.localization.TranslationCatalogService;
import com.example.webstarter.localization.TranslationGridRow;
import com.example.webstarter.localization.TranslationGridRowForm;
import com.example.webstarter.localization.TranslationRowUpdateResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
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

    @GetMapping("/messages")
    public String messages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String search,
            Model model) {
        if (!model.containsAttribute("translationRowForm")) {
            model.addAttribute("translationRowForm", new TranslationGridRowForm());
        }
        return renderMessages(model, page, search, null);
    }

    @PostMapping("/messages")
    public String saveMessageRow(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String search,
            @Valid @ModelAttribute("translationRowForm") TranslationGridRowForm form,
            BindingResult bindingResult,
            Authentication authentication,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return renderMessages(model, page, search, form.getMessageKey());
        }
        try {
            TranslationRowUpdateResult result = translationCatalogService.saveRow(form);
            if (result.changed()) {
                AuditEventType eventType = result.updatedCount() > 0
                        ? AuditEventType.TRANSLATION_UPDATED
                        : AuditEventType.TRANSLATION_DELETED;
                audit(eventType, authentication.getName(), result.messageKey(), request);
                redirectAttributes.addFlashAttribute("message", "admin.translation.row.updated");
            } else {
                redirectAttributes.addFlashAttribute("message", "admin.translation.noChanges");
            }
            redirectAttributes.addAttribute("page", Math.max(0, page));
            if (!search.isBlank()) {
                redirectAttributes.addAttribute("search", search);
            }
            return "redirect:/admin/locales/messages";
        } catch (LocalizationOperationException exception) {
            bindingResult.reject(exception.getMessage());
            return renderMessages(model, page, search, form.getMessageKey());
        }
    }

    @GetMapping("/{localeId}/messages")
    public String legacyMessages(
            @PathVariable Long localeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String search,
            RedirectAttributes redirectAttributes) {
        localeCatalogService.get(localeId);
        redirectAttributes.addAttribute("page", Math.max(0, page));
        if (!search.isBlank()) {
            redirectAttributes.addAttribute("search", search);
        }
        return "redirect:/admin/locales/messages";
    }

    private String renderLocales(Model model, Long editingLocaleId) {
        model.addAttribute("locales", localeCatalogService.listAll());
        model.addAttribute("editingLocaleId", editingLocaleId);
        return "admin/locales";
    }

    private String renderMessages(
            Model model,
            int page,
            String search,
            String failedMessageKey) {
        var locales = localeCatalogService.listAll();
        Page<TranslationGridRow> messages = translationCatalogService.listGrid(
                locales,
                page,
                adminProperties.pageSize(),
                search);
        model.addAttribute("locales", locales);
        model.addAttribute("messages", messages);
        model.addAttribute("newTranslationCells", translationCatalogService.emptyCells(locales));
        model.addAttribute("search", search);
        model.addAttribute("failedMessageKey", failedMessageKey);
        model.addAttribute(
                "failedRowOnPage",
                failedMessageKey != null
                        && messages.getContent().stream()
                                .anyMatch(row -> row.messageKey().equals(failedMessageKey)));
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
