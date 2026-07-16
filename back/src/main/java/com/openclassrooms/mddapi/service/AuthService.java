package com.openclassrooms.mddapi.service;

import com.openclassrooms.mddapi.dto.AuthResponse;
import com.openclassrooms.mddapi.dto.LoginRequest;
import com.openclassrooms.mddapi.dto.RegisterRequest;
import com.openclassrooms.mddapi.dto.UpdateProfileRequest;
import com.openclassrooms.mddapi.exception.UserAlreadyExistsException;

/**
 * Registration, login, token refresh/logout, and profile updates.
 */
public interface AuthService {

    /**
     * Creates a new user account with a BCrypt-hashed password.
     *
     * @param request the registration form
     * @return a fresh access/refresh token pair for the new user
     * @throws UserAlreadyExistsException if the username or email is already taken
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates a user by username/email and password.
     *
     * @param request the login credentials
     * @return a fresh access/refresh token pair
     * @throws org.springframework.security.core.AuthenticationException if the credentials are invalid
     */
    AuthResponse login(LoginRequest request);

    /**
     * Rotates a valid refresh token into a new access/refresh token pair.
     *
     * @param refreshToken the refresh token to consume
     * @return a fresh access/refresh token pair
     * @throws com.openclassrooms.mddapi.exception.InvalidRefreshTokenException if the token is unknown or expired
     */
    AuthResponse refresh(String refreshToken);

    /**
     * Ends the session tied to a refresh token by revoking it.
     *
     * @param refreshToken the refresh token to invalidate
     * @throws com.openclassrooms.mddapi.exception.InvalidRefreshTokenException if the token is unknown or expired
     */
    void logout(String refreshToken);

    /**
     * Updates the caller's username, email and optionally their password.
     * <p>
     * Tokens are rotated on every update, since a username change would
     * otherwise leave the old access token's subject claim stale.
     *
     * @param userId  the id of the user being updated
     * @param request the new profile data; the current password is required to confirm the change
     * @return a fresh access/refresh token pair
     * @throws org.springframework.security.authentication.BadCredentialsException if the current password is wrong
     * @throws UserAlreadyExistsException if the new username or email is already taken by another user
     */
    AuthResponse updateProfile(Long userId, UpdateProfileRequest request);
}
