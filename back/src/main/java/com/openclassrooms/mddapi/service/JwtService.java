package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.model.User;

/**
 * Issues and verifies HMAC-signed JWT access tokens.
 * <p>
 * Refresh tokens are handled separately by {@link RefreshTokenService}: they
 * are opaque random values persisted on the {@link User} entity, not JWTs.
 */
public interface JwtService {

    /**
     * @param user the user to issue a token for
     * @return a signed JWT with the username as subject and the user id/email as claims
     */
    String generateAccessToken(User user);

    /**
     * @param token a previously validated JWT
     * @return the username stored in the token's subject claim
     */
    String extractUsername(String token);

    /**
     * @param token the JWT to check
     * @return {@code true} if the token's signature and expiry are valid
     */
    boolean isTokenValid(String token);
}
