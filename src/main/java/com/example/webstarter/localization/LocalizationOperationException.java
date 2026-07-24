package com.example.webstarter.localization;

public class LocalizationOperationException extends RuntimeException {

    public LocalizationOperationException(String messageCode) {
        super(messageCode);
    }

    public LocalizationOperationException(String messageCode, Throwable cause) {
        super(messageCode, cause);
    }
}
