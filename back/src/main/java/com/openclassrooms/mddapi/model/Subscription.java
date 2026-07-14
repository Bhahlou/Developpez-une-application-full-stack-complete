package com.openclassrooms.mddapi.model;

import java.io.Serializable;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Join entity linking a {@link User} to a {@link Theme} they follow.
 * <p>
 * The {@code user_id}/{@code theme_id} pair is unique: a user can only
 * subscribe once to the same theme.
 */
@Entity
@Table(name = "subscriptions", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "theme_id" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The subscribing user. */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** The followed theme. */
    @ManyToOne
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
