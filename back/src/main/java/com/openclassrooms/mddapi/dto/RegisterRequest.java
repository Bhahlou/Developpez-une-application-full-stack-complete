package com.openclassrooms.mddapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Registration form for creating a new account.
 *
 * @param username 3-20 characters, letters/digits/underscore only
 * @param email    a valid email address
 * @param password up to 72 characters, at least 8, with a digit, a lowercase letter,
 *                 an uppercase letter and a special character
 */
public record RegisterRequest(

        @NotBlank @Size(min = 3, max = 20) @Pattern(regexp = "^\\w+$", message = "username must only contain letters, digits and underscores") String username,

        @NotBlank @Email String email,

        @NotBlank @Size(max = 72) @Pattern(
                regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,}$",
                message = "password must be at least 8 characters long and contain at least one digit, one lowercase letter, one uppercase letter and one special character") String password) {
}
