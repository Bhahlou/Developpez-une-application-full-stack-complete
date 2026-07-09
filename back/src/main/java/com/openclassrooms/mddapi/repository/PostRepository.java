package com.openclassrooms.mddapi.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.openclassrooms.mddapi.model.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByTheme_IdIn(Collection<Long> themeIds, Sort sort);
}
