package com.winx.identity.api;

import com.winx.identity.application.NotFoundException;
import com.winx.identity.domain.ProviderAccount;
import com.winx.identity.domain.service.AuthenticationService;
import com.winx.identity.domain.service.RegistrationService;
import com.winx.identity.repository.ProviderAccountRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/providers")
public class ProviderController {

    private final RegistrationService registrationService;
    private final AuthenticationService authenticationService;
    private final ProviderAccountRepository providerAccountRepository;

    public ProviderController(RegistrationService registrationService,
                               AuthenticationService authenticationService,
                               ProviderAccountRepository providerAccountRepository) {
        this.registrationService = registrationService;
        this.authenticationService = authenticationService;
        this.providerAccountRepository = providerAccountRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<ProviderResponse> register(@Valid @RequestBody ProviderRegisterRequest request) {
        ProviderAccount account = registrationService.registerProvider(
                request.companyName(), request.contactName(), request.email(),
                request.password(), request.phoneNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(ProviderResponse.from(account));
    }

    @PostMapping("/login")
    public ResponseEntity<ProviderLoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthenticationService.AuthenticatedProvider result =
                authenticationService.authenticateProvider(request.email(), request.password());
        ProviderLoginResponse response = new ProviderLoginResponse(
                result.token().value(),
                result.token().expiresAt(),
                result.account().getId(),
                result.account().getCompanyInfo().getCompanyName(),
                result.account().getEmail());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProviderResponse> getById(@PathVariable Long id) {
        ProviderAccount account = providerAccountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Provider not found: " + id));
        return ResponseEntity.ok(ProviderResponse.from(account));
    }
}
