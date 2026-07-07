package com.openclassrooms.mddapi.exception;

import lombok.Getter;

@Getter
public class ThemeAlreadyExistsException extends RuntimeException {

    private final String code;

    public ThemeAlreadyExistsException(String code, String message) {
        super(message);
        this.code = code;
    }
}
