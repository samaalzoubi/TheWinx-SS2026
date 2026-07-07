package com.winx.gateway.web;

import com.winx.gateway.client.IdentityClient;
import feign.FeignException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Both actors from the domain model (User, Provider) authenticate through
 * here. Session holds {@code role} ("USER" or "PROVIDER"), {@code principalId}
 * and {@code principalName} so the rest of the portal can branch on it.
 */
@Controller
public class AuthController {

    private final IdentityClient identityClient;

    public AuthController(IdentityClient identityClient) {
        this.identityClient = identityClient;
    }

    @GetMapping("/register")
    public String registerChoice() {
        return "register-choice";
    }

    @GetMapping("/register/user")
    public String registerUserForm() {
        return "register";
    }

    @PostMapping("/register/user")
    public String registerUser(@RequestParam String name, @RequestParam String email, @RequestParam String password,
                                @RequestParam String phoneNumber, @RequestParam String dateOfBirth, Model model) {
        try {
            identityClient.register(new IdentityClient.RegisterRequest(name, email, password, phoneNumber, dateOfBirth));
            return "redirect:/login?registered";
        } catch (FeignException.Conflict e) {
            model.addAttribute("error", "That email is already registered.");
            return "register";
        } catch (FeignException e) {
            model.addAttribute("error", "Registration failed: " + describeFeignError(e));
            return "register";
        }
    }

    @GetMapping("/register/provider")
    public String registerProviderForm() {
        return "register-provider";
    }

    @PostMapping("/register/provider")
    public String registerProvider(@RequestParam String companyName, @RequestParam String contactName,
                                    @RequestParam String email, @RequestParam String password,
                                    @RequestParam String phoneNumber, Model model) {
        try {
            identityClient.registerProvider(new IdentityClient.RegisterProviderRequest(companyName, contactName, email, password, phoneNumber));
            return "redirect:/login?registered";
        } catch (FeignException.Conflict e) {
            model.addAttribute("error", "That email is already registered.");
            return "register-provider";
        } catch (FeignException e) {
            model.addAttribute("error", "Registration failed: " + describeFeignError(e));
            return "register-provider";
        }
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String registered, Model model) {
        if (registered != null) {
            model.addAttribute("info", "Account created - you can log in now.");
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                         @RequestParam(defaultValue = "USER") String role,
                         HttpSession session, Model model) {
        try {
            if ("PROVIDER".equals(role)) {
                IdentityClient.ProviderLoginResponse response =
                        identityClient.loginProvider(new IdentityClient.LoginRequest(email, password));
                session.setAttribute("role", "PROVIDER");
                session.setAttribute("principalId", response.providerId());
                session.setAttribute("principalName", response.companyName());
                return "redirect:/provider/dashboard";
            } else {
                IdentityClient.LoginResponse response =
                        identityClient.login(new IdentityClient.LoginRequest(email, password));
                session.setAttribute("role", "USER");
                session.setAttribute("principalId", response.userId());
                session.setAttribute("principalName", response.name());
                return "redirect:/dashboard";
            }
        } catch (FeignException.Unauthorized e) {
            model.addAttribute("error", "Invalid email or password.");
            model.addAttribute("role", role);
            return "login";
        } catch (FeignException e) {
            model.addAttribute("error", "Login failed: " + describeFeignError(e));
            model.addAttribute("role", role);
            return "login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    static String describeFeignError(FeignException e) {
        return "backend returned HTTP " + e.status() + " (is the service running?)";
    }
}
