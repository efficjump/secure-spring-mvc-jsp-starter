package com.example.webstarter.config;

import java.util.Locale;

import jakarta.servlet.DispatcherType;

import com.example.webstarter.audit.SecurityAuditService;
import com.example.webstarter.security.AppAuthenticationFailureHandler;
import com.example.webstarter.security.AppAuthenticationSuccessHandler;
import com.example.webstarter.security.AppLogoutSuccessHandler;
import com.example.webstarter.security.AppUserDetailsService;
import com.example.webstarter.security.AuditingAccessDeniedHandler;
import com.example.webstarter.security.LoginProtectionService;
import com.example.webstarter.security.LoginRateLimitFilter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {

    @Bean
    DaoAuthenticationProvider authenticationProvider(
            AppUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsPasswordService(userDetailsService);
        return provider;
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityProperties properties,
            DaoAuthenticationProvider authenticationProvider,
            AppAuthenticationSuccessHandler authenticationSuccessHandler,
            AppAuthenticationFailureHandler authenticationFailureHandler,
            AppLogoutSuccessHandler logoutSuccessHandler,
            AuditingAccessDeniedHandler accessDeniedHandler,
            LoginProtectionService loginProtectionService,
            SecurityAuditService auditService,
            SessionRegistry sessionRegistry,
            @Value("${server.servlet.session.cookie.name:SESSION}") String sessionCookieName) throws Exception {
        SecurityProperties.Headers securityHeaders = properties.headers();

        http.authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                        .requestMatchers(EndpointRequest.to("health", "info")).permitAll()
                        .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ADMIN")
                        .requestMatchers("/", "/login", "/register", "/locale", "/error", "/access-denied", "/assets/**")
                        .permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(authenticationSuccessHandler)
                        .failureHandler(authenticationFailureHandler)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(logoutSuccessHandler)
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies(sessionCookieName))
                .csrf(Customizer.withDefaults())
                .sessionManagement(session -> {
                    session.sessionFixation(fixation -> fixation.changeSessionId());
                    session.maximumSessions(properties.session().maximumConcurrentSessions())
                            .maxSessionsPreventsLogin(false)
                            .sessionRegistry(sessionRegistry)
                            .expiredUrl("/login?expired");
                })
                .exceptionHandling(exceptions -> exceptions.accessDeniedHandler(accessDeniedHandler))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(securityHeaders.contentSecurityPolicy()))
                        .permissionsPolicyHeader(policy -> policy.policy(securityHeaders.permissionsPolicy()))
                        .referrerPolicy(referrer -> referrer.policy(referrerPolicy(securityHeaders.referrerPolicy())))
                        .frameOptions(frame -> {
                            if (securityHeaders.frameOptions() == SecurityProperties.FrameOptions.SAMEORIGIN) {
                                frame.sameOrigin();
                            } else {
                                frame.deny();
                            }
                        })
                        .httpStrictTransportSecurity(hsts -> hsts
                                .maxAgeInSeconds(securityHeaders.hstsMaxAge().toSeconds())
                                .includeSubDomains(securityHeaders.hstsIncludeSubdomains())
                                .preload(securityHeaders.hstsPreload())))
                .addFilterBefore(
                        new LoginRateLimitFilter(loginProtectionService, auditService, properties),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
    }

    @Bean
    SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    private ReferrerPolicyHeaderWriter.ReferrerPolicy referrerPolicy(String configuredPolicy) {
        String enumName = configuredPolicy.strip().replace('-', '_').toUpperCase(Locale.ROOT);
        try {
            return ReferrerPolicyHeaderWriter.ReferrerPolicy.valueOf(enumName);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unsupported referrer policy: " + configuredPolicy, exception);
        }
    }
}
