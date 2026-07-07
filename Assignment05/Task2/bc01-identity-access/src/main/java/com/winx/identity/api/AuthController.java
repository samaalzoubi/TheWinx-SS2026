package com.winx.identity.api;

import com.winx.identity.domain.PrincipalRef;
import com.winx.identity.domain.service.AuthenticationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

// this gets called by other bounded contexts to check tokens, so we always return 200/valid:false instead of throwing on bad input
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping("/validate")
    public ValidateTokenResponse validate(@RequestParam(required = false) String token) {
        Optional<PrincipalRef> principal = authenticationService.validateToken(token);
        return principal
                .map(p -> new ValidateTokenResponse(true, p.id(), p.type()))
                .orElseGet(ValidateTokenResponse::invalid);
    }
}
