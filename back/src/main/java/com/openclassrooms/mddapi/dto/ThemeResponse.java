package com.openclassrooms.mddapi.dto;

/**
 * A theme as returned to the client.
 *
 * @param id          the theme id
 * @param title       the theme title
 * @param description the theme description
 * @param subscribed  whether the current user is subscribed to this theme
 */
public record ThemeResponse(Long id, String title, String description, boolean subscribed) {
}
