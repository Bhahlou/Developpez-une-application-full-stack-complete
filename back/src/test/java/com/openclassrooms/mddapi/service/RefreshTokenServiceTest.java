package com.openclassrooms.mddapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.openclassrooms.mddapi.config.JwtProperties;
import com.openclassrooms.mddapi.exception.InvalidRefreshTokenException;
import com.openclassrooms.mddapi.model.User;
import com.openclassrooms.mddapi.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    private static final long REFRESH_EXPIRATION_MS = 604_800_000L;

    @Mock
    private UserRepository userRepository;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenServiceImpl(userRepository,
                new JwtProperties("unused-secret", 900_000L, REFRESH_EXPIRATION_MS));
    }

    private User buildUser() {
        return User.builder().id(1L).username("johndoe").email("john@doe.com").password("encoded").build();
    }

    @Test
    void issue_setsTokenAndExpiryAndSavesUser() {
        User user = buildUser();

        String token = refreshTokenService.issue(user);

        assertThat(token).isNotBlank();
        assertThat(user.getRefreshToken()).isEqualTo(token);
        assertThat(user.getRefreshTokenExpiry()).isAfter(Instant.now());
        verify(userRepository).save(user);
    }

    @Test
    void validate_returnsUser_whenTokenValidAndNotExpired() {
        User user = buildUser();
        user.setRefreshToken("valid-token");
        user.setRefreshTokenExpiry(Instant.now().plusSeconds(60));
        when(userRepository.findByRefreshToken("valid-token")).thenReturn(Optional.of(user));

        User result = refreshTokenService.validate("valid-token");

        assertThat(result).isEqualTo(user);
    }

    @Test
    void validate_throwsInvalidRefreshToken_whenTokenNotFound() {
        when(userRepository.findByRefreshToken("unknown-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.validate("unknown-token"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .extracting(ex -> ((InvalidRefreshTokenException) ex).getCode())
                .isEqualTo("AUTH_INVALID_REFRESH_TOKEN");
    }

    @Test
    void validate_throwsExpired_whenExpiryIsNull() {
        User user = buildUser();
        user.setRefreshToken("valid-token");
        user.setRefreshTokenExpiry(null);
        when(userRepository.findByRefreshToken("valid-token")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> refreshTokenService.validate("valid-token"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .extracting(ex -> ((InvalidRefreshTokenException) ex).getCode())
                .isEqualTo("AUTH_REFRESH_TOKEN_EXPIRED");
    }

    @Test
    void validate_throwsExpired_whenExpiryIsInThePast() {
        User user = buildUser();
        user.setRefreshToken("valid-token");
        user.setRefreshTokenExpiry(Instant.now().minus(1, ChronoUnit.SECONDS));
        when(userRepository.findByRefreshToken("valid-token")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> refreshTokenService.validate("valid-token"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .extracting(ex -> ((InvalidRefreshTokenException) ex).getCode())
                .isEqualTo("AUTH_REFRESH_TOKEN_EXPIRED");
    }

    @Test
    void revoke_clearsTokenAndExpiryAndSaves() {
        User user = buildUser();
        user.setRefreshToken("some-token");
        user.setRefreshTokenExpiry(Instant.now().plusSeconds(60));

        refreshTokenService.revoke(user);

        assertThat(user.getRefreshToken()).isNull();
        assertThat(user.getRefreshTokenExpiry()).isNull();
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue()).isEqualTo(user);
    }
}
