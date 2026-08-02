package com.classification.domain_system.controller;

import com.classification.domain_system.dto.RecordSearchDto;
import com.classification.domain_system.service.RecordSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/records")
public class RecordSearchController {

    private final RecordSearchService recordSearchService;

    public RecordSearchController(RecordSearchService recordSearchService) {
        this.recordSearchService = recordSearchService;
    }

    @PostMapping("/search/complex")
    @PreAuthorize("hasPermission(null, 'record:read') or hasPermission(null, 'domain:read') or isAuthenticated()")
    public ResponseEntity<RecordSearchDto.SearchResponse> searchRecords(@RequestBody RecordSearchDto.SearchRequest request) {
        RecordSearchDto.SearchResponse response = recordSearchService.searchRecords(request);
        return ResponseEntity.ok(response);
    }
}
