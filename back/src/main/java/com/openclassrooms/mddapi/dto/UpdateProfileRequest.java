package com.openclassrooms.mddapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Payload for editing the current user's profile.
 *
 * @param username        the new username; same rules as registration
 * @param email           the new email; same rules as registration
 * @param currentPassword the account's current password; required to confirm the change
 * @param newPassword     the new password, or blank to leave the password unchanged;
 *                        if provided, must meet the same complexity rules as registration
 */
public record UpdateProfileRequest(

        @NotBlank @Size(min = 3, max = 20) @Pattern(regexp = "^\\w+$", message = "username must only contain letters, digits and underscores") String username,

        @NotBlank @Email String email,

        @NotBlank String currentPassword,

        @Pattern(
                regexp = "^$|^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,}$",
                message = "password must be at least 8 characters long and contain at least one digit, one lowercase letter, one uppercase letter and one special character") String newPassword) {
}
