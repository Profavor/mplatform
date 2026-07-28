package com.classification.domain_system.controller;

import com.classification.domain_system.dto.SystemInstallRequest;
import com.classification.domain_system.dto.SystemInstallStatusResponse;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.service.AuthService;
import com.classification.domain_system.service.PermissionService;
import com.classification.domain_system.service.SystemInstallService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemInstallController {

    private final SystemInstallService installService;
    private final AuthService authService;
    private final PermissionService permissionService;

    @GetMapping("/install-status")
    public ResponseEntity<SystemInstallStatusResponse> getInstallStatus() {
        return ResponseEntity.ok(installService.getInstallStatus());
    }

    @PostMapping("/install")
    public ResponseEntity<?> installSystem(@RequestBody SystemInstallRequest request) {
        try {
            User adminUser = installService.installSystem(request);

            Map<String, String> tokens = authService.loginWithTokens(adminUser.getUsername(), request.getAdminPassword(), "127.0.0.1", "System Install Setup");

            var perms = permissionService.getAuthoritiesForUser(adminUser.getUsername(), adminUser.getRole()).stream()
                    .map(a -> a.getAuthority())
                    .toList();

            String serverOffset = OffsetDateTime.now().getOffset().getId();

            Map<String, Object> response = Map.of(
                    "token", tokens.get("token"),
                    "refreshToken", tokens.get("refreshToken"),
                    "username", adminUser.getUsername(),
                    "role", adminUser.getRole(),
                    "id", adminUser.getId(),
                    "organizationId", adminUser.getOrganizationId() != null ? adminUser.getOrganizationId().toString() : "",
                    "permissions", perms,
                    "serverOffset", serverOffset
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }
}
