package com.openclassrooms.mddapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.openclassrooms.mddapi.model.Comment;

/**
 * Spring Data JPA repository for {@link Comment}.
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * @param postId the post to list comments for
     * @return the post's comments, oldest first
     */
    List<Comment> findByPost_IdOrderByCreatedAtAsc(Long postId);
}
