package com.openclassrooms.mddapi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.mddapi.dto.CommentResponse;
import com.openclassrooms.mddapi.dto.CreateCommentRequest;
import com.openclassrooms.mddapi.service.CommentService;
import com.openclassrooms.mddapi.service.UserPrincipal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Comments attached to a single post.
 */
@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * Lists the comments of a post.
     *
     * @param postId the post to list comments for
     * @return 200 with the comments, oldest first
     */
    @GetMapping
    public ResponseEntity<List<CommentResponse>> findByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.findByPostId(postId));
    }

    /**
     * Adds a comment to a post.
     *
     * @param principal the authenticated user, resolved from the JWT
     * @param postId    the post being commented on
     * @param request   the comment content
     * @return 201 with the created comment
     */
    @PostMapping
    public ResponseEntity<CommentResponse> create(@AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId, @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.create(principal.getUser().getId(), postId, request));
    }
}
