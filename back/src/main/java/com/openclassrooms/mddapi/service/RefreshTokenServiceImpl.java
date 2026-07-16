package com.openclassrooms.mddapi.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.openclassrooms.mddapi.config.JwtProperties;
import com.openclassrooms.mddapi.exception.InvalidRefreshTokenException;
import com.openclassrooms.mddapi.model.User;
import com.openclassrooms.mddapi.repository.UserRepository;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final UserRepository userRepository;
    private final long refreshTokenExpirationMs;

    /**
     * @param userRepository  used to persist the token alongside the user
     * @param jwtProperties   supplies the refresh token lifetime
     */
    public RefreshTokenServiceImpl(UserRepository userRepository, JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.refreshTokenExpirationMs = jwtProperties.refreshTokenExpirationMs();
    }

    @Override
    public String issue(User user) {
        String token = UUID.randomUUID().toString();
        user.setRefreshToken(token);
        user.setRefreshTokenExpiry(Instant.now().plusMillis(refreshTokenExpirationMs));
        userRepository.save(user);
        return token;
    }

    @Override
    public User validate(String token) {
        User user = userRepository.findByRefreshToken(token)
                .orElseThrow(() -> new InvalidRefreshTokenException("AUTH_INVALID_REFRESH_TOKEN", "Invalid refresh token"));

        if (user.getRefreshTokenExpiry() == null || user.getRefreshTokenExpiry().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException("AUTH_REFRESH_TOKEN_EXPIRED", "Refresh token has expired");
        }

        return user;
    }

    @Override
    public void revoke(User user) {
        user.setRefreshToken(null);
        user.setRefreshTokenExpiry(null);
        userRepository.save(user);
    }
}
