package com.winx.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;

/**
 * Value Object holding the personal details of a {@link UserAccount}.
 * Immutable by convention: no setters are exposed after construction.
 */
@Embeddable
public class PersonalInfo {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "phone_number")
    private String phoneNumber;

    /** Required by Hibernate. */
    protected PersonalInfo() {
    }

    public PersonalInfo(String name, LocalDate dateOfBirth, String phoneNumber) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
