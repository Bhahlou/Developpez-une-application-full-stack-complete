package com.openclassrooms.mddapi.service;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.openclassrooms.mddapi.dto.CreatePostRequest;
import com.openclassrooms.mddapi.dto.PostResponse;
import com.openclassrooms.mddapi.exception.PostNotFoundException;
import com.openclassrooms.mddapi.exception.ThemeNotFoundException;
import com.openclassrooms.mddapi.model.Post;
import com.openclassrooms.mddapi.model.Theme;
import com.openclassrooms.mddapi.model.User;
import com.openclassrooms.mddapi.repository.PostRepository;
import com.openclassrooms.mddapi.repository.SubscriptionRepository;
import com.openclassrooms.mddapi.repository.ThemeRepository;
import com.openclassrooms.mddapi.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Articles (posts): feed retrieval, detail lookup and creation.
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ThemeRepository themeRepository;
    private final UserRepository userRepository;

    /**
     * Builds the current user's feed from the themes they are subscribed to.
     *
     * @param userId    the current user's id
     * @param direction sort direction applied to the creation date
     * @return the matching posts, or an empty list if the user has no subscriptions
     */
    public List<PostResponse> findFeed(Long userId, Sort.Direction direction) {
        Set<Long> subscribedThemeIds = subscriptionRepository.findThemeIdsByUserId(userId);
        if (subscribedThemeIds.isEmpty()) {
            return List.of();
        }

        return postRepository.findByTheme_IdIn(subscribedThemeIds, Sort.by(direction, "createdAt")).stream()
                .map(PostService::toResponse)
                .toList();
    }

    /**
     * Publishes a new post under an existing theme.
     *
     * @param userId  the author's id
     * @param request the theme, title and content of the post
     * @return the created post
     * @throws ThemeNotFoundException if no theme matches {@code request.themeId()}
     */
    public PostResponse create(Long userId, CreatePostRequest request) {
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new ThemeNotFoundException("THEME_NOT_FOUND", "Theme not found"));
        User author = userRepository.getReferenceById(userId);

        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .theme(theme)
                .author(author)
                .build();
        postRepository.save(post);

        return toResponse(post);
    }

    /**
     * Returns a single post regardless of the caller's subscriptions.
     *
     * @param id the post id
     * @return the post detail
     * @throws PostNotFoundException if no post matches {@code id}
     */
    public PostResponse findById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("POST_NOT_FOUND", "Post not found"));
        return toResponse(post);
    }

    /**
     * @param post the entity to convert
     * @return the corresponding response DTO
     */
    private static PostResponse toResponse(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getTheme().getId(),
                post.getTheme().getTitle(),
                post.getAuthor().getUsername(),
                post.getCreatedAt());
    }
}
