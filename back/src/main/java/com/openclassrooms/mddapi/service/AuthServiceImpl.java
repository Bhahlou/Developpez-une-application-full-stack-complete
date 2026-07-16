package com.openclassrooms.mddapi.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.openclassrooms.mddapi.dto.AuthResponse;
import com.openclassrooms.mddapi.dto.LoginRequest;
import com.openclassrooms.mddapi.dto.RegisterRequest;
import com.openclassrooms.mddapi.dto.UpdateProfileRequest;
import com.openclassrooms.mddapi.exception.UserAlreadyExistsException;
import com.openclassrooms.mddapi.model.User;
import com.openclassrooms.mddapi.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException("USER_USERNAME_TAKEN", "Username already in use");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("USER_EMAIL_TAKEN", "Email already in use");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();
        userRepository.save(user);
        log.info("New user registered: id={}, username={}", user.getId(), user.getUsername());

        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.identifier(), request.password()));

        User user = userRepository.findByUsernameOrEmail(request.identifier(), request.identifier())
                .orElseThrow();
        log.info("User logged in: id={}, username={}", user.getId(), user.getUsername());

        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        User user = refreshTokenService.validate(refreshToken);
        log.debug("Token refreshed for user: id={}, username={}", user.getId(), user.getUsername());
        return buildAuthResponse(user);
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.validate(refreshToken);
        User user = userRepository.findByRefreshToken(refreshToken).orElseThrow();
        refreshTokenService.revoke(user);
        log.info("User logged out: id={}, username={}", user.getId(), user.getUsername());
    }

    @Override
    public AuthResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId).orElseThrow();

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }
        if (userRepository.existsByUsernameAndIdNot(request.username(), userId)) {
            throw new UserAlreadyExistsException("USER_USERNAME_TAKEN", "Username already in use");
        }
        if (userRepository.existsByEmailAndIdNot(request.email(), userId)) {
            throw new UserAlreadyExistsException("USER_EMAIL_TAKEN", "Email already in use");
        }

        user.setUsername(request.username());
        user.setEmail(request.email());
        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.newPassword()));
        }
        userRepository.save(user);
        log.info("User profile updated: id={}, username={}", user.getId(), user.getUsername());

        return buildAuthResponse(user);
    }

    /**
     * @param user the user to issue tokens for
     * @return a new access token and a newly-issued refresh token
     */
    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.issue(user);
        return new AuthResponse(accessToken, refreshToken);
    }
}
