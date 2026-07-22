package com.example.webstarter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.bootstrap")
public record BootstrapProperties(Admin admin) {

    public record Admin(boolean enabled, String username, String email, String password) {
    }
}

