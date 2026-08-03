package com.classification.domain_system.controller;

import com.classification.domain_system.dto.SensitiveDataAccessLogDto;
import com.classification.domain_system.dto.SensitiveDataStatsDto;
import com.classification.domain_system.service.SensitiveDataService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/sensitive-data")
@RequiredArgsConstructor
public class SensitiveDataController {

    private final SensitiveDataService sensitiveDataService;

    @PostMapping("/approval/{approvalId}/decrypt")
    @PreAuthorize("hasPermission(null, 'record:unmask')")
    public ResponseEntity<Map<String, String>> decryptApprovalFields(
            @PathVariable UUID approvalId,
            @RequestBody(required = false) DecryptRequest request,
            HttpServletRequest httpRequest) {
        List<String> fieldKeys = request != null ? request.getFieldKeys() : null;
        String ip = getClientIp(httpRequest);
        Map<String, String> decrypted = sensitiveDataService.decryptApprovalFields(approvalId, fieldKeys, ip);
        return ResponseEntity.ok(decrypted);
    }

    @PostMapping("/record/{recordId}/decrypt")
    @PreAuthorize("hasPermission(null, 'record:unmask')")
    public ResponseEntity<Map<String, String>> decryptRecordFields(
            @PathVariable UUID recordId,
            @RequestBody(required = false) DecryptRequest request,
            HttpServletRequest httpRequest) {
        List<String> fieldKeys = request != null ? request.getFieldKeys() : null;
        String ip = getClientIp(httpRequest);
        Map<String, String> decrypted = sensitiveDataService.decryptRecordFields(recordId, fieldKeys, ip);
        return ResponseEntity.ok(decrypted);
    }

    @GetMapping("/access-logs")
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'log:read')")
    public ResponseEntity<Page<SensitiveDataAccessLogDto>> getAccessLogs(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) UUID targetId,
            Pageable pageable) {
        return ResponseEntity.ok(sensitiveDataService.getAccessLogs(userId, targetId, pageable));
    }

    @GetMapping("/statistics")
    public ResponseEntity<SensitiveDataStatsDto> getStatistics() {
        return ResponseEntity.ok(sensitiveDataService.getStatistics());
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        String ip;
        if (xf != null && !xf.isBlank()) {
            ip = xf.split(",")[0].trim();
        } else {
            ip = request.getRemoteAddr();
        }
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            ip = "127.0.0.1";
        }
        return ip;
    }

    @Data
    public static class DecryptRequest {
        private List<String> fieldKeys;
    }
}
