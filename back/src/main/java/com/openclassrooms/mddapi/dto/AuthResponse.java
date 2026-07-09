package com.openclassrooms.mddapi.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken) {
}
