package com.openclassrooms.mddapi.dto;

/**
 * The current user's profile as returned to the client.
 *
 * @param id       the user id
 * @param username the username
 * @param email    the email address
 */
public record UserResponse(
        Long id,
        String username,
        String email) {
}
