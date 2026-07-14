package com.openclassrooms.mddapi.dto;

import java.time.Instant;

/**
 * Uniform error body returned by {@link com.openclassrooms.mddapi.exception.GlobalExceptionHandler}.
 *
 * @param timestamp when the error occurred
 * @param status    the HTTP status code
 * @param error     the HTTP status reason phrase
 * @param code      a stable, machine-readable error code (e.g. {@code THEME_NOT_FOUND})
 * @param message   a human-readable description of the error
 * @param path      the request URI that triggered the error
 */
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path) {
}
