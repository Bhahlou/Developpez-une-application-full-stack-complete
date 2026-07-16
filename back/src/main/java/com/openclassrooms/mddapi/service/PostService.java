package com.openclassrooms.mddapi.service;

import org.springframework.data.domain.Sort;

import com.openclassrooms.mddapi.dto.CreatePostRequest;
import com.openclassrooms.mddapi.dto.PostPageResponse;
import com.openclassrooms.mddapi.dto.PostResponse;
import com.openclassrooms.mddapi.exception.PostNotFoundException;
import com.openclassrooms.mddapi.exception.ThemeNotFoundException;

/**
 * Articles (posts): feed retrieval, detail lookup and creation.
 */
public interface PostService {

    /**
     * Builds a page of the current user's feed from the themes they are subscribed to.
     *
     * @param userId    the current user's id
     * @param direction sort direction applied to the creation date
     * @param page      the zero-based page index to fetch
     * @param size      the number of posts per page
     * @return the matching page of posts, or an empty page if the user has no subscriptions
     */
    PostPageResponse findFeed(Long userId, Sort.Direction direction, int page, int size);

    /**
     * Publishes a new post under an existing theme.
     *
     * @param userId  the author's id
     * @param request the theme, title and content of the post
     * @return the created post
     * @throws ThemeNotFoundException if no theme matches {@code request.themeId()}
     */
    PostResponse create(Long userId, CreatePostRequest request);

    /**
     * Returns a single post regardless of the caller's subscriptions.
     *
     * @param id the post id
     * @return the post detail
     * @throws PostNotFoundException if no post matches {@code id}
     */
    PostResponse findById(Long id);
}
