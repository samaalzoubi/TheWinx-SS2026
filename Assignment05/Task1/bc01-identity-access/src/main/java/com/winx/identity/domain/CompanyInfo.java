package com.winx.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

// we keep this immutable by convention - no setters exposed after construction
@Embeddable
public class CompanyInfo {

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "contact_name")
    private String contactName;

    // required by Hibernate
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
