package com.openclassrooms.mddapi.exception;

import lombok.Getter;

/**
 * Thrown when registering or updating a profile with a username or email
 * that's already taken by another account.
 */
@Getter
public class UserAlreadyExistsException extends RuntimeException {

    private final String code;

    /**
     * @param code    a stable, machine-readable error code (e.g. {@code USER_EMAIL_TAKEN})
     * @param message a human-readable description of the error
     */
    public UserAlreadyExistsException(String code, String message) {
        super(message);
        this.code = code;
    }
}
