package com.openclassrooms.mddapi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.mddapi.dto.CreateThemeRequest;
import com.openclassrooms.mddapi.dto.ThemeResponse;
import com.openclassrooms.mddapi.exception.GlobalExceptionHandler;
import com.openclassrooms.mddapi.exception.ThemeAlreadyExistsException;
import com.openclassrooms.mddapi.service.ThemeService;

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { ThemeController.class, GlobalExceptionHandler.class })
class ThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ThemeService themeService;

    @Test
    void findAll_returns200WithThemes() throws Exception {
        when(themeService.findAll()).thenReturn(List.of(new ThemeResponse(1L, "Backend", "desc")));

        mockMvc.perform(get("/api/themes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Backend"))
                .andExpect(jsonPath("$[0].description").value("desc"));
    }

    @Test
    void create_returns201WithTheme_whenRequestIsValid() throws Exception {
        CreateThemeRequest request = new CreateThemeRequest("Backend", "desc");
        when(themeService.create(any(CreateThemeRequest.class)))
                .thenReturn(new ThemeResponse(1L, "Backend", "desc"));

        mockMvc.perform(post("/api/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Backend"));
    }

    @Test
    void create_returns400_whenTitleIsBlank() throws Exception {
        CreateThemeRequest request = new CreateThemeRequest("", "desc");

        mockMvc.perform(post("/api/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void create_returns409_whenTitleAlreadyExists() throws Exception {
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
