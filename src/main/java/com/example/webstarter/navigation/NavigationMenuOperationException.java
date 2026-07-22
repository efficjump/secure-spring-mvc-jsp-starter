package com.example.webstarter.navigation;

public class NavigationMenuOperationException extends RuntimeException {

    public NavigationMenuOperationException(String message) {
        super(message);
    }

    public NavigationMenuOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
