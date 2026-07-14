package com.openclassrooms.mddapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.mddapi.dto.AuthResponse;
import com.openclassrooms.mddapi.dto.LoginRequest;
import com.openclassrooms.mddapi.dto.RefreshRequest;
import com.openclassrooms.mddapi.dto.RegisterRequest;
import com.openclassrooms.mddapi.dto.UpdateProfileRequest;
import com.openclassrooms.mddapi.dto.UserResponse;
import com.openclassrooms.mddapi.service.AuthService;
import com.openclassrooms.mddapi.service.UserPrincipal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Registration, login, token refresh, logout and current-user profile endpoints.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Creates a new user account.
     *
     * @param request the registration form (username, email, password)
     * @return 201 with a fresh access/refresh token pair
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /**
     * Authenticates a user by username/email and password.
     *
     * @param request the login credentials
     * @return 200 with a fresh access/refresh token pair
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Exchanges a valid refresh token for a new token pair.
     *
     * @param request the refresh token to rotate
     * @return 200 with a new access/refresh token pair
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    /**
     * Invalidates a refresh token, ending the session it belongs to.
     *
     * @param request the refresh token to invalidate
     * @return 204 No Content
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns the profile of the currently authenticated user.
     *
     * @param principal the authenticated user, resolved from the JWT
     * @return 200 with the user's id, username and email
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        var user = principal.getUser();
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getUsername(), user.getEmail()));
    }

    /**
     * Updates the current user's profile, optionally changing the password.
     *
     * @param principal the authenticated user, resolved from the JWT
     * @param request   the new profile data; requires the current password
     * @return 200 with a fresh token pair (tokens are rotated since the identity may have changed)
     */
    @PutMapping("/me")
    public ResponseEntity<AuthResponse> updateMe(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(authService.updateProfile(principal.getUser().getId(), request));
    }
}
