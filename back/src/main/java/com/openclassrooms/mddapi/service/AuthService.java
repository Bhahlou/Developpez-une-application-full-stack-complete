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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

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

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.identifier(), request.password()));

        User user = userRepository.findByUsernameOrEmail(request.identifier(), request.identifier())
                .orElseThrow();

        return buildAuthResponse(user);
    }

    public AuthResponse refresh(String refreshToken) {
        User user = refreshTokenService.validate(refreshToken);
        return buildAuthResponse(user);
    }

    public void logout(String refreshToken) {
        refreshTokenService.validate(refreshToken);
        User user = userRepository.findByRefreshToken(refreshToken).orElseThrow();
        refreshTokenService.revoke(user);
    }

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

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.issue(user);
        return new AuthResponse(accessToken, refreshToken);
    }
}
