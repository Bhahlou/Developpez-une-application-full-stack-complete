package com.openclassrooms.mddapi.dto;

import java.time.Instant;

/**
 * A comment as returned to the client.
 *
 * @param id             the comment id
 * @param content        the comment text
 * @param authorUsername the username of the comment's author
 * @param createdAt      when the comment was posted
 */
public record CommentResponse(
        Long id,
        String content,
        String authorUsername,
        Instant createdAt) {
}
