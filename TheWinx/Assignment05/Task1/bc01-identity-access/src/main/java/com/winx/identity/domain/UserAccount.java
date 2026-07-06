package com.winx.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Aggregate root: a customer/rider account in the Identity & Access
 * bounded context.
 */
@Entity
@Table(name = "user_accounts")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private PersonalInfo personalInfo;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status;

    /** Required by Hibernate. */
    protected UserAccount() {
    }

    public UserAccount(PersonalInfo personalInfo, String email, String passwordHash) {
        this.personalInfo = personalInfo;
        this.email = email;
        this.passwordHash = passwordHash;
        this.registeredAt = LocalDateTime.now();
        this.status = AccountStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public PersonalInfo getPersonalInfo() {
        return personalInfo;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void deactivate() {
        this.status = AccountStatus.DEACTIVATED;
    }
}
