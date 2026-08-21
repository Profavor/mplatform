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

    /**
     * 시스템 최초 설치 상태 확인 (설치 여부 및 초기 관리자 계정 생성 여부)
     * 최초 배포 시 비인증 상태의 클라이언트가 시스템 설치 필요 여부를 확인해야 하므로 permitAll로 허용됨
     */
    @GetMapping("/install-status")
    public ResponseEntity<SystemInstallStatusResponse> getInstallStatus() {
        return ResponseEntity.ok(installService.getInstallStatus());
    }

    /**
     * 시스템 최초 설치 및 초기 최고 관리자 계정 구성
     * 시스템 최초 구동 시 비인증 상태에서 관리자 계정을 설정해야 하므로 permitAll로 허용되며,
     * SystemInstallService 내부에서 1회 설치 완료 후 재설치 시도를 방어함
     */
    @PostMapping("/install")
    public ResponseEntity<?> installSystem(@RequestBody SystemInstallRequest request) {
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
    }
}
