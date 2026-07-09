package com.openclassrooms.mddapi.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.openclassrooms.mddapi.model.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    boolean existsByUserIdAndThemeId(Long userId, Long themeId);

    Optional<Subscription> findByUserIdAndThemeId(Long userId, Long themeId);

    List<Subscription> findByUserIdOrderByTheme_TitleAsc(Long userId);

    @Query("select s.theme.id from Subscription s where s.user.id = :userId")
    Set<Long> findThemeIdsByUserId(@Param("userId") Long userId);
}
