package com.example.webstarter.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.admin")
public record AdminProperties(@Min(5) @Max(200) int pageSize) {
}
