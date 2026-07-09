package com.openclassrooms.mddapi.integration;

import java.time.Instant;

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
import com.openclassrooms.mddapi.dto.CreatePostRequest;
import com.openclassrooms.mddapi.model.Post;
import com.openclassrooms.mddapi.model.Theme;
import com.openclassrooms.mddapi.model.User;
import com.openclassrooms.mddapi.repository.PostRepository;
import com.openclassrooms.mddapi.repository.SubscriptionRepository;
import com.openclassrooms.mddapi.repository.ThemeRepository;
import com.openclassrooms.mddapi.repository.UserRepository;

class PostIntegrationTest extends AbstractIntegrationTest {

    private final PostRepository postRepository;
    private final ThemeRepository themeRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Autowired
    PostIntegrationTest(MockMvc mockMvc, UserRepository userRepository, PostRepository postRepository,
            ThemeRepository themeRepository, SubscriptionRepository subscriptionRepository) {
        super(mockMvc, userRepository);
        this.postRepository = postRepository;
        this.themeRepository = themeRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @AfterEach
    void cleanUp() {
        postRepository.deleteAll();
        subscriptionRepository.deleteAll();
        themeRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void create_persistsPost_andReturns201WithAuthorAndTheme() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");
        Theme theme = themeRepository.save(Theme.builder().title("Java").description("The JVM language").build());

        CreatePostRequest request = new CreatePostRequest(theme.getId(), "Why records are great",
                "Records remove boilerplate from Java DTOs.");
        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + tokens.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Why records are great"))
                .andExpect(jsonPath("$.themeTitle").value("Java"))
                .andExpect(jsonPath("$.authorUsername").value("johndoe"));
    }

    @Test
    void create_returns404_whenThemeDoesNotExist() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");

        CreatePostRequest request = new CreatePostRequest(999999L, "Title", "Content");
        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + tokens.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("THEME_NOT_FOUND"));
    }

    @Test
    void create_returns401_whenNoAuthorizationHeader() throws Exception {
        CreatePostRequest request = new CreatePostRequest(1L, "Title", "Content");

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
    }

    @Test
    void findById_returnsPostDetails() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");
        User author = userRepository.findByUsernameOrEmail("johndoe", "johndoe").orElseThrow();
        Theme theme = themeRepository.save(Theme.builder().title("Java").description("The JVM language").build());
        Post post = postRepository.save(Post.builder().title("Title").content("Content").theme(theme).author(author).build());

        mockMvc.perform(get("/api/posts/" + post.getId()).header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Title"))
                .andExpect(jsonPath("$.themeTitle").value("Java"))
                .andExpect(jsonPath("$.authorUsername").value("johndoe"));
    }

    @Test
    void findById_returns404_whenPostDoesNotExist() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");

        mockMvc.perform(get("/api/posts/999999").header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"));
    }

    @Test
    void findFeed_returnsOnlyPostsFromSubscribedThemes_newestFirstByDefault() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");
        User author = userRepository.findByUsernameOrEmail("johndoe", "johndoe").orElseThrow();
        Theme subscribed = themeRepository.save(Theme.builder().title("Java").description("The JVM language").build());
        Theme other = themeRepository.save(Theme.builder().title("Python").description("The scripting language").build());
        mockMvc.perform(post("/api/subscriptions/" + subscribed.getId())
                .header("Authorization", "Bearer " + tokens.accessToken()));

        postRepository.save(Post.builder().title("Older post").content("Content").theme(subscribed).author(author)
                .createdAt(Instant.now().minusSeconds(120)).build());
        postRepository.save(Post.builder().title("Newer post").content("Content").theme(subscribed).author(author)
                .createdAt(Instant.now().minusSeconds(10)).build());
        postRepository.save(Post.builder().title("Unrelated post").content("Content").theme(other).author(author)
                .createdAt(Instant.now()).build());

        mockMvc.perform(get("/api/posts").header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Newer post"))
                .andExpect(jsonPath("$[1].title").value("Older post"));
    }

    @Test
    void findFeed_returnsOldestFirst_whenSortParamIsAsc() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");
        User author = userRepository.findByUsernameOrEmail("johndoe", "johndoe").orElseThrow();
        Theme theme = themeRepository.save(Theme.builder().title("Java").description("The JVM language").build());
        mockMvc.perform(post("/api/subscriptions/" + theme.getId())
                .header("Authorization", "Bearer " + tokens.accessToken()));

        postRepository.save(Post.builder().title("Older post").content("Content").theme(theme).author(author)
                .createdAt(Instant.now().minusSeconds(120)).build());
        postRepository.save(Post.builder().title("Newer post").content("Content").theme(theme).author(author)
                .createdAt(Instant.now().minusSeconds(10)).build());

        mockMvc.perform(get("/api/posts").param("sort", "asc")
                        .header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Older post"))
                .andExpect(jsonPath("$[1].title").value("Newer post"));
    }

    @Test
    void findFeed_returnsEmptyList_whenUserHasNoSubscriptions() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");

        mockMvc.perform(get("/api/posts").header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void findFeed_returns401_whenNoAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
    }
}
