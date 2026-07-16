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

import lombok.extern.slf4j.Slf4j;

/**
 * Central translation of domain exceptions into {@link ApiErrorResponse} bodies.
 * <p>
 * Every handler maps a business error to a fixed HTTP status and a stable
 * {@code code}, without leaking internal details (stack traces, SQL, etc.) to
 * the client.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @param ex      the duplicate-account error
     * @param request the failed request
     * @return 409 Conflict with the exception's code and message
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getCode(), ex.getMessage(), request);
    }

    /**
     * @param ex      the duplicate-theme-title error
     * @param request the failed request
     * @return 409 Conflict with the exception's code and message
     */
    @ExceptionHandler(ThemeAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleThemeAlreadyExists(ThemeAlreadyExistsException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getCode(), ex.getMessage(), request);
    }

    /**
     * @param ex      the expired or unknown refresh token error
     * @param request the failed request
     * @return 401 Unauthorized with the exception's code and message
     */
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRefreshToken(InvalidRefreshTokenException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getCode(), ex.getMessage(), request);
    }

    /**
     * @param ex      the unknown-theme error
     * @param request the failed request
     * @return 404 Not Found with the exception's code and message
     */
    @ExceptionHandler(ThemeNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleThemeNotFound(ThemeNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getCode(), ex.getMessage(), request);
    }

    /**
     * @param ex      the duplicate-subscription error
     * @param request the failed request
     * @return 409 Conflict with the exception's code and message
     */
    @ExceptionHandler(SubscriptionAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleSubscriptionAlreadyExists(SubscriptionAlreadyExistsException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getCode(), ex.getMessage(), request);
    }

    /**
     * @param ex      the unknown-subscription error
     * @param request the failed request
     * @return 404 Not Found with the exception's code and message
     */
    @ExceptionHandler(SubscriptionNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleSubscriptionNotFound(SubscriptionNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getCode(), ex.getMessage(), request);
    }

    /**
     * @param ex      the unknown-post error
     * @param request the failed request
     * @return 404 Not Found with the exception's code and message
     */
    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handlePostNotFound(PostNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getCode(), ex.getMessage(), request);
    }

    /**
     * @param ex      the authentication failure raised by Spring Security
     * @param request the failed request
     * @return 401 Unauthorized with a generic {@code AUTH_BAD_CREDENTIALS} code
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "AUTH_BAD_CREDENTIALS", ex.getMessage(), request);
    }

    /**
     * Handles Bean Validation failures on {@code @Valid} request bodies.
     *
     * @param ex      the validation failure, holding one error per invalid field
     * @param request the failed request
     * @return 400 Bad Request describing the first invalid field
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Validation failed");
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
    }

    /**
     * Assembles the uniform {@link ApiErrorResponse} body for a given status/code/message.
     *
     * @param status  the HTTP status to respond with
     * @param code    the stable, machine-readable error code
     * @param message the human-readable error message
     * @param request the failed request, used to extract the request path
     * @return the fully populated error response, wrapped with the given status
     */
    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String code, String message, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        log.warn("Request failed [{}] {} - {} ({})", status.value(), path, message, code);

        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                path);
        return ResponseEntity.status(status).body(body);
    }
}
