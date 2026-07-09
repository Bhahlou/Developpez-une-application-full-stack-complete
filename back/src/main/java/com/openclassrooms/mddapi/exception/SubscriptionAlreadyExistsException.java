package com.openclassrooms.mddapi.exception;

import lombok.Getter;

@Getter
public class SubscriptionAlreadyExistsException extends RuntimeException {

    private final String code;

    public SubscriptionAlreadyExistsException(String code, String message) {
        super(message);
        this.code = code;
    }
}
