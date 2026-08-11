package com.classification.domain_system.controller;

import com.classification.domain_system.entity.Record;
import com.classification.domain_system.service.GlobalSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class GlobalSearchController {

    private final GlobalSearchService globalSearchService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<Record>> searchGlobal(
            @RequestParam(name = "q", defaultValue = "") String query,
            @PageableDefault(size = 20) Pageable pageable) {
            
        return ResponseEntity.ok(globalSearchService.searchGlobal(query, pageable));
    }
}
