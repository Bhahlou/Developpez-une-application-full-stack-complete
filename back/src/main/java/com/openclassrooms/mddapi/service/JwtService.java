package com.openclassrooms.mddapi.service;

import java.time.Instant;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.openclassrooms.mddapi.config.JwtProperties;
import com.openclassrooms.mddapi.model.User;

import lombok.extern.slf4j.Slf4j;

/**
 * Issues and verifies HMAC-signed JWT access tokens.
 * <p>
 * Refresh tokens are handled separately by {@link RefreshTokenService}: they
 * are opaque random values persisted on the {@link User} entity, not JWTs.
 */
@Slf4j
@Service
public class JwtService {

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final long accessTokenExpirationMs;

    /**
     * @param jwtProperties the signing secret and token lifetimes
     */
    public JwtService(JwtProperties jwtProperties) {
        this.algorithm = Algorithm.HMAC512(Base64.getDecoder().decode(jwtProperties.secret()));
        this.verifier = JWT.require(algorithm).build();
        this.accessTokenExpirationMs = jwtProperties.accessTokenExpirationMs();
    }

    /**
     * @param user the user to issue a token for
     * @return a signed JWT with the username as subject and the user id/email as claims
     */
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(accessTokenExpirationMs);

        return JWT.create()
                .withSubject(user.getUsername())
                .withClaim("userId", user.getId())
                .withClaim("email", user.getEmail())
                .withIssuedAt(now)
                .withExpiresAt(expiry)
                .sign(algorithm);
    }

    /**
     * @param token a previously validated JWT
     * @return the username stored in the token's subject claim
     */
    public String extractUsername(String token) {
        return verifier.verify(token).getSubject();
    }

    /**
     * @param token the JWT to check
     * @return {@code true} if the token's signature and expiry are valid
     */
    public boolean isTokenValid(String token) {
        try {
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            log.debug("JWT verification failed: {}", e.getMessage());
            return false;
        }
    }
}
