package com.openclassrooms.mddapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.openclassrooms.mddapi.model.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    List<Theme> findAllByOrderByTitleAsc();

    boolean existsByTitle(String title);
}
