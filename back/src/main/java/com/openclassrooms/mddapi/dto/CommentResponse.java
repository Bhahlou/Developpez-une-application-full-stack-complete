package com.openclassrooms.mddapi.dto;

import java.time.Instant;

public record CommentResponse(
        Long id,
        String content,
        String authorUsername,
        Instant createdAt) {
}
