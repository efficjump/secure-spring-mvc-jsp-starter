package com.example.webstarter.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import jakarta.servlet.http.Cookie;

import com.example.webstarter.config.LocalizationProperties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LocalizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LocalizationProperties localizationProperties;

    @Test
    void supportedLanguageIsStoredInProtectedCookieAndAppliedToNextRequest() throws Exception {
        MvcResult result = mockMvc.perform(get("/locale")
                        .param("lang", "en")
                        .param("returnTo", "/login"))
                .andExpect(status().isSeeOther())
                .andExpect(redirectedUrl("/login"))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.allOf(
                                org.hamcrest.Matchers.containsString(
                                        localizationProperties.cookieName() + "=en"),
                                org.hamcrest.Matchers.containsString("Path=/"),
                                org.hamcrest.Matchers.containsString("Max-Age="),
                                org.hamcrest.Matchers.containsString("HttpOnly"),
                                org.hamcrest.Matchers.containsString("SameSite=Lax"))))
                .andReturn();

        assertThat(result.getResponse().getHeader(HttpHeaders.SET_COOKIE))
                .doesNotContain("Secure");

        mockMvc.perform(get("/login").cookie(new Cookie(localizationProperties.cookieName(), "en")))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("currentLocaleTag", "en"));
    }

    @Test
    void unsupportedCookieValueFallsBackToAllowedBrowserLanguage() throws Exception {
        mockMvc.perform(get("/login")
                        .cookie(new Cookie(localizationProperties.cookieName(), "fr"))
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.9"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("currentLocaleTag", "en"));
    }

    @Test
    void browserLanguageIsUsedWhenNoPreferenceCookieExists() throws Exception {
        mockMvc.perform(get("/login").header(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.9"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("currentLocaleTag", "en"));
    }

    @Test
    void localeEndpointPreservesSafeInternalQueryString() throws Exception {
        mockMvc.perform(get("/locale")
                        .param("lang", "ko")
                        .param("returnTo", "/admin/logins?page=2&outcome=FAILURE"))
                .andExpect(status().isSeeOther())
                .andExpect(redirectedUrl("/admin/logins?page=2&outcome=FAILURE"));
    }

    @Test
    void localeEndpointRejectsExternalAndTraversalRedirectTargets() throws Exception {
        mockMvc.perform(get("/locale")
                        .param("lang", "en")
                        .param("returnTo", "//example.invalid/account"))
                .andExpect(status().isSeeOther())
                .andExpect(redirectedUrl("/"));

        mockMvc.perform(get("/locale")
                        .param("lang", "en")
                        .param("returnTo", "/safe/../admin"))
                .andExpect(status().isSeeOther())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void returnPathValidationRejectsEncodedAndControlCharacterVariants() {
        List.of(
                        "https://example.invalid/account",
                        "//example.invalid/account",
                        "/safe/../admin",
                        "/safe%5cadmin",
                        "/login#section",
                        "/login%0d%0aLocation:%20https://example.invalid",
                        "/locale/preferences")
                .forEach(candidate -> assertThat(LocaleController.safeReturnPath(candidate))
                        .as(candidate)
                        .isEqualTo("/"));
    }

    @Test
    void unsupportedLanguageDoesNotCreatePreferenceCookie() throws Exception {
        mockMvc.perform(get("/locale")
                        .param("lang", "fr")
                        .param("returnTo", "/login"))
                .andExpect(status().isSeeOther())
                .andExpect(redirectedUrl("/login"))
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
    }
}
