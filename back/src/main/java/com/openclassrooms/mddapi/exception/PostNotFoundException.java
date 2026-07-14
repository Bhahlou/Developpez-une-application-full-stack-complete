package com.openclassrooms.mddapi.exception;

import lombok.Getter;

/**
 * Thrown when a post (article) id doesn't match any existing post.
 */
@Getter
public class PostNotFoundException extends RuntimeException {

    private final String code;

    /**
     * @param code    a stable, machine-readable error code (e.g. {@code POST_NOT_FOUND})
     * @param message a human-readable description of the error
     */
    public PostNotFoundException(String code, String message) {
        super(message);
        this.code = code;
    }
}
