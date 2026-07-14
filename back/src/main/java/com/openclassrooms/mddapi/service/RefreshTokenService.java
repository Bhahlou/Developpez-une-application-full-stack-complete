package com.openclassrooms.mddapi.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.openclassrooms.mddapi.config.JwtProperties;
import com.openclassrooms.mddapi.exception.InvalidRefreshTokenException;
import com.openclassrooms.mddapi.model.User;
import com.openclassrooms.mddapi.repository.UserRepository;

/**
 * Issues, validates and revokes opaque refresh tokens, stored directly on
 * the {@link User} entity (one active refresh token per user at a time).
 */
@Service
public class RefreshTokenService {

    private final UserRepository userRepository;
    private final long refreshTokenExpirationMs;

    /**
     * @param userRepository  used to persist the token alongside the user
     * @param jwtProperties   supplies the refresh token lifetime
     */
    public RefreshTokenService(UserRepository userRepository, JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.refreshTokenExpirationMs = jwtProperties.refreshTokenExpirationMs();
    }

    /**
     * Generates a new random refresh token and stores it on the user,
     * replacing any previous one.
     *
     * @param user the user to issue a token for
     * @return the newly issued refresh token
     */
    public String issue(User user) {
        String token = UUID.randomUUID().toString();
        user.setRefreshToken(token);
        user.setRefreshTokenExpiry(Instant.now().plusMillis(refreshTokenExpirationMs));
        userRepository.save(user);
        return token;
    }

    /**
     * @param token the refresh token to validate
     * @return the user this token belongs to
     * @throws InvalidRefreshTokenException if the token is unknown or has expired
     */
    public User validate(String token) {
        User user = userRepository.findByRefreshToken(token)
                .orElseThrow(() -> new InvalidRefreshTokenException("AUTH_INVALID_REFRESH_TOKEN", "Invalid refresh token"));

        if (user.getRefreshTokenExpiry() == null || user.getRefreshTokenExpiry().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException("AUTH_REFRESH_TOKEN_EXPIRED", "Refresh token has expired");
        }

        return user;
    }

    /**
     * Clears a user's refresh token, ending their session.
     *
     * @param user the user to revoke the token for
     */
    public void revoke(User user) {
        user.setRefreshToken(null);
        user.setRefreshTokenExpiry(null);
        userRepository.save(user);
    }
}
