package com.openclassrooms.mddapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT configuration bound from the {@code app.jwt.*} properties.
 *
 * @param secret                   the HMAC secret used to sign and verify tokens
 * @param accessTokenExpirationMs  access token lifetime, in milliseconds
 * @param refreshTokenExpirationMs refresh token lifetime, in milliseconds
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
                String secret,
                long accessTokenExpirationMs,
                long refreshTokenExpirationMs) {
}
