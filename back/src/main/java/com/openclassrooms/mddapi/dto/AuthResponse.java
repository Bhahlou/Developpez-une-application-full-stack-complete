package com.openclassrooms.mddapi.dto;

/**
 * Token pair returned after a successful register, login, refresh or profile update.
 *
 * @param accessToken  short-lived JWT sent as a bearer token on subsequent requests
 * @param refreshToken long-lived token used to obtain a new access token
 */
public record AuthResponse(
        String accessToken,
        String refreshToken) {
}
