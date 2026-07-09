package com.openclassrooms.mddapi.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.openclassrooms.mddapi.dto.ThemeResponse;
import com.openclassrooms.mddapi.exception.GlobalExceptionHandler;
import com.openclassrooms.mddapi.exception.SubscriptionAlreadyExistsException;
import com.openclassrooms.mddapi.exception.SubscriptionNotFoundException;
import com.openclassrooms.mddapi.exception.ThemeNotFoundException;
import com.openclassrooms.mddapi.model.User;
import com.openclassrooms.mddapi.service.SubscriptionService;
import com.openclassrooms.mddapi.service.UserPrincipal;

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { SubscriptionController.class, GlobalExceptionHandler.class,
        SubscriptionControllerTest.AuthenticationPrincipalTestConfig.class })
class SubscriptionControllerTest {

    @TestConfiguration
    static class AuthenticationPrincipalTestConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new AuthenticationPrincipalArgumentResolver());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubscriptionService subscriptionService;

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
    void findMySubscriptions_returns200WithSubscribedThemes() throws Exception {
        authenticateAs(1L);
        when(subscriptionService.findMySubscriptions(1L))
                .thenReturn(List.of(new ThemeResponse(1L, "Backend", "desc", true)));

        mockMvc.perform(get("/api/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].subscribed").value(true));
    }

    @Test
    void subscribe_returns201_whenThemeExistsAndNotAlreadySubscribed() throws Exception {
        authenticateAs(1L);

        mockMvc.perform(post("/api/subscriptions/{themeId}", 5L))
                .andExpect(status().isCreated());

        verify(subscriptionService).subscribe(eq(1L), eq(5L));
    }

    @Test
    void subscribe_returns404_whenThemeDoesNotExist() throws Exception {
        authenticateAs(1L);
        doThrow(new ThemeNotFoundException("THEME_NOT_FOUND", "Theme not found"))
                .when(subscriptionService).subscribe(1L, 5L);

        mockMvc.perform(post("/api/subscriptions/{themeId}", 5L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("THEME_NOT_FOUND"));
    }

    @Test
    void subscribe_returns409_whenAlreadySubscribed() throws Exception {
        authenticateAs(1L);
        doThrow(new SubscriptionAlreadyExistsException("SUBSCRIPTION_ALREADY_EXISTS", "Already subscribed to this theme"))
                .when(subscriptionService).subscribe(1L, 5L);

        mockMvc.perform(post("/api/subscriptions/{themeId}", 5L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SUBSCRIPTION_ALREADY_EXISTS"));
    }

    @Test
    void unsubscribe_returns204_whenSubscriptionExists() throws Exception {
        authenticateAs(1L);

        mockMvc.perform(delete("/api/subscriptions/{themeId}", 5L))
                .andExpect(status().isNoContent());

        verify(subscriptionService).unsubscribe(eq(1L), eq(5L));
    }

    @Test
    void unsubscribe_returns404_whenSubscriptionDoesNotExist() throws Exception {
        authenticateAs(1L);
        doThrow(new SubscriptionNotFoundException("SUBSCRIPTION_NOT_FOUND", "Subscription not found"))
                .when(subscriptionService).unsubscribe(1L, 5L);

        mockMvc.perform(delete("/api/subscriptions/{themeId}", 5L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SUBSCRIPTION_NOT_FOUND"));
    }
}
