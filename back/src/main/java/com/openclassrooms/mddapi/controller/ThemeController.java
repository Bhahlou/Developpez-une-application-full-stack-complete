package com.openclassrooms.mddapi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.mddapi.dto.CreateThemeRequest;
import com.openclassrooms.mddapi.dto.ThemeResponse;
import com.openclassrooms.mddapi.service.ThemeService;
import com.openclassrooms.mddapi.service.UserPrincipal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Theme catalog: listing and creation.
 * <p>
 * Theme creation is a deliberate addition beyond the original MVP scope — it
 * lets the app be exercised end-to-end (subscribe, feed, articles) without
 * requiring a manual database seed.
 */
@RestController
@RequestMapping("/api/themes")
@RequiredArgsConstructor
public class ThemeController {

    private final ThemeService themeService;

    /**
     * Lists every theme, flagging which ones the current user is subscribed to.
     *
     * @param principal the authenticated user, resolved from the JWT
     * @return 200 with all themes and their subscription status
     */
    @GetMapping
    public ResponseEntity<List<ThemeResponse>> findAll(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(themeService.findAll(principal.getUser().getId()));
    }

    /**
     * Creates a new theme.
     *
     * @param request the theme's title and description
     * @return 201 with the created theme
     */
    @PostMapping
    public ResponseEntity<ThemeResponse> create(@Valid @RequestBody CreateThemeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(themeService.create(request));
    }
}
