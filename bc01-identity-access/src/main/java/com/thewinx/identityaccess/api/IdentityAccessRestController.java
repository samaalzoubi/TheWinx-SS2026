package com.thewinx.identityaccess.api;

import com.thewinx.identityaccess.api.dto.AuthRequest;
import com.thewinx.identityaccess.api.dto.AuthResponse;
import com.thewinx.identityaccess.api.dto.PermissionCheckResponse;
import com.thewinx.identityaccess.api.dto.RegisterUserRequest;
import com.thewinx.identityaccess.api.dto.RoleAssignmentRequest;
import com.thewinx.identityaccess.api.dto.UpdateUserRequest;
import com.thewinx.identityaccess.api.dto.UserResponse;
import com.thewinx.identityaccess.application.IdentityAccessService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/identity")
public class IdentityAccessRestController {

    private final IdentityAccessService identityAccessService;

    public IdentityAccessRestController(IdentityAccessService identityAccessService) {
        this.identityAccessService = identityAccessService;
    }

    @PostMapping("/users")
    public UserResponse register(@Valid @RequestBody RegisterUserRequest request) {
        return UserResponse.from(identityAccessService.register(request.getUsername(), request.getEmail(), request.getPassword()));
    }

    @GetMapping("/users")
    public List<UserResponse> listUsers() {
        return identityAccessService.listUsers().stream().map(UserResponse::from).toList();
    }

    @GetMapping("/users/{userId}")
    public UserResponse getUser(@PathVariable Long userId) {
        return UserResponse.from(identityAccessService.getUser(userId));
    }

    @PutMapping("/users/{userId}")
    public UserResponse updateUser(@PathVariable Long userId, @Valid @RequestBody UpdateUserRequest request) {
        return UserResponse.from(identityAccessService.updateUser(userId, request.getUsername(), request.getEmail()));
    }

    @DeleteMapping("/users/{userId}")
    public UserResponse deactivateUser(@PathVariable Long userId) {
        return UserResponse.from(identityAccessService.deactivateUser(userId));
    }

    @PostMapping("/users/{userId}/roles")
    public UserResponse assignRole(@PathVariable Long userId, @Valid @RequestBody RoleAssignmentRequest request) {
        return UserResponse.from(identityAccessService.assignRole(userId, request.getRoleName()));
    }

    @DeleteMapping("/users/{userId}/roles")
    public UserResponse revokeRole(@PathVariable Long userId, @Valid @RequestBody RoleAssignmentRequest request) {
        return UserResponse.from(identityAccessService.revokeRole(userId, request.getRoleName()));
    }

    @PostMapping("/auth/login")
    public AuthResponse authenticate(@Valid @RequestBody AuthRequest request) {
        return AuthResponse.from(identityAccessService.authenticate(request.getUsername(), request.getPassword()));
    }

    @GetMapping("/permissions/check")
    public PermissionCheckResponse checkPermission(@RequestParam Long userId, @RequestParam String permission) {
        return new PermissionCheckResponse(userId, permission, identityAccessService.hasPermission(userId, permission));
    }
}
