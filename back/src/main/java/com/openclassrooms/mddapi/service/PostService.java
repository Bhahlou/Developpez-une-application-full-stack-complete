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

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ThemeRepository themeRepository;
    private final UserRepository userRepository;

    public List<PostResponse> findFeed(Long userId, Sort.Direction direction) {
        Set<Long> subscribedThemeIds = subscriptionRepository.findThemeIdsByUserId(userId);
        if (subscribedThemeIds.isEmpty()) {
            return List.of();
        }

        return postRepository.findByTheme_IdIn(subscribedThemeIds, Sort.by(direction, "createdAt")).stream()
                .map(PostService::toResponse)
                .toList();
    }

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

    public PostResponse findById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("POST_NOT_FOUND", "Post not found"));
        return toResponse(post);
    }

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
