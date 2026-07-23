package com.example.webstarter.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.util.EnumSet;
import java.util.UUID;

import com.example.webstarter.user.AppUser;
import com.example.webstarter.user.AppUserRepository;
import com.example.webstarter.user.Role;
import com.example.webstarter.user.UserOperationException;
import com.example.webstarter.user.UserSummary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser(username = "admin-one", roles = "ADMIN")
class AdminUserServiceIntegrationTest {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Clock clock;

    private AppUser adminOne;
    private AppUser adminTwo;

    @BeforeEach
    void setUpUsers() {
        userRepository.deleteAll();
        adminOne = saveUser("admin-one", Role.ADMIN);
        adminTwo = saveUser("admin-two", Role.ADMIN);
        saveUser("member", Role.USER);
    }

    @Test
    void anotherAdministratorCanBeDemotedWhileOneAdministratorRemains() {
        UserSummary result = adminUserService.changeRole("admin-one", adminTwo.getId(), Role.USER);

        assertThat(result.admin()).isFalse();
        assertThat(userRepository.findById(adminOne.getId()).orElseThrow().hasRole(Role.ADMIN)).isTrue();
    }

    @Test
    void administratorCannotDemoteOwnAccount() {
        assertThatThrownBy(() -> adminUserService.changeRole("admin-one", adminOne.getId(), Role.USER))
                .isInstanceOf(UserOperationException.class)
                .hasMessage("admin.user.error.selfRole");
    }

    @Test
    void lastEnabledAdministratorCannotBeDisabled() {
        adminTwo.setEnabled(false, clock.instant());
        userRepository.saveAndFlush(adminTwo);

        assertThatThrownBy(() -> adminUserService.toggleEnabled("admin-two", adminOne.getId()))
                .isInstanceOf(UserOperationException.class)
                .hasMessage("admin.user.error.lastActiveAdmin");
    }

    private AppUser saveUser(String username, Role role) {
        return userRepository.saveAndFlush(AppUser.create(
                username,
                username + "@example.com",
                username,
                passwordEncoder.encode(UUID.randomUUID().toString()),
                role == Role.ADMIN ? EnumSet.of(Role.USER, Role.ADMIN) : EnumSet.of(Role.USER),
                clock.instant()));
    }
}
