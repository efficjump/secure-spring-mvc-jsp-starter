package com.example.webstarter.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.Clock;
import java.util.EnumSet;
import java.util.UUID;

import com.example.webstarter.config.SecurityProperties;
import com.example.webstarter.security.SessionRevocationService;
import com.example.webstarter.user.AppUser;
import com.example.webstarter.user.AppUserRepository;
import com.example.webstarter.user.Role;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockHttpSession;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Clock clock;

    @Autowired
    private SessionRevocationService sessionRevocationService;

    @Autowired
    private SecurityProperties securityProperties;

    private String validPassword;

    @BeforeEach
    void setUpUser() {
        validPassword = UUID.randomUUID().toString();
        userRepository.deleteAll();
        userRepository.saveAndFlush(AppUser.create(
                "member",
                "member@example.com",
                "Member",
                passwordEncoder.encode(validPassword),
                EnumSet.of(Role.USER),
                clock.instant()));
    }

    @Test
    void publicPageHasBaselineSecurityHeaders() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"))
                .andExpect(header().string("Content-Security-Policy", "default-src 'self'; object-src 'none'; base-uri 'self'; frame-ancestors 'self'; form-action 'self'"))
                .andExpect(header().exists(RequestContext.REQUEST_ID_HEADER));
    }

    @Test
    void protectedPageRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void loginRequiresCsrfToken() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "member")
                        .param("password", validPassword))
                .andExpect(status().isForbidden());
    }

    @Test
    void oversizedCredentialIsRejectedBeforePasswordHashing() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "member")
                        .param("password", "x".repeat(securityProperties.password().maxLength() + 1)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));

        AppUser unchangedUser = userRepository.findByUsername("member").orElseThrow();
        assertThat(unchangedUser.getFailedLoginAttempts()).isZero();
        assertThat(unchangedUser.getLockedUntil()).isNull();
    }

    @Test
    void validLoginCreatesAuthenticatedSession() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "member")
                        .param("password", validPassword))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/workspace"));
    }

    @Test
    void authenticatedUserCanOpenWorkspace() throws Exception {
        MvcResult login = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "member")
                        .param("password", validPassword))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

        mockMvc.perform(get("/workspace").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("workspace"));
    }

    @Test
    void repeatedFailuresLockThePersistedAccount() throws Exception {
        for (int attempt = 0; attempt < 5; attempt++) {
            mockMvc.perform(post("/login")
                            .with(csrf())
                            .param("username", "member")
                            .param("password", "wrong password value"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login?error"));
        }

        AppUser lockedUser = userRepository.findByUsername("member").orElseThrow();
        assertThat(lockedUser.getLockedUntil()).isAfter(clock.instant());
    }

    @Test
    void revokedSessionCannotAccessProtectedPage() throws Exception {
        MvcResult login = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "member")
                        .param("password", validPassword))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

        sessionRevocationService.expireAll("member");

        mockMvc.perform(get("/dashboard").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?expired"));
    }

    @Test
    void registrationIsNotReachableWhenDisabled() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isNotFound());
    }

    @Test
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/internal/actuator/health"))
                .andExpect(status().isOk());
    }
}
