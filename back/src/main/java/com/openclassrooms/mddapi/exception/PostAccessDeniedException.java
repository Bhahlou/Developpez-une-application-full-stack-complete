package com.openclassrooms.mddapi.exception;

import lombok.Getter;

/**
 * Thrown when a user tries to access a post (or its comments) under a theme
 * they are not subscribed to.
 */
@Getter
public class PostAccessDeniedException extends RuntimeException {

    private final String code;

    /**
     * @param code    a stable, machine-readable error code (e.g. {@code POST_ACCESS_DENIED})
     * @param message a human-readable description of the error
     */
    public PostAccessDeniedException(String code, String message) {
        super(message);
        this.code = code;
    }
}
