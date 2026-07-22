package com.example.webstarter.security;

public enum PasswordPolicyViolation {
    TOO_SHORT,
    TOO_LONG,
    BLANK,
    CONTROL_CHARACTER,
    CONTAINS_USERNAME,
    CONTAINS_EMAIL
}

