package com.example.webstarter.user;

import java.util.List;

import com.example.webstarter.security.PasswordPolicyViolation;

public class PasswordPolicyException extends RuntimeException {

    private final List<PasswordPolicyViolation> violations;

    public PasswordPolicyException(List<PasswordPolicyViolation> violations) {
        super("Password does not satisfy the configured policy");
        this.violations = List.copyOf(violations);
    }

    public List<PasswordPolicyViolation> getViolations() {
        return violations;
    }
}

