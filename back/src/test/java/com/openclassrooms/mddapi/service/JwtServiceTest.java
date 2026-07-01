package com.openclassrooms.mddapi.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.openclassrooms.mddapi.config.JwtProperties;
import com.openclassrooms.mddapi.model.User;

class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder().encodeToString("test-secret-key-for-jwt-tests".getBytes());

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(new JwtProperties(SECRET, 900_000L, 604_800_000L));
        user = User.builder()
                .id(42L)
                .username("johndoe")
                .email("john@doe.com")
                .password("irrelevant")
                .build();
    }

    @Test
    void generateAccessToken_containsExpectedClaims() {
        String token = jwtService.generateAccessToken(user);

        DecodedJWT decoded = JWT.decode(token);
        assertThat(decoded.getSubject()).isEqualTo("johndoe");
        assertThat(decoded.getClaim("userId").asLong()).isEqualTo(42L);
        assertThat(decoded.getClaim("email").asString()).isEqualTo("john@doe.com");
        assertThat(decoded.getExpiresAtAsInstant()).isAfter(decoded.getIssuedAtAsInstant());
    }

    @Test
    void extractUsername_returnsUsernameFromValidToken() {
        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo("johndoe");
    }

    @Test
    void isTokenValid_true_forFreshlyGeneratedToken() {
        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_false_forTamperedToken() {
        String token = jwtService.generateAccessToken(user);
        int payloadStart = token.indexOf('.') + 1;
        char flippedChar = token.charAt(payloadStart) == 'a' ? 'b' : 'a';
        String tampered = token.substring(0, payloadStart) + flippedChar + token.substring(payloadStart + 1);

        assertThat(jwtService.isTokenValid(tampered)).isFalse();
    }

    @Test
    void isTokenValid_false_forMalformedToken() {
        assertThat(jwtService.isTokenValid("not-a-jwt")).isFalse();
    }

    @Test
    void isTokenValid_false_forExpiredToken() {
        JwtService expiringJwtService = new JwtService(new JwtProperties(SECRET, -1_000L, 604_800_000L));
        String expiredToken = expiringJwtService.generateAccessToken(user);

        assertThat(jwtService.isTokenValid(expiredToken)).isFalse();
    }

    @Test
    void isTokenValid_false_forTokenSignedWithDifferentSecret() {
        String otherSecret = Base64.getEncoder().encodeToString("another-secret-key-entirely".getBytes());
        String tokenFromOtherIssuer = JWT.create()
                .withSubject("johndoe")
                .sign(Algorithm.HMAC512(Base64.getDecoder().decode(otherSecret)));

        assertThat(jwtService.isTokenValid(tokenFromOtherIssuer)).isFalse();
    }
}
