package com.example.webstarter.security;

public interface LoginAttemptGuard {

    boolean isBlocked(String ipAddress, String username);

    void recordFailure(String ipAddress, String username);

    void recordSuccess(String ipAddress, String username);
}

