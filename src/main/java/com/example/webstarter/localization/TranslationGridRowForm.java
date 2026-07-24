package com.example.webstarter.localization;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class TranslationGridRowForm {

    @NotBlank
    @Size(max = 190)
    @Pattern(regexp = "[A-Za-z][A-Za-z0-9_.-]{0,189}")
    private String messageKey;

    private Map<Long, String> values = new LinkedHashMap<>();

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey == null ? null : messageKey.strip();
    }

    public Map<Long, String> getValues() {
        return values;
    }

    public void setValues(Map<Long, String> values) {
        this.values = values == null ? new LinkedHashMap<>() : new LinkedHashMap<>(values);
    }
}
