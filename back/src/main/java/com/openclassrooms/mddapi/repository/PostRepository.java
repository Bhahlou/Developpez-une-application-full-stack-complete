package com.openclassrooms.mddapi.repository;

import java.util.Collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.openclassrooms.mddapi.model.Post;

/**
 * Spring Data JPA repository for {@link Post}.
 */
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * @param themeIds the themes to include in the feed
     * @param pageable the page to fetch, along with its sort order (typically by creation date)
     * @return the matching page of posts belonging to any of the given themes
     */
    Page<Post> findByTheme_IdIn(Collection<Long> themeIds, Pageable pageable);
}
