package com.openclassrooms.mddapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.openclassrooms.mddapi.dto.CreateThemeRequest;
import com.openclassrooms.mddapi.dto.ThemeResponse;
import com.openclassrooms.mddapi.exception.ThemeAlreadyExistsException;
import com.openclassrooms.mddapi.model.Theme;
import com.openclassrooms.mddapi.repository.ThemeRepository;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock
    private ThemeRepository themeRepository;

    private ThemeService themeService;

    @BeforeEach
    void setUp() {
        themeService = new ThemeService(themeRepository);
    }

    @Test
    void findAll_returnsThemesOrderedByTitle() {
        Theme theme = Theme.builder().id(1L).title("Backend").description("desc").build();
        when(themeRepository.findAllByOrderByTitleAsc()).thenReturn(List.of(theme));

        List<ThemeResponse> result = themeService.findAll();

        assertThat(result).containsExactly(new ThemeResponse(1L, "Backend", "desc"));
    }

    @Test
    void create_savesAndReturnsTheme_whenTitleIsAvailable() {
        CreateThemeRequest request = new CreateThemeRequest("Backend", "desc");
        when(themeRepository.existsByTitle("Backend")).thenReturn(false);

        ThemeResponse response = themeService.create(request);

        assertThat(response.title()).isEqualTo("Backend");
        assertThat(response.description()).isEqualTo("desc");
        verify(themeRepository).save(any(Theme.class));
    }

    @Test
    void create_throws_whenTitleAlreadyExists() {
        CreateThemeRequest request = new CreateThemeRequest("Backend", "desc");
        when(themeRepository.existsByTitle("Backend")).thenReturn(true);

        assertThatThrownBy(() -> themeService.create(request))
                .isInstanceOf(ThemeAlreadyExistsException.class)
                .hasMessage("Theme title already in use");
    }
}
