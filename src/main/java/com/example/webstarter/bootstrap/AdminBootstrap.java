package com.example.webstarter.bootstrap;

import com.example.webstarter.audit.AuditEventType;
import com.example.webstarter.audit.AuditOutcome;
import com.example.webstarter.audit.RequestMetadata;
import com.example.webstarter.audit.SecurityAuditService;
import com.example.webstarter.config.BootstrapProperties;
import com.example.webstarter.user.AppUser;
import com.example.webstarter.user.AppUserRepository;
import com.example.webstarter.user.RegistrationService;
import com.example.webstarter.user.Role;
import com.example.webstarter.user.UserIdentityNormalizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final BootstrapProperties properties;
    private final AppUserRepository userRepository;
    private final RegistrationService registrationService;
    private final SecurityAuditService auditService;

    public AdminBootstrap(
            BootstrapProperties properties,
            AppUserRepository userRepository,
            RegistrationService registrationService,
            SecurityAuditService auditService) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.registrationService = registrationService;
        this.auditService = auditService;
    }

    @Override
    public void run(ApplicationArguments args) {
        BootstrapProperties.Admin admin = properties.admin();
        if (admin == null || !admin.enabled()) {
            if (userRepository.countByRole(Role.ADMIN) == 0) {
                log.warn("No administrator exists and bootstrap is disabled");
            }
            return;
        }
        if (userRepository.countByRole(Role.ADMIN) > 0) {
            log.info("Administrator bootstrap skipped because an administrator already exists");
            return;
        }

        requireConfigured("APP_BOOTSTRAP_ADMIN_USERNAME", admin.username());
        requireConfigured("APP_BOOTSTRAP_ADMIN_EMAIL", admin.email());
        requireConfigured("APP_BOOTSTRAP_ADMIN_PASSWORD", admin.password());

        String normalizedUsername = UserIdentityNormalizer.username(admin.username());
        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new IllegalStateException(
                    "Bootstrap username already belongs to a non-admin account; refusing automatic privilege escalation");
        }

        AppUser created = registrationService.createBootstrapAdmin(
                admin.username(),
                admin.email(),
                admin.username(),
                admin.password());
        auditService.record(
                AuditEventType.BOOTSTRAP_ADMIN_CREATED,
                AuditOutcome.SUCCESS,
                created.getUsername(),
                created.getUsername(),
                new RequestMetadata(null, null, null),
                "initial administrator created");
        log.warn("Bootstrap administrator '{}' was created; rotate its password and disable bootstrap", created.getUsername());
    }

    private void requireConfigured(String environmentName, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(environmentName + " is required when administrator bootstrap is enabled");
        }
    }
}

