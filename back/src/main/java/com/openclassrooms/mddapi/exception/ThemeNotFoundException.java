package com.openclassrooms.mddapi.exception;

import lombok.Getter;

/**
 * Thrown when a theme id doesn't match any existing theme.
 */
@Getter
public class ThemeNotFoundException extends RuntimeException {

    private final String code;

    /**
     * @param code    a stable, machine-readable error code (e.g. {@code THEME_NOT_FOUND})
     * @param message a human-readable description of the error
     */
    public ThemeNotFoundException(String code, String message) {
        super(message);
        this.code = code;
    }
}
