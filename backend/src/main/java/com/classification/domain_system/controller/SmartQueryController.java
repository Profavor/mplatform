package com.classification.domain_system.controller;

import com.classification.domain_system.dto.SmartQueryDto;
import com.classification.domain_system.service.SmartQueryParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/domains/{domainId}/smart-query")
@RequiredArgsConstructor
public class SmartQueryController {

    private final SmartQueryParserService smartQueryParserService;

    @PostMapping
    @PreAuthorize("hasPermission(null, 'record:read') or hasPermission(null, 'domain:read')")
    public ResponseEntity<SmartQueryDto.SmartQueryResponse> executeSmartQuery(
            @PathVariable UUID domainId,
            @RequestBody SmartQueryDto.SmartQueryRequest request) {
        return ResponseEntity.ok(smartQueryParserService.parseAndExecute(domainId, request.getNaturalLanguageQuery()));
    }
}
