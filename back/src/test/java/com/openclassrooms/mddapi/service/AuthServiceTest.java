package com.openclassrooms.mddapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.openclassrooms.mddapi.dto.AuthResponse;
import com.openclassrooms.mddapi.dto.LoginRequest;
import com.openclassrooms.mddapi.dto.RegisterRequest;
import com.openclassrooms.mddapi.exception.InvalidRefreshTokenException;
import com.openclassrooms.mddapi.exception.UserAlreadyExistsException;
import com.openclassrooms.mddapi.model.User;
import com.openclassrooms.mddapi.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, authenticationManager, jwtService,
                refreshTokenService);
    }

    private User buildUser() {
        return User.builder()
                .id(1L)
                .username("johndoe")
                .email("john@doe.com")
                .password("encoded-password")
                .build();
    }

    @Test
    void register_savesEncodedUserAndReturnsTokens_whenUsernameAndEmailAvailable() {
        RegisterRequest request = new RegisterRequest("johndoe", "john@doe.com", "Passw0rd!");
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(userRepository.existsByEmail("john@doe.com")).thenReturn(false);
        when(passwordEncoder.encode("Passw0rd!")).thenReturn("encoded-password");
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(refreshTokenService.issue(any(User.class))).thenReturn("refresh-token");

        AuthResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("johndoe");
        assertThat(savedUser.getEmail()).isEqualTo("john@doe.com");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void register_throwsUserAlreadyExists_whenUsernameTaken() {
        RegisterRequest request = new RegisterRequest("johndoe", "john@doe.com", "Passw0rd!");
        when(userRepository.existsByUsername("johndoe")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .extracting(ex -> ((UserAlreadyExistsException) ex).getCode())
                .isEqualTo("USER_USERNAME_TAKEN");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_throwsUserAlreadyExists_whenEmailTaken() {
        RegisterRequest request = new RegisterRequest("johndoe", "john@doe.com", "Passw0rd!");
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(userRepository.existsByEmail("john@doe.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .extracting(ex -> ((UserAlreadyExistsException) ex).getCode())
                .isEqualTo("USER_EMAIL_TAKEN");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_authenticatesAndReturnsTokens_whenCredentialsValid() {
        LoginRequest request = new LoginRequest("johndoe", "Passw0rd!");
        User user = buildUser();
        when(userRepository.findByUsernameOrEmail("johndoe", "johndoe")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(refreshTokenService.issue(user)).thenReturn("refresh-token");

        AuthResponse response = authService.login(request);

        verify(authenticationManager, times(1)).authenticate(any());
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void login_propagatesBadCredentials_whenAuthenticationManagerRejects() {
        LoginRequest request = new LoginRequest("johndoe", "wrong-password");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad credentials"));

        assertThatThrownBy(() -> authService.login(request)).isInstanceOf(BadCredentialsException.class);

        verify(userRepository, never()).findByUsernameOrEmail(anyString(), anyString());
        verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    void refresh_returnsTokens_whenRefreshTokenValid() {
        User user = buildUser();
        when(refreshTokenService.validate("valid-refresh-token")).thenReturn(user);
        when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");
        when(refreshTokenService.issue(user)).thenReturn("new-refresh-token");

        AuthResponse response = authService.refresh("valid-refresh-token");

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    void refresh_propagatesException_whenRefreshTokenInvalid() {
        when(refreshTokenService.validate("invalid-token"))
                .thenThrow(new InvalidRefreshTokenException("AUTH_INVALID_REFRESH_TOKEN", "Invalid refresh token"));

        assertThatThrownBy(() -> authService.refresh("invalid-token"))
                .isInstanceOf(InvalidRefreshTokenException.class);

        verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    void logout_revokesUser_whenRefreshTokenValid() {
        User user = buildUser();
        when(refreshTokenService.validate("valid-refresh-token")).thenReturn(user);
        when(userRepository.findByRefreshToken("valid-refresh-token")).thenReturn(Optional.of(user));

        authService.logout("valid-refresh-token");

        verify(refreshTokenService).revoke(user);
    }

    @Test
    void logout_propagatesException_whenRefreshTokenInvalid() {
        when(refreshTokenService.validate("invalid-token"))
                .thenThrow(new InvalidRefreshTokenException("AUTH_INVALID_REFRESH_TOKEN", "Invalid refresh token"));

        assertThatThrownBy(() -> authService.logout("invalid-token"))
                .isInstanceOf(InvalidRefreshTokenException.class);

        verify(refreshTokenService, never()).revoke(any());
    }
}
