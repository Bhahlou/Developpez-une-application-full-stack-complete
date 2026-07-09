package com.openclassrooms.mddapi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
import com.openclassrooms.mddapi.dto.CreateThemeRequest;
import com.openclassrooms.mddapi.dto.ThemeResponse;
import com.openclassrooms.mddapi.exception.GlobalExceptionHandler;
import com.openclassrooms.mddapi.exception.ThemeAlreadyExistsException;
import com.openclassrooms.mddapi.model.User;
import com.openclassrooms.mddapi.service.ThemeService;
import com.openclassrooms.mddapi.service.UserPrincipal;

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { ThemeController.class, GlobalExceptionHandler.class,
        ThemeControllerTest.AuthenticationPrincipalTestConfig.class })
class ThemeControllerTest {

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
    private ThemeService themeService;

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
    void findAll_returns200WithThemes() throws Exception {
        authenticateAs(1L);
        when(themeService.findAll(1L)).thenReturn(List.of(new ThemeResponse(1L, "Backend", "desc", true)));

        mockMvc.perform(get("/api/themes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Backend"))
                .andExpect(jsonPath("$[0].description").value("desc"))
                .andExpect(jsonPath("$[0].subscribed").value(true));
    }

    @Test
    void create_returns201WithTheme_whenRequestIsValid() throws Exception {
        authenticateAs(1L);
        CreateThemeRequest request = new CreateThemeRequest("Backend", "desc");
        when(themeService.create(any(CreateThemeRequest.class)))
                .thenReturn(new ThemeResponse(1L, "Backend", "desc", false));

        mockMvc.perform(post("/api/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Backend"));
    }

    @Test
    void create_returns400_whenTitleIsBlank() throws Exception {
        authenticateAs(1L);
        CreateThemeRequest request = new CreateThemeRequest("", "desc");

        mockMvc.perform(post("/api/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void create_returns409_whenTitleAlreadyExists() throws Exception {
        authenticateAs(1L);
        CreateThemeRequest request = new CreateThemeRequest("Backend", "desc");
        when(themeService.create(any(CreateThemeRequest.class)))
                .thenThrow(new ThemeAlreadyExistsException("THEME_TITLE_TAKEN", "Theme title already in use"));

        mockMvc.perform(post("/api/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("THEME_TITLE_TAKEN"));
    }
}
