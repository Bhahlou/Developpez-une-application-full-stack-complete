package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.exception.InvalidRefreshTokenException;
import com.openclassrooms.mddapi.model.User;

/**
 * Issues, validates and revokes opaque refresh tokens, stored directly on
 * the {@link User} entity (one active refresh token per user at a time).
 */
public interface RefreshTokenService {

    /**
     * Generates a new random refresh token and stores it on the user,
     * replacing any previous one.
     *
     * @param user the user to issue a token for
     * @return the newly issued refresh token
     */
    String issue(User user);

    /**
     * @param token the refresh token to validate
     * @return the user this token belongs to
     * @throws InvalidRefreshTokenException if the token is unknown or has expired
     */
    User validate(String token);

    /**
     * Clears a user's refresh token, ending their session.
     *
     * @param user the user to revoke the token for
     */
    void revoke(User user);
}
