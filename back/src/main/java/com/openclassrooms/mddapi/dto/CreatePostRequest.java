package com.openclassrooms.mddapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload for publishing a new article.
 *
 * @param themeId the theme the article is published under; required
 * @param title   the article title; required, up to 100 characters
 * @param content the article body; required, up to 5000 characters
 */
public record CreatePostRequest(

        @NotNull Long themeId,

        @NotBlank @Size(max = 100) String title,

        @NotBlank @Size(max = 5000) String content) {
}
