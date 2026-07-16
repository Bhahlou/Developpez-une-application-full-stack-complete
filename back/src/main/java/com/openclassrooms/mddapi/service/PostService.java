package com.openclassrooms.mddapi.service;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.openclassrooms.mddapi.dto.CreatePostRequest;
import com.openclassrooms.mddapi.dto.PostPageResponse;
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
import lombok.extern.slf4j.Slf4j;

/**
 * Articles (posts): feed retrieval, detail lookup and creation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ThemeRepository themeRepository;
    private final UserRepository userRepository;

    /**
     * Builds a page of the current user's feed from the themes they are subscribed to.
     *
     * @param userId    the current user's id
     * @param direction sort direction applied to the creation date
     * @param page      the zero-based page index to fetch
     * @param size      the number of posts per page
     * @return the matching page of posts, or an empty page if the user has no subscriptions
     */
    public PostPageResponse findFeed(Long userId, Sort.Direction direction, int page, int size) {
        Set<Long> subscribedThemeIds = subscriptionRepository.findThemeIdsByUserId(userId);
        if (subscribedThemeIds.isEmpty()) {
            return new PostPageResponse(List.of(), page, size, 0, false);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
        Page<Post> result = postRepository.findByTheme_IdIn(subscribedThemeIds, pageable);
        List<PostResponse> content = result.getContent().stream()
                .map(PostService::toResponse)
                .toList();

        return new PostPageResponse(content, page, size, result.getTotalElements(), result.hasNext());
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
        log.info("Post created: id={}, themeId={}, authorId={}", post.getId(), theme.getId(), userId);

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
