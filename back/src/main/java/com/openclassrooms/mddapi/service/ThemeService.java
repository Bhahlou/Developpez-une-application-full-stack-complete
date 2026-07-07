package com.openclassrooms.mddapi.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.openclassrooms.mddapi.dto.CreateThemeRequest;
import com.openclassrooms.mddapi.dto.ThemeResponse;
import com.openclassrooms.mddapi.exception.ThemeAlreadyExistsException;
import com.openclassrooms.mddapi.model.Theme;
import com.openclassrooms.mddapi.repository.ThemeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ThemeService {

    private final ThemeRepository themeRepository;

    public List<ThemeResponse> findAll() {
        return themeRepository.findAllByOrderByTitleAsc().stream()
                .map(ThemeService::toResponse)
                .toList();
    }

    public ThemeResponse create(CreateThemeRequest request) {
        if (themeRepository.existsByTitle(request.title())) {
            throw new ThemeAlreadyExistsException("THEME_TITLE_TAKEN", "Theme title already in use");
        }

        Theme theme = Theme.builder()
                .title(request.title())
                .description(request.description())
                .build();
        themeRepository.save(theme);

        return toResponse(theme);
    }

    private static ThemeResponse toResponse(Theme theme) {
        return new ThemeResponse(theme.getId(), theme.getTitle(), theme.getDescription());
    }
}
