package com.classification.domain_system.controller;

import com.classification.domain_system.entity.User;
import com.classification.domain_system.service.AuthService;
import com.classification.domain_system.utils.ClientIpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final com.classification.domain_system.repository.UserRepository userRepository;
    private final com.classification.domain_system.service.PermissionService permissionService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(401).body("Unauthenticated");
        }
        User user = authService.findByUsername(authentication.getName());
        if (user == null) {
            user = authService.autoProvisionUser(authentication);
        }
        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }
        var perms = permissionService.getAuthoritiesForUser(user.getUsername(), user.getRole()).stream()
            .map(a -> a.getAuthority())
            .filter(auth -> !auth.startsWith("ROLE_"))
            .toList();
        String serverOffset = OffsetDateTime.now().getOffset().getId();
        return ResponseEntity.ok(new LoginResponse(null, null, user.getUsername(), user.getRole(), user.getId(), user.getId(), user.getOrganizationId(), user.getDepartmentId(), user.getTimezone(), serverOffset, perms, user.getMustChangePassword() != null ? user.getMustChangePassword() : false));
    }



    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ip = ClientIpUtil.getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        java.util.Map<String, String> tokens = authService.loginWithTokens(request.getUsername(), request.getPassword(), ip, userAgent);
        User user = authService.findByUsername(request.getUsername());

        var perms = permissionService.getAuthoritiesForUser(user.getUsername(), user.getRole()).stream()
            .map(a -> a.getAuthority())
            .filter(auth -> !auth.startsWith("ROLE_"))
            .toList();

        String serverOffset = OffsetDateTime.now().getOffset().getId();
        return ResponseEntity.ok(new LoginResponse(
            tokens.get("token"),
            tokens.get("refreshToken"),
            user.getUsername(),
            user.getRole(),
            user.getId(),
            user.getId(),
            user.getOrganizationId(),
            user.getDepartmentId(),
            user.getTimezone(),
            serverOffset,
            perms,
            user.getMustChangePassword() != null ? user.getMustChangePassword() : false
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody java.util.Map<String, String> request) {
        String refreshToken = request != null ? request.get("refreshToken") : null;
        java.util.Map<String, String> tokens = authService.refreshTokens(refreshToken);
        return ResponseEntity.ok(tokens);
    }

    @GetMapping("/login-logs")
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'log:read')")
    public ResponseEntity<?> getLoginLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            org.springframework.security.core.Authentication authentication) {
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "loginAt"));
        return ResponseEntity.ok(authService.getLoginLogs(pageable));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        authService.register(request.getUsername(), request.getPassword(), request.getRole(), request.getTimezone());
        return ResponseEntity.ok("User registered successfully");
    }

    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        boolean exists = authService.existsByUsername(username);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("exists", exists);
        result.put("available", !exists);
        return ResponseEntity.ok(result);
    }

    @Data
    static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    static class RegisterRequest {
        private String username;
        private String password;
        private String role;
        private String timezone;
    }

    @Data
    static class LoginResponse {
        private final String token;
        private final String refreshToken;
        private final String username;
        private final String role;
        private final String uuid;
        private final String id;
        private final UUID organizationId;
        private final UUID departmentId;
        private final String timezone;
        private final String serverOffset;
        private final java.util.List<String> permissions;
        private final boolean mustChangePassword;
    }
}
