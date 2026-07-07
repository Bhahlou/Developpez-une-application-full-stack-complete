package com.openclassrooms.mddapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.openclassrooms.mddapi.dto.ThemeResponse;
import com.openclassrooms.mddapi.exception.SubscriptionAlreadyExistsException;
import com.openclassrooms.mddapi.exception.SubscriptionNotFoundException;
import com.openclassrooms.mddapi.exception.ThemeNotFoundException;
import com.openclassrooms.mddapi.model.Subscription;
import com.openclassrooms.mddapi.model.Theme;
import com.openclassrooms.mddapi.model.User;
import com.openclassrooms.mddapi.repository.SubscriptionRepository;
import com.openclassrooms.mddapi.repository.ThemeRepository;
import com.openclassrooms.mddapi.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private UserRepository userRepository;

    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        subscriptionService = new SubscriptionService(subscriptionRepository, themeRepository, userRepository);
    }

    @Test
    void findMySubscriptions_returnsSubscribedThemesOrderedByTitle() {
        Theme theme = Theme.builder().id(1L).title("Backend").description("desc").build();
        Subscription subscription = Subscription.builder().id(10L).theme(theme).build();
        when(subscriptionRepository.findByUserIdOrderByTheme_TitleAsc(1L)).thenReturn(List.of(subscription));

        List<ThemeResponse> result = subscriptionService.findMySubscriptions(1L);

        assertThat(result).containsExactly(new ThemeResponse(1L, "Backend", "desc", true));
    }

    @Test
    void subscribe_savesSubscription_whenThemeExistsAndNotAlreadySubscribed() {
        Theme theme = Theme.builder().id(5L).title("Backend").description("desc").build();
        User user = User.builder().id(1L).build();
        when(subscriptionRepository.existsByUserIdAndThemeId(1L, 5L)).thenReturn(false);
        when(themeRepository.findById(5L)).thenReturn(Optional.of(theme));
        when(userRepository.getReferenceById(1L)).thenReturn(user);

        subscriptionService.subscribe(1L, 5L);

        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void subscribe_throws_whenAlreadySubscribed() {
        when(subscriptionRepository.existsByUserIdAndThemeId(1L, 5L)).thenReturn(true);

        assertThatThrownBy(() -> subscriptionService.subscribe(1L, 5L))
                .isInstanceOf(SubscriptionAlreadyExistsException.class)
                .hasMessage("Already subscribed to this theme");
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    void subscribe_throws_whenThemeDoesNotExist() {
        when(subscriptionRepository.existsByUserIdAndThemeId(1L, 5L)).thenReturn(false);
        when(themeRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.subscribe(1L, 5L))
                .isInstanceOf(ThemeNotFoundException.class)
                .hasMessage("Theme not found");
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    void unsubscribe_deletesSubscription_whenItExists() {
        Subscription subscription = Subscription.builder().id(10L).build();
        when(subscriptionRepository.findByUserIdAndThemeId(1L, 5L)).thenReturn(Optional.of(subscription));

        subscriptionService.unsubscribe(1L, 5L);

        verify(subscriptionRepository).delete(subscription);
    }

    @Test
    void unsubscribe_throws_whenSubscriptionDoesNotExist() {
        when(subscriptionRepository.findByUserIdAndThemeId(1L, 5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.unsubscribe(1L, 5L))
                .isInstanceOf(SubscriptionNotFoundException.class)
                .hasMessage("Subscription not found");
    }
}
