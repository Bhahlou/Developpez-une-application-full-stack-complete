package com.openclassrooms.mddapi.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import com.openclassrooms.mddapi.dto.AuthResponse;
import com.openclassrooms.mddapi.model.Theme;
import com.openclassrooms.mddapi.repository.SubscriptionRepository;
import com.openclassrooms.mddapi.repository.ThemeRepository;
import com.openclassrooms.mddapi.repository.UserRepository;

class SubscriptionIntegrationTest extends AbstractIntegrationTest {

    private final ThemeRepository themeRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Autowired
    SubscriptionIntegrationTest(MockMvc mockMvc, UserRepository userRepository, ThemeRepository themeRepository,
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
    void subscribe_persistsSubscription_andReturns201() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");
        Theme theme = themeRepository.save(Theme.builder().title("Java").description("The JVM language").build());

        mockMvc.perform(post("/api/subscriptions/" + theme.getId())
                        .header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isCreated());

        Long userId = userRepository.findByUsernameOrEmail("johndoe", "johndoe").orElseThrow().getId();
        assertThat(subscriptionRepository.existsByUserIdAndThemeId(userId, theme.getId())).isTrue();
    }

    @Test
    void subscribe_returns409_whenAlreadySubscribed() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");
        Theme theme = themeRepository.save(Theme.builder().title("Java").description("The JVM language").build());
        mockMvc.perform(post("/api/subscriptions/" + theme.getId())
                .header("Authorization", "Bearer " + tokens.accessToken()));

        mockMvc.perform(post("/api/subscriptions/" + theme.getId())
                        .header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SUBSCRIPTION_ALREADY_EXISTS"));
    }

    @Test
    void subscribe_returns404_whenThemeDoesNotExist() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");

        mockMvc.perform(post("/api/subscriptions/999999")
                        .header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("THEME_NOT_FOUND"));
    }

    @Test
    void findMySubscriptions_returnsOnlyThemesTheCurrentUserIsSubscribedTo() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");
        Theme java = themeRepository.save(Theme.builder().title("Java").description("The JVM language").build());
        themeRepository.save(Theme.builder().title("Angular").description("The frontend framework").build());
        mockMvc.perform(post("/api/subscriptions/" + java.getId())
                .header("Authorization", "Bearer " + tokens.accessToken()));

        mockMvc.perform(get("/api/subscriptions").header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Java"));
    }

    @Test
    void unsubscribe_removesSubscription_andReturns204() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");
        Theme theme = themeRepository.save(Theme.builder().title("Java").description("The JVM language").build());
        mockMvc.perform(post("/api/subscriptions/" + theme.getId())
                .header("Authorization", "Bearer " + tokens.accessToken()));

        mockMvc.perform(delete("/api/subscriptions/" + theme.getId())
                        .header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isNoContent());

        Long userId = userRepository.findByUsernameOrEmail("johndoe", "johndoe").orElseThrow().getId();
        assertThat(subscriptionRepository.existsByUserIdAndThemeId(userId, theme.getId())).isFalse();
    }

    @Test
    void unsubscribe_returns404_whenNotSubscribed() throws Exception {
        AuthResponse tokens = registerUser("johndoe", "john@doe.com", "Passw0rd!");
        Theme theme = themeRepository.save(Theme.builder().title("Java").description("The JVM language").build());

        mockMvc.perform(delete("/api/subscriptions/" + theme.getId())
                        .header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SUBSCRIPTION_NOT_FOUND"));
    }

    @Test
    void findMySubscriptions_returns401_whenNoAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/subscriptions"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"));
    }
}
