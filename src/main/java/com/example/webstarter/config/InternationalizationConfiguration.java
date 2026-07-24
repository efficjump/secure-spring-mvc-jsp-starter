package com.example.webstarter.config;

import java.util.Enumeration;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;

import com.example.webstarter.localization.DatabaseMessageSource;
import com.example.webstarter.localization.LocaleCatalogService;
import com.example.webstarter.localization.LocalizedMessageRepository;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

@Configuration
public class InternationalizationConfiguration {

    @Bean
    CookieLocaleResolver localeResolver(
            LocalizationProperties properties,
            LocaleCatalogService localeCatalogService) {
        CookieLocaleResolver resolver =
                new AllowlistedCookieLocaleResolver(properties, localeCatalogService);
        resolver.setCookieMaxAge(properties.cookieMaxAge());
        resolver.setCookiePath(properties.cookiePath());
        resolver.setCookieSecure(properties.cookieSecure());
        resolver.setCookieHttpOnly(true);
        resolver.setCookieSameSite(properties.cookieSameSite());
        resolver.setLanguageTagCompliant(true);
        resolver.setRejectInvalidCookies(false);
        resolver.setDefaultLocaleFunction(
                request -> resolveDefaultLocale(request, properties, localeCatalogService));
        return resolver;
    }

    @Bean(name = AbstractApplicationContext.MESSAGE_SOURCE_BEAN_NAME)
    DatabaseMessageSource messageSource(
            ObjectProvider<LocalizedMessageRepository> repositoryProvider,
            LocalizationProperties properties,
            @Value("${spring.messages.basename:messages}") String basenames) {
        ResourceBundleMessageSource bundleMessageSource = new ResourceBundleMessageSource();
        bundleMessageSource.setBasenames(java.util.Arrays.stream(basenames.split(","))
                .map(String::strip)
                .filter(basename -> !basename.isBlank())
                .toArray(String[]::new));
        bundleMessageSource.setDefaultEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
        bundleMessageSource.setFallbackToSystemLocale(false);
        bundleMessageSource.setDefaultLocale(properties.defaultLocale());

        DatabaseMessageSource databaseMessageSource =
                new DatabaseMessageSource(repositoryProvider, properties);
        databaseMessageSource.setParentMessageSource(bundleMessageSource);
        return databaseMessageSource;
    }

    private Locale resolveDefaultLocale(
            HttpServletRequest request,
            LocalizationProperties properties,
            LocaleCatalogService localeCatalogService) {
        if (properties.respectAcceptLanguage()) {
            Enumeration<Locale> requestedLocales = request.getLocales();
            while (requestedLocales.hasMoreElements()) {
                Locale match = localeCatalogService.matchEnabled(requestedLocales.nextElement()).orElse(null);
                if (match != null) {
                    return match;
                }
            }
        }
        return localeCatalogService.defaultLocale();
    }

    private static final class AllowlistedCookieLocaleResolver extends CookieLocaleResolver {

        private final LocaleCatalogService localeCatalogService;

        private AllowlistedCookieLocaleResolver(
                LocalizationProperties properties,
                LocaleCatalogService localeCatalogService) {
            super(properties.cookieName());
            this.localeCatalogService = localeCatalogService;
        }

        @Override
        protected Locale parseLocaleValue(String localeValue) {
            Locale parsed = super.parseLocaleValue(localeValue);
            return localeCatalogService.matchEnabled(parsed)
                    .orElseThrow(() -> new IllegalArgumentException("Unsupported locale cookie value"));
        }
    }
}
