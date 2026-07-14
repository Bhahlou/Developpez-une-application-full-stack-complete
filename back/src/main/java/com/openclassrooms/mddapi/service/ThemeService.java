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

/**
 * Theme catalog: listing (with subscription status) and creation.
 * <p>
 * Theme creation is a deliberate addition beyond the original MVP scope,
 * validated with the training mentor, so the app can be exercised end-to-end
 * without a manual database seed.
 */
@Service
@RequiredArgsConstructor
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * @param userId the current user, used to flag their subscribed themes
     * @return every theme, ordered by title, flagged with the user's subscription status
     */
    public List<ThemeResponse> findAll(Long userId) {
        Set<Long> subscribedThemeIds = subscriptionRepository.findThemeIdsByUserId(userId);
        return themeRepository.findAllByOrderByTitleAsc().stream()
                .map(theme -> toResponse(theme, subscribedThemeIds.contains(theme.getId())))
                .toList();
    }

    /**
     * Creates a new theme.
     *
     * @param request the theme's title and description
     * @return the created theme, not yet subscribed by anyone
     * @throws ThemeAlreadyExistsException if a theme with this title already exists
     */
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

    /**
     * @param theme      the entity to convert
     * @param subscribed whether the current user is subscribed to this theme
     * @return the corresponding response DTO
     */
    private static ThemeResponse toResponse(Theme theme, boolean subscribed) {
        return new ThemeResponse(theme.getId(), theme.getTitle(), theme.getDescription(), subscribed);
    }
}
