package com.openclassrooms.mddapi.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.openclassrooms.mddapi.model.Subscription;

/**
 * Spring Data JPA repository for {@link Subscription}.
 */
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * @param userId  the subscriber
     * @param themeId the theme
     * @return {@code true} if the user is already subscribed to the theme
     */
    boolean existsByUserIdAndThemeId(Long userId, Long themeId);

    /**
     * @param userId  the subscriber
     * @param themeId the theme
     * @return the matching subscription, if any
     */
    Optional<Subscription> findByUserIdAndThemeId(Long userId, Long themeId);

    /**
     * @param userId the subscriber
     * @return the user's subscriptions, ordered by theme title
     */
    List<Subscription> findByUserIdOrderByTheme_TitleAsc(Long userId);

    /**
     * @param userId the subscriber
     * @return the ids of the themes the user is subscribed to
     */
    @Query("select s.theme.id from Subscription s where s.user.id = :userId")
    Set<Long> findThemeIdsByUserId(@Param("userId") Long userId);
}
