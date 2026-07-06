package com.winx.identity.api;

import com.winx.identity.application.NotFoundException;
import com.winx.identity.domain.UserAccount;
import com.winx.identity.domain.service.AuthenticationService;
import com.winx.identity.domain.service.RegistrationService;
import com.winx.identity.repository.UserAccountRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final RegistrationService registrationService;
    private final AuthenticationService authenticationService;
    private final UserAccountRepository userAccountRepository;

    public UserController(RegistrationService registrationService,
                           AuthenticationService authenticationService,
                           UserAccountRepository userAccountRepository) {
        this.registrationService = registrationService;
        this.authenticationService = authenticationService;
        this.userAccountRepository = userAccountRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        UserAccount account = registrationService.registerUser(
                request.name(), request.email(), request.password(),
                request.phoneNumber(), request.dateOfBirth());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(account));
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthenticationService.AuthenticatedUser result =
                authenticationService.authenticateUser(request.email(), request.password());
        UserLoginResponse response = new UserLoginResponse(
                result.token().value(),
                result.token().expiresAt(),
                result.account().getId(),
                result.account().getPersonalInfo().getName(),
                result.account().getEmail());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        UserAccount account = userAccountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
        return ResponseEntity.ok(UserResponse.from(account));
    }
}
