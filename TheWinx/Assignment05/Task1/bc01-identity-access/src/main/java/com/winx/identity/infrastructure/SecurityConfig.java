package com.winx.identity.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Provides the PasswordEncoder bean. We depend only on
 * spring-security-crypto (no filter chain / spring-security-web) since
 * this lab only needs password hashing plus a simple opaque token,
 * not full request-level security.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
