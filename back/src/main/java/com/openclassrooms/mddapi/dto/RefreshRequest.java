package com.openclassrooms.mddapi.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Payload carrying the refresh token for the refresh and logout endpoints.
 *
 * @param refreshToken the refresh token to rotate or invalidate
 */
public record RefreshRequest(

        @NotBlank
        String refreshToken) {
}
