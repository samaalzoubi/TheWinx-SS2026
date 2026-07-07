package com.winx.identity.domain.service;

import com.winx.identity.application.InvalidCredentialsException;
import com.winx.identity.domain.AuthToken;
import com.winx.identity.domain.PrincipalRef;
import com.winx.identity.domain.ProviderAccount;
import com.winx.identity.domain.UserAccount;
import com.winx.identity.repository.ProviderAccountRepository;
import com.winx.identity.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// we kept tokens in a plain in-memory map since real JWT/session infra was out of scope for this lab
@Service
public class AuthenticationService {

    private static final long TOKEN_TTL_HOURS = 2;

    private final UserAccountRepository userAccountRepository;
    private final ProviderAccountRepository providerAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, StoredToken> tokenStore = new ConcurrentHashMap<>();

    public AuthenticationService(UserAccountRepository userAccountRepository,
                                  ProviderAccountRepository providerAccountRepository,
                                  PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.providerAccountRepository = providerAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthenticatedUser authenticateUser(String email, String rawPassword) {
        UserAccount account = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(rawPassword, account.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        AuthToken token = issueToken(account.getId(), PrincipalRef.USER);
        return new AuthenticatedUser(token, account);
    }

    public AuthenticatedProvider authenticateProvider(String email, String rawPassword) {
        ProviderAccount account = providerAccountRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(rawPassword, account.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        AuthToken token = issueToken(account.getId(), PrincipalRef.PROVIDER);
        return new AuthenticatedProvider(token, account);
    }

    // we return empty instead of throwing here since other services call this as a simple check
    public Optional<PrincipalRef> validateToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        StoredToken stored = tokenStore.get(token);
        if (stored == null) {
            return Optional.empty();
        }
        if (stored.authToken.isExpired()) {
            tokenStore.remove(token);
            return Optional.empty();
        }
        return Optional.of(stored.principalRef);
    }

    private AuthToken issueToken(Long principalId, String principalType) {
        String value = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(TOKEN_TTL_HOURS, ChronoUnit.HOURS);
        AuthToken token = new AuthToken(value, expiresAt);
        tokenStore.put(value, new StoredToken(token, new PrincipalRef(principalId, principalType)));
        return token;
    }

    private record StoredToken(AuthToken authToken, PrincipalRef principalRef) {
    }

    public record AuthenticatedUser(AuthToken token, UserAccount account) {
    }

    public record AuthenticatedProvider(AuthToken token, ProviderAccount account) {
    }
}
