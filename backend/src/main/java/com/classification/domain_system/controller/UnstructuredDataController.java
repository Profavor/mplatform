package com.classification.domain_system.controller;

import com.classification.domain_system.dto.UnstructuredDataDto;
import com.classification.domain_system.service.UnstructuredDataExtractorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/domains/{domainId}/ai/extract-unstructured")
@RequiredArgsConstructor
public class UnstructuredDataController {

    private final UnstructuredDataExtractorService unstructuredDataExtractorService;

    @PostMapping
    @PreAuthorize("hasPermission(null, 'domain:write') or hasPermission(null, 'record:write')")
    public ResponseEntity<UnstructuredDataDto.ExtractionResponse> extractFields(
            @PathVariable UUID domainId,
            @RequestBody UnstructuredDataDto.ExtractFromTextRequest request) {
        return ResponseEntity.ok(unstructuredDataExtractorService.extractFields(domainId, request.getRawText()));
    }
}
