package com.openclassrooms.mddapi.service;

import java.util.List;

import com.openclassrooms.mddapi.dto.CommentResponse;
import com.openclassrooms.mddapi.dto.CreateCommentRequest;
import com.openclassrooms.mddapi.exception.PostNotFoundException;

/**
 * Comments attached to posts.
 */
public interface CommentService {

    /**
     * @param postId the post to list comments for
     * @return the post's comments, oldest first
     * @throws PostNotFoundException if no post matches {@code postId}
     */
    List<CommentResponse> findByPostId(Long postId);

    /**
     * Adds a comment to a post. A user may comment on any post, even on a
     * theme they are no longer subscribed to.
     *
     * @param userId  the author's id
     * @param postId  the post being commented on
     * @param request the comment content
     * @return the created comment
     * @throws PostNotFoundException if no post matches {@code postId}
     */
    CommentResponse create(Long userId, Long postId, CreateCommentRequest request);
}
