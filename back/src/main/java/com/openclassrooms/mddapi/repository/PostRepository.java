package com.openclassrooms.mddapi.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.openclassrooms.mddapi.model.Post;

/**
 * Spring Data JPA repository for {@link Post}.
 */
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * @param themeIds the themes to include in the feed
     * @param sort     the sort order to apply (typically by creation date)
     * @return the posts belonging to any of the given themes
     */
    List<Post> findByTheme_IdIn(Collection<Long> themeIds, Sort sort);
}
