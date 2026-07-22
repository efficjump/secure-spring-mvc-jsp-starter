package com.example.webstarter.web;

import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import com.example.webstarter.audit.AuditEventType;
import com.example.webstarter.audit.AuditOutcome;
import com.example.webstarter.audit.RequestMetadata;
import com.example.webstarter.audit.SecurityAuditService;
import com.example.webstarter.security.PasswordPolicyViolation;
import com.example.webstarter.security.SessionRevocationService;
import com.example.webstarter.user.AccountService;
import com.example.webstarter.user.InvalidCurrentPasswordException;
import com.example.webstarter.user.PasswordChangeForm;
import com.example.webstarter.user.PasswordPolicyException;
import com.example.webstarter.user.UserOperationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    private final AccountService accountService;
    private final SecurityAuditService auditService;
    private final SessionRevocationService sessionRevocationService;
    private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

    public AccountController(
            AccountService accountService,
            SecurityAuditService auditService,
            SessionRevocationService sessionRevocationService) {
        this.accountService = accountService;
        this.auditService = auditService;
        this.sessionRevocationService = sessionRevocationService;
    }

    @GetMapping("/account/password")
    public String passwordForm(Model model) {
        if (!model.containsAttribute("passwordChangeForm")) {
            model.addAttribute("passwordChangeForm", new PasswordChangeForm());
        }
        return "account/password";
    }

    @PostMapping("/account/password")
    public String changePassword(
            @Valid @ModelAttribute("passwordChangeForm") PasswordChangeForm form,
            BindingResult bindingResult,
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response) {
        if (!Objects.equals(form.getNewPassword(), form.getNewPasswordConfirmation())) {
            bindingResult.rejectValue("newPasswordConfirmation", "password.confirmation");
        }
        if (bindingResult.hasErrors()) {
            return "account/password";
        }

        try {
            accountService.changePassword(
                    authentication.getName(),
                    form.getCurrentPassword(),
                    form.getNewPassword());
        } catch (InvalidCurrentPasswordException exception) {
            bindingResult.rejectValue("currentPassword", "password.current.invalid");
            return "account/password";
        } catch (PasswordPolicyException exception) {
            for (PasswordPolicyViolation violation : exception.getViolations()) {
                bindingResult.rejectValue(
                        "newPassword",
                        "password.policy." + violation.name().toLowerCase().replace('_', '.'));
            }
            return "account/password";
        } catch (UserOperationException exception) {
            bindingResult.rejectValue("newPassword", exception.getMessage());
            return "account/password";
        }

        sessionRevocationService.expireAll(authentication.getName());

        try {
            auditService.record(
                    AuditEventType.PASSWORD_CHANGED,
                    AuditOutcome.SUCCESS,
                    authentication.getName(),
                    authentication.getName(),
                    RequestMetadata.from(request),
                    null);
        } catch (RuntimeException exception) {
            log.error("Unable to persist password-change audit event", exception);
        }
        logoutHandler.logout(request, response, authentication);
        return "redirect:/login?password_changed";
    }
}
