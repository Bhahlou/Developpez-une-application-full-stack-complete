package com.openclassrooms.mddapi.dto;

import java.time.Instant;

/**
 * An article as returned to the client.
 *
 * @param id             the post id
 * @param title          the article title
 * @param content        the article body
 * @param themeId        the id of the theme the article belongs to
 * @param themeTitle     the title of the theme the article belongs to
 * @param authorUsername the username of the article's author
 * @param createdAt      when the article was published
 */
public record PostResponse(
        Long id,
        String title,
        String content,
        Long themeId,
        String themeTitle,
        String authorUsername,
        Instant createdAt) {
}
