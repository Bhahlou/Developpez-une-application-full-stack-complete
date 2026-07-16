package com.openclassrooms.mddapi.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.openclassrooms.mddapi.dto.AuthResponse;
import com.openclassrooms.mddapi.dto.CreateCommentRequest;
import com.openclassrooms.mddapi.model.Post;
import com.openclassrooms.mddapi.model.Theme;
import com.openclassrooms.mddapi.model.User;
import com.openclassrooms.mddapi.repository.CommentRepository;
import com.openclassrooms.mddapi.repository.PostRepository;
import com.openclassrooms.mddapi.repository.SubscriptionRepository;
import com.openclassrooms.mddapi.repository.ThemeRepository;
import com.openclassrooms.mddapi.repository.UserRepository;

class CommentIntegrationTest extends AbstractIntegrationTest {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ThemeRepository themeRepository;

    @Autowired
    CommentIntegrationTest(MockMvc mockMvc, UserRepository userRepository, CommentRepository commentRepository,
            PostRepository postRepository, SubscriptionRepository subscriptionRepository, ThemeRepository themeRepository) {
        super(mockMvc, userRepository);
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.themeRepository = themeRepository;
    }

    @AfterEach
    void cleanUp() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        subscriptionRepository.deleteAll();
        themeRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void create_persistsComment_andReturns201WithAuthor() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");
        Post savedPost = createPostAndSubscribe("johndoe", tokens);

        CreateCommentRequest request = new CreateCommentRequest("Great read, thanks!");
        mockMvc.perform(post("/api/posts/" + savedPost.getId() + "/comments")
                        .header("Authorization", "Bearer " + tokens.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Great read, thanks!"))
                .andExpect(jsonPath("$.authorUsername").value("johndoe"));
    }

    @Test
    void create_returns403_whenNotSubscribedToTheme() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");
        Post savedPost = createPost("johndoe");

        CreateCommentRequest request = new CreateCommentRequest("Great read, thanks!");
        mockMvc.perform(post("/api/posts/" + savedPost.getId() + "/comments")
                        .header("Authorization", "Bearer " + tokens.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POST_ACCESS_DENIED"));
    }

    @Test
    void create_returns404_whenPostDoesNotExist() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");

        CreateCommentRequest request = new CreateCommentRequest("Great read, thanks!");
        mockMvc.perform(post("/api/posts/999999/comments")
                        .header("Authorization", "Bearer " + tokens.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"));
    }

    @Test
    void create_returns401_whenNoAuthorizationHeader() throws Exception {
        registerUser("johndoe", "john@doe.com", "Passw0rd!");
        Post savedPost = createPost("johndoe");

        CreateCommentRequest request = new CreateCommentRequest("Great read, thanks!");
        mockMvc.perform(post("/api/posts/" + savedPost.getId() + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
    }

    @Test
    void findByPostId_returnsCommentsInChronologicalOrder() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");
        Post savedPost = createPostAndSubscribe("johndoe", tokens);
        addComment(savedPost, tokens, "First comment");
        addComment(savedPost, tokens, "Second comment");

        mockMvc.perform(get("/api/posts/" + savedPost.getId() + "/comments")
                        .header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].content").value("First comment"))
                .andExpect(jsonPath("$[1].content").value("Second comment"));
    }

    @Test
    void findByPostId_returns403_whenNotSubscribedToTheme() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");
        Post savedPost = createPost("johndoe");

        mockMvc.perform(get("/api/posts/" + savedPost.getId() + "/comments")
                        .header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POST_ACCESS_DENIED"));
    }

    @Test
    void findByPostId_returns404_whenPostDoesNotExist() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");

        mockMvc.perform(get("/api/posts/999999/comments")
                        .header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"));
    }

    private Post createPost(String authorUsername) {
        User author = userRepository.findByUsernameOrEmail(authorUsername, authorUsername).orElseThrow();
        Theme theme = themeRepository.save(Theme.builder().title("Java").description("The JVM language").build());
        return postRepository.save(Post.builder().title("Title").content("Content").theme(theme).author(author).build());
    }

    private Post createPostAndSubscribe(String authorUsername, AuthResponse tokens) throws Exception {
        Post savedPost = createPost(authorUsername);
        mockMvc.perform(post("/api/subscriptions/" + savedPost.getTheme().getId())
                .header("Authorization", "Bearer " + tokens.accessToken()));
        return savedPost;
    }

    private void addComment(Post targetPost, AuthResponse tokens, String content) throws Exception {
        CreateCommentRequest request = new CreateCommentRequest(content);
        mockMvc.perform(post("/api/posts/" + targetPost.getId() + "/comments")
                .header("Authorization", "Bearer " + tokens.accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }
}
