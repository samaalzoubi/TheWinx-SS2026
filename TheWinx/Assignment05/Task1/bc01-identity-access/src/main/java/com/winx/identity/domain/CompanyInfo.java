package com.winx.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Value Object holding the company details of a {@link ProviderAccount}.
 * Immutable by convention: no setters are exposed after construction.
 */
@Embeddable
public class CompanyInfo {

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "contact_name")
    private String contactName;

    /** Required by Hibernate. */
    protected CompanyInfo() {
    }

    public CompanyInfo(String companyName, String contactName) {
        this.companyName = companyName;
        this.contactName = contactName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getContactName() {
        return contactName;
    }
}
