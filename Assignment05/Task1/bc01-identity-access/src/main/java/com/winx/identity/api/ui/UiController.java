package com.winx.identity.api.ui;

import com.winx.identity.api.UserResponse;
import com.winx.identity.api.ProviderResponse;
import com.winx.identity.application.EmailAlreadyRegisteredException;
import com.winx.identity.application.InvalidCredentialsException;
import com.winx.identity.application.NotFoundException;
import com.winx.identity.domain.PrincipalRef;
import com.winx.identity.domain.ProviderAccount;
import com.winx.identity.domain.UserAccount;
import com.winx.identity.domain.service.AuthenticationService;
import com.winx.identity.domain.service.RegistrationService;
import com.winx.identity.repository.ProviderAccountRepository;
import com.winx.identity.repository.UserAccountRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class UiController {

    private final RegistrationService registrationService;
    private final AuthenticationService authenticationService;
    private final UserAccountRepository userAccountRepository;
    private final ProviderAccountRepository providerAccountRepository;

    public UiController(RegistrationService registrationService,
                         AuthenticationService authenticationService,
                         UserAccountRepository userAccountRepository,
                         ProviderAccountRepository providerAccountRepository) {
        this.registrationService = registrationService;
        this.authenticationService = authenticationService;
        this.userAccountRepository = userAccountRepository;
        this.providerAccountRepository = providerAccountRepository;
    }

    @GetMapping({"/", "/ui"})
    public String home(Model model) {
        model.addAttribute("totalUsers", userAccountRepository.count());
        model.addAttribute("totalProviders", providerAccountRepository.count());
        return "home";
    }

    // ---- Directory (read-only browsing of existing accounts) ----

    @GetMapping("/directory")
    public String directory(Model model) {
        List<UserResponse> users = userAccountRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
        List<ProviderResponse> providers = providerAccountRepository.findAll().stream()
                .map(ProviderResponse::from)
                .toList();
        model.addAttribute("users", users);
        model.addAttribute("providers", providers);
        return "directory";
    }

    // ---- Token inspector (exercises the same validation logic as /api/auth/validate) ----

    @GetMapping("/token-inspector")
    public String tokenInspector(@RequestParam(required = false) String token, Model model) {
        model.addAttribute("token", token);
        if (token != null && !token.isBlank()) {
            model.addAttribute("checked", true);
            authenticationService.validateToken(token)
                    .ifPresentOrElse(
                            principal -> {
                                model.addAttribute("valid", true);
                                model.addAttribute("principalType", principal.type());
                                model.addAttribute("principalId", principal.id());
                            },
                            () -> model.addAttribute("valid", false));
        }
        return "token-inspector";
    }

    // ---- User registration ----

    @GetMapping("/ui/register/user")
    public String registerUserForm() {
        return "register-user";
    }

    @PostMapping("/ui/register/user")
    public String registerUser(@RequestParam String name,
                                @RequestParam String email,
                                @RequestParam String password,
                                @RequestParam(required = false) String phoneNumber,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
                                Model model) {
        try {
            UserAccount account = registrationService.registerUser(name, email, password, phoneNumber, dateOfBirth);
            model.addAttribute("user", UserResponse.from(account));
            return "register-user-success";
        } catch (EmailAlreadyRegisteredException | IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            model.addAttribute("phoneNumber", phoneNumber);
            model.addAttribute("dateOfBirth", dateOfBirth);
            return "register-user";
        }
    }

    // ---- Provider registration ----

    @GetMapping("/ui/register/provider")
    public String registerProviderForm() {
        return "register-provider";
    }

    @PostMapping("/ui/register/provider")
    public String registerProvider(@RequestParam String companyName,
                                    @RequestParam String contactName,
                                    @RequestParam String email,
                                    @RequestParam String password,
                                    @RequestParam(required = false) String phoneNumber,
                                    Model model) {
        try {
            ProviderAccount account = registrationService.registerProvider(companyName, contactName, email, password, phoneNumber);
            model.addAttribute("provider", ProviderResponse.from(account));
            return "register-provider-success";
        } catch (EmailAlreadyRegisteredException | IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("companyName", companyName);
            model.addAttribute("contactName", contactName);
            model.addAttribute("email", email);
            model.addAttribute("phoneNumber", phoneNumber);
            return "register-provider";
        }
    }

    // ---- Login ----

    @GetMapping("/ui/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/ui/login")
    public String login(@RequestParam String principalType,
                         @RequestParam String email,
                         @RequestParam String password,
                         Model model) {
        try {
            if (PrincipalRef.PROVIDER.equals(principalType)) {
                AuthenticationService.AuthenticatedProvider result = authenticationService.authenticateProvider(email, password);
                model.addAttribute("principalType", PrincipalRef.PROVIDER);
                model.addAttribute("principalId", result.account().getId());
                model.addAttribute("token", result.token().value());
                model.addAttribute("expiresAt", result.token().expiresAt());
            } else {
                AuthenticationService.AuthenticatedUser result = authenticationService.authenticateUser(email, password);
                model.addAttribute("principalType", PrincipalRef.USER);
                model.addAttribute("principalId", result.account().getId());
                model.addAttribute("token", result.token().value());
                model.addAttribute("expiresAt", result.token().expiresAt());
            }
            return "login-success";
        } catch (InvalidCredentialsException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("email", email);
            model.addAttribute("principalType", principalType);
            return "login";
        }
    }

    // ---- Profile views ----

    @GetMapping("/ui/users/{id}")
    public String userProfile(@PathVariable Long id, Model model) {
        try {
            UserAccount account = userAccountRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("User not found: " + id));
            model.addAttribute("user", UserResponse.from(account));
            return "user-profile";
        } catch (NotFoundException ex) {
            model.addAttribute("error", ex.getMessage());
            return "ui-error";
        }
    }

    @GetMapping("/ui/providers/{id}")
    public String providerProfile(@PathVariable Long id, Model model) {
        try {
            ProviderAccount account = providerAccountRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Provider not found: " + id));
            model.addAttribute("provider", ProviderResponse.from(account));
            return "provider-profile";
        } catch (NotFoundException ex) {
            model.addAttribute("error", ex.getMessage());
            return "ui-error";
        }
    }
}
