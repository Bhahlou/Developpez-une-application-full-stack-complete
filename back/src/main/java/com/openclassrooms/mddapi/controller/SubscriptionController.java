package com.openclassrooms.mddapi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.mddapi.dto.ThemeResponse;
import com.openclassrooms.mddapi.service.SubscriptionService;
import com.openclassrooms.mddapi.service.UserPrincipal;

import lombok.RequiredArgsConstructor;

/**
 * Theme subscriptions of the current user.
 */
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * Lists the themes the current user is subscribed to.
     *
     * @param principal the authenticated user, resolved from the JWT
     * @return 200 with the subscribed themes
     */
    @GetMapping
    public ResponseEntity<List<ThemeResponse>> findMySubscriptions(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(subscriptionService.findMySubscriptions(principal.getUser().getId()));
    }

    /**
     * Subscribes the current user to a theme.
     *
     * @param principal the authenticated user, resolved from the JWT
     * @param themeId   the theme to subscribe to
     * @return 201 Created
     */
    @PostMapping("/{themeId}")
    public ResponseEntity<Void> subscribe(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long themeId) {
        subscriptionService.subscribe(principal.getUser().getId(), themeId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Unsubscribes the current user from a theme.
     *
     * @param principal the authenticated user, resolved from the JWT
     * @param themeId   the theme to unsubscribe from
     * @return 204 No Content
     */
    @DeleteMapping("/{themeId}")
    public ResponseEntity<Void> unsubscribe(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long themeId) {
        subscriptionService.unsubscribe(principal.getUser().getId(), themeId);
        return ResponseEntity.noContent().build();
    }
}
