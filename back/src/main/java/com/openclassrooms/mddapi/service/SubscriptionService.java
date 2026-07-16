package com.openclassrooms.mddapi.service;

import java.util.List;

import com.openclassrooms.mddapi.dto.ThemeResponse;
import com.openclassrooms.mddapi.exception.SubscriptionAlreadyExistsException;
import com.openclassrooms.mddapi.exception.SubscriptionNotFoundException;
import com.openclassrooms.mddapi.exception.ThemeNotFoundException;

/**
 * Theme subscriptions of a user.
 */
public interface SubscriptionService {

    /**
     * @param userId the user whose subscriptions to list
     * @return the subscribed themes, ordered by title
     */
    List<ThemeResponse> findMySubscriptions(Long userId);

    /**
     * Subscribes a user to a theme.
     *
     * @param userId  the subscribing user
     * @param themeId the theme to subscribe to
     * @throws SubscriptionAlreadyExistsException if the user is already subscribed to this theme
     * @throws ThemeNotFoundException if no theme matches {@code themeId}
     */
    void subscribe(Long userId, Long themeId);

    /**
     * Unsubscribes a user from a theme.
     *
     * @param userId  the unsubscribing user
     * @param themeId the theme to unsubscribe from
     * @throws SubscriptionNotFoundException if the user isn't subscribed to this theme
     */
    void unsubscribe(Long userId, Long themeId);
}
