package com.classification.domain_system.controller;

import com.classification.domain_system.dto.DataMaskingDto;
import com.classification.domain_system.service.DataMaskingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/records/{recordId}/masked")
@RequiredArgsConstructor
public class DataMaskingController {

    private final DataMaskingService dataMaskingService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'record:read')")
    public ResponseEntity<DataMaskingDto.MaskedDataResponse> getMaskedRecord(
            @PathVariable UUID recordId,
            Authentication authentication) {
        boolean hasUnmask = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("record:unmask"));
        return ResponseEntity.ok(dataMaskingService.getMaskedRecord(recordId, hasUnmask));
    }

    @PostMapping("/preview")
    @PreAuthorize("hasPermission(null, 'security:read') or hasPermission(null, 'domain:read')")
    public ResponseEntity<DataMaskingDto.MaskedDataResponse> previewMasking(
            @PathVariable UUID recordId,
            @RequestParam(defaultValue = "false") boolean simulateUnmasked) {
        return ResponseEntity.ok(dataMaskingService.getMaskedRecord(recordId, simulateUnmasked));
    }
}
