package com.classification.domain_system.controller;

import com.classification.domain_system.dto.DataProfilingResponse;
import com.classification.domain_system.service.DataProfilingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/domains")
@RequiredArgsConstructor
public class DataProfilingController {

    private final DataProfilingService dataProfilingService;

    @GetMapping("/{id}/profiling")
    @PreAuthorize("hasPermission(null, 'dq:read')")
    public ResponseEntity<List<DataProfilingResponse>> getDomainProfiling(@PathVariable UUID id) {
        return ResponseEntity.ok(dataProfilingService.profileDomainData(id));
    }
}
