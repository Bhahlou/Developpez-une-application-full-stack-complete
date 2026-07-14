package com.openclassrooms.mddapi.exception;

import lombok.Getter;

/**
 * Thrown when a refresh token is unknown or expired.
 */
@Getter
public class InvalidRefreshTokenException extends RuntimeException {

    private final String code;

    /**
     * @param code    a stable, machine-readable error code (e.g. {@code AUTH_REFRESH_TOKEN_EXPIRED})
     * @param message a human-readable description of the error
     */
    public InvalidRefreshTokenException(String code, String message) {
        super(message);
        this.code = code;
    }
}
