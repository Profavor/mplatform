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

    @GetMapping("/page")
    public ResponseEntity<org.springframework.data.domain.Page<CodeGroup>> getGroupsPaged(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "groupCode,asc") String sort) {
        String[] sortParams = sort.split(",");
        org.springframework.data.domain.Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") ? org.springframework.data.domain.Sort.Direction.DESC : org.springframework.data.domain.Sort.Direction.ASC;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(direction, sortParams[0]));
        return ResponseEntity.ok(codeManagementService.getGroupsPaged(keyword, pageable));
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

    @GetMapping("/code/{groupCode}/details")
    public ResponseEntity<List<CodeDetail>> getActiveDetailsByGroupCode(@PathVariable String groupCode) {
        return ResponseEntity.ok(codeManagementService.getActiveDetailsByGroupCode(groupCode));
    }

    @GetMapping("/export")
    public ResponseEntity<List<com.classification.domain_system.dto.CodeExportDto>> exportCodes() {
        return ResponseEntity.ok(codeManagementService.exportCodes());
    }

    @PostMapping("/import")
    public ResponseEntity<Void> importCodes(@RequestBody List<com.classification.domain_system.dto.CodeExportDto> importData) {
        codeManagementService.importCodes(importData);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/dump-seed")
    @org.springframework.security.access.prepost.PreAuthorize("hasPermission(null, 'admin:write')")
    public ResponseEntity<Void> dumpSeedFiles() {
        codeManagementService.dumpCodeStateToSeedFiles();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sync-seed")
    @org.springframework.security.access.prepost.PreAuthorize("hasPermission(null, 'admin:write')")
    public ResponseEntity<Void> syncSeedFiles() {
        codeManagementService.syncCodes();
        return ResponseEntity.ok().build();
    }
}
