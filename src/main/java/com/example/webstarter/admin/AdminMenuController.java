package com.example.webstarter.admin;

import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import com.example.webstarter.audit.AuditEventType;
import com.example.webstarter.audit.AuditOutcome;
import com.example.webstarter.audit.RequestMetadata;
import com.example.webstarter.audit.SecurityAuditService;
import com.example.webstarter.navigation.NavigationIcon;
import com.example.webstarter.navigation.NavigationMenuLocalizer;
import com.example.webstarter.navigation.NavigationMenuForm;
import com.example.webstarter.navigation.NavigationMenuOperationException;
import com.example.webstarter.navigation.NavigationMenuService;
import com.example.webstarter.navigation.NavigationMenuSummary;
import com.example.webstarter.user.Role;

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
@RequestMapping("/admin/menus")
public class AdminMenuController {

    private static final Logger log = LoggerFactory.getLogger(AdminMenuController.class);

    private final NavigationMenuService menuService;
    private final SecurityAuditService auditService;
    private final NavigationMenuLocalizer menuLocalizer;

    public AdminMenuController(
            NavigationMenuService menuService,
            SecurityAuditService auditService,
            NavigationMenuLocalizer menuLocalizer) {
        this.menuService = menuService;
        this.auditService = auditService;
        this.menuLocalizer = menuLocalizer;
    }

    @GetMapping
    public String menus(
            @RequestParam(required = false) Long edit,
            Locale locale,
            Model model) {
        if (!model.containsAttribute("menuForm")) {
            model.addAttribute("menuForm", edit == null ? new NavigationMenuForm() : menuService.formFor(edit));
        }
        return render(model, edit, locale);
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("menuForm") NavigationMenuForm form,
            BindingResult bindingResult,
            Authentication authentication,
            HttpServletRequest request,
            Locale locale,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return render(model, null, locale);
        }
        try {
            NavigationMenuSummary created = menuService.create(form);
            audit(AuditEventType.MENU_CREATED, authentication.getName(), created.menuKey(), request);
            redirectAttributes.addFlashAttribute("message", "admin.menu.created");
            return "redirect:/admin/menus?edit=" + created.id();
        } catch (NavigationMenuOperationException exception) {
            bindingResult.reject(exception.getMessage());
            return render(model, null, locale);
        }
    }

    @PostMapping("/{menuId}")
    public String update(
            @PathVariable Long menuId,
            @Valid @ModelAttribute("menuForm") NavigationMenuForm form,
            BindingResult bindingResult,
            Authentication authentication,
            HttpServletRequest request,
            Locale locale,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return render(model, menuId, locale);
        }
        try {
            NavigationMenuSummary updated = menuService.update(menuId, form);
            audit(AuditEventType.MENU_UPDATED, authentication.getName(), updated.menuKey(), request);
            redirectAttributes.addFlashAttribute("message", "admin.menu.updated");
            return "redirect:/admin/menus?edit=" + menuId;
        } catch (NavigationMenuOperationException exception) {
            bindingResult.reject(exception.getMessage());
            return render(model, menuId, locale);
        }
    }

    @PostMapping("/{menuId}/toggle")
    public String toggle(
            @PathVariable Long menuId,
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            NavigationMenuSummary menu = menuService.toggleEnabled(menuId);
            AuditEventType eventType = menu.enabled()
                    ? AuditEventType.MENU_ENABLED
                    : AuditEventType.MENU_DISABLED;
            audit(eventType, authentication.getName(), menu.menuKey(), request);
            redirectAttributes.addFlashAttribute(
                    "message",
                    menu.enabled() ? "admin.menu.enabled" : "admin.menu.disabled");
        } catch (NavigationMenuOperationException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/menus";
    }

    private String render(Model model, Long editingMenuId, Locale locale) {
        model.addAttribute("menus", menuService.listAll().stream()
                .map(menu -> menuLocalizer.localize(menu, locale))
                .toList());
        model.addAttribute("editingMenuId", editingMenuId);
        model.addAttribute("iconOptions", NavigationIcon.values());
        model.addAttribute("roleOptions", Role.values());
        return "admin/menus";
    }

    private void audit(
            AuditEventType type,
            String actor,
            String subject,
            HttpServletRequest request) {
        try {
            auditService.record(type, AuditOutcome.SUCCESS, actor, subject, RequestMetadata.from(request), null);
        } catch (RuntimeException exception) {
            log.error("Unable to persist menu administration audit event", exception);
        }
    }
}
