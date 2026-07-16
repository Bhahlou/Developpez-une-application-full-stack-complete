package com.openclassrooms.mddapi.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.openclassrooms.mddapi.dto.CommentResponse;
import com.openclassrooms.mddapi.dto.CreateCommentRequest;
import com.openclassrooms.mddapi.exception.PostAccessDeniedException;
import com.openclassrooms.mddapi.exception.PostNotFoundException;
import com.openclassrooms.mddapi.model.Comment;
import com.openclassrooms.mddapi.model.Post;
import com.openclassrooms.mddapi.model.User;
import com.openclassrooms.mddapi.repository.CommentRepository;
import com.openclassrooms.mddapi.repository.PostRepository;
import com.openclassrooms.mddapi.repository.SubscriptionRepository;
import com.openclassrooms.mddapi.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Override
    public List<CommentResponse> findByPostId(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("POST_NOT_FOUND", "Post not found"));
        requireSubscribed(userId, post);

        return commentRepository.findByPost_IdOrderByCreatedAtAsc(postId).stream()
                .map(CommentServiceImpl::toResponse)
                .toList();
    }

    @Override
    public CommentResponse create(Long userId, Long postId, CreateCommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("POST_NOT_FOUND", "Post not found"));
        requireSubscribed(userId, post);
        User author = userRepository.getReferenceById(userId);

        Comment comment = Comment.builder()
                .content(request.content())
                .post(post)
                .author(author)
                .build();
        commentRepository.save(comment);
        log.info("Comment created: id={}, postId={}, authorId={}", comment.getId(), postId, userId);

        return toResponse(comment);
    }

    /**
     * @param userId the caller's id
     * @param post   the post being accessed
     * @throws PostAccessDeniedException if the caller is not subscribed to the post's theme
     */
    private void requireSubscribed(Long userId, Post post) {
        if (!subscriptionRepository.existsByUserIdAndThemeId(userId, post.getTheme().getId())) {
            throw new PostAccessDeniedException("POST_ACCESS_DENIED", "You are not subscribed to this post's theme");
        }
    }

    /**
     * @param comment the entity to convert
     * @return the corresponding response DTO
     */
    private static CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getAuthor().getUsername(),
                comment.getCreatedAt());
    }
}
