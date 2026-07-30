package com.classification.domain_system.controller;

import com.classification.domain_system.dto.CodeDetailRequest;
import com.classification.domain_system.dto.CodeGroupRequest;
import com.classification.domain_system.entity.CodeDetail;
import com.classification.domain_system.entity.CodeGroup;
import com.classification.domain_system.service.CodeManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/code-groups")
@RequiredArgsConstructor
public class CodeManagementController {

    private final CodeManagementService codeManagementService;

    @PostMapping
    public ResponseEntity<CodeGroup> createGroup(@RequestBody CodeGroupRequest request) {
        return ResponseEntity.ok(codeManagementService.createGroup(request));
    }

    @GetMapping
    public ResponseEntity<List<CodeGroup>> getGroups() {
        return ResponseEntity.ok(codeManagementService.getGroups());
    }

    @GetMapping("/{groupCode}")
    public ResponseEntity<CodeGroup> getGroupByCode(@PathVariable String groupCode) {
        return ResponseEntity.ok(codeManagementService.getGroupByCode(groupCode));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CodeGroup> updateGroup(@PathVariable UUID id, @RequestBody CodeGroupRequest request) {
        return ResponseEntity.ok(codeManagementService.updateGroup(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable UUID id) {
        codeManagementService.deleteGroup(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{groupId}/details")
    public ResponseEntity<CodeDetail> createDetail(@PathVariable UUID groupId, @RequestBody CodeDetailRequest request) {
        return ResponseEntity.ok(codeManagementService.createDetail(groupId, request));
    }

    @GetMapping("/{groupId}/details")
    public ResponseEntity<List<CodeDetail>> getDetailsByGroup(@PathVariable UUID groupId) {
        return ResponseEntity.ok(codeManagementService.getDetailsByGroup(groupId));
    }

    @PutMapping("/details/{detailId}")
    public ResponseEntity<CodeDetail> updateDetail(@PathVariable UUID detailId, @RequestBody CodeDetailRequest request) {
        return ResponseEntity.ok(codeManagementService.updateDetail(detailId, request));
    }

    @DeleteMapping("/details/{detailId}")
    public ResponseEntity<Void> deleteDetail(@PathVariable UUID detailId) {
        codeManagementService.deleteDetail(detailId);
        return ResponseEntity.ok().build();
    }
}
