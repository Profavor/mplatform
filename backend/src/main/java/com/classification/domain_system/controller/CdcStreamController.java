package com.classification.domain_system.controller;

import com.classification.domain_system.dto.CdcStreamDto;
import com.classification.domain_system.service.CdcStreamingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/domains/{domainId}/cdc")
@RequiredArgsConstructor
public class CdcStreamController {

    private final CdcStreamingService cdcStreamingService;

    @GetMapping("/stream")
    @PreAuthorize("hasPermission(null, 'domain:read') or hasPermission(null, 'record:read')")
    public ResponseEntity<CdcStreamDto.CdcStreamResponse> getLiveCdcEvents(
            @PathVariable UUID domainId,
            @RequestParam(required = false) UUID recordId) {
        return ResponseEntity.ok(cdcStreamingService.getLiveCdcEvents(domainId, recordId));
    }
}
