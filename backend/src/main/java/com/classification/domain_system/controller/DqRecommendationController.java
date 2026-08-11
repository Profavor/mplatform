package com.classification.domain_system.controller;

import com.classification.domain_system.dto.DqRecommendationResponse;
import com.classification.domain_system.service.DqRecommendationService;
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
@RequestMapping("/api/v1/dq")
@RequiredArgsConstructor
public class DqRecommendationController {

    private final DqRecommendationService dqRecommendationService;

    @GetMapping("/recommendations/{domainId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'DATA_STEWARD')")
    public ResponseEntity<List<DqRecommendationResponse>> getRecommendations(@PathVariable UUID domainId) {
        return ResponseEntity.ok(dqRecommendationService.getRecommendations(domainId));
    }
}
