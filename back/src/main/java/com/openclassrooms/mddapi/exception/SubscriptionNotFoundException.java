package com.openclassrooms.mddapi.exception;

import lombok.Getter;

@Getter
public class SubscriptionNotFoundException extends RuntimeException {

    private final String code;

    public SubscriptionNotFoundException(String code, String message) {
        super(message);
        this.code = code;
    }
}
