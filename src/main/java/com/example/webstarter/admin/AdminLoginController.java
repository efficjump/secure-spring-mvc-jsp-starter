package com.example.webstarter.admin;

import com.example.webstarter.audit.AuditEventType;
import com.example.webstarter.audit.AuditOutcome;
import com.example.webstarter.config.AdminProperties;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/logins")
public class AdminLoginController {

    private final AdminLoginAuditService loginAuditService;
    private final AdminProperties adminProperties;

    public AdminLoginController(
            AdminLoginAuditService loginAuditService,
            AdminProperties adminProperties) {
        this.loginAuditService = loginAuditService;
        this.adminProperties = adminProperties;
    }

    @GetMapping
    public String logins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) AuditEventType eventType,
            @RequestParam(required = false) AuditOutcome outcome,
            @RequestParam(defaultValue = "") String search,
            Model model) {
        model.addAttribute("events", loginAuditService.list(
                page,
                adminProperties.pageSize(),
                eventType,
                outcome,
                search));
        model.addAttribute("eventTypes", loginAuditService.supportedEventTypes());
        model.addAttribute("outcomes", AuditOutcome.values());
        model.addAttribute("selectedEventType", eventType);
        model.addAttribute("selectedOutcome", outcome);
        model.addAttribute("search", search);
        return "admin/logins";
    }
}
