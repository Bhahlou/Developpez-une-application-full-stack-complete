package com.openclassrooms.mddapi.model;

import java.io.Serializable;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A registered MDD user.
 * <p>
 * Holds the current refresh token (if any) alongside its expiry, so refresh
 * tokens can be rotated and invalidated without a separate table.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique, 3-20 characters, letters/digits/underscore only. */
    @Column(nullable = false, unique = true)
    private String username;

    /** Unique email address. */
    @Column(nullable = false, unique = true)
    private String email;

    /** BCrypt hash of the account password; never stored or returned in clear text. */
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    /** The current refresh token, or {@code null} if none is active. */
    @Column(unique = true)
    private String refreshToken;

    /** Expiry of {@link #refreshToken}, or {@code null} if none is active. */
    private Instant refreshTokenExpiry;
}
