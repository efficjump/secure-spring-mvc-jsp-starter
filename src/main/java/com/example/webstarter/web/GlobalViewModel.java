package com.example.webstarter.web;

import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;

import com.example.webstarter.config.LocalizationProperties;
import com.example.webstarter.config.SecurityProperties;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalViewModel {

    private final SecurityProperties securityProperties;
    private final LocalizationProperties localizationProperties;

    public GlobalViewModel(
            SecurityProperties securityProperties,
            LocalizationProperties localizationProperties) {
        this.securityProperties = securityProperties;
        this.localizationProperties = localizationProperties;
    }

    @ModelAttribute("registrationEnabled")
    public boolean registrationEnabled() {
        return securityProperties.registration().enabled();
    }

    @ModelAttribute("passwordMaxLength")
    public int passwordMaxLength() {
        return securityProperties.password().maxLength();
    }

    @ModelAttribute("embeddedView")
    public boolean embeddedView(HttpServletRequest request) {
        return "true".equalsIgnoreCase(request.getParameter("embedded"))
                || "iframe".equalsIgnoreCase(request.getHeader("Sec-Fetch-Dest"));
    }

    @ModelAttribute("currentLocaleTag")
    public String currentLocaleTag(Locale locale) {
        return locale.toLanguageTag();
    }

    @ModelAttribute("supportedLanguages")
    public List<LanguageOption> supportedLanguages(Locale currentLocale) {
        return localizationProperties.supportedLocales().stream()
                .map(locale -> new LanguageOption(
                        locale.toLanguageTag(),
                        locale.getDisplayLanguage(currentLocale),
                        locale.equals(currentLocale)))
                .toList();
    }

    @ModelAttribute("localeReturnTo")
    public String localeReturnTo(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        String path = contextPath.isEmpty() ? requestUri : requestUri.substring(contextPath.length());
        String query = request.getQueryString();
        return query == null || query.isBlank() ? path : path + "?" + query;
    }

    public record LanguageOption(String tag, String label, boolean active) {
        public boolean isActive() {
            return active;
        }
    }
}
