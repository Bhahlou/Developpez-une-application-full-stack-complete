package com.openclassrooms.mddapi.exception;

import lombok.Getter;

/**
 * Thrown when creating a theme whose title is already taken.
 */
@Getter
public class ThemeAlreadyExistsException extends RuntimeException {

    private final String code;

    /**
     * @param code    a stable, machine-readable error code (e.g. {@code THEME_ALREADY_EXISTS})
     * @param message a human-readable description of the error
     */
    public ThemeAlreadyExistsException(String code, String message) {
        super(message);
        this.code = code;
    }
}
