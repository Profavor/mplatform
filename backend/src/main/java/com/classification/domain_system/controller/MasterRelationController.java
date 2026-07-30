package com.classification.domain_system.controller;

import com.classification.domain_system.dto.MasterRelationRequest;
import com.classification.domain_system.entity.MasterRelation;
import com.classification.domain_system.service.MasterRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/master-relations")
@RequiredArgsConstructor
public class MasterRelationController {

    private final MasterRelationService masterRelationService;

    @PostMapping
    public ResponseEntity<MasterRelation> createRelation(@RequestBody MasterRelationRequest request) {
        return ResponseEntity.ok(masterRelationService.createRelation(request));
    }

    @GetMapping
    public ResponseEntity<List<MasterRelation>> getAllRelations() {
        return ResponseEntity.ok(masterRelationService.getAllRelations());
    }

    @PutMapping("/{id}")
    public ResponseEntity<MasterRelation> updateRelation(@PathVariable UUID id, @RequestBody MasterRelationRequest request) {
        return ResponseEntity.ok(masterRelationService.updateRelation(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRelation(@PathVariable UUID id) {
        masterRelationService.deleteRelation(id);
        return ResponseEntity.ok().build();
    }
}
