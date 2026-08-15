package com.classification.domain_system.controller;

import com.classification.domain_system.dto.SemanticOntologyDto;
import com.classification.domain_system.service.SemanticOntologyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ontology")
@RequiredArgsConstructor
public class SemanticOntologyController {

    private final SemanticOntologyService semanticOntologyService;

    @GetMapping("/graph")
    @PreAuthorize("hasPermission(null, 'domain:read') or hasPermission(null, 'schema:read')")
    public ResponseEntity<SemanticOntologyDto.OntologyGraphResponse> getOntologyGraph() {
        return ResponseEntity.ok(semanticOntologyService.getOntologyGraph());
    }

    @GetMapping("/search")
    @PreAuthorize("hasPermission(null, 'domain:read') or hasPermission(null, 'schema:read')")
    public ResponseEntity<SemanticOntologyDto.OntologyGraphResponse> searchOntology(
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(semanticOntologyService.searchOntology(keyword));
    }
}
