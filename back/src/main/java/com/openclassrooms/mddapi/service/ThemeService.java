package com.openclassrooms.mddapi.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.openclassrooms.mddapi.dto.CreateThemeRequest;
import com.openclassrooms.mddapi.dto.ThemeResponse;
import com.openclassrooms.mddapi.exception.ThemeAlreadyExistsException;
import com.openclassrooms.mddapi.model.Theme;
import com.openclassrooms.mddapi.repository.SubscriptionRepository;
import com.openclassrooms.mddapi.repository.ThemeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final SubscriptionRepository subscriptionRepository;

    public List<ThemeResponse> findAll(Long userId) {
        Set<Long> subscribedThemeIds = subscriptionRepository.findThemeIdsByUserId(userId);
        return themeRepository.findAllByOrderByTitleAsc().stream()
                .map(theme -> toResponse(theme, subscribedThemeIds.contains(theme.getId())))
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

        return toResponse(theme, false);
    }

    private static ThemeResponse toResponse(Theme theme, boolean subscribed) {
        return new ThemeResponse(theme.getId(), theme.getTitle(), theme.getDescription(), subscribed);
    }
}
