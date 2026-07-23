package com.example.webstarter.config;

import java.util.Enumeration;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

@Configuration
public class InternationalizationConfiguration {

    @Bean
    CookieLocaleResolver localeResolver(LocalizationProperties properties) {
        CookieLocaleResolver resolver = new AllowlistedCookieLocaleResolver(properties);
        resolver.setCookieMaxAge(properties.cookieMaxAge());
        resolver.setCookiePath(properties.cookiePath());
        resolver.setCookieSecure(properties.cookieSecure());
        resolver.setCookieHttpOnly(true);
        resolver.setCookieSameSite(properties.cookieSameSite());
        resolver.setLanguageTagCompliant(true);
        resolver.setRejectInvalidCookies(false);
        resolver.setDefaultLocaleFunction(request -> resolveDefaultLocale(request, properties));
        return resolver;
    }

    private Locale resolveDefaultLocale(
            HttpServletRequest request,
            LocalizationProperties properties) {
        if (properties.respectAcceptLanguage()) {
            Enumeration<Locale> requestedLocales = request.getLocales();
            while (requestedLocales.hasMoreElements()) {
                Locale match = properties.match(requestedLocales.nextElement()).orElse(null);
                if (match != null) {
                    return match;
                }
            }
        }
        return properties.defaultLocale();
    }

    private static final class AllowlistedCookieLocaleResolver extends CookieLocaleResolver {

        private final LocalizationProperties properties;

        private AllowlistedCookieLocaleResolver(LocalizationProperties properties) {
            super(properties.cookieName());
            this.properties = properties;
        }

        @Override
        protected Locale parseLocaleValue(String localeValue) {
            Locale parsed = super.parseLocaleValue(localeValue);
            return properties.match(parsed)
                    .orElseThrow(() -> new IllegalArgumentException("Unsupported locale cookie value"));
        }
    }
}
