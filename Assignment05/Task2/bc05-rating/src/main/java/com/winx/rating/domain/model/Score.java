package com.winx.rating.domain.model;

import com.winx.rating.domain.exception.InvalidScoreException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

// validated on construction so an out-of-range score can never exist as an object, per the tactical design
@Embeddable
public class Score {

    private static final int MIN = 1;
    private static final int MAX = 5;

    @Column(nullable = false)
    private Integer value;

    protected Score() {
    }

    private Score(Integer value) {
        this.value = value;
    }

    public static Score of(String fieldName, Integer value) {
        if (value == null || value < MIN || value > MAX) {
            throw new InvalidScoreException(fieldName + " must be between " + MIN + " and " + MAX + " (got " + value + ")");
        }
        return new Score(value);
    }

    public Integer getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Score score)) return false;
        return Objects.equals(value, score.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
