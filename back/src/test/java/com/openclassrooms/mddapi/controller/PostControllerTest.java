package com.openclassrooms.mddapi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.mddapi.dto.CreatePostRequest;
import com.openclassrooms.mddapi.dto.PostPageResponse;
import com.openclassrooms.mddapi.dto.PostResponse;
import com.openclassrooms.mddapi.exception.GlobalExceptionHandler;
import com.openclassrooms.mddapi.exception.PostAccessDeniedException;
import com.openclassrooms.mddapi.exception.PostNotFoundException;
import com.openclassrooms.mddapi.exception.ThemeNotFoundException;
import com.openclassrooms.mddapi.model.User;
import com.openclassrooms.mddapi.service.PostService;
import com.openclassrooms.mddapi.service.UserPrincipal;

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { PostController.class, GlobalExceptionHandler.class,
        PostControllerTest.AuthenticationPrincipalTestConfig.class })
class PostControllerTest {

    @TestConfiguration
    static class AuthenticationPrincipalTestConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new AuthenticationPrincipalArgumentResolver());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PostService postService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(Long userId) {
        User user = User.builder().id(userId).username("johndoe").email("john@doe.com").password("encoded").build();
        UserPrincipal principal = new UserPrincipal(user);
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken(principal, null, "ROLE_USER"));
    }

    @Test
    void findFeed_returns200WithPosts_sortedDescByDefault() throws Exception {
        authenticateAs(1L);
        PostResponse response = new PostResponse(1L, "Title", "Content", 2L, "Backend", "johndoe", Instant.now());
        when(postService.findFeed(1L, Sort.Direction.DESC, 0, 10))
                .thenReturn(new PostPageResponse(List.of(response), 0, 10, 1, false));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Title"))
                .andExpect(jsonPath("$.content[0].themeTitle").value("Backend"))
                .andExpect(jsonPath("$.content[0].authorUsername").value("johndoe"))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void findFeed_returns200WithPosts_sortedAscWhenRequested() throws Exception {
        authenticateAs(1L);
        when(postService.findFeed(1L, Sort.Direction.ASC, 0, 10))
                .thenReturn(new PostPageResponse(List.of(), 0, 10, 0, false));

        mockMvc.perform(get("/api/posts").param("sort", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void findFeed_forwardsPageAndSizeParams() throws Exception {
        authenticateAs(1L);
        when(postService.findFeed(1L, Sort.Direction.DESC, 2, 5))
                .thenReturn(new PostPageResponse(List.of(), 2, 5, 12, true));

        mockMvc.perform(get("/api/posts").param("page", "2").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(12))
                .andExpect(jsonPath("$.hasNext").value(true));
    }

    @Test
    void findById_returns200WithPost_whenExists() throws Exception {
        authenticateAs(1L);
        PostResponse response = new PostResponse(1L, "Title", "Content", 2L, "Backend", "johndoe", Instant.now());
        when(postService.findById(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Title"));
    }

    @Test
    void findById_returns404_whenNotFound() throws Exception {
        authenticateAs(1L);
        when(postService.findById(1L, 1L)).thenThrow(new PostNotFoundException("POST_NOT_FOUND", "Post not found"));

        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"));
    }

    @Test
    void findById_returns403_whenNotSubscribed() throws Exception {
        authenticateAs(1L);
        when(postService.findById(1L, 1L))
                .thenThrow(new PostAccessDeniedException("POST_ACCESS_DENIED", "You are not subscribed to this post's theme"));

        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POST_ACCESS_DENIED"));
    }

    @Test
    void create_returns201WithPost_whenRequestIsValid() throws Exception {
        authenticateAs(1L);
        CreatePostRequest request = new CreatePostRequest(2L, "Title", "Content");
        PostResponse response = new PostResponse(1L, "Title", "Content", 2L, "Backend", "johndoe", Instant.now());
        when(postService.create(eq(1L), any(CreatePostRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Title"));
    }

    @Test
    void create_returns400_whenTitleIsBlank() throws Exception {
        authenticateAs(1L);
        CreatePostRequest request = new CreatePostRequest(2L, "", "Content");

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void create_returns404_whenThemeDoesNotExist() throws Exception {
        authenticateAs(1L);
        CreatePostRequest request = new CreatePostRequest(2L, "Title", "Content");
        when(postService.create(eq(1L), any(CreatePostRequest.class)))
                .thenThrow(new ThemeNotFoundException("THEME_NOT_FOUND", "Theme not found"));

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("THEME_NOT_FOUND"));
    }
}
