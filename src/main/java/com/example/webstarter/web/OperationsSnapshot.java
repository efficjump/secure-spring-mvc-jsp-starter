package com.example.webstarter.web;

public record OperationsSnapshot(
        boolean administrator,
        long totalUsers,
        long enabledUsers,
        long activeSessions,
        long rejectedLoginsLast24Hours) {

    public static OperationsSnapshot standardUser() {
        return new OperationsSnapshot(false, 0, 0, 0, 0);
    }

    public boolean isAdministrator() {
        return administrator;
    }
}
