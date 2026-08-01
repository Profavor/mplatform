package com.classification.domain_system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping({"/", "/health", "/api/health"})
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }
}
