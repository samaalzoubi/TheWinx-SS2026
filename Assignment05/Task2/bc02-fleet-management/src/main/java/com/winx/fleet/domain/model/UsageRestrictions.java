package com.winx.fleet.domain.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class UsageRestrictions {

    private Integer maxDurationMinutes;
    private Integer maxKilometers;
    private Integer minAge;
    private Integer maxPersons;

    public UsageRestrictions() {
    }

    public UsageRestrictions(Integer maxDurationMinutes, Integer maxKilometers, Integer minAge, Integer maxPersons) {
        this.maxDurationMinutes = maxDurationMinutes;
        this.maxKilometers = maxKilometers;
        this.minAge = minAge;
        this.maxPersons = maxPersons;
    }

    public Integer getMaxDurationMinutes() {
        return maxDurationMinutes;
    }

    public void setMaxDurationMinutes(Integer maxDurationMinutes) {
        this.maxDurationMinutes = maxDurationMinutes;
    }

    public Integer getMaxKilometers() {
        return maxKilometers;
    }

    public void setMaxKilometers(Integer maxKilometers) {
        this.maxKilometers = maxKilometers;
    }

    public Integer getMinAge() {
        return minAge;
    }

    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }

    public Integer getMaxPersons() {
        return maxPersons;
    }

    public void setMaxPersons(Integer maxPersons) {
        this.maxPersons = maxPersons;
    }
}
