package com.example.webstarter.user;

import java.time.Clock;
import java.util.List;

import com.example.webstarter.security.PasswordPolicyService;
import com.example.webstarter.security.PasswordPolicyViolation;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyService passwordPolicyService;
    private final Clock clock;

    public AccountService(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            PasswordPolicyService passwordPolicyService,
            Clock clock) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicyService = passwordPolicyService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public UserSummary getByUsername(String rawUsername) {
        AppUser user = userRepository.findByUsername(UserIdentityNormalizer.username(rawUsername))
                .orElseThrow(() -> new UsernameNotFoundException("User no longer exists"));
        return UserSummary.from(user, clock.instant());
    }

    @Transactional
    public void changePassword(String rawUsername, String currentPassword, String newPassword) {
        String username = UserIdentityNormalizer.username(rawUsername);
        AppUser user = userRepository.findByUsernameForUpdate(username)
                .orElseThrow(() -> new UsernameNotFoundException("User no longer exists"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCurrentPasswordException();
        }
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new UserOperationException("password.reuse");
        }

        List<PasswordPolicyViolation> violations =
                passwordPolicyService.validate(newPassword, user.getUsername(), user.getEmail());
        if (!violations.isEmpty()) {
            throw new PasswordPolicyException(violations);
        }
        user.updatePassword(passwordEncoder.encode(newPassword), clock.instant());
    }
}

