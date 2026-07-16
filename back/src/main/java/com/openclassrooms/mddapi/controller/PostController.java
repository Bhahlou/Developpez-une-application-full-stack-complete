package com.openclassrooms.mddapi.controller;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.mddapi.dto.CreatePostRequest;
import com.openclassrooms.mddapi.dto.PostPageResponse;
import com.openclassrooms.mddapi.dto.PostResponse;
import com.openclassrooms.mddapi.service.PostService;
import com.openclassrooms.mddapi.service.UserPrincipal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Articles (posts) feed, article detail, and article creation.
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * Returns a page of the current user's feed: posts from the themes they
     * are subscribed to, sorted by creation date.
     *
     * @param principal the authenticated user, resolved from the JWT
     * @param sort      {@code "asc"} for oldest first, anything else for newest first
     * @param page      the zero-based page index to fetch
     * @param size      the number of posts per page
     * @return 200 with the matching page of posts
     */
    @GetMapping
    public ResponseEntity<PostPageResponse> findFeed(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "desc") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return ResponseEntity.ok(postService.findFeed(principal.getUser().getId(), direction, page, size));
    }

    /**
     * Returns a single post, provided the caller is subscribed to its theme.
     *
     * @param principal the authenticated user, resolved from the JWT
     * @param id        the post id
     * @return 200 with the post detail
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> findById(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(postService.findById(id, principal.getUser().getId()));
    }

    /**
     * Publishes a new post under a theme.
     *
     * @param principal the authenticated user, resolved from the JWT
     * @param request   the theme, title and content of the post
     * @return 201 with the created post
     */
    @PostMapping
    public ResponseEntity<PostResponse> create(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreatePostRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.create(principal.getUser().getId(), request));
    }
}
