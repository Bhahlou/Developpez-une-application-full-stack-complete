package com.openclassrooms.mddapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.openclassrooms.mddapi.dto.CommentResponse;
import com.openclassrooms.mddapi.dto.CreateCommentRequest;
import com.openclassrooms.mddapi.exception.PostNotFoundException;
import com.openclassrooms.mddapi.model.Comment;
import com.openclassrooms.mddapi.model.Post;
import com.openclassrooms.mddapi.model.Theme;
import com.openclassrooms.mddapi.model.User;
import com.openclassrooms.mddapi.repository.CommentRepository;
import com.openclassrooms.mddapi.repository.PostRepository;
import com.openclassrooms.mddapi.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    private CommentService commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(commentRepository, postRepository, userRepository);
    }

    @Test
    void findByPostId_throws_whenPostDoesNotExist() {
        when(postRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> commentService.findByPostId(1L))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessage("Post not found");
    }

    @Test
    void findByPostId_returnsCommentsOrderedByCreatedAtAsc() {
        Theme theme = Theme.builder().id(1L).title("Backend").description("desc").build();
        User author = User.builder().id(2L).username("johndoe").email("john@doe.com").password("encoded").build();
        Post post = Post.builder().id(1L).title("t").content("c").theme(theme).author(author).build();
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Comment comment = Comment.builder().id(1L).content("Nice article").post(post).author(author)
                .createdAt(createdAt).build();
        when(postRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findByPost_IdOrderByCreatedAtAsc(1L)).thenReturn(List.of(comment));

        List<CommentResponse> result = commentService.findByPostId(1L);

        assertThat(result).containsExactly(new CommentResponse(1L, "Nice article", "johndoe", createdAt));
    }

    @Test
    void create_savesAndReturnsComment_whenPostExists() {
        Theme theme = Theme.builder().id(1L).title("Backend").description("desc").build();
        User author = User.builder().id(2L).username("johndoe").email("john@doe.com").password("encoded").build();
        Post post = Post.builder().id(1L).title("t").content("c").theme(theme).author(author).build();
        CreateCommentRequest request = new CreateCommentRequest("Nice article");
        when(postRepository.findById(1L)).thenReturn(java.util.Optional.of(post));
        when(userRepository.getReferenceById(2L)).thenReturn(author);

        CommentResponse response = commentService.create(2L, 1L, request);

        assertThat(response.content()).isEqualTo("Nice article");
        assertThat(response.authorUsername()).isEqualTo("johndoe");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void create_throws_whenPostDoesNotExist() {
        CreateCommentRequest request = new CreateCommentRequest("Nice article");
        when(postRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> commentService.create(2L, 1L, request))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessage("Post not found");
        verify(commentRepository, never()).save(any(Comment.class));
    }
}
