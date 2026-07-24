package com.example.webstarter.localization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class LocalizedMessageForm {

    @NotBlank
    @Size(max = 190)
    @Pattern(regexp = "[A-Za-z][A-Za-z0-9_.-]{0,189}")
    private String messageKey;

    @NotBlank
    @Size(max = 4000)
    private String messageValue;

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey == null ? null : messageKey.strip();
    }

    public String getMessageValue() {
        return messageValue;
    }

    public void setMessageValue(String messageValue) {
        this.messageValue = messageValue == null ? null : messageValue.strip();
    }
}
