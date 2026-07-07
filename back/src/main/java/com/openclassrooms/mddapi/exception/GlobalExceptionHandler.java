package com.openclassrooms.mddapi.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.openclassrooms.mddapi.dto.ApiErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(ThemeAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleThemeAlreadyExists(ThemeAlreadyExistsException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRefreshToken(InvalidRefreshTokenException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(ThemeNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleThemeNotFound(ThemeNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(SubscriptionAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleSubscriptionAlreadyExists(SubscriptionAlreadyExistsException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(SubscriptionNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleSubscriptionNotFound(SubscriptionNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handlePostNotFound(PostNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "AUTH_BAD_CREDENTIALS", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Validation failed");
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String code, String message, WebRequest request) {
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(status).body(body);
    }
}
