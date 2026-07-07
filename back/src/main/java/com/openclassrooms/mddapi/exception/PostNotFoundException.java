package com.openclassrooms.mddapi.exception;

import lombok.Getter;

@Getter
public class PostNotFoundException extends RuntimeException {

    private final String code;

    public PostNotFoundException(String code, String message) {
        super(message);
        this.code = code;
    }
}
