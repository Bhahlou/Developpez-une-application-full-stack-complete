package com.openclassrooms.mddapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateThemeRequest(

        @NotBlank @Size(max = 100) String title,

        @NotBlank @Size(max = 1000) String description) {
}
