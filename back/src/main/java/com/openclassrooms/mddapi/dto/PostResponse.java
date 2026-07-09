package com.openclassrooms.mddapi.dto;

import java.time.Instant;

public record PostResponse(
        Long id,
        String title,
        String content,
        Long themeId,
        String themeTitle,
        String authorUsername,
        Instant createdAt) {
}
