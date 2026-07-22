package com.example.webstarter.admin;

import jakarta.servlet.http.HttpServletRequest;

import com.example.webstarter.audit.AuditEventType;
import com.example.webstarter.audit.AuditOutcome;
import com.example.webstarter.audit.RequestMetadata;
import com.example.webstarter.audit.SecurityAuditService;
import com.example.webstarter.config.AdminProperties;
import com.example.webstarter.user.Role;
import com.example.webstarter.user.UserOperationException;
import com.example.webstarter.user.UserSummary;
import com.example.webstarter.security.SessionRevocationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private static final Logger log = LoggerFactory.getLogger(AdminUserController.class);

    private final AdminUserService adminUserService;
    private final AdminProperties adminProperties;
    private final SecurityAuditService auditService;
    private final SessionRevocationService sessionRevocationService;

    public AdminUserController(
            AdminUserService adminUserService,
            AdminProperties adminProperties,
            SecurityAuditService auditService,
            SessionRevocationService sessionRevocationService) {
        this.adminUserService = adminUserService;
        this.adminProperties = adminProperties;
        this.auditService = auditService;
        this.sessionRevocationService = sessionRevocationService;
    }

    @GetMapping
    public String users(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<UserSummary> users = adminUserService.list(page, adminProperties.pageSize());
        model.addAttribute("users", users);
        return "admin/users";
    }

    @PostMapping("/{userId}/toggle-enabled")
    public String toggleEnabled(
            @PathVariable Long userId,
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            UserSummary target = adminUserService.toggleEnabled(authentication.getName(), userId);
            if (!target.enabled()) {
                sessionRevocationService.expireAll(target.username());
            }
            AuditEventType type = target.enabled() ? AuditEventType.USER_ENABLED : AuditEventType.USER_DISABLED;
            audit(type, authentication.getName(), target.username(), request, null);
            redirectAttributes.addFlashAttribute("message", "admin.user.updated");
        } catch (UserOperationException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{userId}/unlock")
    public String unlock(
            @PathVariable Long userId,
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            UserSummary target = adminUserService.unlock(userId);
            audit(AuditEventType.USER_UNLOCKED, authentication.getName(), target.username(), request, null);
            redirectAttributes.addFlashAttribute("message", "admin.user.unlocked");
        } catch (UserOperationException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{userId}/role")
    public String changeRole(
            @PathVariable Long userId,
            @RequestParam Role role,
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            UserSummary target = adminUserService.changeRole(authentication.getName(), userId, role);
            sessionRevocationService.expireAll(target.username());
            audit(
                    AuditEventType.USER_ROLE_CHANGED,
                    authentication.getName(),
                    target.username(),
                    request,
                    "role=" + role.name());
            redirectAttributes.addFlashAttribute("message", "admin.user.role.updated");
        } catch (UserOperationException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/users";
    }

    private void audit(
            AuditEventType type,
            String actor,
            String subject,
            HttpServletRequest request,
            String detail) {
        try {
            auditService.record(type, AuditOutcome.SUCCESS, actor, subject, RequestMetadata.from(request), detail);
        } catch (RuntimeException exception) {
            log.error("Unable to persist administrator action audit event", exception);
        }
    }
}
