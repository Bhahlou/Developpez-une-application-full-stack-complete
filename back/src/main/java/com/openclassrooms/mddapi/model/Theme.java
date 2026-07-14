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
 * A programming topic (e.g. Java, Angular) that users can subscribe to and
 * publish articles under.
 */
@Entity
@Table(name = "themes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Theme implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The theme's unique display title. */
    @Column(nullable = false, unique = true)
    private String title;

    /** The theme description, up to 1000 characters. */
    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
