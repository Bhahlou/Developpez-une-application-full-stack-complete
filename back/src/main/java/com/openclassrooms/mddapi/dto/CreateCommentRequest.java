package com.openclassrooms.mddapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload for posting a comment on an article.
 *
 * @param content the comment text; required, up to 1000 characters
 */
public record CreateCommentRequest(

        @NotBlank @Size(max = 1000) String content) {
}
