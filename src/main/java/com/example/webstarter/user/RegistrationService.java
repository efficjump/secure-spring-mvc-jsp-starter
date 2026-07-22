package com.example.webstarter.user;

import java.text.Normalizer;
import java.time.Clock;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import com.example.webstarter.security.PasswordPolicyService;
import com.example.webstarter.security.PasswordPolicyViolation;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyService passwordPolicyService;
    private final Validator validator;
    private final Clock clock;

    public RegistrationService(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            PasswordPolicyService passwordPolicyService,
            Validator validator,
            Clock clock) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicyService = passwordPolicyService;
        this.validator = validator;
        this.clock = clock;
    }

    @Transactional
    public AppUser register(RegistrationForm form) {
        validateForm(form);
        return create(
                form.getUsername(),
                form.getEmail(),
                form.getDisplayName(),
                form.getPassword(),
                EnumSet.of(Role.USER));
    }

    @Transactional
    public AppUser createBootstrapAdmin(
            String username,
            String email,
            String displayName,
            String password) {
        RegistrationForm form = new RegistrationForm();
        form.setUsername(username);
        form.setEmail(email);
        form.setDisplayName(displayName);
        form.setPassword(password);
        form.setPasswordConfirmation(password);
        validateForm(form);
        return create(username, email, displayName, password, EnumSet.of(Role.USER, Role.ADMIN));
    }

    private AppUser create(
            String rawUsername,
            String rawEmail,
            String rawDisplayName,
            String password,
            Set<Role> roles) {
        String username = UserIdentityNormalizer.username(rawUsername);
        String email = UserIdentityNormalizer.email(rawEmail);
        String displayName = Normalizer.normalize(rawDisplayName.strip(), Normalizer.Form.NFKC);

        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            throw new DuplicateUserException();
        }

        List<PasswordPolicyViolation> violations = passwordPolicyService.validate(password, username, email);
        if (!violations.isEmpty()) {
            throw new PasswordPolicyException(violations);
        }

        AppUser user = AppUser.create(
                username,
                email,
                displayName,
                passwordEncoder.encode(password),
                roles,
                clock.instant());
        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateUserException();
        }
    }

    private void validateForm(RegistrationForm form) {
        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Registration data failed validation");
        }
        if (!form.getPassword().equals(form.getPasswordConfirmation())) {
            throw new IllegalArgumentException("Password confirmation does not match");
        }
    }
}

