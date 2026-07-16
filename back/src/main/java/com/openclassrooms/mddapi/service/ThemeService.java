package com.openclassrooms.mddapi.service;

import java.util.List;

import com.openclassrooms.mddapi.dto.CreateThemeRequest;
import com.openclassrooms.mddapi.dto.ThemeResponse;
import com.openclassrooms.mddapi.exception.ThemeAlreadyExistsException;

/**
 * Theme catalog: listing (with subscription status) and creation.
 * <p>
 * Theme creation is a deliberate addition beyond the original MVP scope,
 * validated with the training mentor, so the app can be exercised end-to-end
 * without a manual database seed.
 */
public interface ThemeService {

    /**
     * @param userId the current user, used to flag their subscribed themes
     * @return every theme, ordered by title, flagged with the user's subscription status
     */
    List<ThemeResponse> findAll(Long userId);

    /**
     * Creates a new theme.
     *
     * @param request the theme's title and description
     * @return the created theme, not yet subscribed by anyone
     * @throws ThemeAlreadyExistsException if a theme with this title already exists
     */
    ThemeResponse create(CreateThemeRequest request);
}
