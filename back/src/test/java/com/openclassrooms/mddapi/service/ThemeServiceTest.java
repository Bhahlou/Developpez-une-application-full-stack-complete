package com.openclassrooms.mddapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.openclassrooms.mddapi.dto.CreateThemeRequest;
import com.openclassrooms.mddapi.dto.ThemeResponse;
import com.openclassrooms.mddapi.exception.ThemeAlreadyExistsException;
import com.openclassrooms.mddapi.model.Theme;
import com.openclassrooms.mddapi.repository.SubscriptionRepository;
import com.openclassrooms.mddapi.repository.ThemeRepository;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    private ThemeService themeService;

    @BeforeEach
    void setUp() {
        themeService = new ThemeService(themeRepository, subscriptionRepository);
    }

    @Test
    void findAll_returnsThemesOrderedByTitle_withSubscribedFlag() {
        Theme theme = Theme.builder().id(1L).title("Backend").description("desc").build();
        Theme otherTheme = Theme.builder().id(2L).title("Frontend").description("desc2").build();
        when(themeRepository.findAllByOrderByTitleAsc()).thenReturn(List.of(theme, otherTheme));
        when(subscriptionRepository.findThemeIdsByUserId(42L)).thenReturn(Set.of(1L));

        List<ThemeResponse> result = themeService.findAll(42L);

        assertThat(result).containsExactly(
                new ThemeResponse(1L, "Backend", "desc", true),
                new ThemeResponse(2L, "Frontend", "desc2", false));
    }

    @Test
    void create_savesAndReturnsTheme_whenTitleIsAvailable() {
        CreateThemeRequest request = new CreateThemeRequest("Backend", "desc");
        when(themeRepository.existsByTitle("Backend")).thenReturn(false);

        ThemeResponse response = themeService.create(request);

        assertThat(response.title()).isEqualTo("Backend");
        assertThat(response.description()).isEqualTo("desc");
        assertThat(response.subscribed()).isFalse();
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
