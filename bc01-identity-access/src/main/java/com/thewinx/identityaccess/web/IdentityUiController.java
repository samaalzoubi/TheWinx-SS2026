package com.thewinx.identityaccess.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.thewinx.identityaccess.api.dto.AuthResponse;
import com.thewinx.identityaccess.api.dto.UserResponse;
import com.thewinx.identityaccess.application.IdentityAccessService;

@Controller
public class IdentityUiController {

    private final IdentityAccessService identityAccessService;

    public IdentityUiController(IdentityAccessService identityAccessService) {
        this.identityAccessService = identityAccessService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("users", identityAccessService.listUsers().stream().map(UserResponse::from).toList());
        return "index";
    }

    @PostMapping("/ui/users")
    public String createUser(@RequestParam String firstName,
                             @RequestParam String lastName,
                             @RequestParam String email,
                             @RequestParam String username,
                             @RequestParam String phoneNumber,
                             @RequestParam String password,
                             @RequestParam String confirmPassword,
                             Model model,
                             @RequestParam(defaultValue = "/") String redirectTo) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Password and confirm password must match.");
            return "register";
        }

        identityAccessService.register(username, email, password);
        if (!redirectTo.startsWith("/")) {
            redirectTo = "/";
        }
        return "redirect:" + redirectTo;
    }

    @PostMapping("/ui/users/deactivate")
    public String deactivateUser(@RequestParam Long userId) {
        identityAccessService.deactivateUser(userId);
        return "redirect:/";
    }

    @PostMapping("/ui/users/assign-role")
    public String assignRole(@RequestParam Long userId, @RequestParam String roleName) {
        identityAccessService.assignRole(userId, roleName);
        return "redirect:/";
    }

    @GetMapping("/ui/users/{id}/edit")
    public String editUserPage(@PathVariable Long id, Model model) {
        model.addAttribute("user", UserResponse.from(identityAccessService.getUser(id)));
        return "edit";
    }

    @PostMapping("/ui/users/{id}")
    public String updateUser(@PathVariable Long id,
                             @RequestParam String username,
                             @RequestParam String email,
                             Model model) {
        identityAccessService.updateUser(id, username, email);
        return "redirect:/";
    }

    @GetMapping("/ui/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/ui/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/ui/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        Model model) {
        AuthResponse response = AuthResponse.from(identityAccessService.authenticate(username, password));
        model.addAttribute("auth", response);
        return "login";
    }
}
