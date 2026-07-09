package com.openclassrooms.mddapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

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

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private UserRepository userRepository;

    private PostService postService;

    @BeforeEach
    void setUp() {
        postService = new PostService(postRepository, subscriptionRepository, themeRepository, userRepository);
    }

    @Test
    void findFeed_returnsEmptyList_whenUserHasNoSubscriptions() {
        when(subscriptionRepository.findThemeIdsByUserId(42L)).thenReturn(Set.of());

        List<PostResponse> result = postService.findFeed(42L, Sort.Direction.DESC);

        assertThat(result).isEmpty();
        verify(postRepository, never()).findByTheme_IdIn(anyCollection(), any(Sort.class));
    }

    @Test
    void findFeed_returnsPostsFromSubscribedThemes() {
        Theme theme = Theme.builder().id(1L).title("Backend").description("desc").build();
        User author = User.builder().id(2L).username("johndoe").email("john@doe.com").password("encoded").build();
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Post post = Post.builder().id(1L).title("t").content("c").theme(theme).author(author).createdAt(createdAt)
                .build();
        when(subscriptionRepository.findThemeIdsByUserId(42L)).thenReturn(Set.of(1L));
        when(postRepository.findByTheme_IdIn(Set.of(1L), Sort.by(Sort.Direction.DESC, "createdAt")))
                .thenReturn(List.of(post));

        List<PostResponse> result = postService.findFeed(42L, Sort.Direction.DESC);

        assertThat(result).containsExactly(
                new PostResponse(1L, "t", "c", 1L, "Backend", "johndoe", createdAt));
    }

    @Test
    void create_savesAndReturnsPost_whenThemeExists() {
        Theme theme = Theme.builder().id(1L).title("Backend").description("desc").build();
        User author = User.builder().id(2L).username("johndoe").email("john@doe.com").password("encoded").build();
        CreatePostRequest request = new CreatePostRequest(1L, "t", "c");
        when(themeRepository.findById(1L)).thenReturn(java.util.Optional.of(theme));
        when(userRepository.getReferenceById(2L)).thenReturn(author);

        PostResponse response = postService.create(2L, request);

        assertThat(response.title()).isEqualTo("t");
        assertThat(response.content()).isEqualTo("c");
        assertThat(response.themeId()).isEqualTo(1L);
        assertThat(response.themeTitle()).isEqualTo("Backend");
        assertThat(response.authorUsername()).isEqualTo("johndoe");
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void create_throws_whenThemeDoesNotExist() {
        CreatePostRequest request = new CreatePostRequest(1L, "t", "c");
        when(themeRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> postService.create(2L, request))
                .isInstanceOf(ThemeNotFoundException.class)
                .hasMessage("Theme not found");
    }

    @Test
    void findById_returnsPost_whenExists() {
        Theme theme = Theme.builder().id(1L).title("Backend").description("desc").build();
        User author = User.builder().id(2L).username("johndoe").email("john@doe.com").password("encoded").build();
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Post post = Post.builder().id(1L).title("t").content("c").theme(theme).author(author).createdAt(createdAt)
                .build();
        when(postRepository.findById(1L)).thenReturn(java.util.Optional.of(post));

        PostResponse response = postService.findById(1L);

        assertThat(response).isEqualTo(new PostResponse(1L, "t", "c", 1L, "Backend", "johndoe", createdAt));
    }

    @Test
    void findById_throws_whenNotFound() {
        when(postRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> postService.findById(1L))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessage("Post not found");
    }
}
