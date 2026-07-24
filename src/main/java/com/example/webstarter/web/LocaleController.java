package com.example.webstarter.web;

import java.net.URI;
import java.net.URISyntaxException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.example.webstarter.localization.LocaleCatalogService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class LocaleController {

    private static final String DEFAULT_RETURN_PATH = "/";

    private final LocaleResolver localeResolver;
    private final LocaleCatalogService localeCatalogService;

    public LocaleController(
            LocaleResolver localeResolver,
            LocaleCatalogService localeCatalogService) {
        this.localeResolver = localeResolver;
        this.localeCatalogService = localeCatalogService;
    }

    @GetMapping("/locale")
    public RedirectView changeLocale(
            @RequestParam String lang,
            @RequestParam(defaultValue = DEFAULT_RETURN_PATH) String returnTo,
            HttpServletRequest request,
            HttpServletResponse response) {
        localeCatalogService.findEnabled(lang)
                .ifPresent(locale -> localeResolver.setLocale(request, response, locale));

        RedirectView redirectView = new RedirectView(safeReturnPath(returnTo));
        redirectView.setContextRelative(true);
        redirectView.setExposeModelAttributes(false);
        redirectView.setExpandUriTemplateVariables(false);
        redirectView.setHttp10Compatible(false);
        return redirectView;
    }

    static String safeReturnPath(String candidate) {
        if (candidate == null
                || !candidate.startsWith("/")
                || candidate.startsWith("//")
                || candidate.indexOf('\\') >= 0
                || candidate.indexOf('\r') >= 0
                || candidate.indexOf('\n') >= 0) {
            return DEFAULT_RETURN_PATH;
        }
        try {
            URI uri = new URI(candidate);
            String path = uri.getPath();
            String query = uri.getQuery();
            if (uri.isAbsolute()
                    || uri.getRawAuthority() != null
                    || uri.getRawFragment() != null
                    || path == null
                    || !path.startsWith("/")
                    || path.startsWith("//")
                    || path.indexOf('\\') >= 0
                    || containsControlCharacter(path)
                    || containsControlCharacter(query)
                    || !uri.normalize().getPath().equals(path)
                    || path.equals("/locale")
                    || path.startsWith("/locale/")) {
                return DEFAULT_RETURN_PATH;
            }
            return candidate;
        } catch (URISyntaxException exception) {
            return DEFAULT_RETURN_PATH;
        }
    }

    private static boolean containsControlCharacter(String value) {
        return value != null && value.chars().anyMatch(Character::isISOControl);
    }
}
