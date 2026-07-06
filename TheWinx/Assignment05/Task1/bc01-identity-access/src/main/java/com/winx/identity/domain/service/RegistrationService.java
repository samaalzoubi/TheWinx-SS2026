package com.winx.identity.domain.service;

import com.winx.identity.application.EmailAlreadyRegisteredException;
import com.winx.identity.domain.CompanyInfo;
import com.winx.identity.domain.EmailValidator;
import com.winx.identity.domain.PersonalInfo;
import com.winx.identity.domain.ProviderAccount;
import com.winx.identity.domain.UserAccount;
import com.winx.identity.repository.ProviderAccountRepository;
import com.winx.identity.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Domain service responsible for enforcing registration invariants
 * (email format + uniqueness) for both Users and Providers, and for
 * persisting the resulting aggregates.
 */
@Service
public class RegistrationService {

    private final UserAccountRepository userAccountRepository;
    private final ProviderAccountRepository providerAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserAccountRepository userAccountRepository,
                                ProviderAccountRepository providerAccountRepository,
                                PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.providerAccountRepository = providerAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserAccount registerUser(String name, String email, String rawPassword,
                                     String phoneNumber, LocalDate dateOfBirth) {
        validateEmail(email);
        if (userAccountRepository.existsByEmail(email) || providerAccountRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }
        PersonalInfo personalInfo = new PersonalInfo(name, dateOfBirth, phoneNumber);
        UserAccount account = new UserAccount(personalInfo, email, passwordEncoder.encode(rawPassword));
        return userAccountRepository.save(account);
    }

    public ProviderAccount registerProvider(String companyName, String contactName, String email,
                                             String rawPassword, String phoneNumber) {
        validateEmail(email);
        if (providerAccountRepository.existsByEmail(email) || userAccountRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }
        CompanyInfo companyInfo = new CompanyInfo(companyName, contactName);
        ProviderAccount account = new ProviderAccount(companyInfo, email, passwordEncoder.encode(rawPassword), phoneNumber);
        return providerAccountRepository.save(account);
    }

    private void validateEmail(String email) {
        if (!EmailValidator.isValid(email)) {
            throw new IllegalArgumentException("Invalid email address: " + email);
        }
    }
}
