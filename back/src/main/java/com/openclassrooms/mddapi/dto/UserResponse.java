package com.openclassrooms.mddapi.dto;

public record UserResponse(
        Long id,
        String username,
        String email) {
}
