package com.openclassrooms.mddapi.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.openclassrooms.mddapi.dto.CommentResponse;
import com.openclassrooms.mddapi.dto.CreateCommentRequest;
import com.openclassrooms.mddapi.exception.PostNotFoundException;
import com.openclassrooms.mddapi.model.Comment;
import com.openclassrooms.mddapi.model.Post;
import com.openclassrooms.mddapi.model.User;
import com.openclassrooms.mddapi.repository.CommentRepository;
import com.openclassrooms.mddapi.repository.PostRepository;
import com.openclassrooms.mddapi.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public List<CommentResponse> findByPostId(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException("POST_NOT_FOUND", "Post not found");
        }

        return commentRepository.findByPost_IdOrderByCreatedAtAsc(postId).stream()
                .map(CommentService::toResponse)
                .toList();
    }

    public CommentResponse create(Long userId, Long postId, CreateCommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("POST_NOT_FOUND", "Post not found"));
        User author = userRepository.getReferenceById(userId);

        Comment comment = Comment.builder()
                .content(request.content())
                .post(post)
                .author(author)
                .build();
        commentRepository.save(comment);

        return toResponse(comment);
    }

    private static CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getAuthor().getUsername(),
                comment.getCreatedAt());
    }
}
