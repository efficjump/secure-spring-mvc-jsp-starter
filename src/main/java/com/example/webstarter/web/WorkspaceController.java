package com.example.webstarter.web;

import java.util.List;
import java.util.Locale;

import com.example.webstarter.config.WorkspaceProperties;
import com.example.webstarter.navigation.NavigationMenuLocalizer;
import com.example.webstarter.navigation.NavigationMenuService;
import com.example.webstarter.navigation.NavigationMenuSummary;
import com.example.webstarter.user.AccountService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WorkspaceController {

    private final NavigationMenuService navigationMenuService;
    private final AccountService accountService;
    private final WorkspaceProperties workspaceProperties;
    private final NavigationMenuLocalizer navigationMenuLocalizer;

    public WorkspaceController(
            NavigationMenuService navigationMenuService,
            AccountService accountService,
            WorkspaceProperties workspaceProperties,
            NavigationMenuLocalizer navigationMenuLocalizer) {
        this.navigationMenuService = navigationMenuService;
        this.accountService = accountService;
        this.workspaceProperties = workspaceProperties;
        this.navigationMenuLocalizer = navigationMenuLocalizer;
    }

    @GetMapping("/workspace")
    public String workspace(Authentication authentication, Locale locale, Model model) {
        List<NavigationMenuSummary> menus = navigationMenuService.visibleMenus(authentication).stream()
                .map(menu -> navigationMenuLocalizer.localize(menu, locale))
                .toList();
        String defaultMenuKey = menus.stream()
                .filter(menu -> menu.menuKey().equals(workspaceProperties.defaultMenuKey()))
                .map(NavigationMenuSummary::menuKey)
                .findFirst()
                .orElseGet(() -> menus.isEmpty() ? "" : menus.getFirst().menuKey());

        model.addAttribute("menus", menus);
        model.addAttribute("workspaceUser", accountService.getByUsername(authentication.getName()));
        model.addAttribute("workspaceMaxTabs", workspaceProperties.maxTabs());
        model.addAttribute("workspaceStorageKey", workspaceProperties.storageKey());
        model.addAttribute("workspaceDefaultMenuKey", defaultMenuKey);
        return "workspace";
    }
}
