package com.openclassrooms.mddapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload for creating a new theme.
 *
 * @param title       the theme title; required, unique, up to 100 characters
 * @param description the theme description; required, up to 1000 characters
 */
public record CreateThemeRequest(

        @NotBlank @Size(max = 100) String title,

        @NotBlank @Size(max = 1000) String description) {
}
