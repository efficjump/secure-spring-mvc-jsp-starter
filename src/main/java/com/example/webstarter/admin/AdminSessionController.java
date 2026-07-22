package com.example.webstarter.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import com.example.webstarter.audit.AuditEventType;
import com.example.webstarter.audit.AuditOutcome;
import com.example.webstarter.audit.RequestMetadata;
import com.example.webstarter.audit.SecurityAuditService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/sessions")
public class AdminSessionController {

    private static final Logger log = LoggerFactory.getLogger(AdminSessionController.class);

    private final AdminSessionService sessionService;
    private final SecurityAuditService auditService;

    public AdminSessionController(AdminSessionService sessionService, SecurityAuditService auditService) {
        this.sessionService = sessionService;
        this.auditService = auditService;
    }

    @GetMapping
    public String sessions(HttpServletRequest request, Model model) {
        String currentSessionId = currentSessionId(request);
        model.addAttribute("sessions", sessionService.list(currentSessionId));
        return "admin/sessions";
    }

    @PostMapping("/terminate")
    public String terminate(
            @RequestParam String sessionToken,
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            String username = sessionService.terminate(sessionToken, currentSessionId(request));
            recordAudit(authentication.getName(), username, request);
            redirectAttributes.addFlashAttribute("message", "세션을 종료했습니다.");
        } catch (SessionOperationException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/sessions";
    }

    private String currentSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : session.getId();
    }

    private void recordAudit(String actor, String subject, HttpServletRequest request) {
        try {
            auditService.record(
                    AuditEventType.SESSION_TERMINATED,
                    AuditOutcome.SUCCESS,
                    actor,
                    subject,
                    RequestMetadata.from(request),
                    null);
        } catch (RuntimeException exception) {
            log.error("Unable to persist session-termination audit event", exception);
        }
    }
}
