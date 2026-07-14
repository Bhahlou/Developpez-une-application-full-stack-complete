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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An article published under a {@link Theme}, independent of the author's
 * current subscription to that theme.
 */
@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The article title. */
    @Column(nullable = false)
    private String title;

    /** The article body, up to 5000 characters. */
    @Column(nullable = false, length = 5000)
    private String content;

    /** The theme this article is categorized under. */
    @ManyToOne
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    /** The user who wrote this article. */
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
