package com.example.webstarter.user;

public class DuplicateUserException extends RuntimeException {

    public DuplicateUserException() {
        super("The username or email address is already registered");
    }
}

