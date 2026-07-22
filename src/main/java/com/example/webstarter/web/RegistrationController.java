package com.example.webstarter.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.Objects;

import com.example.webstarter.audit.AuditEventType;
import com.example.webstarter.audit.AuditOutcome;
import com.example.webstarter.audit.RequestMetadata;
import com.example.webstarter.audit.SecurityAuditService;
import com.example.webstarter.config.SecurityProperties;
import com.example.webstarter.security.PasswordPolicyViolation;
import com.example.webstarter.user.AppUser;
import com.example.webstarter.user.DuplicateUserException;
import com.example.webstarter.user.PasswordPolicyException;
import com.example.webstarter.user.RegistrationForm;
import com.example.webstarter.user.RegistrationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegistrationController {

    private static final Logger log = LoggerFactory.getLogger(RegistrationController.class);

    private final SecurityProperties securityProperties;
    private final RegistrationService registrationService;
    private final SecurityAuditService auditService;

    public RegistrationController(
            SecurityProperties securityProperties,
            RegistrationService registrationService,
            SecurityAuditService auditService) {
        this.securityProperties = securityProperties;
        this.registrationService = registrationService;
        this.auditService = auditService;
    }

    @GetMapping("/register")
    public String registrationForm(Model model) {
        requireRegistrationEnabled();
        if (!model.containsAttribute("registrationForm")) {
            model.addAttribute("registrationForm", new RegistrationForm());
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registrationForm") RegistrationForm form,
            BindingResult bindingResult,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        requireRegistrationEnabled();
        if (!Objects.equals(form.getPassword(), form.getPasswordConfirmation())) {
            bindingResult.rejectValue("passwordConfirmation", "password.confirmation");
        }
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            AppUser user = registrationService.register(form);
            recordAudit(
                    AuditEventType.REGISTRATION_SUCCESS,
                    AuditOutcome.SUCCESS,
                    user.getUsername(),
                    request,
                    null);
            redirectAttributes.addFlashAttribute("message", "registration.success");
            return "redirect:/login?registered";
        } catch (DuplicateUserException exception) {
            bindingResult.reject("registration.duplicate");
            recordAudit(
                    AuditEventType.REGISTRATION_FAILURE,
                    AuditOutcome.FAILURE,
                    form.getUsername(),
                    request,
                    "duplicate identity");
        } catch (PasswordPolicyException exception) {
            rejectPasswordViolations(bindingResult, exception);
        }
        return "register";
    }

    private void rejectPasswordViolations(BindingResult bindingResult, PasswordPolicyException exception) {
        for (PasswordPolicyViolation violation : exception.getViolations()) {
            bindingResult.rejectValue("password", passwordMessageCode(violation));
        }
    }

    private String passwordMessageCode(PasswordPolicyViolation violation) {
        return "password.policy." + violation.name().toLowerCase().replace('_', '.');
    }

    private void requireRegistrationEnabled() {
        if (!securityProperties.registration().enabled()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    private void recordAudit(
            AuditEventType type,
            AuditOutcome outcome,
            String username,
            HttpServletRequest request,
            String detail) {
        try {
            auditService.record(type, outcome, username, username, RequestMetadata.from(request), detail);
        } catch (RuntimeException exception) {
            log.error("Unable to persist registration audit event", exception);
        }
    }
}
