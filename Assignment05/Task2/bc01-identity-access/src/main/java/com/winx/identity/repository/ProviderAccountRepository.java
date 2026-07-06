package com.winx.identity.repository;

import com.winx.identity.domain.ProviderAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProviderAccountRepository extends JpaRepository<ProviderAccount, Long> {

    Optional<ProviderAccount> findByEmail(String email);

    boolean existsByEmail(String email);
}
