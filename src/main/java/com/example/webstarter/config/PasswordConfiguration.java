package com.example.webstarter.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

@Configuration
public class PasswordConfiguration {

    @Bean
    PasswordEncoder passwordEncoder(SecurityProperties properties) {
        SecurityProperties.Password password = properties.password();
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put(
                "argon2",
                new Argon2PasswordEncoder(
                        password.argon2().saltLength(),
                        password.argon2().hashLength(),
                        password.argon2().parallelism(),
                        password.argon2().memoryKib(),
                        password.argon2().iterations()));
        encoders.put("bcrypt", new BCryptPasswordEncoder(password.bcryptStrength()));
        encoders.put("pbkdf2", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8());
        encoders.put("scrypt", SCryptPasswordEncoder.defaultsForSpringSecurity_v5_8());

        if (!encoders.containsKey(password.algorithm())) {
            throw new IllegalArgumentException(
                    "Unsupported password algorithm: " + password.algorithm() + ". Allowed: " + encoders.keySet());
        }
        return new DelegatingPasswordEncoder(password.algorithm(), encoders);
    }
}

