package com.openclassrooms.mddapi.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import com.openclassrooms.mddapi.dto.ApiErrorResponse;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/auth/register");
    }

    @Test
    void handleUserAlreadyExists_returnsConflictWithExceptionCodeAndPath() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException("USER_EMAIL_TAKEN", "Email already in use");

        ResponseEntity<ApiErrorResponse> response = handler.handleUserAlreadyExists(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("USER_EMAIL_TAKEN");
        assertThat(response.getBody().message()).isEqualTo("Email already in use");
        assertThat(response.getBody().path()).isEqualTo("/api/auth/register");
        assertThat(response.getBody().status()).isEqualTo(409);
    }

    @Test
    void handleInvalidRefreshToken_returnsUnauthorizedWithExceptionCode() {
        InvalidRefreshTokenException ex = new InvalidRefreshTokenException("AUTH_REFRESH_TOKEN_EXPIRED",
                "Refresh token has expired");

        ResponseEntity<ApiErrorResponse> response = handler.handleInvalidRefreshToken(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().code()).isEqualTo("AUTH_REFRESH_TOKEN_EXPIRED");
        assertThat(response.getBody().message()).isEqualTo("Refresh token has expired");
    }

    @Test
    void handleBadCredentials_returnsUnauthorizedWithGenericCode() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");

        ResponseEntity<ApiErrorResponse> response = handler.handleBadCredentials(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().code()).isEqualTo("AUTH_BAD_CREDENTIALS");
    }

    @Test
    void handleValidation_returnsBadRequestWithFirstFieldError() {
        FieldError fieldError = new FieldError("registerRequest", "username", "must not be blank");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ApiErrorResponse> response = handler.handleValidation(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().message()).isEqualTo("username must not be blank");
    }

    @Test
    void handleValidation_fallsBackToGenericMessage_whenNoFieldErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ApiErrorResponse> response = handler.handleValidation(ex, webRequest);

        assertThat(response.getBody().message()).isEqualTo("Validation failed");
    }
}
