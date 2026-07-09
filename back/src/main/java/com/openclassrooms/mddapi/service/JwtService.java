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

@Service
public class JwtService {

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final long accessTokenExpirationMs;

    public JwtService(JwtProperties jwtProperties) {
        this.algorithm = Algorithm.HMAC512(Base64.getDecoder().decode(jwtProperties.secret()));
        this.verifier = JWT.require(algorithm).build();
        this.accessTokenExpirationMs = jwtProperties.accessTokenExpirationMs();
    }

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

    public String extractUsername(String token) {
        return verifier.verify(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException _) {
            return false;
        }
    }
}
