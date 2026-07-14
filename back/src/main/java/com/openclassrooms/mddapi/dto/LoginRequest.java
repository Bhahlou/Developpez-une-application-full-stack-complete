package com.openclassrooms.mddapi.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Login credentials.
 *
 * @param identifier the username or email to authenticate with
 * @param password   the account password
 */
public record LoginRequest(

        @NotBlank
        String identifier,

        @NotBlank
        String password) {
}
