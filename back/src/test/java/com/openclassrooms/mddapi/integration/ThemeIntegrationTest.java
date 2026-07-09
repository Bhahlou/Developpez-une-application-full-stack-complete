package com.openclassrooms.mddapi.integration;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.openclassrooms.mddapi.dto.CreateThemeRequest;
import com.openclassrooms.mddapi.model.Theme;
import com.openclassrooms.mddapi.repository.SubscriptionRepository;
import com.openclassrooms.mddapi.repository.ThemeRepository;
import com.openclassrooms.mddapi.repository.UserRepository;

class ThemeIntegrationTest extends AbstractIntegrationTest {

    private final ThemeRepository themeRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Autowired
    ThemeIntegrationTest(MockMvc mockMvc, UserRepository userRepository, ThemeRepository themeRepository,
            SubscriptionRepository subscriptionRepository) {
        super(mockMvc, userRepository);
        this.themeRepository = themeRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @AfterEach
    void cleanUp() {
        subscriptionRepository.deleteAll();
        themeRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findAll_returnsEveryTheme_orderedByTitle_withSubscribedFlagForCurrentUser() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");
        Theme java = themeRepository.save(Theme.builder().title("Java").description("The JVM language").build());
        themeRepository.save(Theme.builder().title("Angular").description("The frontend framework").build());
        subscribe(tokens, java);

        mockMvc.perform(get("/api/themes").header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Angular"))
                .andExpect(jsonPath("$[0].subscribed").value(false))
                .andExpect(jsonPath("$[1].title").value("Java"))
                .andExpect(jsonPath("$[1].subscribed").value(true));
    }

    @Test
    void findAll_returns401_whenNoAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/themes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
    }

    @Test
    void create_persistsTheme_andReturns201() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");
        CreateThemeRequest request = new CreateThemeRequest("Web3", "Blockchain and decentralized apps");

        mockMvc.perform(post("/api/themes")
                        .header("Authorization", "Bearer " + tokens.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Web3"))
                .andExpect(jsonPath("$.subscribed").value(false));

        assertThat(themeRepository.existsByTitle("Web3")).isTrue();
    }

    @Test
    void create_returns409_whenTitleAlreadyTaken() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");
        themeRepository.save(Theme.builder().title("Java").description("The JVM language").build());

        CreateThemeRequest request = new CreateThemeRequest("Java", "Another description");
        mockMvc.perform(post("/api/themes")
                        .header("Authorization", "Bearer " + tokens.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("THEME_TITLE_TAKEN"));
    }

    @Test
    void create_returns401_whenNoAuthorizationHeader() throws Exception {
        CreateThemeRequest request = new CreateThemeRequest("Web3", "Blockchain and decentralized apps");

        mockMvc.perform(post("/api/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
    }

    private void subscribe(AuthResponse tokens, Theme theme) throws Exception {
        mockMvc.perform(post("/api/subscriptions/" + theme.getId())
                .header("Authorization", "Bearer " + tokens.accessToken()));
    }
}
