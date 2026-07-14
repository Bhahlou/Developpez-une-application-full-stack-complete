package com.openclassrooms.mddapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.openclassrooms.mddapi.model.Theme;

/**
 * Spring Data JPA repository for {@link Theme}.
 */
public interface ThemeRepository extends JpaRepository<Theme, Long> {

    /**
     * @return every theme, ordered alphabetically by title
     */
    List<Theme> findAllByOrderByTitleAsc();

    /**
     * @param title the theme title to check
     * @return {@code true} if a theme with this title already exists
     */
    boolean existsByTitle(String title);
}
