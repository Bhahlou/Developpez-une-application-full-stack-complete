package com.openclassrooms.mddapi.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.openclassrooms.mddapi.dto.ThemeResponse;
import com.openclassrooms.mddapi.exception.SubscriptionAlreadyExistsException;
import com.openclassrooms.mddapi.exception.SubscriptionNotFoundException;
import com.openclassrooms.mddapi.exception.ThemeNotFoundException;
import com.openclassrooms.mddapi.model.Subscription;
import com.openclassrooms.mddapi.model.Theme;
import com.openclassrooms.mddapi.repository.SubscriptionRepository;
import com.openclassrooms.mddapi.repository.ThemeRepository;
import com.openclassrooms.mddapi.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final ThemeRepository themeRepository;
    private final UserRepository userRepository;

    public List<ThemeResponse> findMySubscriptions(Long userId) {
        return subscriptionRepository.findByUserIdOrderByTheme_TitleAsc(userId).stream()
                .map(subscription -> toResponse(subscription.getTheme()))
                .toList();
    }

    public void subscribe(Long userId, Long themeId) {
        if (subscriptionRepository.existsByUserIdAndThemeId(userId, themeId)) {
            throw new SubscriptionAlreadyExistsException("SUBSCRIPTION_ALREADY_EXISTS",
                    "Already subscribed to this theme");
        }

        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new ThemeNotFoundException("THEME_NOT_FOUND", "Theme not found"));

        Subscription subscription = Subscription.builder()
                .user(userRepository.getReferenceById(userId))
                .theme(theme)
                .build();
        subscriptionRepository.save(subscription);
    }

    public void unsubscribe(Long userId, Long themeId) {
        Subscription subscription = subscriptionRepository.findByUserIdAndThemeId(userId, themeId)
                .orElseThrow(() -> new SubscriptionNotFoundException("SUBSCRIPTION_NOT_FOUND", "Subscription not found"));

        subscriptionRepository.delete(subscription);
    }

    private static ThemeResponse toResponse(Theme theme) {
        return new ThemeResponse(theme.getId(), theme.getTitle(), theme.getDescription(), true);
    }
}
