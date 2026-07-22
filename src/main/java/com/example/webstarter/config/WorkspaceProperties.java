package com.example.webstarter.config;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.workspace")
public record WorkspaceProperties(
        @Min(2) @Max(30) int maxTabs,
        @NotBlank @Pattern(regexp = "[a-z][a-z0-9-]{1,39}") String defaultMenuKey,
        @NotBlank @Size(max = 80) String storageKey,
        @NotNull @Size(min = 1, max = 30) List<@NotBlank @Pattern(regexp = "/[A-Za-z0-9/_-]*") String> reservedPaths) {
}
