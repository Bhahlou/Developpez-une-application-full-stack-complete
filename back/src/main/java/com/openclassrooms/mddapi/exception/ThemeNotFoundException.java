package com.openclassrooms.mddapi.exception;

import lombok.Getter;

@Getter
public class ThemeNotFoundException extends RuntimeException {

    private final String code;

    public ThemeNotFoundException(String code, String message) {
        super(message);
        this.code = code;
    }
}
