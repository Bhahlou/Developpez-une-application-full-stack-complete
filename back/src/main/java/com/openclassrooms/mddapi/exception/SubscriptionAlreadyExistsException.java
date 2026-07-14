package com.openclassrooms.mddapi.exception;

import lombok.Getter;

/**
 * Thrown when a user tries to subscribe to a theme they're already subscribed to.
 */
@Getter
public class SubscriptionAlreadyExistsException extends RuntimeException {

    private final String code;

    /**
     * @param code    a stable, machine-readable error code (e.g. {@code SUBSCRIPTION_ALREADY_EXISTS})
     * @param message a human-readable description of the error
     */
    public SubscriptionAlreadyExistsException(String code, String message) {
        super(message);
        this.code = code;
    }
}
