package com.example.webstarter.web;

import com.example.webstarter.user.AccountService;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final AccountService accountService;
    private final DashboardService dashboardService;

    public HomeController(AccountService accountService, DashboardService dashboardService) {
        this.accountService = accountService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/workspace";
        }
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        model.addAttribute("user", accountService.getByUsername(authentication.getName()));
        model.addAttribute("operations", dashboardService.snapshot(authentication));
        return "dashboard";
    }
}
