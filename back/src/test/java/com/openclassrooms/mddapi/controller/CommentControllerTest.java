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
import com.openclassrooms.mddapi.dto.CommentResponse;
import com.openclassrooms.mddapi.dto.CreateCommentRequest;
import com.openclassrooms.mddapi.exception.GlobalExceptionHandler;
import com.openclassrooms.mddapi.exception.PostNotFoundException;
import com.openclassrooms.mddapi.model.User;
import com.openclassrooms.mddapi.service.CommentService;
import com.openclassrooms.mddapi.service.UserPrincipal;

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { CommentController.class, GlobalExceptionHandler.class,
        CommentControllerTest.AuthenticationPrincipalTestConfig.class })
class CommentControllerTest {

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
    private CommentService commentService;

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
    void findByPostId_returns200WithComments() throws Exception {
        CommentResponse response = new CommentResponse(1L, "Nice article", "johndoe", Instant.now());
        when(commentService.findByPostId(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/posts/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].content").value("Nice article"))
                .andExpect(jsonPath("$[0].authorUsername").value("johndoe"));
    }

    @Test
    void findByPostId_returns404_whenPostDoesNotExist() throws Exception {
        when(commentService.findByPostId(1L)).thenThrow(new PostNotFoundException("POST_NOT_FOUND", "Post not found"));

        mockMvc.perform(get("/api/posts/1/comments"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"));
    }

    @Test
    void create_returns201WithComment_whenRequestIsValid() throws Exception {
        authenticateAs(1L);
        CreateCommentRequest request = new CreateCommentRequest("Nice article");
        CommentResponse response = new CommentResponse(1L, "Nice article", "johndoe", Instant.now());
        when(commentService.create(eq(1L), eq(1L), any(CreateCommentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("Nice article"));
    }

    @Test
    void create_returns400_whenContentIsBlank() throws Exception {
        authenticateAs(1L);
        CreateCommentRequest request = new CreateCommentRequest("");

        mockMvc.perform(post("/api/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void create_returns404_whenPostDoesNotExist() throws Exception {
        authenticateAs(1L);
        CreateCommentRequest request = new CreateCommentRequest("Nice article");
        when(commentService.create(eq(1L), eq(1L), any(CreateCommentRequest.class)))
                .thenThrow(new PostNotFoundException("POST_NOT_FOUND", "Post not found"));

        mockMvc.perform(post("/api/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"));
    }
}
