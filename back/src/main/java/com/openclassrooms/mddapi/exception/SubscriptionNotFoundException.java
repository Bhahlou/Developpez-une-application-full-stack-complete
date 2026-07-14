package com.openclassrooms.mddapi.exception;

import lombok.Getter;

/**
 * Thrown when a user tries to unsubscribe from a theme they're not subscribed to.
 */
@Getter
public class SubscriptionNotFoundException extends RuntimeException {

    private final String code;

    /**
     * @param code    a stable, machine-readable error code (e.g. {@code SUBSCRIPTION_NOT_FOUND})
     * @param message a human-readable description of the error
     */
    public SubscriptionNotFoundException(String code, String message) {
        super(message);
        this.code = code;
    }
}
