package com.winx.rating.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Aggregate root of the Rating bounded context. A Rating is feedback a user
 * gives for a Vehicle and its Provider after a completed ride (Booking).
 * It is immutable once created: there is intentionally no setter/update path.
 */
@Entity
@Table(name = "ratings")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Embedded
    private RatingTarget ratingTarget;

    @Embedded
    private Review review;

    protected Rating() {
        // JPA
    }

    private Rating(Long userId, RatingTarget ratingTarget, Review review, LocalDateTime createdAt) {
        this.userId = userId;
        this.ratingTarget = ratingTarget;
        this.review = review;
        this.createdAt = createdAt;
    }

    public static Rating create(Long userId, RatingTarget ratingTarget, Review review) {
        return new Rating(userId, ratingTarget, review, LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public RatingTarget getRatingTarget() {
        return ratingTarget;
    }

    public Review getReview() {
        return review;
    }
}
